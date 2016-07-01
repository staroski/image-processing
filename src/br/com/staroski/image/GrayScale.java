package br.com.staroski.image;

public enum GrayScale {

	/**
	 * Mantains the original colors
	 */
	NONE() {

		@Override
		public int compute(int value) {
			return value;
		}
	},

	/**
	 * The lightness method averages the most prominent and least prominent colors:<BR>
	 * 
	 * <pre>
	 * int gray = (max(r, g, b) + min(r, g, b)) / 2;
	 * </pre>
	 */
	LIGHTNESS() {

		@Override
		public int compute(int value) {
			int[] rgb = derivate(value);
			int r = rgb[0];
			int g = rgb[1];
			int b = rgb[2];
			int gray = (max(r, g, b) + min(r, g, b)) / 2;
			rgb[0] = gray;
			rgb[1] = gray;
			rgb[2] = gray;
			return integrate(rgb);
		}
	},

	/**
	 * The average method simply averages the values:<BR>
	 * 
	 * <pre>
	 * int gray = (r + g + b) / 3;
	 * </pre>
	 */
	AVERAGE() {

		@Override
		public int compute(int value) {
			int[] rgb = derivate(value);
			int r = rgb[0];
			int g = rgb[1];
			int b = rgb[2];
			int gray = (r + g + b) / 3;
			rgb[0] = gray;
			rgb[1] = gray;
			rgb[2] = gray;
			return integrate(rgb);
		}
	},

	/**
	 * The luminosity method is a more sophisticated version of the average method.<BR>
	 * It also averages the values, but it forms a weighted average to account for human perception.<BR>
	 * We�re more sensitive to green than other colors, so green is weighted most heavily.<BR>
	 * The formula for luminosity is:<BR>
	 * 
	 * <pre>
	 * int gray = (int) (0.21 * r + 0.72 * g + 0.07 * b);
	 * </pre>
	 */
	LUMINOSITY() {

		@Override
		public int compute(int value) {
			int[] rgb = derivate(value);
			int r = rgb[0];
			int g = rgb[1];
			int b = rgb[2];
			int gray = (int) (0.21 * r + 0.72 * g + 0.07 * b);
			rgb[0] = gray;
			rgb[1] = gray;
			rgb[2] = gray;
			return integrate(rgb);
		}
	};

	private static int[] derivate(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = (rgb >> 0) & 0xFF;
		return new int[] { r, g, b };
	}

	private static int integrate(int[] rgb) {
		int r = (rgb[0] & 0xFF) << 16;
		int g = (rgb[1] & 0xFF) << 8;
		int b = (rgb[2] & 0xFF) << 0;
		return r | g | b;
	}

	private static int max(int a, int b) {
		return a > b ? a : b;
	}

	private static int max(int a, int b, int c) {
		return max(a, max(b, c));
	}

	private static int min(int a, int b) {
		return a < b ? a : b;
	}

	private static int min(int a, int b, int c) {
		return min(a, min(b, c));
	}

	public abstract int compute(int value);

	public int[] compute(int[] pixels) {
		if (this != NONE) {
			for (int i = 0, n = pixels.length; i < n; i++) {
				pixels[i] = compute(pixels[i]);
			}
		}
		return pixels;
	}
}
