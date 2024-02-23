package sea.Erweiterungen.GUI;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import sea.CompanyApp.CompanyApp;
import sea.CompanyApp.Receiver;

public class CompanyAppGUI {
	private JFrame frame;
	private CompanyApp companyApp;
	private Receiver receiver;
	private JPanel panel;
	private JTextField companyNameField;
	private JLabel updatedBalanceLabel, newBalanceLabel;
	private JComboBox<String> cargoDropdown, shipDropdown;

	public CompanyAppGUI() {
		this.companyApp = new CompanyApp();
		companyApp.setReceiver(new Receiver());
		setReceiver(companyApp.getReceiver());

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
		newBalanceLabel = addLabel(panel, "", 80, 60, 160, 25);
		addButton(panel, "Add Ship", 250, 60, 160, 25, e -> {
			ShipAppGUI ship = new ShipAppGUI();
			// TODO: Implement ship list action
		});
		addButton(panel, "Harbours", 80, 100, 160, 25, e -> showHarbourInfo());
		addButton(panel, "Cargos", 250, 100, 160, 25, e -> showCargoInfo());

		String[] shipOptions = { "Ship1", "Ship2", "Ship3" };
		shipDropdown = addComboBox(panel, shipOptions, 80, 180, 160, 25);

		// Button für Order und Label für Cargo
		addButton(panel, "Order", 250, 180, 160, 65, e -> orderShip());
		addLabel(panel, "Cargo:", 10, 140, 80, 25);

		// Exit Button
		addButton(panel, "Exit", 250, 260, 160, 25, e -> exitApplication());
	}

	// Helfermethoden
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

	private JComboBox<String> addComboBox(JPanel panel, String[] options, int x, int y, int width, int height) {
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
				((JButton) component).setEnabled(result);
			}
		}
	}

	public void ButtonsEnabled(boolean result, String button) {
		String[] buttons = new String[] { button }; // Standardwert
		if (button.contains(",")) {
			buttons = button.split(",");
		}
		Component[] components = panel.getComponents();
		for (Component component : components) {
			if (component instanceof JButton) {
				JButton currentButton = (JButton) component;
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
		if (!CompanyName.isEmpty() && companyApp.getReceiver() != null) {
			if (!companyApp.getReceiver().isAlive()) {
				companyApp.getReceiver().start();
				try {
					Thread.sleep(1000);
					companyApp.getReceiver().out.println("register:" + companyNameField.getText());

					AllButtonEnabled(true);
					ButtonsEnabled(false, "Register");
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void updateBalance(String newBalance) {
		updatedBalanceLabel.setText(newBalance);
	}

	private void showHarbourInfo() {
		companyApp.getHarbourInfo();

	}

	private void showCargoInfo() {
		// Die Methode getCargoInfo() muss die Cargos aktualisieren
		companyApp.getCargoInfo();

		String[] aa = receiver.getReceivedCargos();

		if (cargoDropdown != null) {
			panel.remove(cargoDropdown); // Entferne die vorhandene JComboBox
		}
		cargoDropdown = addComboBox(panel, aa, 80, 140, 330, 25); // Erstelle die JComboBox neu
		panel.revalidate(); // Aktualisiere das Panel, um die Änderungen anzuzeigen
		panel.repaint();
	}

	private void orderShip() {
		companyApp.distributeOrder();
	}

	private void sendCommand(String command) {
		// TODO
	}

	private void exitApplication() {
		frame.dispose(); // Schließe das Fenster
		System.exit(0); // Beende die Anwendung
	}

	public Receiver getReceiver() {
		return receiver;
	}

	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

}
