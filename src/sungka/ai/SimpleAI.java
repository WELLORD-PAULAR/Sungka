package sungka.ai;

import sungka.core.SungkaGame;
import sungka.model.Player;

 // Thin facade that selects a concrete AI implementation by difficulty
 // and delegates `chooseMove(...)` calls to it.
public class SimpleAI {
    public enum Difficulty { EASY, MEDIUM, HARD }

    private final AIPlayer impl;

    public SimpleAI(Difficulty d) {
        switch (d) {
            case EASY: impl = new EasyAI(); break;
            case MEDIUM: impl = new MediumAI(); break;
            case HARD: default: impl = new HardAI(); break;
        }
    }

    public int chooseMove(SungkaGame game, Player ai) {
        return impl.chooseMove(game, ai);
    }
}
