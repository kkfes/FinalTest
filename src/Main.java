import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

// Database manager class
class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/urban_registry";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

// City class
class City {
    private String name;
    private ArrayList<Building> buildings = new ArrayList<>();

    public City(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void addBuilding(Building building) { buildings.add(building); }
    public void removeBuilding(Building building) { buildings.remove(building); }
    public ArrayList<Building> getBuildings() { return buildings; }

    public Building findBuilding(String streetName, int houseNumber) {
        for (Building building : buildings) {
            if (building.getStreetName().equals(streetName) && building.getHouseNumber() == houseNumber) {
                return building;
            }
        }
        return null;
    }
}

// Building class
class Building {
    private String streetName;
    private int houseNumber;
    private double basePayment;
    private ArrayList<Room> rooms = new ArrayList<>();

    public Building(String streetName, int houseNumber, double basePayment) {
        this.streetName = streetName;
        this.houseNumber = houseNumber;
        this.basePayment = basePayment;
    }

    public String getStreetName() { return streetName; }
    public int getHouseNumber() { return houseNumber; }
    public double getBasePayment() { return basePayment; }
    public ArrayList<Room> getRooms() { return rooms; }
    public void addRoom(Room room) { rooms.add(room); }
    public void removeRoom(Room room) { rooms.remove(room); }
}

// Room class
class Room {
    private int number;
    private double area;

    public Room(int number, double area) {
        this.number = number;
        this.area = area;
    }

    public int getNumber() { return number; }
    public double getArea() { return area; }


}
