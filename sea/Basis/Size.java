package sea.Basis;

import java.io.Serializable;

// Planetengroesse

public class Size implements Serializable {

	private static final long serialVersionUID = 2L;

	public static Size parse(String s) {
		String[] token = s.trim().split("\\|");
		if (token.length == 3) {
			if (token[0].equals("SIZE")) {
				int w = Integer.parseInt(token[1]);
				int h = Integer.parseInt(token[2]);
				return new Size(w, h);
			}
		}
		return null;
	}

	private int width;

	private int height;

	public Size() {
		this(0, 0);
	}

	public Size(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SIZE|");
		sb.append(width);
		sb.append("|");
		sb.append(height);
		return sb.toString();
	}

}
