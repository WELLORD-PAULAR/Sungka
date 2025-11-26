package sungka.test;

import sungka.core.SungkaGame;
import sungka.ai.SimpleAI;
import sungka.model.Player;

public class SungkaSimulator {
    public static void main(String[] args) {
        SungkaGame game = new SungkaGame();
        SimpleAI aiA = new SimpleAI(SimpleAI.Difficulty.MEDIUM);
        SimpleAI aiB = new SimpleAI(SimpleAI.Difficulty.MEDIUM);

        int turn = 0;
        while (turn < 300) {
            Player cur = game.getCurrent();
            int move = (cur == game.playerA) ? aiA.chooseMove(game, cur) : aiB.chooseMove(game, cur);
            if (move < 0) break;
            if (game.board[move].hasPowerUp()) {
                game.activatePowerUpInPit(move);
                System.out.println(cur.getName() + " activated power-up at " + move + ".");
            } else {
                game.makeMove(move);
                System.out.println(cur.getName() + " sowed from " + move + ".");
            }
            int a = game.board[game.playerA.getHouseIndex()].getStones();
            int b = game.board[game.playerB.getHouseIndex()].getStones();
            System.out.println("Score: A=" + a + " B=" + b);
            if (game.checkForWinner() != null) {
                System.out.println("Winner: " + game.checkForWinner().getName());
                break;
            }
            turn++;
        }
        System.out.println("Simulation ended after " + turn + " turns.");
    }
}
