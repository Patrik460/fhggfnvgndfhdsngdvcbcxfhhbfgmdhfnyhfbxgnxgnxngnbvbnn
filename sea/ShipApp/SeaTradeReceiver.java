package sea.ShipApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import sea.Basis.Ground;
import sea.Basis.RadarField;
import sea.CompanyApp.Cargo;

public class SeaTradeReceiver extends Thread {

	private Socket shipSocket = null;
	private BufferedReader in = null;
	public PrintWriter out = null;
	public static String radarCommand = "";
	public RadarField radarfield = new RadarField(Ground.HAFEN);
	private ShipApp shipApp;
	private Cargo loadedCargo = null;

	public SeaTradeReceiver(ShipApp shipApp) {
		this.shipApp = shipApp;
	}

	public void setShipApp(ShipApp shipApp) {
		this.shipApp = shipApp;
	}

	public Cargo getLoadedCargo() {
		return loadedCargo;
	}

	@Override
	public void run() {
		try {
			shipSocket = new Socket(ShipApp.getHost(), ShipApp.getPort());
			in = new BufferedReader(new InputStreamReader(shipSocket.getInputStream()));
			out = new PrintWriter(shipSocket.getOutputStream(), true);

			System.out.println("Verbindung erfolgreich: " + ShipApp.getHost());

			String line;
			while ((line = in.readLine()) != null) {

// loaded
				if (line.contains("loaded:")) {
					shipApp.setLoaded(true);
					loadedCargo = Cargo.parse(line);
				}

// unloaded				
				if (line.contains("unloaded:")) {
					shipApp.setLoaded(false);
					shipApp.sendProfit(loadedCargo);
				}

// Radarnachrichten herausfiltern
				if (line.contains("radarscreen")) {
					radarCommand = line.split(":")[1];
				} else {
					System.out.println(line);
				}

// Autopilot nur vom Hafen aus aktivierbar
				if (line.contains("reached")) {
					radarfield.setGround(Ground.HAFEN);
				} else {
					radarfield.setGround(Ground.WASSER);
				}

// Fehlermeldung
				if (line.contains("error")) {
					Object errorMessage = null;
					if (line.contains("money")) {
						errorMessage = "Error: Company hasn't enough moneyd.";
					}
					if (line.contains("company")) {
						errorMessage = "Error: The Company is not registered.";
					}
					if (errorMessage == null) {
						errorMessage = line;
					}
					radarCommand = errorMessage.toString().toLowerCase();
					JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);

				}
			}
		} catch (UnknownHostException e) {
			System.err.println("Unbekannter Hostname: " + ShipApp.getHost());
		} catch (IOException e) {
			System.err.println("Keine Verbindung zu '" + ShipApp.getHost() + "' moeglich");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (shipSocket != null && !shipSocket.isClosed()) {
					shipSocket.close();
				}
			} catch (IOException e) {
				System.err.println("Fehler beim Schließen der Ressourcen: " + e.getMessage());
			}
		}
		System.out.println("SeaTradeReceiver beendet");
	}

}
