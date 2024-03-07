package sea.Erweiterungen.GUI;

import sea.Basis.Direction;
import sea.Basis.Ground;
import sea.Basis.RadarField;
import sea.Basis.RadarScreen;
import sea.CompanyApp.Cargo;
import sea.ShipApp.SeaTradeReceiver;
import sea.ShipApp.ShipApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

public class ShipAppGUI extends ShipApp {

  private JFrame frame;
  private final ShipApp shipApp;
  private SeaTradeReceiver seaTradeReceiver;

  private static String helpCommand;
  private JPanel panel;
  private Direction direction = Direction.WEST;
  private JTextArea statusLabel;
  private JTextField shipNameField, companyNameField;
  private JComboBox<String> harbourDropdown, destinationDropdown;
  private final String[] harbours;
  private int level = 1;

  public ShipAppGUI() {
    this.shipApp = new ShipApp();
    this.harbours = new String[]{"Algier", "Brest", "Carracas", "Cotonau", "Dakar", "Halifax",
        "Lissabon",
        "New York", "Plymouth", "Reykjavik"};
    initializeGUI();
    startSettings();
  }

  private void initializeGUI() {
    frame = new JFrame("SeaTrade Ship App");
    frame.setSize(400, 500);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    panel = new JPanel();
    frame.getContentPane().add(panel);
    placeComponents();
    startSettings();

    frame.setVisible(true);
  }

  public void placeComponents() {
    panel.setLayout(null);

    addLabel(panel, "Name:", 10, 20, 80, 25);
    shipNameField = addTextField(panel, 100, 20, 160, 25);

    addLabel(panel, "Company:", 10, 60, 80, 25);
    companyNameField = addTextField(panel, 100, 60, 160, 25);

    addLabel(panel, "Harbour:", 10, 100, 80, 25);
    harbourDropdown = addComboBox(panel, harbours, 100, 100, 160, 25);

    addLabel(panel, "Destination:", 10, 140, 80, 25);
    destinationDropdown = addComboBox(panel, harbours, 100, 140, 160, 25);

    // Radar und Status
    addLabel(panel, "Radar:", 10, 300, 80, 25);
    statusLabel = addTextArea(panel, "", 220, 320, 400, 130);

    // Aktionen
    addButton(panel, "Launch", 280, 20, 80, 105, e -> launchShipAction());
    addButton(panel, "Move", 280, 140, 80, 25, e -> moveShipAction());

    // Richtungs- und Ladungssteuerung
    addButton(panel, "Up", 140, 180, 80, 25, e -> manualMoving(Direction.NORTH));
    addButton(panel, "Down", 140, 260, 80, 25, e -> manualMoving(Direction.SOUTH));
    addButton(panel, "Left", 90, 220, 80, 25, e -> manualMoving(Direction.WEST));
    addButton(panel, "Right", 190, 220, 80, 25, e -> manualMoving(Direction.EAST));
    addButton(panel, "Load Cargo", 10, 180, 100, 25, e -> loadCargoAction());
    addButton(panel, "Unload Cargo", 250, 180, 110, 25, e -> unloadCargoAction());

    frame.add(panel);
    frame.repaint();
    frame.setVisible(true);
  }

  private void launchShipAction() {
    String shipName = shipNameField.getText().trim();
    String companyName = companyNameField.getText().trim();
    String selectedHarbour = harbourDropdown.getSelectedItem().toString().toLowerCase();
    addShipToDB(shipName,companyName,selectedHarbour);

    if (!shipName.isEmpty() && !companyName.isEmpty()) {
      startServer();
      if (seaTradeReceiver.isAlive()) {
        shipApp.launch(companyName, selectedHarbour, shipName);
        launchSettings();
        checkLevel();
        radarRequest();

      } else {
        startSettings();
        showConnectionInformations();
      }
    }
  }

  private void addShipToDB(String shipName, String companyName, String selectedHarbour) {
    Connection connection = null;

    try {
      connection = connectToDatabase();

      // Get HarbourID and CompanyID from the database based on the provided names
      int harbourID = getHarbourID(connection, selectedHarbour);
      int companyID = getCompanyID(connection, companyName);

      String sql = "INSERT INTO ship (Name, PosX, PosY, Direction, HarbourID, CompanyID) VALUES (?, ?, ?, ?, ?, ?)";

      try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        preparedStatement.setString(1, shipName);
        preparedStatement.setInt(2, 0); // Set initial PosX to 0
        preparedStatement.setInt(3, 0); // Set initial PosY to 0
        preparedStatement.setString(4, "NONE"); // Set initial Direction to NONE
        preparedStatement.setInt(5, harbourID);
        preparedStatement.setInt(6, companyID);

        int affectedRows = preparedStatement.executeUpdate();

        if (affectedRows > 0) {
          System.out.println("Ship added successfully");
        } else {
          System.out.println("Error adding ship");
        }
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(null, "Error adding ship: " + e.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
    } finally {
      closeDatabaseConnection(connection);
    }
  }

  private int getHarbourID(Connection connection, String harbourName) throws SQLException {
    String sql = "SELECT HarbourID FROM harbour WHERE Name = ?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, harbourName);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getInt("HarbourID");
        }
        return -1; // Return -1 if the harbour is not found
      }
    }
  }

  private int getCompanyID(Connection connection, String companyName) throws SQLException {
    String sql = "SELECT CompanyID FROM company WHERE Name = ?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setString(1, companyName);
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getInt("CompanyID");
        }
        return -1; // Return -1 if the company is not found
      }
    }
  }

  private void moveShipAction() {
    String selectedHarbour = destinationDropdown.getSelectedItem().toString().toLowerCase();
    shipApp.moveTo(selectedHarbour);
  }

  private void loadCargoAction() {
    try {
      shipApp.receiveOrder();
      shipApp.loadCargo();

      Thread.sleep(1000);
      updateStatusLabel();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  private void unloadCargoAction() {
    try {
      Thread.sleep(500);
      shipApp.endOrder();
      shipApp.sendProfit(seaTradeReceiver.getLoadedCargo().getValue());
      shipApp.unloadCargo();
      setLoaded(false);

      StringBuilder text = new StringBuilder();

          text.append("No Cargo,\nwork harder!");


      statusLabel.setText(text.toString());
      panel.repaint();
      panel.revalidate();


    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  private JLabel addLabel(JPanel panel, String text, int x, int y, int width, int height) {
    JLabel label = new JLabel(text);
    label.setBounds(x, y, width, height);
    panel.add(label);
    return label;
  }

  private JTextField addTextField(JPanel panel, int x, int y, int width, int height) {
    JTextField textField = new JTextField();
    textField.setBounds(x, y, width, height);
    panel.add(textField);
    return textField;
  }

  private JComboBox<String> addComboBox(JPanel panel, String[] options, int x, int y, int width,
      int height) {
    JComboBox<String> comboBox = new JComboBox<>(options);
    comboBox.setBounds(x, y, width, height);
    panel.add(comboBox);
    return comboBox;
  }

  private JButton addButton(JPanel panel, String text, int x, int y, int width, int height,
      ActionListener actionListener) {
    JButton button = new JButton(text);
    button.setBounds(x, y, width, height);
    button.addActionListener(actionListener);
    panel.add(button);
    return button;
  }

  private JTextArea addTextArea(JPanel panel, String text, int x, int y, int width, int height) {
    JTextArea textArea = new JTextArea();
    textArea.setText(text);
    textArea.setEditable(false);
    textArea.setOpaque(false);
    textArea.setBorder(null);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setBounds(x, y, width, height);
    panel.add(textArea);
    return textArea;
  }

  public void startSettings() {
    allEnabled(false);
    buttonsEnabled(true, "Launch");
    shipNameField.setEditable(true);
    companyNameField.setEditable(true);
    harbourDropdown.setEnabled(true);
    panel.repaint();
    panel.revalidate();
  }

  private void launchSettings() {
    allEnabled(true);
    buttonsEnabled(false, "Launch");
    harbourDropdown.setEnabled(false);
    shipNameField.setEditable(false);
    companyNameField.setEditable(false);
    panel.repaint();
    panel.revalidate();
  }

  public void allEnabled(boolean result) {
    Component[] components = panel.getComponents();
    for (Component component : components) {
      if (component instanceof JButton) {
        component.setEnabled(result);
      }
    }
    shipNameField.setEditable(result);
    companyNameField.setEditable(result);
    destinationDropdown.setEnabled(result);
    harbourDropdown.setEnabled(result);
  }

  public void buttonsEnabled(boolean result, String button) {
    String[] buttons = new String[]{button}; // Standardwert
    if (button.contains(",")) {
      buttons = button.split(",");
    }
    Component[] components = panel.getComponents();
    for (Component component : components) {
      if (component instanceof JButton currentButton) {
        for (String btn : buttons) {
          if (currentButton.getText().equals(btn.trim())) {
            currentButton.setEnabled(result);
            break;
          }
        }
      }
    }
  }

  private void updateStatusLabel() throws InterruptedException {
    StringBuilder text = new StringBuilder();
    Cargo loadedCargo = seaTradeReceiver.getLoadedCargo();
    Thread.sleep(2000);
    if (shipApp.isLoaded()) {
      if (loadedCargo != null) {
        text.append("CARGO INFORMATION").append("\nID: " + loadedCargo.getId())
            .append("\nValue: "
                + NumberFormat.getCurrencyInstance(Locale.US).format(loadedCargo.getValue()))
            .append("\nFrom: " + loadedCargo.getSource())
            .append("\nTo: " + loadedCargo.getDestination());
      } else {
        text.append("No Cargo,\nwork harder!");
      }
    }
    statusLabel.setText(text.toString());
    panel.repaint();
    panel.revalidate();
  }

  private String readSeaTradeCommand() {
    String value = SeaTradeReceiver.helpCommand;
    SeaTradeReceiver.helpCommand = "";
    return value;
  }

  private void startServer() {
    shipApp.setSeaTradeReceiver(new SeaTradeReceiver(this));
    seaTradeReceiver = shipApp.getSeaTradeReceiver();

    try {
      seaTradeReceiver.start();
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  private void radarRequest() {
    JPanel gridPanel = createGridPanel();
    gridPanel.setBounds(10, 320, 200, 130);

// Entferne das bestehende GridPanel, falls vorhanden
    for (Component comp : panel.getComponents()) {
      if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("gridPanel")) {
        panel.remove(comp);
        break;
      }
    }
    panel.add(gridPanel);

// GridPanel in den Vordergrund setzen
    panel.setComponentZOrder(gridPanel, 0);

    panel.revalidate();
    panel.repaint();
  }

  private void checkLevel() {
    radarRequest();
    String command = readSeaTradeCommand();
    if (command.contains("error")) {
      try {
        startSettings();
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
      if (command.contains("level")) {
        allEnabled(true);
        buttonsEnabled(false, "Up,Down,Left,Right");
        level = 0;
        showConnectionInformations();
      }
    } else {
      showConnectionInformations(); // Begrüßung beim Start ohne Fehler
    }
  }

  private void showConnectionInformations() {

    String head, text, status = null, listenzeichen = "", levelInformation;
    if (level == 0) {
      listenzeichen = "- ";
      status = "Deactivated Features:";
    } else if (level == 1) {
      listenzeichen = "+ ";
      status = "Activated Features:";
    }
    levelInformation =
            "\r\n" + listenzeichen + "Move Manual\r\n" + listenzeichen + "RadarRequest\r\n"
                    + listenzeichen + "Load with ID\r\n";
    status = "\r\n\r\n" + status + levelInformation;

    if (seaTradeReceiver.isAlive()) {
      head = "Launch successfull!";
      text =
              "Enjoy your ride on the SeaTrade-Server!" + "\r\n\r\nYour Level is: Level " + level;
      JOptionPane.showMessageDialog(null, text + status, head, JOptionPane.INFORMATION_MESSAGE);
    } else if (!seaTradeReceiver.isAlive()) {
      head = "Connection failed.";
      text = "Try to reconnect?";

      try {
        Thread.sleep(2000);
        int result = JOptionPane.showConfirmDialog(null, text, head,
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
          head = "Connection successfull!";
          text = "Launch in progress.";
          launchShipAction();
          JOptionPane.showMessageDialog(null, text, head, JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void manualMoving(Direction direction) {
    try {
      this.direction = direction;
      shipApp.moveManualTo(direction);
      Thread.sleep(100);
      radarRequest();
      // Load nur im Hafen möglich, sowie Auto-Move
      buttonsEnabled(getCenterGround() == Ground.HAFEN, "Load Cargo,Move");
      if (!shipApp.getSeaTradeReceiver().isAlive()) {
        allEnabled(false);
        harbourDropdown.setEnabled(false);
        String head = "Sunken!";
        String text = "The Game is over!";
        JOptionPane.showMessageDialog(null, text, head, JOptionPane.INFORMATION_MESSAGE);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private JPanel createGridPanel() {
    shipApp.getRadarRequest();

    String arrow = switch (direction) {
      case NORTH -> "↑";
      case WEST -> "←";
      case SOUTH -> "↓";
      case EAST -> "→";
      default -> "";
    };

    String[] guiDirections = {"NW", "N", "NE", "W", arrow, "E", "SW", "S", "SE"};
    String[] serverDirections = {"W", "NW", "N", "NE", "E", "SE", "S", "SW"};

    JPanel gridPanel = new JPanel(new GridLayout(3, 3));

// Falls Radar-Feedback vorhanden ist
    String command = SeaTradeReceiver.helpCommand;
    if (RadarScreen.parse(command) != null) {
      RadarField[] radarFields = RadarScreen.parse(command).getMeasures();

      for (String direction : guiDirections) {
        JLabel label = new JLabel(direction, SwingConstants.CENTER);
        label.setOpaque(true);
        gridPanel.add(label);
      }

      for (int i = 0; i < serverDirections.length; i++) {
        String direction = serverDirections[i];
        Ground ground = radarFields[i].getGround();
        boolean isHasShip = radarFields[i].isHasShip();

        JLabel label = (JLabel) gridPanel.getComponent(
            Arrays.asList(guiDirections).indexOf(direction));
        label.setForeground(Color.WHITE);
        if (isHasShip) {
          label.setBackground(Color.CYAN);
        } else {
          label.setBackground(getColorForGround(ground));
        }
      }
// Pfeil hervorsetzen
      JLabel arrowLabel = (JLabel) gridPanel.getComponent(
          Arrays.asList(guiDirections).indexOf(arrow));
      arrowLabel.setBackground(Color.PINK);
      arrowLabel.setFont(new Font("Arial", Font.BOLD, 24));
    }
    return gridPanel;
  }

  private Color getColorForGround(Ground ground) {
    return switch (ground) {
      case WASSER -> Color.BLUE;
      case LAND -> Color.GREEN;
      case FELS -> Color.GRAY;
      case HAFEN -> Color.RED;
      case EIS -> Color.WHITE;
      case NICHTS -> Color.BLACK;
    };
  }

  private Connection connectToDatabase() throws SQLException {
// Load the MySQL JDBC driver
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e1) {
      e1.printStackTrace();
    }

    String jdbcUrl = "jdbc:mysql://localhost:3305/seatradedb";
    String username = "root";
    String password = "";

    return DriverManager.getConnection(jdbcUrl, username, password);
  }

  private void closeDatabaseConnection(Connection connection) {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      System.out.println("Error closing database connection: " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    new ShipAppGUI();
  }
}
