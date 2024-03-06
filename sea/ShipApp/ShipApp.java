package sea.ShipApp;

import sea.Basis.Direction;
import sea.Basis.Ground;
import sea.Basis.Position;
import sea.CompanyApp.Cargo;

public class ShipApp {

  private String name;
  private Cargo cargo;
  private Position position;
  private Direction direction;
  private Ground centerGround;
  private boolean loaded;
  private static String host = "localhost"; // 10.53.5.102
  private static int port = 8151;
  private SeaTradeReceiver seaTradeReceiver;
  private CompanyReceiver caReceiver;

  public ShipApp() {
	  caReceiver = new CompanyReceiver();
    caReceiver.start();

    setSeaTradeReceiver(new SeaTradeReceiver(this));
  }

  public static void main(String[] args) {
    new ShipApp();
  }

  public void send2CompanyReceiver(String message, int threadTimer) {
    if (caReceiver.out != null) {
    	caReceiver.out.println(message);
      try {
        Thread.sleep(threadTimer);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void send2SeaTradeReceiver(String message, int threadTimer) {
    if (getSeaTradeReceiver().out != null) {
      getSeaTradeReceiver().out.println(message);
      try {
        Thread.sleep(threadTimer);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public CompanyReceiver getCompanyReceiver() {
    return caReceiver;
  }

  private void setCompanyReceiver(CompanyReceiver caReceiver) {
    this.caReceiver = caReceiver;
  }

  public SeaTradeReceiver getSeaTradeReceiver() {
    return seaTradeReceiver;
  }

  public void setSeaTradeReceiver(SeaTradeReceiver seaTradeReceiver) {
    this.seaTradeReceiver = seaTradeReceiver;
  }

  public void loginCompany(String shipName, String companyName) {
    send2CompanyReceiver("login:" + shipName + ":" + companyName, 100);
  }

  public void loginSeaTrade(String shipName, String companyName) {
    // überprüfen
    send2SeaTradeReceiver("loginseatrade:" + shipName + ":" + companyName, 100);
  }

  public void logoutCompany() {
    send2CompanyReceiver("logout:" + getName(), 100);
  }

  public void logoutSeaTrade() {
    // überprüfen
    send2SeaTradeReceiver("logoutseatrade:" + getName(), 100);
  }

  public void receiveOrder() {
    send2SeaTradeReceiver("receiveorder:" + getName(), 100);
  }

  public void endOrder() {
    send2SeaTradeReceiver("endorder:" + cargo.getId(), 100);
  }

  public void loadCargo() {
    send2SeaTradeReceiver("loadcargo", 100);
    cargo = seaTradeReceiver.getLoadedCargo();
    setLoaded(true);

    if (cargo != null) {
      send2CompanyReceiver("loadcargo:" + cargo.getId(), 100);
    }
  }

  public void unloadCargo() {
    send2SeaTradeReceiver("unloadcargo", 100);
    send2CompanyReceiver("unloadcargo:" + cargo.getId(), 100);

    cargo = null;
    setLoaded(false);
  }

  public void loseCargo() {
    // überprüfen
    send2SeaTradeReceiver("losecargo", 100);
  }

  public void moveTo(String harbour) {
    send2SeaTradeReceiver("moveto:" + harbour, 100);
  }

  public void moveManualTo(Direction direction) {
    send2SeaTradeReceiver("move:" + direction, 100);
  }

  public void sendPos(Position position) {
    send2SeaTradeReceiver("sendpos:" + position.getX() + ":" + position.getY(), 100);
  }

  public void sendDir(Direction direction) {
    send2SeaTradeReceiver("senddir:" + direction.toString(), 100);
  }

  public void launch(String company, String harbour, String shipName) {
    send2SeaTradeReceiver("launch:" + company + ":" + harbour + ":" + shipName, 100);

  }

  public void sendProfit(Cargo cargo) {
    send2CompanyReceiver("sendprofit:" + cargo.getValue(), 100);
  }

  public void sendCost(int cost) {
    System.out.println("kost: "+cost);
    
	  send2CompanyReceiver("sendCost:" + cost, 1000);
    
  }

  public void getRadarRequest() {
    // überprüfen
    send2SeaTradeReceiver("radarrequest", 900);

  }

  public void exit() {
    // überprüfen
    send2SeaTradeReceiver("exit", 100);
  }

  // Getter und Setter
  public Cargo getCargo() {
    return cargo;
  }

  public Direction getDirection() {
    return direction;
  }

  public static String getHost() {
    return host;
  }

  public String getName() {
    return name;
  }

  public static int getPort() {
    return port;
  }

  public Position getPosition() {
    return position;
  }

  public void setCargo(Cargo cargo) {
    this.cargo = cargo;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public void setHost(String host) {
    ShipApp.host = host;
  }

  public void setLoaded(boolean loaded) {
    this.loaded = loaded;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPort(int port) {
    ShipApp.port = port;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public boolean isLoaded() {
    return loaded;
  }

  public Ground getCenterGround() {
    return centerGround;
  }

  public void setCenterGround(Ground ground) {
    centerGround = ground;
  }

}
