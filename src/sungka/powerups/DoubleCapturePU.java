package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;

public class DoubleCapturePU extends PowerUp {
    public DoubleCapturePU() { super("Double Capture", "D", "Next capture doubles shells."); }
    @Override
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.setDoubleCapture(true);
        game.log(player.getName() + " activated Double Capture.");
    }
}
