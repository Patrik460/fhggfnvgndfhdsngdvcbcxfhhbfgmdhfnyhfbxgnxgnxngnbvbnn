package sea.CompanyApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ShipSession extends Thread {

	private CompanyApp companyApp;
	private Socket toSeaTrade;
	private final Map<String, Socket> connectedShips = Collections.synchronizedMap(new HashMap<>());
	private BufferedReader inSession;
	private PrintWriter outSession;

	public ShipSession(CompanyApp companyApp, Socket toSeaTrade) {
		this.companyApp = companyApp;
		this.toSeaTrade = toSeaTrade;
		try {
			this.inSession = new BufferedReader(new InputStreamReader(toSeaTrade.getInputStream()));
			this.outSession = new PrintWriter(toSeaTrade.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("IOException in ShipSession constructor: " + e.getMessage());
		}
	}

	@Override
	public void run() {
		String shipName;
		try {
			shipName = inSession.readLine();
			System.out.println("Ship " + shipName + " connected to Company " + companyApp.getCompName());
			String message;
			while ((message = inSession.readLine()) != null && !Thread.currentThread().isInterrupted()) {
				System.out.println("Received message from " + shipName + ": " + message);
				// Process message
			}
		} catch (IOException e) {
			System.out.println("IOException in ShipSession run: " + e.getMessage());
		} finally {
			Close();
			companyApp.remove(this);
		}
	}

	public synchronized void AddShip(Socket shipSocket) {
		try {
			BufferedReader shipReader = new BufferedReader(new InputStreamReader(shipSocket.getInputStream()));
			String shipName = shipReader.readLine();
			connectedShips.put(shipName, shipSocket);
		} catch (IOException e) {
			System.out.println("IOException in addShip: " + e.getMessage());
		}
	}

	public void Send(String message) {
		outSession.println(message);
	}

	// TODO: Zur ShipApp auslagern
	public void RegisterToCompany(String companyName) {
		companyApp.addShipSession(this);
	}

	// TODO: Zur ShipApp auslagern
	public void UnregisterFromCompany() {
		companyApp.remove(this);
	}

	public synchronized void Close() {
		try {
			if (toSeaTrade != null) {
				toSeaTrade.close();
			}
			// Weitere Aufräumarbeiten
		} catch (IOException e) {
			System.out.println("IOException in close: " + e.getMessage());
		}
	}

	public Map<String, Socket> getConnectedShips() {
		return connectedShips;
	}

	public Socket GetSocket() {
		return toSeaTrade;
	}
}
