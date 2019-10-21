package abdom.util;

public class SW {
	private static long t0 = -1L;
	
	public static void start() {
		t0 = System.currentTimeMillis();
	}
	
	public static void print() {
		System.out.print(lap());
	}
	
	public static void print(String msg) {
		System.out.print(msg);
		print();
	}
	
	public static void println() {
		print();
		System.out.println();
	}
	
	public static void println(String msg) {
		System.out.print(msg);
		println();
	}
	
	public static long lap() {
		if (t0 == -1) start();
		return System.currentTimeMillis() - t0;
	}
}

