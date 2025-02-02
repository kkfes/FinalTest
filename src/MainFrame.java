import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MainFrame extends JFrame {
    private DefaultListModel<String> cityListModel = new DefaultListModel<>();
    private JList<String> cityList = new JList<>(cityListModel);
    private DefaultListModel<String> buildingListModel = new DefaultListModel<>();
    private JList<String> buildingList = new JList<>(buildingListModel);
    private DefaultListModel<String> roomListModel = new DefaultListModel<>();
    private JList<String> roomList = new JList<>(roomListModel);
    private JTextField searchField = new JTextField();
    private JButton searchButton = new JButton("Search");

    public MainFrame() {
        setTitle("Urban Housing Registry");
        setSize(1000, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 3));

        JPanel cityPanel = new JPanel(new BorderLayout());
        JPanel buildingPanel = new JPanel(new BorderLayout());
        JPanel roomPanel = new JPanel(new BorderLayout());

        cityPanel.add(new JLabel("Cities"), BorderLayout.NORTH);
        cityPanel.add(new JScrollPane(cityList), BorderLayout.CENTER);
        buildingPanel.add(new JLabel("Buildings"), BorderLayout.NORTH);
        buildingPanel.add(new JScrollPane(buildingList), BorderLayout.CENTER);
        roomPanel.add(new JLabel("Rooms"), BorderLayout.NORTH);
        roomPanel.add(new JScrollPane(roomList), BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        buildingPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel buildingButtonsPanel = new JPanel(new GridLayout(2, 1));
        JButton addBuildingButton = new JButton("Add Building");
        JButton removeBuildingButton = new JButton("Remove Building");
        buildingButtonsPanel.add(addBuildingButton);
        buildingButtonsPanel.add(removeBuildingButton);
        buildingPanel.add(buildingButtonsPanel, BorderLayout.SOUTH);

        JPanel roomButtonsPanel = new JPanel(new GridLayout(2, 1));
        JButton addRoomButton = new JButton("Add Room");
        JButton removeRoomButton = new JButton("Remove Room");
        roomButtonsPanel.add(addRoomButton);
        roomButtonsPanel.add(removeRoomButton);
        roomPanel.add(roomButtonsPanel, BorderLayout.SOUTH);

        addRoomButton.addActionListener(e -> addRoom());
        removeRoomButton.addActionListener(e -> removeRoom());

        add(cityPanel);
        add(buildingPanel);
        add(roomPanel);

        cityList.addListSelectionListener(e -> loadBuildings());
        buildingList.addListSelectionListener(e -> loadRooms());

        addBuildingButton.addActionListener(e -> addBuilding());
        removeBuildingButton.addActionListener(e -> removeBuilding());
        searchButton.addActionListener(e -> searchBuildings());

        loadCities();
    }

    private void loadCities() {
        cityListModel.clear();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM cities")) {
            while (rs.next()) {
                cityListModel.addElement("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBuildings() {
        buildingListModel.clear();
        String selectedCity = cityList.getSelectedValue();
        if (selectedCity != null) {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM buildings WHERE city_id = ?")) {
                stmt.setString(1, selectedCity.split(",")[0].split(":")[1].trim());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    buildingListModel.addElement("ID: " + rs.getInt("id") + ", Street: " + rs.getString("street_name") + ", House: " + rs.getInt("house_number") + ", Payment: " + rs.getDouble("base_payment"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadRooms() {
        roomListModel.clear();
        String selectedBuilding = buildingList.getSelectedValue();
        if (selectedBuilding != null) {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM rooms WHERE building_id = ?")) {
                stmt.setString(1, selectedBuilding.split(",")[0].split(":")[1].trim());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    roomListModel.addElement("ID: " + rs.getInt("id") + ", Number: " + rs.getInt("number") + ", Area: " + rs.getDouble("area"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addBuilding() {
        String selectedCity = cityList.getSelectedValue();
        if (selectedCity != null) {
            String cityId = selectedCity.split(",")[0].split(":")[1].trim();
            String streetName = JOptionPane.showInputDialog(this, "Enter Street Name:");
            int houseNumber = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter House Number:"));
            double basePayment = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter Base Payment:"));

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO buildings (city_id, street_name, house_number, base_payment) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, cityId);
                stmt.setString(2, streetName);
                stmt.setInt(3, houseNumber);
                stmt.setDouble(4, basePayment);
                stmt.executeUpdate();
                loadBuildings();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeBuilding() {
        String selectedBuilding = buildingList.getSelectedValue();
        if (selectedBuilding != null) {
            String buildingId = selectedBuilding.split(",")[0].split(":")[1].trim();

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM buildings WHERE id = ?")) {
                stmt.setString(1, buildingId);
                stmt.executeUpdate();
                loadBuildings();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void searchBuildings() {
        buildingListModel.clear();
        String selectedCity = cityList.getSelectedValue();
        if (selectedCity != null) {
            String cityId = selectedCity.split(",")[0].split(":")[1].trim();
            String searchText = searchField.getText().trim();

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM buildings WHERE city_id = ? AND (street_name LIKE ? OR house_number LIKE ?)")) {
                stmt.setString(1, cityId);
                stmt.setString(2, "%" + searchText + "%");
                stmt.setString(3, "%" + searchText + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    buildingListModel.addElement("ID: " + rs.getInt("id") + ", Street: " + rs.getString("street_name") + ", House: " + rs.getInt("house_number") + ", Payment: " + rs.getDouble("base_payment"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private void addRoom() {
        String selectedBuilding = buildingList.getSelectedValue();
        if (selectedBuilding != null) {
            String buildingId = selectedBuilding.split(",")[0].split(":")[1].trim();
            int roomNumber = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Room Number:"));
            double area = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter Room Area:"));

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO rooms (building_id, number, area) VALUES (?, ?, ?)")) {
                stmt.setString(1, buildingId);
                stmt.setInt(2, roomNumber);
                stmt.setDouble(3, area);
                stmt.executeUpdate();
                loadRooms();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeRoom() {
        String selectedRoom = roomList.getSelectedValue();
        if (selectedRoom != null) {
            String roomId = selectedRoom.split(",")[0].split(":")[1].trim();

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM rooms WHERE id = ?")) {
                stmt.setString(1, roomId);
                stmt.executeUpdate();
                loadRooms();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}