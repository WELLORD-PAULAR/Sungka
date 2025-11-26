package sungka.powerups;

import sungka.core.SungkaGame;
import sungka.model.Player;

public abstract class PowerUp {
    protected final String name;
    protected final String code; // single-letter indicator for UI
    protected final String description;

    public PowerUp(String name, String code, String description) {
        this.name = name; this.code = code; this.description = description;
    }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getDescription() { return description; }

    // pitIndex is the pit where the power-up was activated (may be -1 if not tied to a pit)
    public abstract void apply(SungkaGame game, Player player, int pitIndex);
}
