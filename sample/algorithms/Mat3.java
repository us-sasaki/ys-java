/**
 * Zm ã‚Ì 3x3 s—ñ
 * Š|‚¯Z‚Ì‚İ
 */
class Mat3 {
	static final int MOD = 998244353;
	static final Mat3 E = new Mat3(new int[] {1,0,0, 0,1,0, 0,0,1});

	/** i s j —ñ‚Ì—v‘f‚Í e[i*3 + j] */
	int[] e;

	Mat3(int[] e) {
		this.e = e;
	}

	Mat3 multiply(Mat3 b) {
		int[] result = new int[9];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				result[i*3 + j] = (int) ((((long)e[i*3] * b.e[j] %MOD + (long)e[i*3 + 1] * b.e[3 + j] %MOD) %MOD + (long)e[i*3 + 2] * b.e[6 + j] %MOD) %MOD);
			}
		}
		return new Mat3(result);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int i = 0; i < 9; i++) {
			sb.append(e[i]);
			sb.append(' ');
			if (i%3 == 2) sb.append(' ');
		}
		sb.append(']');
		return sb.toString();
	}
}
