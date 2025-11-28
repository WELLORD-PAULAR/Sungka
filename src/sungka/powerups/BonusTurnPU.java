package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;

public class BonusTurnPU extends PowerUp {
    public BonusTurnPU() { super("Bonus Turn", "B", "Gain an extra turn."); }
    @Override
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.setBonusTurn(true);
        game.log(player.getName() + " activated Bonus Turn.");
    }
}
