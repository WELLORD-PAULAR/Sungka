package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;
import sungka.model.Pit;

import java.util.List;

public class ShellMagnetPU extends PowerUp {
    public ShellMagnetPU() { super("Shell Magnet", "M", "Pulls 2 shells from adjacent pits into this pit."); }
    @Override
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
