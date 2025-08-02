package services;

import db.DBConnection;
import models.ParkingSlot;
import models.VehicleLog;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ParkingManager {
    private Connection conn;
    
    public ParkingManager() {
        this.conn = DBConnection.getConnection();
    }
    
    // Get vehicle ID by number, create if doesn't exist
    private int getOrCreateVehicle(String vehicleNumber, int typeId) throws SQLException {
        String query = "SELECT vehicle_id FROM vehicles WHERE vehicle_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, vehicleNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("vehicle_id");
            } else {
                // Create new vehicle
                String insertQuery = "INSERT INTO vehicles (vehicle_number, type_id, owner_name) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                    insertStmt.setString(1, vehicleNumber);
                    insertStmt.setInt(2, typeId);
                    insertStmt.setString(3, "Unknown Owner");
                    insertStmt.executeUpdate();
                    
                    ResultSet keys = insertStmt.getGeneratedKeys();
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to get or create vehicle");
    }
    
    // Assign nearest available slot using PriorityQueue (min-heap)
    public String assignSlot(String vehicleNumber, int vehicleTypeId) {
        try {
            // Check if vehicle is already parked
            if (isVehicleParked(vehicleNumber)) {
                return "Vehicle " + vehicleNumber + " is already parked!";
            }
            
            // Get available slots for this vehicle type
            PriorityQueue<ParkingSlot> availableSlots = getAvailableSlots(vehicleTypeId);
            
            if (availableSlots.isEmpty()) {
                return "No available slots for this vehicle type!";
            }
            
            // Get nearest slot (min-heap automatically gives us the nearest)
            ParkingSlot nearestSlot = availableSlots.poll();
            
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Mark slot as occupied
                markSlotOccupied(nearestSlot.getSlotId(), true);
                
                // Get or create vehicle
                int vehicleId = getOrCreateVehicle(vehicleNumber, vehicleTypeId);
                
                // Create entry log
                createEntryLog(vehicleId, nearestSlot.getSlotId());
                
                // Create slot assignment record
                createSlotAssignment(vehicleId, nearestSlot.getSlotId());
                
                conn.commit();
                return "Vehicle " + vehicleNumber + " assigned to slot " + nearestSlot.getSlotNumber() + 
                       " (Distance: " + nearestSlot.getDistanceFromEntry() + "m)";
                       
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (Exception e) {
            return "Error assigning slot: " + e.getMessage();
        }
    }
    
    // Process vehicle exit and calculate bill
    public String processExit(String vehicleNumber) {
        try {
            // Get active parking log
            String query = """
                SELECT vl.log_id, vl.vehicle_id, vl.slot_id, vl.entry_time, ps.slot_number, vt.rate_per_hour
                FROM vehicle_logs vl
                JOIN parking_slots ps ON vl.slot_id = ps.slot_id
                JOIN vehicles v ON vl.vehicle_id = v.vehicle_id
                JOIN vehicle_types vt ON v.type_id = vt.type_id
                WHERE v.vehicle_number = ? AND vl.status = 'PARKED'
            """;
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, vehicleNumber);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    return "Vehicle " + vehicleNumber + " is not currently parked!";
                }
                
                int logId = rs.getInt("log_id");
                int vehicleId = rs.getInt("vehicle_id");
                int slotId = rs.getInt("slot_id");
                LocalDateTime entryTime = rs.getTimestamp("entry_time").toLocalDateTime();
                String slotNumber = rs.getString("slot_number");
                double ratePerHour = rs.getDouble("rate_per_hour");
                
                LocalDateTime exitTime = LocalDateTime.now();
                
                // Calculate parking duration and amount
                long minutesParked = ChronoUnit.MINUTES.between(entryTime, exitTime);
                double hoursParked = Math.max(1, Math.ceil(minutesParked / 60.0)); // Minimum 1 hour
                double amount = hoursParked * ratePerHour;
                
                // Start transaction
                conn.setAutoCommit(false);
                
                try {
                    // Update exit log
                    updateExitLog(logId, exitTime, amount);
                    
                    // Free up the slot
                    markSlotOccupied(slotId, false);
                    
                    // Update slot assignment
                    updateSlotAssignment(vehicleId, slotId, exitTime);
                    
                    conn.commit();
                    
                    return String.format("Vehicle %s exited from slot %s.\nParking Duration: %.1f hours\nAmount: ₹%.2f", 
                                       vehicleNumber, slotNumber, hoursParked, amount);
                                       
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            
        } catch (Exception e) {
            return "Error processing exit: " + e.getMessage();
        }
    }
    
    // Helper methods
    private PriorityQueue<ParkingSlot> getAvailableSlots(int vehicleTypeId) throws SQLException {
        PriorityQueue<ParkingSlot> slots = new PriorityQueue<>();
        
        String query = """
            SELECT slot_id, slot_number, distance_from_entry, is_occupied, floor_id, type_id 
            FROM parking_slots 
            WHERE is_occupied = FALSE AND type_id = ?
            ORDER BY distance_from_entry
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, vehicleTypeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                slots.add(new ParkingSlot(
                    rs.getInt("slot_id"),
                    rs.getString("slot_number"),
                    rs.getInt("distance_from_entry"),
                    rs.getBoolean("is_occupied"),
                    rs.getInt("floor_id"),
                    rs.getInt("type_id")
                ));
            }
        }
        return slots;
    }
    
    private boolean isVehicleParked(String vehicleNumber) throws SQLException {
        String query = """
            SELECT COUNT(*) FROM vehicle_logs vl
            JOIN vehicles v ON vl.vehicle_id = v.vehicle_id
            WHERE v.vehicle_number = ? AND vl.status = 'PARKED'
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, vehicleNumber);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }
    
    private void markSlotOccupied(int slotId, boolean occupied) throws SQLException {
        String query = "UPDATE parking_slots SET is_occupied = ? WHERE slot_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, occupied);
            pstmt.setInt(2, slotId);
            pstmt.executeUpdate();
        }
    }
    
    private void createEntryLog(int vehicleId, int slotId) throws SQLException {
        String query = "INSERT INTO vehicle_logs (vehicle_id, slot_id, entry_time, status) VALUES (?, ?, ?, 'PARKED')";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, vehicleId);
            pstmt.setInt(2, slotId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        }
    }
    
    private void createSlotAssignment(int vehicleId, int slotId) throws SQLException {
        String query = "INSERT INTO slot_assignments (vehicle_id, slot_id, assigned_time) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, vehicleId);
            pstmt.setInt(2, slotId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        }
    }
    
    private void updateExitLog(int logId, LocalDateTime exitTime, double amount) throws SQLException {
        String query = "UPDATE vehicle_logs SET exit_time = ?, amount_charged = ?, status = 'EXITED' WHERE log_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(exitTime));
            pstmt.setDouble(2, amount);
            pstmt.setInt(3, logId);
            pstmt.executeUpdate();
        }
    }
    
    private void updateSlotAssignment(int vehicleId, int slotId, LocalDateTime exitTime) throws SQLException {
        String query = "UPDATE slot_assignments SET released_time = ? WHERE vehicle_id = ? AND slot_id = ? AND released_time IS NULL";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(exitTime));
            pstmt.setInt(2, vehicleId);
            pstmt.setInt(3, slotId);
            pstmt.executeUpdate();
        }
    }
    
    // Get all available slots
    public List<ParkingSlot> getAllAvailableSlots() throws SQLException {
        List<ParkingSlot> slots = new ArrayList<>();
        String query = """
            SELECT ps.slot_id, ps.slot_number, ps.distance_from_entry, ps.is_occupied, 
                   ps.floor_id, ps.type_id, f.floor_name, vt.type_name
            FROM parking_slots ps
            JOIN floors f ON ps.floor_id = f.floor_id
            JOIN vehicle_types vt ON ps.type_id = vt.type_id
            WHERE ps.is_occupied = FALSE
            ORDER BY ps.distance_from_entry
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                slots.add(new ParkingSlot(
                    rs.getInt("slot_id"),
                    rs.getString("floor_name") + "-" + rs.getString("slot_number") + " (" + rs.getString("type_name") + ")",
                    rs.getInt("distance_from_entry"),
                    rs.getBoolean("is_occupied"),
                    rs.getInt("floor_id"),
                    rs.getInt("type_id")
                ));
            }
        }
        return slots;
    }
    
    // Get all parking logs
    public List<VehicleLog> getAllLogs() throws SQLException {
        List<VehicleLog> logs = new ArrayList<>();
        String query = """
            SELECT vl.log_id, v.vehicle_number, vl.slot_id, ps.slot_number, 
                   vl.entry_time, vl.exit_time, vl.amount_charged, vl.status
            FROM vehicle_logs vl
            JOIN vehicles v ON vl.vehicle_id = v.vehicle_id
            JOIN parking_slots ps ON vl.slot_id = ps.slot_id
            ORDER BY vl.entry_time DESC
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                logs.add(new VehicleLog(
                    rs.getInt("log_id"),
                    rs.getString("vehicle_number"),
                    rs.getInt("slot_id"),
                    rs.getString("slot_number"),
                    rs.getTimestamp("entry_time").toLocalDateTime(),
                    rs.getTimestamp("exit_time") != null ? rs.getTimestamp("exit_time").toLocalDateTime() : null,
                    rs.getDouble("amount_charged"),
                    rs.getString("status")
                ));
            }
        }
        return logs;
    }
}