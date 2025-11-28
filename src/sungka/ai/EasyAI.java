package sungka.ai;

import sungka.core.SungkaGame;
import sungka.model.Player;
import sungka.model.Pit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EasyAI implements AIPlayer {
    private final Random rnd = new Random();

    @Override
    public int chooseMove(SungkaGame game, Player ai) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = ai.getStart(); i <= ai.getEnd(); i++) {
            Pit p = game.board[i];
            if (p.getStones() > 0) candidates.add(i);
        }
        if (candidates.isEmpty()) return -1;
        return candidates.get(rnd.nextInt(candidates.size()));
    }
}
