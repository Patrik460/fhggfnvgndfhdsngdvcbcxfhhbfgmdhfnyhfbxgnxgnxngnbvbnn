package sea.Erweiterungen.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import sea.Basis.Direction;
import sea.Basis.Ground;
import sea.Basis.RadarField;
import sea.Basis.RadarScreen;
import sea.CompanyApp.Cargo;
import sea.ShipApp.SeaTradeReceiver;
import sea.ShipApp.ShipApp;

public class ShipAppGUI extends ShipApp {

  private JFrame frame;
  private final ShipApp shipApp;
  private SeaTradeReceiver seaTradeReceiver;
  private JPanel panel;
  private Direction direction = Direction.WEST;
  private JLabel statusLabel;
  private JTextField shipNameField, companyNameField;
  private JComboBox<String> harbourDropdown, destinationDropdown;
  private final String[] harbours;
  private int level = 1;

  public ShipAppGUI() {
    this.shipApp = new ShipApp();
    this.harbours = new String[]{"Algier", "Brest", "Carracas", "Cotonau", "Dakar", "Halifax",
        "Lissabon", "New York", "Plymouth", "Reykjavik"};
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

    // Grundlegende Komponenten hinzufügen
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
    statusLabel = addLabel(panel, "Status", 220, 300, 140, 65);

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

    if (!shipName.isEmpty() && !companyName.isEmpty()) {
      startServer();
      if (seaTradeReceiver.isAlive()) {
        shipApp.launch(companyName, selectedHarbour, shipName);

        allEnabled(true);
        buttonsEnabled(false, "Launch");
        harbourDropdown.setEnabled(false);
        checkLevel();
        radarRequest();

      } else {
        startSettings();
        showConnectionInformations();
      }
    }
  }


  private void moveShipAction() {
    String selectedHarbour = destinationDropdown.getSelectedItem().toString().toLowerCase();
    shipApp.moveTo(selectedHarbour);
  }

  private void loadCargoAction() {
    shipApp.loadCargo();
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
    updateStatusLabel();
  }

  private void unloadCargoAction() {
    shipApp.unloadCargo();
    updateStatusLabel();
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

  public void startSettings() {
    allEnabled(false);
    buttonsEnabled(true, "Launch");
    shipNameField.setEditable(true);
    companyNameField.setEditable(true);
    harbourDropdown.setEnabled(true);
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

  private void updateStatusLabel() {
    if (shipApp.isLoaded()) {
      Cargo loadedCargo = seaTradeReceiver.getLoadedCargo();
      statusLabel.setText(
          loadedCargo.getValue() + " is waiting \r\nin " + loadedCargo.getDestination());
    } else {
      statusLabel.setText("No Cargo, \r\nwork harder!");
    }
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
      status = "Nachfolgendes wurde deaktiviert:";
    } else if (level == 1) {
      listenzeichen = "+ ";
      status = "Nachfolgendes wurde aktiviert:";
    }
    levelInformation = "\r\n" +
        listenzeichen + "Move Manual\r\n" +
        listenzeichen + "RadarRequest\r\n" +
        listenzeichen + "Load with ID\r\n";
    status = "\r\n\r\n" + status + levelInformation;

    if (seaTradeReceiver.isAlive()) {
      head = "Erfolgreicher Launch";
      text = "Viel Spaß auf dem SeaTrade-Server und gute Fahrt!" +
          "\r\n\r\nIhr Level beträgt: Level " + level;
      JOptionPane.showMessageDialog(null, text + status, head, JOptionPane.INFORMATION_MESSAGE);
    } else if (!seaTradeReceiver.isAlive()) {
      head = "Verbindungsfehler";
      text = "Soll eine erneute Verbindung zum SeaTrade-Server aufgebaut werden?";

      try {
        Thread.sleep(2000);
        int result = JOptionPane.showConfirmDialog(null, text, head,
            JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
          head = "Verbindung erfolgreich";
          text = "Der Launch wird erneut probiert.";
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
      Thread.sleep(200);
      radarRequest();
      // Load nur im Hafen möglich, sowie Auto-Move
      buttonsEnabled(getCenterGround() == Ground.HAFEN, "Load Cargo,Move");
      if (!shipApp.getSeaTradeReceiver().isAlive()) {
        allEnabled(false);
        harbourDropdown.setEnabled(false);
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

  public static void main(String[] args) {
    new ShipAppGUI();
  }
}
