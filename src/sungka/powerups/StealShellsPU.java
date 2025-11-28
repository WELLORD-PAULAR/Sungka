package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;
import sungka.model.Pit;

public class StealShellsPU extends PowerUp {
    public StealShellsPU() { super("Steal Shells", "S", "Steal 3 shells from opponent's house."); }
    @Override
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
