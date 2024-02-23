package sea.CompanyApp;

public class Cargo {

	public static Cargo parse(String s) {
		String[] token = s.trim().split("\\|");
		if (token.length == 5) {
			if (token[0].equals("CARGO")) {
				int id = Integer.parseInt(token[1]);
				int wert = Integer.parseInt(token[4]);
				return new Cargo(id, token[2], token[3], wert);
			}
		}
		return null;
	}

	private int id;
	private String source;
	private String destination;

	private int value;

	public Cargo(int id, String source, String destination, int value) {
		super();
		this.source = source;
		this.destination = destination;
		this.value = value;
		this.id = (int) (Math.random() * 1000) + 1;
	}

	public Cargo(String source, String destination, int value) {
		super();
		this.source = source;
		this.destination = destination;
		this.value = value;
		this.id = (int) (Math.random() * 1000) + 1;
	}

	public String getDestination() {
		return destination;
	}

	public int getId() {
		return id;
	}

	public String getSource() {
		return source;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "CARGO|" + id + "|" + source + "|" + destination + "|" + value;
	}

}
