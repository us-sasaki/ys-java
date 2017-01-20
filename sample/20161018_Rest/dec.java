public class dec {
	public static void main(String[] args) throws Exception {
		System.out.println( new String(java.util.Base64.getDecoder().decode("dXMuc2FzYWtpQG50dC5jb206bnR0Y29tc2FzYWtpMw==")));
		System.out.println( new String(java.util.Base64.getDecoder().decode("bnR0Y29tL3VzLnNhc2FraUBudHQuY29tOm50dGNvbXNhc2FraTM=")));
		
//		System.out.println(System.getProperties());
		System.out.println(Float.parseFloat("3e-1"));
		System.out.println(Double.parseDouble("333e+5"));
	}
}

	private void skipsp(PushbackReader r) {
		while (true) {
			int c = r.read();
			if (c == -1) return;
			if (c == ' ') continue;
			if (c == '\t') continue;
			if (c == '\r') continue;
			if (c == '\n') continue;
			r.unread(c);
			return;
		}
	}
	
	private void parseValue(PushbackReader r) {
		skipsp(r); // 空白、タブ、改行をスキップ
		int c = r.read();
		if (c == -1) throw new java.io.EOFException();
		switch (c) {
		}
	}
	