import java.io.*;

import abdom.data.json.*;

public class Read {
	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();
		BufferedReader r = new BufferedReader(new FileReader("GPSLog181028_tanzawa.json"));
		String s = r.readLine();
		r.close();
		JsonType j = null;
		for (int i = 0; i < 100; i++) {
			j = JsonType.parse(s);
		}
		long t = System.currentTimeMillis();
		System.out.println(j.toString("  "));
		System.out.println( t - t0 );
		System.out.println( j.size() );
	}
}
