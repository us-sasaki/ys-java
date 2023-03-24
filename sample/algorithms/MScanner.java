import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/*------------------------------------------------------------
 *     Scanner
 */
/**
 * AtCoder—pŠÈˆÕ‚‘¬”Å Scanner
 */
class MScanner {
	BufferedReader br;
	String[] tokens;
	int cursor;
	
	MScanner() {
		br = new BufferedReader(new InputStreamReader(System.in));
	}
	
	String next() { return fetch(); }
	int nextInt() { return Integer.parseInt(fetch()); }
	long nextLong() { return Long.parseLong(fetch()); }
	double nextDouble() { return Double.parseDouble(fetch()); }
	
	String fetch() {
		if (tokens == null || cursor >= tokens.length) readNext();
		String r = tokens[cursor++];
		return r;
	}

	void readNext() {
		try {
			String line = br.readLine();
			if (line == null || line.equals("")) br.close();
			else {
				tokens = line.split(" ");
				cursor = 0;
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	@SuppressWarnings("deprecated")
	public void finalize() throws Exception {
		if (br != null) br.close();
	}
}
