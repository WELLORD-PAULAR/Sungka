package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;

public class ReverseSowPU extends PowerUp {
    public ReverseSowPU() { super("Reverse Sowing", "R", "Reverse sow direction for next sow."); }
    @Override
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.setReverseSowing(true);
        game.log(player.getName() + " activated Reverse Sowing.");
    }
}
