package sungka.model;

import sungka.powerups.PowerUp;

public class Pit {
    private int stones;
    private final boolean house;
    private PowerUp powerUp;

    public Pit(int stones, boolean house) {
        this.stones = stones;
        this.house = house;
        this.powerUp = null;
    }
    public int getStones() { return stones; }
    public void setStones(int s) { stones = s; }
    public void addStone() { stones++; }
    public boolean isHouse() { return house; }

    public boolean hasPowerUp() { return powerUp != null; }
    public PowerUp getPowerUp() { return powerUp; }
    public void setPowerUp(PowerUp p) { powerUp = p; }
    public void clearPowerUp() { powerUp = null; }
}
