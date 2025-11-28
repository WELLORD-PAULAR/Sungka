package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;
import sungka.model.Pit;

public class SwapHousesPU extends PowerUp {
    public SwapHousesPU() { super("Swap Houses", "W", "Swap shells between houses."); }
    @Override
    public void apply(SungkaGame game, Player player, int pitIndex) {
        Player opp = game.getOpponent(player);
        Pit myHouse = game.board[player.getHouseIndex()];
        Pit oppHouse = game.board[opp.getHouseIndex()];
        int t = myHouse.getStones(); myHouse.setStones(oppHouse.getStones()); oppHouse.setStones(t);
        game.log(player.getName() + " swapped houses with opponent.");
    }
}
