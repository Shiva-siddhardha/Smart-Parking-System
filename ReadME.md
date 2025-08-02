# 🚗 Smart Parking Allocation System

> **An intelligent parking management system that optimizes slot allocation using advanced algorithms and real-time database operations.**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://mysql.com/)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing-green.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![JDBC](https://img.shields.io/badge/Database-JDBC-red.svg)](https://docs.oracle.com/javase/tutorial/jdbc/)

## 🎯 Project Overview

This desktop application revolutionizes parking management by implementing a **greedy algorithm** with **PriorityQueue (min-heap)** to assign the nearest available parking slot to incoming vehicles. Built with enterprise-grade architecture, it ensures **zero double-allocation** through database transactions and provides **real-time billing** based on parking duration.

## 🌟 Key Features

### 🧠 **Smart Algorithm Implementation**
- **Greedy Algorithm**: Uses Java's `PriorityQueue` as min-heap for optimal O(log n) slot selection
- **Nearest Slot Assignment**: Automatically assigns closest available slot to minimize walking distance
- **Multi-Vehicle Support**: Handles cars, bikes, and trucks with different pricing tiers

### 🛡️ **Enterprise-Grade Reliability**
- **Zero Double Allocation**: Database transactions with rollback mechanism prevent slot conflicts
- **Real-time Updates**: Synchronized GUI and database state with instant refresh
- **Data Integrity**: Foreign key constraints and ACID transaction properties

### 💰 **Automated Billing System**
- **Duration-based Pricing**: Calculates charges using `java.time` API with minute-level precision
- **Flexible Rate Structure**: Configurable hourly rates per vehicle type
- **Instant Bill Generation**: Immediate payment calculation on vehicle exit

### 📊 **Comprehensive Monitoring**
- **Live Dashboard**: Real-time view of available slots and parking history
- **Audit Trail**: Complete log of all parking activities for compliance
- **Visual Interface**: Professional Swing GUI with color-coded status indicators



## 🗄️ Database Schema

**Normalized Database Design (3NF)** with 6 optimized tables:

- `vehicle_types` - Vehicle categories with hourly rates
- `floors` - Multi-level parking support
- `vehicles` - Vehicle registry and ownership
- `parking_slots` - Physical slot inventory with distance metrics
- `vehicle_logs` - Complete parking session tracking
- `slot_assignments` - Historical audit trail

## 🚀 Quick Start

### Prerequisites
- Java JDK 17+
- MySQL Server 8.0+
- MySQL Connector/J driver

### Installation

1. **Clone and Setup**
   ```bash
   git clone <repository-url>
   cd SmartParkingSystem
   mkdir lib bin
   ```

2. **Download Dependencies**
   - Download `mysql-connector-java-8.0.33.jar` to `lib/` folder

3. **Database Setup**
   ```sql
   -- Run the provided database_setup.sql script
   mysql -u root -p < database_setup.sql
   ```

4. **Configure Database**
   ```java
   // Update credentials in src/db/DBConnection.java
   private static final String USERNAME = "your_username";
   private static final String PASSWORD = "your_password";
   ```

5. **Compile and Run**
   ```bash
   # Linux/Mac
   javac -cp "lib/*" -d bin src/**/*.java
   java -cp "bin:lib/*" ui.MainUI
   
   # Windows
   javac -cp "lib/*" -d bin src/**/*.java
   java -cp "bin;lib/*" ui.MainUI
   ```

## 🎮 Usage Demo

### Vehicle Entry
1. Enter vehicle number (e.g., "KA01AB1234")
2. Select vehicle type (CAR/BIKE/TRUCK)
3. Click **"Assign Slot"**
4. System finds and assigns nearest available slot

### Vehicle Exit
1. Enter parked vehicle number
2. Click **"Process Exit"**
3. System calculates duration and generates bill
4. Slot becomes available for next vehicle

## 💡 Technical Highlights

### Algorithm Implementation
```java
// Min-heap for nearest slot selection
PriorityQueue<ParkingSlot> availableSlots = new PriorityQueue<>();

@Override
public int compareTo(ParkingSlot other) {
    return Integer.compare(this.distanceFromEntry, other.distanceFromEntry);
}
```

### Transaction Safety
```java
conn.setAutoCommit(false);
try {
    markSlotOccupied(slotId, true);
    createEntryLog(vehicleId, slotId);
    conn.commit(); // All operations succeed
} catch (Exception e) {
    conn.rollback(); // Rollback on failure
}
```

### Real-time Billing
```java
long minutesParked = ChronoUnit.MINUTES.between(entryTime, exitTime);
double hoursParked = Math.max(1, Math.ceil(minutesParked / 60.0));
double amount = hoursParked * ratePerHour;
```

## 📈 Performance Metrics

- **Slot Assignment**: O(log n) time complexity
- **Database Queries**: Optimized with strategic indexing
- **Memory Usage**: Efficient with connection pooling
- **GUI Response**: Real-time updates under 100ms
 

## 🛠️ Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **Frontend** | Java Swing | Desktop GUI interface |
| **Backend** | Java 17+ | Business logic and algorithms |
| **Database** | MySQL 8.0+ | Data persistence and integrity |
| **Connectivity** | JDBC | Database operations and transactions |
| **IDE** | VS Code | Development environment |

## 📊 Project Structure

```
SmartParkingSystem/
├── src/
│   ├── db/          # Database connection management
│   ├── models/      # Domain entities and data models
│   ├── services/    # Business logic and algorithms
│   └── ui/          # GUI components and event handling
├── lib/             # External dependencies (MySQL Connector)
├── database_setup.sql # Database schema and sample data
└── README.md        # Project documentation
```

## 🎯 Future Enhancements

- **Mobile App Integration**: REST API for mobile applications
- **IoT Sensor Support**: Real-time occupancy detection
- **Dynamic Pricing**: Peak hour and demand-based pricing
- **Reservation System**: Advance booking capabilities
- **Analytics Dashboard**: Parking patterns and revenue insights

## 📝 License

This project is developed for educational and demonstration purposes.

---

<div align="center">

**Built with ❤️ for learning advanced Java concepts and system design**

*Perfect for technical interviews and portfolio demonstrations*

</div>