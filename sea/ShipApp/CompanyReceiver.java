package sea.ShipApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class CompanyReceiver extends Thread {

    private Socket shipCompanySocket;
    private BufferedReader in;
    public PrintWriter out;

    public void ausgabe(String line) {
        System.out.println(line);
    }

    @Override
    public void run() {
        final String serverAddress = "localhost"; // Hier den Host des Servers eintragen
        final int port = 5555; // Hier den Port des Servers eintragen

        try {
            shipCompanySocket = new Socket(serverAddress, port);
            out = new PrintWriter(shipCompanySocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(shipCompanySocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: localhost.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: localhost.");
            System.exit(1);
        }
        String line = null;
        try {
			while ((line = in.readLine()) != null) {
			    if (line.contains(":")) {
			      sendToServer(line);
			    }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Weitere Verarbeitung hier...

        System.out.println("CompanyReceiver terminated");
    }

    public void sendToServer(String message) {
        if (out != null) {
            out.println(message);
        } else {
            System.err.println("Error: PrintWriter is null.");
        }
    }
}
