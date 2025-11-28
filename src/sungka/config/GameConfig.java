package sungka.config;

import java.util.LinkedHashSet;
import java.util.Set;

// Central game configuration singleton for pre-game settings
public class GameConfig {
    private static final GameConfig INSTANCE = new GameConfig();

    private Set<String> enabledPowerUps = null; // null means default (all)
    private int winThreshold = 50;

    private GameConfig() {}

    public static GameConfig getInstance() { return INSTANCE; }

    public synchronized Set<String> getEnabledPowerUps() {
        return enabledPowerUps == null ? null : new LinkedHashSet<>(enabledPowerUps);
    }

    public synchronized void setEnabledPowerUps(Set<String> codes) {
        if (codes == null) this.enabledPowerUps = null;
        else this.enabledPowerUps = new LinkedHashSet<>(codes);
    }

    public synchronized int getWinThreshold() { return winThreshold; }
    public synchronized void setWinThreshold(int t) { this.winThreshold = Math.max(1, t); }
}
