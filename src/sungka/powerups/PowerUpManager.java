package sungka.powerups;

import sungka.model.Player;
import sungka.model.Pit;

import java.util.*;

public class PowerUpManager {
    private final List<PowerUp> pool = new ArrayList<>();
    private final Random rnd = new Random();

    public PowerUpManager() {
        pool.add(new DoubleCapturePU());
        pool.add(new BonusTurnPU());
        pool.add(new ReverseSowPU());
        pool.add(new ShellMagnetPU());
        pool.add(new StealShellsPU());
        pool.add(new PitShieldPU());
        pool.add(new AddShellsPU());
        pool.add(new SwapHousesPU());
        pool.add(new SkipOpponentPU());
        pool.add(new LuckyDropPU());
    }

    public PowerUp randomPowerUp() {
        PowerUp p = pool.get(rnd.nextInt(pool.size()));
        if (p instanceof DoubleCapturePU) return new DoubleCapturePU();
        if (p instanceof BonusTurnPU) return new BonusTurnPU();
        if (p instanceof ReverseSowPU) return new ReverseSowPU();
        if (p instanceof ShellMagnetPU) return new ShellMagnetPU();
        if (p instanceof StealShellsPU) return new StealShellsPU();
        if (p instanceof PitShieldPU) return new PitShieldPU();
        if (p instanceof AddShellsPU) return new AddShellsPU();
        if (p instanceof SwapHousesPU) return new SwapHousesPU();
        if (p instanceof SkipOpponentPU) return new SkipOpponentPU();
        if (p instanceof LuckyDropPU) return new LuckyDropPU();
        return new LuckyDropPU();
    }

    public void refillToCap(Player player, Pit[] board, int cap) {
        List<Integer> empties = new ArrayList<>();
        for (int i = player.getStart(); i <= player.getEnd(); i++) {
            if (!board[i].hasPowerUp()) empties.add(i);
        }
        Collections.shuffle(empties, rnd);

        int current = 0;
        for (int i = player.getStart(); i <= player.getEnd(); i++) if (board[i].hasPowerUp()) current++;
        int need = cap - current;
        for (int k = 0; k < need && k < empties.size(); k++) {
            board[empties.get(k)].setPowerUp(randomPowerUp());
        }
    }
}
