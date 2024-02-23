package sea.CompanyApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestListener extends Thread {
	private ServerSocket sSocket;
	private Socket cSocket;
	private int port;
	private CompanyApp companyApp;
	private PrintWriter out;

	public RequestListener(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			sSocket = new ServerSocket(port);
			System.out.println("Server started. Waiting for connection...");

			while (!Thread.currentThread().isInterrupted()) {
				try (Socket clientSocket = sSocket.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

					synchronized (this) {
						cSocket = clientSocket;
						this.out = out;
					}

					System.out.println("Connected to: " + clientSocket);
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						System.out.println("Message from client: " + inputLine);
						if (out != null) {
							processInput(inputLine, out);
						}
					}
				} catch (IOException e) {
					System.out.println("IOException in RequestListener run: " + e.getMessage());
					// Handle exception or continue to accept new connections
				}
			}
		} catch (IOException e) {
			System.out.println("IOException in RequestListener: " + e.getMessage());
		} finally {
			closeServerSocket();
		}
	}

	private void processInput(String inputLine, PrintWriter out) {
		String command = getCommand(inputLine);
		String value = getValue(inputLine);

		switch (command) {
		case "login":
			out.println("Login to Company: " + value);

			ShipSession shipSession = new ShipSession(companyApp.getCompanyApp(), cSocket); // Hier wird der Konstruktor
																							// von ShipSession
			// aufgerufen
			shipSession.start();
			shipSession.AddShip(shipSession.GetSocket());
			break;
		case "sendPos":
			sendPos(out);
			break;
		case "sendDir":
			sendDir(out);
			break;
		case "sendProfit":
			sendProfit(out);
			break;
		case "logout":
			logoutFromCompany(out);
			break;
		case "receiveOrder":
			receiveOrder(out);
			break;
		case "endOrder":
			endOrder(out);
			break;
		case "loadCargo":
			loadCargo(out);
			break;
		case "unloadCargo":
			unloadCargo(out);
			break;
		default:
			// irgendein default
			break;
		}
	}

	private void sendProfit(PrintWriter out) {
		// TODO Anzeige in der CompanyApp und Übertragung in die DB
	}

	private void sendDir(PrintWriter out) {
		// TODO Übertragung in die DB

	}

	private void sendPos(PrintWriter out) {
		// TODO Übertragung in die DB

	}

	private String getCommand(String inputLine) {
		String[] splitCommand = inputLine.split(":");
		return (splitCommand.length > 0) ? splitCommand[0] : "";
	}

	private String getValue(String inputLine) {
		String[] splitCommand = inputLine.split(":");
		return (splitCommand.length > 1) ? splitCommand[1] : "";
	}

	private void logoutFromCompany(PrintWriter out) {
		out.println("Logout from Company");
	}

	private void receiveOrder(PrintWriter out) {
		out.println("Order received");
	}

	private void endOrder(PrintWriter out) {
		out.println("Order ended");
	}

	private void loadCargo(PrintWriter out) {
		out.println("Cargo loaded");
	}

	private void unloadCargo(PrintWriter out) {
		out.println("Cargo unloaded");
	}

	public void closeServerSocket() {
		try {
			if (sSocket != null && !sSocket.isClosed()) {
				sSocket.close();
			}
		} catch (IOException e) {
			System.out.println("IOException while closing server socket: " + e.getMessage());
		}
	}
}
