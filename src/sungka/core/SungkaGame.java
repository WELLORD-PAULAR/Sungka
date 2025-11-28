package sungka.core;

import sungka.model.Pit;
import sungka.model.Player;
import sungka.powerups.PowerUpManager;
import sungka.config.GameConfig;
import sungka.powerups.PowerUp;

import java.util.*;

    // Core game model: board, players, turn logic, power-ups and win detection.
    // Final to avoid subclassing and to prevent 'this' escaping during construction.
public final class SungkaGame {
    public static final int WIN_THRESHOLD = 50;
    // instance-level win threshold (configurable per-game)
    private int winThreshold = WIN_THRESHOLD;
    // 16 pits: 0..6 small B, 7 house B, 8..14 small A, 15 house A
    public Pit[] board = new Pit[16];
    // Board layout: 0..6 small pits (Player B side), 7 house B,
    // 8..14 small pits (Player A side), 15 house A
    public final Player playerB, playerA;
    // the player who has the current turn
    Player current;
    // power-up manager (supplies and refills power-ups)
    PowerUpManager pum = new PowerUpManager();
    // pseudo-random source for placement choices
    Random rnd = new Random();

    // flags & states for power-ups
    private boolean doubleCapture = false;
    private boolean bonusTurn = false;
    private boolean reverseSowing = false;
    private boolean skipOpponent = false;
    private final Map<Player, Boolean> shielded = new HashMap<>();
    // protected pit index for each player (nullable) and remaining shield turns
    private final Map<Player, Integer> protectedPit = new HashMap<>();
    private final Map<Player, Integer> protectedPitTurns = new HashMap<>();
    // currently active power-up per player (if any)
    private final Map<Player, sungka.powerups.PowerUp> activePowerUp = new HashMap<>();
    // number of power-ups used by each player in the current turn
    private final Map<Player, Integer> powerUpsUsedThisTurn = new HashMap<>();

    // logging hook for GUI
    private ConsumerLogger logger = (s) -> {};

    // Initialize board, players, and fill initial power-ups via PowerUpManager.
    public SungkaGame() {
        for (int i = 0; i < 16; i++) {
            if (i == 7 || i == 15) board[i] = new Pit(0, true);
            else board[i] = new Pit(7, false);
        }
        playerB = new Player("Player B", 0, 6, 7);
        playerA = new Player("Player A", 8, 14, 15);
        current = playerA;
        shielded.put(playerA, false);
        shielded.put(playerB, false);
        powerUpsUsedThisTurn.put(playerA, 0);
        powerUpsUsedThisTurn.put(playerB, 0);

        // Apply central pre-game configuration (if present) so direct construction
        // of SungkaGame picks up user-selected allowed power-ups and win threshold.
        java.util.Set<String> cfgPUs = GameConfig.getInstance().getEnabledPowerUps();
        if (cfgPUs != null) pum.setAllowedCodes(cfgPUs);
        this.winThreshold = GameConfig.getInstance().getWinThreshold();

        pum.refillToCap(playerA, board, 3);
        pum.refillToCap(playerB, board, 3);
    }
    // Set per-game win threshold (clamped to >= 1).
    public void setWinThreshold(int t) { this.winThreshold = Math.max(1, t); }

    // Get per-game win threshold.
    public int getWinThreshold() { return this.winThreshold; }

    // Configure allowed power-up codes for this game (pass codes understood by PowerUpManager).
    public void setAllowedPowerUps(Collection<String> codes) { pum.setAllowedCodes(codes); }

    // Set a logger callback used by the model to emit textual messages (UI may display these).
    public void setLogger(ConsumerLogger l) { this.logger = l; }

    // Functional interface for a simple text logger.
    public interface ConsumerLogger { void accept(String s); }

    // Emit a text message to the configured logger (no-op by default).
    public void log(String s) { logger.accept(s); }

    // getters
    //Get the player whose turn it currently is.
    public Player getCurrent() { return current; }

    // Return the opponent of the given player.
    public Player getOpponent(Player p) { return p == playerA ? playerB : playerA; }

    //Return the opponent of the current player.
    public Player getOpponentOfCurrent() { return getOpponent(current); }

    // Expose reverse sowing flag so UI can display current sow direction
    // Whether sowing direction is currently reversed.
    public boolean isReverseSowing() { return reverseSowing; }

    // flags setters
    // Enable/disable the double-capture effect for the next capture.
    public void setDoubleCapture(boolean v) { doubleCapture = v; }

    // Grant or revoke an immediate bonus-turn condition.
    public void setBonusTurn(boolean v) { bonusTurn = v; }

    // Set the reverse-sowing flag which affects the next sowing action.
    public void setReverseSowing(boolean v) { reverseSowing = v; }

    // Cause the opponent to be skipped on their next turn.
    public void setSkipOpponent(boolean v) { skipOpponent = v; }

    // Set whether a player's side is shielded (external helper).
    public void setShieldForPlayer(Player p, boolean v) { shielded.put(p, v); }

    // Return whether the given player is currently shielded.
    public boolean isShielded(Player p) { return shielded.getOrDefault(p, false); }

    // Return the two adjacent pit indices (left and right) on the circular board.
    // The returned list contains [leftIndex, rightIndex].

    public List<Integer> getAdjacentIndices(int pitIdx) {
        List<Integer> out = new ArrayList<>();
        int left = (pitIdx - 1 + 16) % 16;
        int right = (pitIdx + 1) % 16;
        out.add(left); out.add(right);
        return out;
    }

    // Protected pit API: set/remove a protected pit and its remaining turns.
    // Passing a negative pitIndex removes protection for the player.
    public void setProtectedPit(Player p, int pitIndex, int turns) {
        if (pitIndex < 0) { protectedPit.remove(p); protectedPitTurns.remove(p); return; }
        protectedPit.put(p, pitIndex);
        protectedPitTurns.put(p, turns);
    }

    // Return the index of the protected pit for a player, or null if none.
    public Integer getProtectedPit(Player p) { return protectedPit.get(p); }

    // Return the remaining protection turns for the given player's protected pit.
    public Integer getProtectedPitTurns(Player p) { return protectedPitTurns.getOrDefault(p, 0); }


    // Active power-up API: attach/get/clear the currently active power-up for a player.
    public void setActivePowerUp(Player p, sungka.powerups.PowerUp pu) { activePowerUp.put(p, pu); }

    public sungka.powerups.PowerUp getActivePowerUp(Player p) { return activePowerUp.get(p); }

    public void clearActivePowerUp(Player p) { activePowerUp.remove(p); }

    // Return how many power-ups the player has used during the current turn.
    public int getPowerUpsUsedThisTurn(Player p) { return powerUpsUsedThisTurn.getOrDefault(p, 0); }

    // Increment the per-turn activation counter for a player. When the
    // activation budget is exceeded the player's side is cleared of power-ups.
    private void incrementPowerUpsUsed(Player p) {
        int now = getPowerUpsUsedThisTurn(p) + 1;
        powerUpsUsedThisTurn.put(p, now);
        // if limit reached, wipe all power-ups on player's side so they're forced to pick a pit
        if (now >= 2) {
            wipePowerUpsForPlayer(p);
        }
    }

    // Reset the per-turn activation counter for the provided player.
    private void resetPowerUpsUsed(Player p) { powerUpsUsedThisTurn.put(p, 0); }

    // Wipe all power-ups on the given player's side immediately.
    public void wipePowerUpsForPlayer(Player p) {
        for (int i = p.getStart(); i <= p.getEnd(); i++) board[i].clearPowerUp();
        log(p.getName() + " reached power-up limit: all power-ups on their side were wiped.");
    }
    /*
    Utility methods
    Return the board index opposite to `pos` on the small-pit ring.
    This mapping is used to compute the opposing pit for capture logic.
    */ 
    private int oppositeOf(int pos) { return 14 - pos; }
    /*
    Execute a sowing move for the current player. Handles sowing direction,
    skipping opponent house, captures (including double-capture), captured
    power-up transfer/activation, and then advances the turn via endTurn().
    Returns true if the move was valid and applied.
    */
    public boolean makeMove(int pitIndex) {
        Pit startPit = board[pitIndex];
        if (!current.ownsPit(pitIndex) || startPit.isHouse() || startPit.getStones() == 0) return false;

        int stones = startPit.getStones();
        startPit.setStones(0);

        int pos = pitIndex;
        boolean cc = reverseSowing; // if reversed, use backward sowing
        reverseSowing = false; // consume reverse flag when used

        while (stones > 0) {
            pos = cc ? (pos - 1 + 16) % 16 : (pos + 1) % 16;
            Player opp = getOpponent(current);
            if (pos == opp.getHouseIndex()) continue;
            board[pos].addStone();
            stones--;
        }

        if (pos == current.getHouseIndex()) {
            // landed in house — player keeps the turn (bonusTurn handled elsewhere)
            log(current.getName() + " landed in house and gets another turn.");
        } else {
                    if (current.ownsPit(pos) && !board[pos].isHouse() && board[pos].getStones() == 1) {
                        int oppPos = oppositeOf(pos);
                        Pit oppPit = board[oppPos];

                        // If opponent protected this pit with PitShield, block the capture and remove protection
                        Player pitOwner = getOpponent(current);
                        Integer prot = protectedPit.get(pitOwner);
                        if (prot != null && prot.equals(oppPos)) {
                            protectedPit.remove(pitOwner);
                            protectedPitTurns.remove(pitOwner);
                            log(pitOwner.getName() + "'s pit shield prevented a capture on pit " + oppPos + ".");
                        } else {
                            int captured = oppPit.getStones();
                            if (doubleCapture) captured *= 2;
                            int add = captured + 1;
                            board[current.getHouseIndex()].setStones(board[current.getHouseIndex()].getStones() + add);
                            oppPit.setStones(0);
                            board[pos].setStones(0);

                            log(current.getName() + " captured " + captured + " from pit " + oppPos + " (plus 1).");

                            if (oppPit.hasPowerUp()) {
                                // when a power-up is captured, try to activate it immediately for the captor
                                PowerUp capturedPU = oppPit.getPowerUp();
                                oppPit.clearPowerUp();
                                if (getPowerUpsUsedThisTurn(current) < 2) {
                                    setActivePowerUp(current, capturedPU);
                                    capturedPU.apply(this, current, pos);
                                    incrementPowerUpsUsed(current);
                                    log(current.getName() + " activated captured power-up " + capturedPU.getName() + ".");
                                } else {
                                    // player's activation budget exhausted — place captured PU into a random empty pit on their side
                                        Integer protCur = protectedPit.get(current);
                                    List<Integer> empties = new ArrayList<>();
                                        for (int i = current.getStart(); i <= current.getEnd(); i++) {
                                            if (protCur != null && i == protCur) continue;
                                        if (!board[i].hasPowerUp() && !board[i].isHouse()) empties.add(i);
                                    }
                                    if (!empties.isEmpty()) {
                                        Collections.shuffle(empties, rnd);
                                        int dest = empties.get(0);
                                        board[dest].setPowerUp(capturedPU);
                                        log(current.getName() + " captured a power-up but reached per-turn limit; stored in pit " + dest + ".");
                                    } else {
                                        // nowhere to store — discard
                                        log(current.getName() + " captured a power-up but has no space to store it; it is discarded.");
                                    }
                                }
                            }
                            doubleCapture = false;
                        }
                            }
                endTurn();
        }
        return true;
    }

    // Return the sequence of pit indices that would receive stones when sowing
    // from the given pit index. Does not modify game state.
    public List<Integer> previewSowSequence(int pitIndex) {
        List<Integer> seq = new ArrayList<>();
        if (!current.ownsPit(pitIndex)) return seq;
        Pit startPit = board[pitIndex];
        if (startPit.isHouse() || startPit.getStones() == 0) return seq;

        int stones = startPit.getStones();
        int pos = pitIndex;
        boolean cc = reverseSowing; // preview uses current reverse flag but does not consume it

        while (stones > 0) {
            pos = cc ? (pos - 1 + 16) % 16 : (pos + 1) % 16;
            Player opp = getOpponent(current);
            if (pos == opp.getHouseIndex()) continue;
            seq.add(pos);
            stones--;
        }
        return seq;
    }

    // Activate the power-up in the specified pit (owned by current player).
    // Enforces per-turn activation budget; returns true if activation occurred.
    public boolean activatePowerUpInPit(int pitIndex) {
        if (!current.ownsPit(pitIndex)) return false;
        Pit p = board[pitIndex];
        if (!p.hasPowerUp()) return false;
        PowerUp pu = p.getPowerUp();
        p.clearPowerUp();
        // Ensure leftover bonusTurn flags don't let unrelated power-ups keep the turn.
        bonusTurn = false;
        // enforce per-turn activation limit
        if (getPowerUpsUsedThisTurn(current) >= 2) {
            log(current.getName() + " has already used maximum power-ups this turn.");
            return false;
        }
        pu.apply(this, current, pitIndex);
        incrementPowerUpsUsed(current);
        return true;
    }

    // Return the player who reached the win threshold in their house, or null.
    public Player checkForWinner() {
        if (board[playerA.getHouseIndex()].getStones() >= winThreshold) return playerA;
        if (board[playerB.getHouseIndex()].getStones() >= winThreshold) return playerB;
        return null;
    }
    /*
    Advance the turn, handling bonus-turn and skip-opponent flags.
    Also refresh power-ups for the player who finished the round and
    decrement any protected-pit timers.
    Note: skipOpponent semantics intentionally cause the original player
    to retain the turn after a skipped opponent (see implementation).
    */
    private void endTurn() {
        if (bonusTurn) {
            bonusTurn = false;
            log(current.getName() + " keeps turn due to BonusTurn.");
            refreshPowerUpsForPlayer(current, 3);
            return;
        }
        Player prev = current;
        if (skipOpponent) {
            skipOpponent = false;
            current = getOpponent(current);
            log(current.getName() + " was skipped.");
            current = getOpponent(current);
        } else {
            current = getOpponent(current);
        }
        refreshPowerUpsForPlayer(prev, 3);

        // reset per-turn power-up usage counter for the player whose turn is starting
        if (current != prev) {
            resetPowerUpsUsed(current);
        }

        // decrement protected pit turns for all players
        for (Player p : Arrays.asList(playerA, playerB)) {
            int t = protectedPitTurns.getOrDefault(p, 0);
            if (t > 0) {
                t--;
                if (t <= 0) {
                    protectedPit.remove(p);
                    protectedPitTurns.remove(p);
                } else {
                    protectedPitTurns.put(p, t);
                }
            }
        }
    }

    // Clear and refill up to `cap` power-ups on the player's side (skips protected pit).
    public void refreshPowerUpsForPlayer(Player player, int cap) {
        // clear current power-ups on player's side
        for (int i = player.getStart(); i <= player.getEnd(); i++) board[i].clearPowerUp();

        // place up to cap power-ups into non-house pits, skipping protected pit
        Integer prot = protectedPit.get(player);
        List<Integer> empties = new ArrayList<>();
        for (int i = player.getStart(); i <= player.getEnd(); i++) {
            if (prot != null && i == prot) continue;
            if (!board[i].hasPowerUp()) empties.add(i);
        }
        Collections.shuffle(empties, rnd);
        int toPlace = Math.min(cap, empties.size());
        for (int k = 0; k < toPlace; k++) {
            board[empties.get(k)].setPowerUp(pum.randomPowerUp());
        }
    }
    /*
    Create a fresh copy of the game for simulation. Note:
    Pits are copied, but PowerUp instances are referenced (not deep-cloned).
    PowerUpManager (`pum`) is shared between original and copy.
    Logger is set to noop in copies to avoid noisy simulation logs.
    */
    public SungkaGame copy() {
        SungkaGame g2 = new SungkaGame();

        // replace board with copied pits
        g2.board = new Pit[16];
        for (int i = 0; i < 16; i++) {
            Pit p = this.board[i];
            Pit np = new Pit(p.getStones(), p.isHouse());
            if (p.hasPowerUp()) np.setPowerUp(p.getPowerUp());
            g2.board[i] = np;
        }

        // copy simple flags
        g2.doubleCapture = this.doubleCapture;
        g2.bonusTurn = this.bonusTurn;
        g2.reverseSowing = this.reverseSowing;
        g2.skipOpponent = this.skipOpponent;

        // copy win threshold and power-up manager reference
        g2.winThreshold = this.winThreshold;
        g2.pum = this.pum;

        // copy shielded mapping by matching players
        g2.shielded.put(g2.playerA, this.shielded.getOrDefault(this.playerA, false));
        g2.shielded.put(g2.playerB, this.shielded.getOrDefault(this.playerB, false));

        // copy protected pit maps (map old players to new players)
        if (this.protectedPit.containsKey(this.playerA)) g2.protectedPit.put(g2.playerA, this.protectedPit.get(this.playerA));
        if (this.protectedPit.containsKey(this.playerB)) g2.protectedPit.put(g2.playerB, this.protectedPit.get(this.playerB));
        if (this.protectedPitTurns.containsKey(this.playerA)) g2.protectedPitTurns.put(g2.playerA, this.protectedPitTurns.get(this.playerA));
        if (this.protectedPitTurns.containsKey(this.playerB)) g2.protectedPitTurns.put(g2.playerB, this.protectedPitTurns.get(this.playerB));

        // copy active power-ups mapping (map to new players)
        if (this.activePowerUp.containsKey(this.playerA)) g2.activePowerUp.put(g2.playerA, this.activePowerUp.get(this.playerA));
        if (this.activePowerUp.containsKey(this.playerB)) g2.activePowerUp.put(g2.playerB, this.activePowerUp.get(this.playerB));

        // copy per-turn usage counters
        g2.powerUpsUsedThisTurn.put(g2.playerA, this.powerUpsUsedThisTurn.getOrDefault(this.playerA, 0));
        g2.powerUpsUsedThisTurn.put(g2.playerB, this.powerUpsUsedThisTurn.getOrDefault(this.playerB, 0));

        // set current player reference to the clone's player objects
        g2.current = (this.current == this.playerA) ? g2.playerA : g2.playerB;

        // quiet logger for simulations
        g2.setLogger((s) -> {});

        return g2;
    }
}
