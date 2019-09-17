package abdom.util;

public class Timer {
	private static long t0 = -1L;
	
	public static void start() {
		t0 = System.currentTimeMillis();
	}
	
	public static void print() {
		if (t0 == -1) start();
		System.out.print(lap());
	}
	
	public static void println() {
		if (t0 == -1) start();
		System.out.println(lap());
	}
	
	public static long lap() {
		if (t0 == -1) start();
		return System.currentTimeMillis() - t0;
	}
}

