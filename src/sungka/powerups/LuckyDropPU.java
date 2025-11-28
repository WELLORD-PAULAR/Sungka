package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;

public class LuckyDropPU extends PowerUp {
    public LuckyDropPU() { super("Lucky Drop", "L", "Add 5 shells directly to your house."); }
    @Override
    public void apply(SungkaGame game, Player player, int pitIndex) {
        game.board[player.getHouseIndex()].setStones(game.board[player.getHouseIndex()].getStones() + 5);
        game.log(player.getName() + " got a Lucky Drop (+5 to house).");
    }
}
