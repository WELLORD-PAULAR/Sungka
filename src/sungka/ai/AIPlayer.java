package sungka.ai;

import sungka.core.SungkaGame;
import sungka.model.Player;

public interface AIPlayer {
    int chooseMove(SungkaGame game, Player ai);
}
