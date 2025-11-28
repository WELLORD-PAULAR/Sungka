package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;

public class AddShellsPU extends PowerUp {
    public AddShellsPU() { super("Add Shells", "A", "Add 3 shells to selected pit."); }
    @Override
    public void apply(SungkaGame game, Player player, int pitIndex) {
        if (pitIndex < 0) { game.log("Add Shells needs a pit target."); return; }
        game.board[pitIndex].setStones(game.board[pitIndex].getStones() + 3);
        game.log(player.getName() + " added 3 shells to pit " + pitIndex + ".");
    }
}
