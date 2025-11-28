package sungka.ai;

import sungka.core.SungkaGame;
import sungka.model.Player;
import sungka.model.Pit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HardAI implements AIPlayer {
    private final Random rnd = new Random();
    private final double[] weights = new double[16];
    private final double baseLearningRate = 0.02;

    @Override
    public int chooseMove(SungkaGame game, Player ai) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = ai.getStart(); i <= ai.getEnd(); i++) {
            Pit p = game.board[i];
            if (p.getStones() > 0) candidates.add(i);
        }
        if (candidates.isEmpty()) return -1;

        int maxDepth = 6;
        double bestScore = Double.NEGATIVE_INFINITY;
        int best;
        int nodes = 0;
        double[] scores = new double[16];
        double minS = Double.POSITIVE_INFINITY, maxS = Double.NEGATIVE_INFINITY;
        for (int idx : candidates) {
            SungkaGame sim = game.copy();
            if (sim.board[idx].hasPowerUp()) sim.activatePowerUpInPit(idx);
            else sim.makeMove(idx);
            double score = -negamax(sim, maxDepth - 1, ai);
            score += rnd.nextDouble() * 1e-4; // tiny jitter
            scores[idx] = score;
            if (score < minS) minS = score;
            if (score > maxS) maxS = score;
            if (score > bestScore) { bestScore = score; }
            nodes++;
            if (nodes > 5000) break; // safety
        }

        double houseSum = game.board[game.playerA.getHouseIndex()].getStones() + game.board[game.playerB.getHouseIndex()].getStones();
        double progress = houseSum / (double)(game.getWinThreshold() * 2);
        double lr = Math.min(0.25, baseLearningRate * (1.0 + progress * 2.0));
        if (maxS - minS < 1e-6) maxS = minS + 1.0;
        for (int idx : candidates) {
            double norm = (scores[idx] - minS) / (maxS - minS);
            weights[idx] = weights[idx] * (1.0 - lr) + lr * norm;
        }

        bestScore = Double.NEGATIVE_INFINITY; best = -1;
        for (int idx : candidates) {
            double composed = scores[idx] + weights[idx] * (1.0 + progress * 5.0);
            composed += rnd.nextDouble() * 0.01; // exploration
            if (composed > bestScore) { bestScore = composed; best = idx; }
        }
        if (best != -1) return best;
        return candidates.get(rnd.nextInt(candidates.size()));
    }

    private double negamax(SungkaGame game, int depth, Player rootAI) {
        Player winner = game.checkForWinner();
        if (depth <= 0 || winner != null) return evaluate(game, rootAI);

        List<Integer> moves = new ArrayList<>();
        Player cur = game.getCurrent();
        for (int i = cur.getStart(); i <= cur.getEnd(); i++) if (game.board[i].getStones() > 0 || game.board[i].hasPowerUp()) moves.add(i);
        if (moves.isEmpty()) return evaluate(game, rootAI);

        double best = Double.NEGATIVE_INFINITY;
        for (int m : moves) {
            SungkaGame sim = game.copy();
            if (sim.board[m].hasPowerUp()) sim.activatePowerUpInPit(m);
            else sim.makeMove(m);
            double val = -negamax(sim, depth - 1, rootAI);
            if (val > best) best = val;
        }
        return best;
    }

    private double evaluate(SungkaGame g, Player ai) {
        Player opp = g.getOpponent(ai);
        int myHouse = g.board[ai.getHouseIndex()].getStones();
        int oppHouse = g.board[opp.getHouseIndex()].getStones();
        int houseDiff = myHouse - oppHouse;
        int mySide = 0, oppSide = 0;
        for (int i = ai.getStart(); i <= ai.getEnd(); i++) mySide += g.board[i].getStones();
        for (int i = opp.getStart(); i <= opp.getEnd(); i++) oppSide += g.board[i].getStones();
        int sideDiff = mySide - oppSide;
        double score = houseDiff * 10.0 + sideDiff * 0.5;
        if (g.getCurrent() == ai) score += 2.0;
        return score;
    }
}
