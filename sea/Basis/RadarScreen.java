package sea.Basis;

import java.io.Serializable;

public class RadarScreen implements Serializable {

	private static final long serialVersionUID = -3952504957695069975L;

	public static RadarScreen parse(String s) {
		String[] token = s.trim().split("\\;");
		if (token.length == 10) {
			if (token[0].equals("RSCREEN")) {
				try {
					Position center = Position.parse(token[1]);
					RadarField[] rf = new RadarField[8];
					for (int i = 2; i < 10; i++) {
						rf[i - 2] = RadarField.parse(token[i]);
					}
					return new RadarScreen(center, rf);
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	private Position center;

	private RadarField[] measures;

	public RadarScreen(Position center, RadarField[] measures) {
		super();
		this.center = center;
		this.measures = measures;
	}

	public Position getCenter() {
		return center;
	}

	public Ground getGround(RadarPos rpos) {
		return measures[rpos.ordinal()].getGround();
	}

	public RadarField[] getMeasures() {
		return measures;
	}

	public boolean hasShip(RadarPos rpos) {
		return measures[rpos.ordinal()].isHasShip();
	}

	public void setCenter(Position center) {
		this.center = center;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RSCREEN;");
		sb.append(center.toString());
		for (RadarField rf : measures) {
			sb.append(";");
			sb.append(rf.toString());
		}
		return sb.toString();
	}
}
