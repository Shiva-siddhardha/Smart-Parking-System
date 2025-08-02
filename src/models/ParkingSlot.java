package models;

public class ParkingSlot implements Comparable<ParkingSlot> {
    private int slotId;
    private String slotNumber;
    private int distanceFromEntry;
    private boolean isOccupied;
    private int floorId;
    private int typeId;
    
    public ParkingSlot(int slotId, String slotNumber, int distanceFromEntry, 
                      boolean isOccupied, int floorId, int typeId) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.distanceFromEntry = distanceFromEntry;
        this.isOccupied = isOccupied;
        this.floorId = floorId;
        this.typeId = typeId;
    }
    
    @Override
    public int compareTo(ParkingSlot other) {
        return Integer.compare(this.distanceFromEntry, other.distanceFromEntry);
    }
    
    // Getters and Setters
    public int getSlotId() { return slotId; }
    public String getSlotNumber() { return slotNumber; }
    public int getDistanceFromEntry() { return distanceFromEntry; }
    public boolean isOccupied() { return isOccupied; }
    public int getFloorId() { return floorId; }
    public int getTypeId() { return typeId; }
    
    public void setOccupied(boolean occupied) { this.isOccupied = occupied; }
    
    @Override
    public String toString() {
        return slotNumber + " (Distance: " + distanceFromEntry + ")";
    }
}
