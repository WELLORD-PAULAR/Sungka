package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;

public class SkipOpponentPU extends PowerUp {
    public SkipOpponentPU() { super("Skip Opponent", "K", "Opponent loses next turn."); }
    @Override
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.setSkipOpponent(true);
        game.log(player.getName() + " activated Skip Opponent Turn.");
    }
}
