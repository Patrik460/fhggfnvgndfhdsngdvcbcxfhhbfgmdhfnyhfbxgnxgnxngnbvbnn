package sea.ShipApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CompanyReceiver extends Thread {

	private Socket shipCompanySocket;
	private BufferedReader in;
	public PrintWriter out;

	@Override
	public void run() {
		final String serverAddress = "localhost"; // Hier den Host des Servers eintragen
		final int port = 5555; // Hier den Port des Servers eintragen

		try {
			shipCompanySocket = new Socket(serverAddress, port);
			out = new PrintWriter(shipCompanySocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(shipCompanySocket.getInputStream()));
			System.out.println("CompanyReceiver verbunden");

			String userInput;
			while ((userInput = in.readLine()) != null) {
				System.out.println("Server response: " + userInput);
			}
		} catch (IOException e) {
			System.err.println("Error while communicating with server: " + e.getMessage());
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (shipCompanySocket != null && !shipCompanySocket.isClosed()) {
					shipCompanySocket.close();
				}
			} catch (IOException e) {
				System.err.println("Error closing resources: " + e.getMessage());
			}
			System.out.println("CompanyReceiver terminated");
		}
	}
}
