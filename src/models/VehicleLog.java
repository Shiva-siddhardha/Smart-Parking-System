package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VehicleLog {
    private int logId;
    private String vehicleNumber;
    private int slotId;
    private String slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double amountCharged;
    private String status;
    
    public VehicleLog(int logId, String vehicleNumber, int slotId, String slotNumber,
                     LocalDateTime entryTime, LocalDateTime exitTime, 
                     double amountCharged, String status) {
        this.logId = logId;
        this.vehicleNumber = vehicleNumber;
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.amountCharged = amountCharged;
        this.status = status;
    }
    
    // Getters
    public int getLogId() { return logId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public int getSlotId() { return slotId; }
    public String getSlotNumber() { return slotNumber; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getAmountCharged() { return amountCharged; }
    public String getStatus() { return status; }
    
    public String getFormattedEntryTime() {
        return entryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public String getFormattedExitTime() {
        return exitTime != null ? exitTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "Still Parked";
    }
}