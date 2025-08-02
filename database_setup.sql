-- Create and use the database
DROP DATABASE IF EXISTS smart_parking_db;
CREATE DATABASE smart_parking_db;
USE smart_parking_db;

-- Vehicle types with hourly rates
CREATE TABLE vehicle_types (
    type_id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(20) NOT NULL UNIQUE,
    rate_per_hour DECIMAL(10,2) NOT NULL
);

-- Floors/zones for scalability
CREATE TABLE floors (
    floor_id INT PRIMARY KEY AUTO_INCREMENT,
    floor_name VARCHAR(50) NOT NULL,
    total_slots INT NOT NULL
);

-- Registered vehicles
CREATE TABLE vehicles (
    vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_number VARCHAR(20) NOT NULL UNIQUE,
    type_id INT NOT NULL,
    owner_name VARCHAR(100),
    FOREIGN KEY (type_id) REFERENCES vehicle_types(type_id)
);

-- Parking slots
CREATE TABLE parking_slots (
    slot_id INT PRIMARY KEY AUTO_INCREMENT,
    floor_id INT NOT NULL,
    slot_number VARCHAR(10) NOT NULL,
    distance_from_entry INT NOT NULL,
    is_occupied BOOLEAN DEFAULT FALSE,
    type_id INT NOT NULL,
    FOREIGN KEY (floor_id) REFERENCES floors(floor_id),
    FOREIGN KEY (type_id) REFERENCES vehicle_types(type_id),
    UNIQUE KEY unique_floor_slot (floor_id, slot_number)
);

-- Vehicle entry/exit logs
CREATE TABLE vehicle_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    slot_id INT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    amount_charged DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('PARKED', 'EXITED') DEFAULT 'PARKED',
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id)
);

-- Historical slot assignments (audit trail)
CREATE TABLE slot_assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    slot_id INT NOT NULL,
    assigned_time DATETIME NOT NULL,
    released_time DATETIME NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id)
);

-- Insert sample vehicle types
INSERT INTO vehicle_types (type_name, rate_per_hour) VALUES 
('CAR', 10.00), 
('BIKE', 5.00), 
('TRUCK', 20.00);

-- Insert sample floors
INSERT INTO floors (floor_name, total_slots) VALUES 
('Ground Floor', 50), 
('First Floor', 40),
('Second Floor', 30),
('Third Floor', 25);

-- Insert sample vehicles
INSERT INTO vehicles (vehicle_number, type_id, owner_name) VALUES 
('KA01AB1234', 1, 'John Doe'), 
('KA02CD5678', 2, 'Jane Smith');

-- Ground Floor slots
INSERT INTO parking_slots (floor_id, slot_number, distance_from_entry, type_id) VALUES 
(1, 'A01', 10, 1), 
(1, 'A02', 15, 1), 
(1, 'A03', 20, 1),
(1, 'B01', 25, 2), 
(1, 'B02', 30, 2);

-- First Floor slots
INSERT INTO parking_slots (floor_id, slot_number, distance_from_entry, type_id) VALUES
(2, 'A11', 12, 1), 
(2, 'A12', 17, 1),
(2, 'B11', 22, 2), 
(2, 'B12', 28, 2),
(2, 'C11', 35, 3);

-- Second Floor slots
INSERT INTO parking_slots (floor_id, slot_number, distance_from_entry, type_id) VALUES
(3, 'C01', 10, 1), 
(3, 'C02', 15, 1), 
(3, 'C03', 20, 1),
(3, 'D01', 12, 2), 
(3, 'D02', 18, 2),
(3, 'E01', 25, 3);

-- Third Floor slots
INSERT INTO parking_slots (floor_id, slot_number, distance_from_entry, type_id) VALUES
(4, 'F01', 8, 1), 
(4, 'F02', 16, 1),
(4, 'G01', 9, 2), 
(4, 'G02', 14, 2),
(4, 'H01', 22, 3);
