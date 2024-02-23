package sea.Basis;

public class RadarField {
	public static RadarField parse(String s) {
		String[] token = s.trim().split("\\|");
		if (token.length == 3) {
			if (token[0].equals("RF")) {
				try {
					Ground g = Ground.valueOf(token[1]);
					boolean ship = false;
					if (token[2].equalsIgnoreCase("S")) {
						ship = true;
					}
					return new RadarField(g, ship);
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	private Ground ground;

	private boolean hasShip;

	public RadarField(Ground ground) {
		this(ground, false);
	}

	public RadarField(Ground ground, boolean hasShip) {
		this.ground = ground;
		this.hasShip = hasShip;
	}

	public Ground getGround() {
		return ground;
	}

	public boolean isHasShip() {
		return hasShip;
	}

	public void setGround(Ground ground) {
		this.ground = ground;
	}

	public void setHasShip(boolean hasShip) {
		this.hasShip = hasShip;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RF|");
		sb.append(ground.name());
		sb.append("|");
		if (hasShip) {
			sb.append("S");
		} else {
			sb.append("-");
		}
		return sb.toString();
	}

}
