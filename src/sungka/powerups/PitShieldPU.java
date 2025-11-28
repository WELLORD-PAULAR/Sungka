package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;

public class PitShieldPU extends PowerUp {
    public PitShieldPU() { super("Pit Shield", "P", "Prevents your next pit's power-up from being stolen once."); }
    @Override
    public void apply(SungkaGame game, Player player, int pitIndex) {
        if (pitIndex < 0) { game.log("Pit Shield needs a pit target."); return; }
        game.setProtectedPit(player, pitIndex, 2);
        game.log(player.getName() + " activated Pit Shield on pit " + pitIndex + " (2 turns).");
    }
}
