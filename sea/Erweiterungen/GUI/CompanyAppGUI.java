package sea.Erweiterungen.GUI;

import sea.CompanyApp.CompanyApp;
import sea.CompanyApp.Receiver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;

public class CompanyAppGUI {

	private JFrame frame;
	private final CompanyApp companyApp;
	private JPanel panel;
	private JTextField companyNameField;
	private JLabel updatedBalanceLabel, newBalanceLabel;
	private JComboBox<String> cargoDropdown, shipDropdown;
	private Timer balanceUpdateTimer;

	public CompanyAppGUI() {
		this.companyApp = new CompanyApp();
		initializeGUI();
	}

	public static void main(String[] args) {
		new CompanyAppGUI();
	}

	private void initializeGUI() {
		frame = new JFrame("SeaTrade Company App");
		frame.setSize(450, 330);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		panel = new JPanel();

		frame.getContentPane().add(panel); // ??

		placeComponents();

		AllButtonEnabled(false);
		ButtonsEnabled(true, "Register");

		frame.setVisible(true);
	}

	private void placeComponents() {
		panel.setLayout(null);

// Labels und Textfeld für Name und Register-Button
		addLabel(panel, "Name:", 10, 20, 80, 25);
		companyNameField = addTextField(panel, 80, 20, 160, 25);
		addButton(panel, "Register", 250, 20, 160, 25, e -> registerCompany());

// Labels und Buttons für Balance, Ship list, Harbours, Cargos
		addLabel(panel, "Balance:", 10, 60, 80, 25);
		newBalanceLabel = addLabel(panel, companyApp.getBalance() + "", 80, 60, 160, 25);
		addButton(panel, "Add Ship", 250, 60, 160, 25, e -> {
			new ShipAppGUI();
		});
		addButton(panel, "Harbours", 80, 100, 160, 25, e -> showHarbourInfo());
		addButton(panel, "Cargos", 250, 100, 160, 25, e -> showCargoInfo());

		//String[] shipOptions = {"Ship1", "Ship2", "Ship3"};
		//shipDropdown = addComboBox(panel, shipOptions, 80, 180, 160, 25);

// Button für Order und Label für Cargo
		//addButton(panel, "Order", 250, 180, 160, 65, e -> orderShip());
		addLabel(panel, "Cargo:", 10, 140, 80, 25);

// Exit Button
		addButton(panel, "Exit", 250, 260, 160, 25, e -> exitApplication());

// Clear DB Button
		addButton(panel, "Clear DB", 80, 260, 160, 25, e -> clearDatabase());

// Timer für die Companybalance
		balanceUpdateTimer = new Timer(500, e -> updateBalanceLabel());
		balanceUpdateTimer.start();
	}

	private void updateBalanceLabel() {
		newBalanceLabel.setText(companyApp.getBalance() + "");
	}

	private void updateBalance(String newBalance) {
		updatedBalanceLabel.setText(newBalance);
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

	public void AllButtonEnabled(boolean result) {
		Component[] components = panel.getComponents();
		for (Component component : components) {
			if (component instanceof JButton) {
				component.setEnabled(result);
			}
		}
	}

	public void ButtonsEnabled(boolean result, String button) {
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

	private void registerCompany() {
		String CompanyName = companyNameField.getText().trim();

		if (!CompanyName.isEmpty()){
				startServer();
				companyApp.register(companyNameField.getText());
				addCompany(companyNameField.getText());
				companyApp.setCompName(companyNameField.getText().trim());
				AllButtonEnabled(true);
				ButtonsEnabled(false, "Register");
		}
	}
	private void startServer() {
		companyApp.setReceiver(new Receiver());

		try {
			companyApp.getReceiver().start();
			companyApp.getReceiver().setCompanyApp(this.companyApp);
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
		      ex.printStackTrace();
		}
	}

	private void addCompany(String companyName) {
		Connection connection = null;
		try {
			connection = connectToDatabase();
			String sql = "INSERT INTO company (Name, Balance) VALUES (?, 5000000)";

			try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				preparedStatement.setString(1, companyName);

				preparedStatement.executeUpdate();
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Error adding company: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		} finally {
			closeDatabaseConnection(connection);
		}
	}

	private void showHarbourInfo() {
		companyApp.getHarbourInfo();
	}

	private void showCargoInfo() {
// Die Methode getCargoInfo() muss die Cargos aktualisieren
		companyApp.getCargoInfo();

		String[] rc = companyApp.getReceiver().getReceivedCargos();
		addCargosToDB(rc);

		if (cargoDropdown != null) {
			panel.remove(cargoDropdown); // Entferne die vorhandene JComboBox
		}
		cargoDropdown = addComboBox(panel, rc, 80, 140, 330, 25); // Erstelle die JComboBox neu
		panel.revalidate(); // Aktualisiere das Panel, um die Änderungen anzuzeigen
		panel.repaint();
	}

	private void addCargosToDB(String[] cargos) {
		Connection connection = null;

		try {
			connection = connectToDatabase();

			for (String cargoInfo : cargos) {
				String[] cargoValues = cargoInfo.split("\\|");

				if (cargoValues.length == 5) {
					int cargoID = Integer.parseInt(cargoValues[1]);
					int value = Integer.parseInt(cargoValues[4]);
					String harbourStart = cargoValues[2];
					String harbourDest = cargoValues[3];

					// Get HarbourIDStart and HarbourIDDest from the database based on the provided names
					int harbourIDStart = getHarbourID(connection, harbourStart);
					int harbourIDDest = getHarbourID(connection, harbourDest);

					String sql = "INSERT INTO cargo (CargoID, Value, HarbourIDStart, HarbourIDDest, Status) VALUES (?, ?, ?, ?, 0)";

					try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
						preparedStatement.setInt(1, cargoID);
						preparedStatement.setInt(2, value);
						preparedStatement.setInt(3, harbourIDStart);
						preparedStatement.setInt(4, harbourIDDest);

						int affectedRows = preparedStatement.executeUpdate();

						if (affectedRows > 0) {
							System.out.println("Cargo added successfully");
						} else {
							System.out.println("Error adding cargo");
						}
					}
				}
			}
		} catch (SQLException | NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Error adding cargos: " + e.getMessage(), "Error",
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

	private void orderShip() {
		updateBalanceLabel();
		companyApp.distributeOrder();
	}

	private Connection connectToDatabase() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		String jdbcUrl = "jdbc:mysql://localhost:3306/seatradedb";
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
			JOptionPane.showMessageDialog(null, "Error closing database connection: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void clearDatabase() {
		Connection connection = null;
		try {
			connection = connectToDatabase();
			clearTable(connection, "company");
			clearTable(connection, "ship");
			clearTable(connection, "cargo");

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Error clearing the database: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		} finally {
			closeDatabaseConnection(connection);
		}
	}

	private void clearTable(Connection connection, String tableName) throws SQLException {
		String sql = "DELETE FROM " + tableName;
		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate(sql);
		}
	}

	private void exitApplication() {
		balanceUpdateTimer.stop();
		frame.dispose(); // Schließe das Fenster
		System.exit(0); // Beende die Anwendung
	}
}
