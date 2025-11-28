package sungka.powerups;

import sungka.model.Player;
import sungka.model.Pit;

import java.util.*;
import java.util.function.Supplier;

public class PowerUpManager {
    private final List<Supplier<PowerUp>> pool = new ArrayList<>();
    private final Random rnd = new Random();
    public PowerUpManager() {
        // by default enable all registered codes: populate pool from registry directly
        pool.addAll(REGISTRY.values());
    }

    public PowerUp randomPowerUp() {
        if (pool.isEmpty()) return null;
        Supplier<PowerUp> sup = pool.get(rnd.nextInt(pool.size()));
        return sup.get();
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
            PowerUp p = randomPowerUp();
            if (p != null) board[empties.get(k)].setPowerUp(p);
        }
    }
    // --- New API: allow enabling/disabling specific power-up codes ---
    // Static registry of all supported power-up codes -> supplier
    private static final Map<String, Supplier<PowerUp>> REGISTRY = new LinkedHashMap<>();
    // cached set of all codes (LinkedHashSet preserves insertion order)
    private static final Set<String> ALL_CODES;

    static {
        REGISTRY.put("D", DoubleCapturePU::new);
        REGISTRY.put("B", BonusTurnPU::new);
        REGISTRY.put("R", ReverseSowPU::new);
        REGISTRY.put("M", ShellMagnetPU::new);
        REGISTRY.put("S", StealShellsPU::new);
        REGISTRY.put("P", PitShieldPU::new);
        REGISTRY.put("A", AddShellsPU::new);
        REGISTRY.put("W", SwapHousesPU::new);
        REGISTRY.put("K", SkipOpponentPU::new);
        REGISTRY.put("L", LuckyDropPU::new);
        ALL_CODES = new LinkedHashSet<>(REGISTRY.keySet());
    }
    // Configure which power-up codes are enabled (codes are single letters like "P", "S", etc.).
    public void setAllowedCodes(Collection<String> codes) {
        pool.clear();
        if (codes == null) codes = ALL_CODES;
        for (String c : codes) {
            Supplier<PowerUp> s = REGISTRY.get(c);
            if (s != null) pool.add(s);
        }
    }

    // Return a map of available power-up codes to their display name.
    public static Map<String, String> getAvailableDefinitions() {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Supplier<PowerUp>> e : REGISTRY.entrySet()) {
            PowerUp p = e.getValue().get();
            out.put(e.getKey(), p.getName());
        }
        return out;
    }
    // Return a map of code -> description for available power-ups.
    public static Map<String, String> getAvailableDescriptions() {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Supplier<PowerUp>> e : REGISTRY.entrySet()) {
            PowerUp p = e.getValue().get();
            out.put(e.getKey(), p.getDescription());
        }
        return out;
    }
}
