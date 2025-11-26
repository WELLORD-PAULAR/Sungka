package sungka.core;

import sungka.model.Pit;
import sungka.model.Player;
import sungka.powerups.PowerUpManager;
import sungka.powerups.PowerUp;

import java.util.*;

public class SungkaGame {
    public static final int WIN_THRESHOLD = 50;
    // 16 pits: 0..6 small B, 7 house B, 8..14 small A, 15 house A
    public Pit[] board = new Pit[16];
    public final Player playerB, playerA;
    Player current;
    PowerUpManager pum = new PowerUpManager();
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

        pum.refillToCap(playerA, board, 3);
        pum.refillToCap(playerB, board, 3);
    }

    public void setLogger(ConsumerLogger l) { this.logger = l; }

    public interface ConsumerLogger { void accept(String s); }

    public void log(String s) { logger.accept(s); }

    // getters
    public Player getCurrent() { return current; }
    public Player getOpponent(Player p) { return p == playerA ? playerB : playerA; }
    public Player getOpponentOfCurrent() { return getOpponent(current); }

    // Expose reverse sowing flag so UI can display current sow direction
    public boolean isReverseSowing() { return reverseSowing; }

    // flags setters
    public void setDoubleCapture(boolean v) { doubleCapture = v; }
    public void setBonusTurn(boolean v) { bonusTurn = v; }
    public void setReverseSowing(boolean v) { reverseSowing = v; }
    public void setSkipOpponent(boolean v) { skipOpponent = v; }
    public void setShieldForPlayer(Player p, boolean v) { shielded.put(p, v); }

    public boolean isShielded(Player p) { return shielded.getOrDefault(p, false); }

    public List<Integer> getAdjacentIndices(int pitIdx) {
        List<Integer> out = new ArrayList<>();
        int left = (pitIdx - 1 + 16) % 16;
        int right = (pitIdx + 1) % 16;
        out.add(left); out.add(right);
        return out;
    }

    // Protected pit API
    public void setProtectedPit(Player p, int pitIndex, int turns) {
        if (pitIndex < 0) { protectedPit.remove(p); protectedPitTurns.remove(p); return; }
        protectedPit.put(p, pitIndex);
        protectedPitTurns.put(p, turns);
    }
    public Integer getProtectedPit(Player p) { return protectedPit.get(p); }
    public Integer getProtectedPitTurns(Player p) { return protectedPitTurns.getOrDefault(p, 0); }

    // Active power-up API
    public void setActivePowerUp(Player p, sungka.powerups.PowerUp pu) { activePowerUp.put(p, pu); }
    public sungka.powerups.PowerUp getActivePowerUp(Player p) { return activePowerUp.get(p); }
    public void clearActivePowerUp(Player p) { activePowerUp.remove(p); }

    public int getPowerUpsUsedThisTurn(Player p) { return powerUpsUsedThisTurn.getOrDefault(p, 0); }
    private void incrementPowerUpsUsed(Player p) {
        int now = getPowerUpsUsedThisTurn(p) + 1;
        powerUpsUsedThisTurn.put(p, now);
        // if limit reached, wipe all power-ups on player's side so they're forced to pick a pit
        if (now >= 2) {
            wipePowerUpsForPlayer(p);
        }
    }
    private void resetPowerUpsUsed(Player p) { powerUpsUsedThisTurn.put(p, 0); }

    /** Wipe all power-ups on the given player's side immediately. */
    public void wipePowerUpsForPlayer(Player p) {
        for (int i = p.getStart(); i <= p.getEnd(); i++) board[i].clearPowerUp();
        log(p.getName() + " reached power-up limit: all power-ups on their side were wiped.");
    }

    private int oppositeOf(int pos) { return 14 - pos; }

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
                endTurn(false);
        }
        return true;
    }

    /**
     * Return the sequence of pit indices that would receive stones when sowing
     * from the given pit index, in order. This does not modify game state.
     */
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

    /**
     * Check if a player has reached the win threshold in their house.
     * Returns the winning Player, or null if none.
     */
    public Player checkForWinner() {
        if (board[playerA.getHouseIndex()].getStones() >= WIN_THRESHOLD) return playerA;
        if (board[playerB.getHouseIndex()].getStones() >= WIN_THRESHOLD) return playerB;
        return null;
    }

    private void endTurn(boolean activatedPowerUp) {
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

    /**
     * Clear existing power-ups on player's side and refill up to cap, skipping protected pit.
     */
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

    /**
     * Create a deep-ish clone of the current game suitable for AI simulation.
     * Note: PowerUp instances are shared (they are stateless), but board/pits
     * and player-turn state are duplicated and mapped to the cloned players.
     */
    @Override
    public SungkaGame clone() {
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
