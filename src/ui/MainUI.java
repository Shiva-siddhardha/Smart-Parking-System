package ui;

import services.ParkingManager;
import models.ParkingSlot;
import models.VehicleLog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MainUI extends JFrame {
    private ParkingManager parkingManager;
    private JTextField vehicleNumberField;
    private JComboBox<String> vehicleTypeCombo;
    private JTextArea resultArea;
    private JTable slotsTable, logsTable;
    private DefaultTableModel slotsModel, logsModel;
    
    public MainUI() {
        parkingManager = new ParkingManager();
        initializeUI();
        refreshTables();
    }
    
    private void initializeUI() {
        setTitle("Smart Parking Allocation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panels
        JPanel topPanel = createControlPanel();
        JPanel centerPanel = createTablesPanel();
        JPanel bottomPanel = createResultPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Vehicle Operations"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Vehicle Number Input
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Vehicle Number:"), gbc);
        
        gbc.gridx = 1;
        vehicleNumberField = new JTextField(15);
        panel.add(vehicleNumberField, gbc);
        
        // Vehicle Type Selection
        gbc.gridx = 2;
        panel.add(new JLabel("Type:"), gbc);
        
        gbc.gridx = 3;
        vehicleTypeCombo = new JComboBox<>(new String[]{"CAR (₹10/hr)", "BIKE (₹5/hr)", "TRUCK (₹20/hr)"});
        panel.add(vehicleTypeCombo, gbc);
        
        // Buttons
        gbc.gridx = 4;
        JButton assignButton = new JButton("Assign Slot");
        assignButton.setBackground(new Color(34, 139, 34));
        assignButton.setForeground(Color.WHITE);
        assignButton.addActionListener(e -> assignSlot());
        panel.add(assignButton, gbc);
        
        gbc.gridx = 5;
        JButton exitButton = new JButton("Process Exit");
        exitButton.setBackground(new Color(220, 20, 60));
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(e -> processExit());
        panel.add(exitButton, gbc);
        
        gbc.gridx = 6;
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(30, 144, 255));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> refreshTables());
        panel.add(refreshButton, gbc);
        
        return panel;
    }
    
    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Available Slots Table
        String[] slotsColumns = {"Slot", "Distance (m)", "Type"};
        slotsModel = new DefaultTableModel(slotsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        slotsTable = new JTable(slotsModel);
        slotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane slotsScrollPane = new JScrollPane(slotsTable);
        slotsScrollPane.setBorder(BorderFactory.createTitledBorder("Available Slots"));
        panel.add(slotsScrollPane);
        
        // Parking Logs Table
        String[] logsColumns = {"Vehicle", "Slot", "Entry", "Exit", "Amount", "Status"};
        logsModel = new DefaultTableModel(logsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        logsTable = new JTable(logsModel);
        logsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane logsScrollPane = new JScrollPane(logsTable);
        logsScrollPane.setBorder(BorderFactory.createTitledBorder("Parking Logs"));
        panel.add(logsScrollPane);
        
        return panel;
    }
    
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Operation Results"));
        
        resultArea = new JTextArea(4, 50);
        resultArea.setEditable(false);
        resultArea.setBackground(Color.BLACK);
        resultArea.setForeground(Color.GREEN);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void assignSlot() {
        String vehicleNumber = vehicleNumberField.getText().trim().toUpperCase();
        
        if (vehicleNumber.isEmpty()) {
            showMessage("Please enter vehicle number!", "ERROR");
            return;
        }
        
        int typeId = vehicleTypeCombo.getSelectedIndex() + 1; // 1=CAR, 2=BIKE, 3=TRUCK
        
        String result = parkingManager.assignSlot(vehicleNumber, typeId);
        showMessage(result, result.contains("Error") ? "ERROR" : "SUCCESS");
        
        if (!result.contains("Error")) {
            vehicleNumberField.setText("");
            refreshTables();
        }
    }
    
    private void processExit() {
        String vehicleNumber = vehicleNumberField.getText().trim().toUpperCase();
        
        if (vehicleNumber.isEmpty()) {
            showMessage("Please enter vehicle number!", "ERROR");
            return;
        }
        
        String result = parkingManager.processExit(vehicleNumber);
        showMessage(result, result.contains("Error") ? "ERROR" : "SUCCESS");
        
        if (!result.contains("Error")) {
            vehicleNumberField.setText("");
            refreshTables();
        }
    }
    
    private void refreshTables() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Refresh available slots
                slotsModel.setRowCount(0);
                List<ParkingSlot> slots = parkingManager.getAllAvailableSlots();
                for (ParkingSlot slot : slots) {
                    slotsModel.addRow(new Object[]{
                        slot.getSlotNumber(),
                        slot.getDistanceFromEntry(),
                        getTypeFromId(slot.getTypeId())
                    });
                }
                
                // Refresh logs
                logsModel.setRowCount(0);
                List<VehicleLog> logs = parkingManager.getAllLogs();
                for (VehicleLog log : logs) {
                    logsModel.addRow(new Object[]{
                        log.getVehicleNumber(),
                        log.getSlotNumber(),
                        log.getFormattedEntryTime(),
                        log.getFormattedExitTime(),
                        log.getAmountCharged() > 0 ? String.format("₹%.2f", log.getAmountCharged()) : "-",
                        log.getStatus()
                    });
                }
                
                // Update status
                showMessage("Tables refreshed successfully! Available slots: " + slots.size(), "INFO");
                
            } catch (Exception e) {
                showMessage("Error refreshing tables: " + e.getMessage(), "ERROR");
            }
        });
    }
    
    private String getTypeFromId(int typeId) {
        switch (typeId) {
            case 1: return "CAR";
            case 2: return "BIKE";
            case 3: return "TRUCK";
            default: return "UNKNOWN";
        }
    }
    
    private void showMessage(String message, String type) {
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        String coloredMessage = String.format("[%s] %s: %s%n", timestamp, type, message);
        
        SwingUtilities.invokeLater(() -> {
            resultArea.append(coloredMessage);
            resultArea.setCaretPosition(resultArea.getDocument().getLength());
        });
        
        // Also show popup for important messages
        if ("ERROR".equals(type)) {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        } else if ("SUCCESS".equals(type) && message.contains("assigned")) {
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        } else if ("SUCCESS".equals(type) && message.contains("exited")) {
            JOptionPane.showMessageDialog(this, message, "Billing Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            new MainUI();
        });
    }
}