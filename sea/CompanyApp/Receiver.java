package sea.CompanyApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sea.ShipApp.ShipApp;

public class Receiver extends Thread {

	private Socket companySocket = null;
	private BufferedReader in = null;
	public PrintWriter out = null;
	private int balance = 0;
	private final List<String> allCargos = Collections.synchronizedList(new ArrayList<>()); // Threadsichere Liste
	private final List<String> allHarbours = Collections.synchronizedList(new ArrayList<>()); // Threadsichere Liste

	public String[] getReceivedHarbours() {
		Collections.sort(allHarbours);
		String[] stringArray = allHarbours.toArray(new String[0]);
		allHarbours.clear();
		if (stringArray == null) {
			stringArray = new String[] { "N/A" };
		}
		return stringArray;
	}
	public int getBalance() {
		return balance;
	}

	public String[] getReceivedCargos() {
		Collections.sort(allCargos);
		String[] stringArray = allCargos.toArray(new String[0]);
		allCargos.clear();
		if (stringArray == null) {
			stringArray = new String[] { "N/A" };
		}
		return stringArray;
	}

	@Override
	public void run() {
		try {
			// Socket und Streams erzeugen
			companySocket = new Socket(CompanyApp.getHost(), CompanyApp.getPort());
			in = new BufferedReader(new InputStreamReader(companySocket.getInputStream()));
			out = new PrintWriter(companySocket.getOutputStream(), true);

			System.out.println("Verbindung erfolgreich: " + CompanyApp.getHost());

			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line); // Servernachricht anzeigen

				if (line.startsWith("harbour:")) {
					String harbourName = line.substring("harbour:".length());
					harbourName = harbourName.split(":")[1];
					allHarbours.add(harbourName);
				}
				if (line.startsWith("cargo:")) {
					String cargoName = line.substring("cargo:".length());
					allCargos.add(cargoName);
				}
				if (line.startsWith("registered:")) {
					String money = line.split(":")[2];
					balance = Integer.parseInt(money);
					
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
				if (companySocket != null && !companySocket.isClosed()) {
					companySocket.close();
				}
			} catch (IOException e) {
				System.err.println("Fehler beim Schließen der Ressourcen: " + e.getMessage());
			}
			System.out.println("Receiver beendet");
		}
	}
}
