package sea.CompanyApp;

import sea.Basis.Position;

public class Harbour {

	private int id;
	private String name;
	private Position position;

	public Harbour(int id, String name, Position position) {
		this.id = id;
		this.name = name;
		this.position = position;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Position getPosition() {
		return position;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
}
