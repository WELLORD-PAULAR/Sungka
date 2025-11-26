package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;
import sungka.model.Pit;

import java.util.List;

// All concrete power-ups in one file for simplicity
public class concrete_powerups {
    // individual classes are static-like top-level classes in this file
}

class DoubleCapturePU extends PowerUp {
    public DoubleCapturePU() { super("Double Capture", "D", "Next capture doubles shells."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.setDoubleCapture(true);
        game.log(player.getName() + " activated Double Capture.");
    }
}

class BonusTurnPU extends PowerUp {
    public BonusTurnPU() { super("Bonus Turn", "B", "Gain an extra turn."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.setBonusTurn(true);
        game.log(player.getName() + " activated Bonus Turn.");
    }
}

class ReverseSowPU extends PowerUp {
    public ReverseSowPU() { super("Reverse Sowing", "R", "Reverse sow direction for next sow."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.setReverseSowing(true);
        game.log(player.getName() + " activated Reverse Sowing.");
    }
}

class ShellMagnetPU extends PowerUp {
    public ShellMagnetPU() { super("Shell Magnet", "M", "Pulls 2 shells from adjacent pits into this pit."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        if (pitIndex < 0) { game.log("Shell Magnet needs a pit to target."); return; }
        List<Integer> neighbors = game.getAdjacentIndices(pitIndex);
        int pulled = 0;
        for (int ni: neighbors) {
            Pit np = game.board[ni];
            while (np.getStones() > 0 && pulled < 2) { np.setStones(np.getStones() - 1); pulled++; }
            if (pulled >= 2) break;
        }
        game.board[pitIndex].setStones(game.board[pitIndex].getStones() + pulled);
        game.log(player.getName() + " activated Shell Magnet and pulled " + pulled + " shells into pit " + pitIndex + ".");
    }
}

class StealShellsPU extends PowerUp {
    public StealShellsPU() { super("Steal Shells", "S", "Steal 3 shells from opponent's house."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        Player opp = game.getOpponent(player);
        Pit oppHouse = game.board[opp.getHouseIndex()];
        Pit myHouse = game.board[player.getHouseIndex()];
        int steal = Math.min(3, oppHouse.getStones());
        oppHouse.setStones(oppHouse.getStones() - steal);
        myHouse.setStones(myHouse.getStones() + steal);
        game.log(player.getName() + " activated Steal Shells: stole " + steal + " from opponent house.");
    }
}

class PitShieldPU extends PowerUp {
    public PitShieldPU() { super("Pit Shield", "P", "Prevents your next pit's power-up from being stolen once."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        // Protect the targeted pit for 2 of the player's turns from receiving new power-ups on refresh
        if (pitIndex < 0) { game.log("Pit Shield needs a pit target."); return; }
        game.setProtectedPit(player, pitIndex, 2);
        game.log(player.getName() + " activated Pit Shield on pit " + pitIndex + " (2 turns).");
    }
}

class AddShellsPU extends PowerUp {
    public AddShellsPU() { super("Add Shells", "A", "Add 3 shells to selected pit."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        if (pitIndex < 0) { game.log("Add Shells needs a pit target."); return; }
        game.board[pitIndex].setStones(game.board[pitIndex].getStones() + 3);
        game.log(player.getName() + " added 3 shells to pit " + pitIndex + ".");
    }
}

class SwapHousesPU extends PowerUp {
    public SwapHousesPU() { super("Swap Houses", "W", "Swap shells between houses."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        Player opp = game.getOpponent(player);
        Pit myHouse = game.board[player.getHouseIndex()];
        Pit oppHouse = game.board[opp.getHouseIndex()];
        int t = myHouse.getStones(); myHouse.setStones(oppHouse.getStones()); oppHouse.setStones(t);
        game.log(player.getName() + " swapped houses with opponent.");
    }
}

class SkipOpponentPU extends PowerUp {
    public SkipOpponentPU() { super("Skip Opponent", "K", "Opponent loses next turn."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.setSkipOpponent(true);
        game.log(player.getName() + " activated Skip Opponent Turn.");
    }
}

class LuckyDropPU extends PowerUp {
    public LuckyDropPU() { super("Lucky Drop", "L", "Add 5 shells directly to your house."); }
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.board[player.getHouseIndex()].setStones(game.board[player.getHouseIndex()].getStones() + 5);
        game.log(player.getName() + " got a Lucky Drop (+5 to house).");
    }
}
