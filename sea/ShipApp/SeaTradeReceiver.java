package sea.ShipApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;
import sea.Basis.Ground;
import sea.CompanyApp.Cargo;

public class SeaTradeReceiver extends Thread {

  private Socket shipSocket = null;
  private BufferedReader in = null;
  public PrintWriter out = null;
  public static String helpCommand = "";
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

      String line = null;
      String infoLine = null;
      String valueLine = null;
      while ((line = in.readLine()) != null) {
        if (line.contains(":")) {
          infoLine = line.split(":")[0].toLowerCase();
          valueLine = line.split(":")[1];
        }
// Autopilot nur vom Hafen aus aktivierbar
        if (infoLine.equals("reached")) {
          shipApp.setCenterGround(Ground.HAFEN);
        } else {
          if (infoLine.equals("moved")) {
            shipApp.setCenterGround(Ground.WASSER);
            
            //TODO: dir und pos objekt erstellen
            //TODO: cost value aus der servernachricht ziehen
            shipApp.sendDir();
            shipApp.sendPos(null);
            shipApp.sendCost();
          }
        }
// loaded
        if (infoLine.equals("loaded")) {
          shipApp.setLoaded(true);
          loadedCargo = Cargo.parse(valueLine);
        }

// unloaded				
        if (infoLine.equals("unloaded")) {
          shipApp.setLoaded(false);
          shipApp.sendProfit(loadedCargo);
        }

// Radarnachrichten herausfiltern
        if (infoLine.equals("radarscreen")) {
          helpCommand = valueLine;
        } else {
          System.out.println(line);
        }

// Fehlermeldung
        if (line.contains("error")) {
          Object errorMessage = line;
          if (line.contains("company")) {
            errorMessage = "Error: The Company is not registered.";
          }
          if (line.contains("money")) {
            errorMessage = "Error: Company hasn't enough money.";
          }
          if (line.contains("loadcargo") && line.contains("available")) {
            errorMessage = "Error: No Cargo available here.";
          }
          helpCommand = errorMessage.toString().toLowerCase();
          if (!line.contains("LEVEL")) {
            JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
          }
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
