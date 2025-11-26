package sungka.model;

public class Player {
    private final String name;
    private final int start, end, houseIndex;

    public Player(String name, int start, int end, int houseIndex) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.houseIndex = houseIndex;
    }
    public String getName() { return name; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public int getHouseIndex() { return houseIndex; }
    public boolean ownsPit(int idx) { return idx >= start && idx <= end; }
}
