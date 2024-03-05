package sea.CompanyApp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CompanyApp {

  private String name;
  private double balance = 0;
  private List<Cargo> cargos;
  private List<Harbour> harbours;
  private static String host = "localhost";
  private static int port = 8150;
  private static final int requestPort = 5555;
  private final List<ShipSession> shipSessions;
  private Receiver receiver;
  private RequestListener requestListener;
  private final ArrayList<String> shipNames = new ArrayList<>();

  public CompanyApp() {
    shipSessions = Collections.synchronizedList(new ArrayList<>());
    startRequestListener(new RequestListener(requestPort));
  }

  public static void main(String[] args) {
    new CompanyApp();
  }

  public void register(String companyName) {
    setReceiver(new Receiver());
    try {
      Thread.sleep(2000);
      send2Server("register:" + companyName);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void getHarbourInfo() {
    send2Server("getinfo:harbour");

  }

  public void getCargoInfo() {
    send2Server("getinfo:cargo");

  }

  public void send2Server(String message) {
    if (getReceiver().out != null) {
      getReceiver().out.println(message);
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void send2Ship(ShipSession shipSession, String message) {
    shipSession.Send(message);
  }

  public String[] getAllHarbours() {
    return receiver.getReceivedHarbours();
  }

  public void distributeOrder() {
    if (!shipSessions.isEmpty()) {
      Random random = new Random();
      ShipSession randomShipSession = shipSessions.get(random.nextInt(shipSessions.size()));
      send2Ship(randomShipSession, "loadcargo");
    }
  }

  // TODO: irgendwie auslagern?
  public synchronized void addShipSession(ShipSession shipSession) {
    shipSessions.add(shipSession);
  }
  /*
   * public String[] getAllShips() { for (ShipSession shipSession : shipSessions)
   * { synchronized (shipSession) { for (String shipName :
   * shipSession.getConnectedShips().keySet()) { shipNames.add(shipName); } } }
   *
   * return shipNames.toArray(new String[0]); } public synchronized void
   * remove(ShipSession session) { String shipNameToRemove = null;
   *
   * for (Map.Entry<String, Socket> entry :
   * session.getConnectedShips().entrySet()) { if
   * (entry.getValue().equals(session.GetSocket())) { shipNameToRemove =
   * entry.getKey(); break; } }
   *
   * if (shipNameToRemove != null) {
   * session.getConnectedShips().remove(shipNameToRemove);
   * System.out.println("Ship " + shipNameToRemove + " removed from Company " +
   * getCompName()); } else {
   * System.out.println("Ship not found in the connected ships map."); } }
   */

  public synchronized void shutdown() {
    requestListener.closeServerSocket();
    requestListener.interrupt(); // Interrupt the RequestListener thread

    for (ShipSession shipSession : shipSessions) {
      shipSession.Close();
    }

    shipSessions.clear();
    receiver.interrupt();
    System.out.println(getCompName() + " shutdown");
  }

  public void exit() {
    shutdown();

    System.out.println(getCompName() + " exited");
    System.exit(0);
  }

  public void startServer() {
    // Starte den RequestListener-Thread, um auf Verbindungen von ShipApps zu warten
    Thread requestListenerThread = new Thread(() -> {
      try (ServerSocket serverSocket = new ServerSocket(getPort())) {
        while (true) {
          Socket clientSocket = serverSocket.accept();
          ShipSession shipSession = new ShipSession(this, clientSocket);
          shipSessions.add(shipSession);
          shipSession.start();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    requestListenerThread.start();
  }

  // Getter und Setter
  public double getBalance() {
    return balance;
  }

  public List<Cargo> getCargos() {
    return cargos;
  }

  public String getCompName() {
    String value = "";
    if (name != null) {
      value = name;
    }
    return value;
  }

  public List<Harbour> getHarbours() {
    return harbours;
  }

  public static String getHost() {
    return host;
  }

  public static int getPort() {
    return port;
  }

  public static int getRequestPort() {
    return requestPort;
  }

  public void addBalance(double balance) {
    this.balance = this.balance + balance;
  }

  public void setCargos(List<Cargo> cargos) {
    this.cargos = cargos;
  }

  public void setCompName(String name) {
    this.name = name;
  }

  public void setHarbours(List<Harbour> harbours) {
    this.harbours = harbours;
  }

  public void setHost(String host) {
    CompanyApp.host = host;
  }

  public void setPort(int port) {
    CompanyApp.port = port;
  }

  public Receiver getReceiver() {
    return receiver;
  }

  public void setReceiver(Receiver receiver) {
    this.receiver = receiver;
  }

  public RequestListener getRequestlistener() {
    return requestListener;
  }

  public void startRequestListener(RequestListener requestListener) {
    this.requestListener = requestListener;
    if (this.requestListener == null || !this.requestListener.isAlive()) {
      closeRequestListener(); // Schließe den aktuellen RequestListener, falls vorhanden
      this.requestListener = new RequestListener(
          requestPort); // Erstelle einen neuen RequestListener mit dem
      // Port 5555
      this.requestListener.setCompanyApp(this);
      this.requestListener.start();
    }
  }

  public String[] loadHarbours() {
    String[] harbours = null;
    try {
      setReceiver(new Receiver());
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (receiver != null) {
      receiver.start();
      try {
        Thread.sleep(1000);
        send2Server("register:Quickstart");
        getHarbourInfo();
        harbours = getAllHarbours();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    return harbours;

  }

  private void closeRequestListener() {
    if (this.requestListener != null) {
      this.requestListener.interrupt(); // Unterbricht den RequestListener-Thread
      try {
        this.requestListener.closeServerSocket(); // Schließt den ServerSocket des RequestListeners
        this.requestListener.join(); // Wartet darauf, dass der RequestListener-Thread beendet wird
      } catch (InterruptedException e) {
        System.err.println("Interrupted while closing request listener: " + e.getMessage());
      }
    }
  }

}
