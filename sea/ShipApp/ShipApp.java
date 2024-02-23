package sea.ShipApp;

import sea.Basis.Direction;
import sea.Basis.Position;
import sea.CompanyApp.Cargo;

public class ShipApp {

	private String name;
	private Cargo cargo;
	private Position position;
	private Direction direction;
	private boolean loaded;
	private static String host = "localhost"; // 10.53.5.102
	private static int port = 8151;
	private SeaTradeReceiver seaTradeReceiver;
	private CompanyReceiver caReceiver;

	public ShipApp() {
		setSeaTradeReceiver(new SeaTradeReceiver(this));
		setCompanyReceiver(new CompanyReceiver());
	}

	public static void main(String[] args) {
		new ShipApp();
	}

	public void send2CompanyReceiver(String message) {
		if (getCompanyReceiver().out != null) {
			getCompanyReceiver().out.println(message);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void send2SeaTradeReceiver(String message) {
		if (getSeaTradeReceiver().out != null) {
			getSeaTradeReceiver().out.println(message);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * public void send2RequestListener(String message) { if (getSeaTradeReceiver()
	 * != null) { getSeaTradeReceiver().out.println(message); try {
	 * Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
	 * } }
	 */

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
		send2CompanyReceiver("login:" + shipName + ":" + companyName);
	}

	public void loginSeaTrade(String shipName, String companyName) {
		// überprüfen
		send2SeaTradeReceiver("loginseatrade:" + shipName + ":" + companyName);
	}

	public void logoutCompany() {
		send2CompanyReceiver("logout:" + getName());
	}

	public void logoutSeaTrade() {
		// überprüfen
		send2SeaTradeReceiver("logoutseatrade:" + getName());
	}

	public void receiveOrder() {
		// eigentlich unnötig
		send2SeaTradeReceiver("receiveorder");
	}

	public void endOrder() {
		// überprüfen
		send2SeaTradeReceiver("endorder");
	}

	public void loadCargo() {
		getSeaTradeReceiver().out.println("loadcargo");
		setLoaded(true);
	}

	public void unloadCargo() {
		// überprüfen
		send2SeaTradeReceiver("unloadcargo");
		setLoaded(false);

	}

	public void loseCargo() {
		// überprüfen
		send2SeaTradeReceiver("losecargo");
	}

	public void moveTo(String harbour) {
		send2SeaTradeReceiver("moveto:" + harbour);
	}

	public void moveManualTo(Direction direction) {
		send2SeaTradeReceiver("move:" + direction);
	}

	public void sendPos(Position position) {
		// überprüfen
		send2SeaTradeReceiver("sendpos:" + position.getX() + ":" + position.getY());
	}

	public void sendDir(Direction direction) {
		// überprüfen
		send2SeaTradeReceiver("senddir:" + direction.toString());
	}

	public void launch(String company, String harbour, String shipName) {
		send2SeaTradeReceiver("launch:" + company + ":" + harbour + ":" + shipName);

	}

	public void sendProfit(Cargo cargo) {
		send2CompanyReceiver("sendprofit:" + cargo.getValue());
	}

	public void getRadarRequest() {
		// überprüfen
		getSeaTradeReceiver().out.println("radarrequest");

	}

	public void exit() {
		// überprüfen
		getSeaTradeReceiver().out.println("exit");
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

}
