package sea.CompanyApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ShipSession extends Thread {

    private CompanyApp companyApp;
    private Socket toSeaTrade;
    private BufferedReader inSession;
    private PrintWriter outSession;
    private String shipName;

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
        try {
            String message;
            while ((message = inSession.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                System.out.println("Received message from " + shipName + ": " + message);

                // Process the incoming messages
                processMessage(message);
            }
        } catch (IOException e) {
            System.out.println("IOException in ShipSession run: " + e.getMessage());
        } finally {
            Close();
        }
    }

    private void processMessage(String message) {
        if (message.startsWith("login:")) {
            // Extract shipName from the login message
            shipName = message.substring("login:".length());
            System.out.println("Ship " + shipName + " connected to Company " + companyApp.getCompName());
            companyApp.addShipSession(this);
        } else if (message.startsWith("logout:")) {
            // Extract shipName from the logout message
            shipName = message.substring("logout:".length());
            System.out.println("Ship " + shipName + " logged out from Company " + companyApp.getCompName());
            Close(); // Close the ShipSession
        } else {
            // Process other messages as needed
            // ...
        }
    }

    public void Send(String message) {
        outSession.println(message);
    }

    public synchronized void Close() {
        try {
            if (toSeaTrade != null) {
                toSeaTrade.close();
            }
            // Additional cleanup tasks
        } catch (IOException e) {
            System.out.println("IOException in close: " + e.getMessage());
        }
    }

    public String getShipName() {
        return shipName;
    }
}
