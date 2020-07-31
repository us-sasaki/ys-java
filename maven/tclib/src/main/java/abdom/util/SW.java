package abdom.util;

/**
 * 性能測定などで利用する簡易ストップウオッチ(msec オーダー)。
 * static method を提供します。
 */
public class SW {
	private static long t0 = -1L;
	
	/**
	 * ストップウォッチを開始します。
	 */
	public static void start() {
		t0 = System.currentTimeMillis();
	}
	
	/**
	 * ラップ経過時間(msec) を表示します。改行しません。
	 */
	public static void print() {
		System.out.print(lap());
	}
	
	/**
	 * 指定メッセージの後にラップ経過時間(msec) を表示します。改行しません。
	 */
	public static void print(String msg) {
		System.out.print(msg);
		print();
	}
	
	/**
	 * ラップ経過時間(msec)を表示します。
	 */
	public static void println() {
		print();
		System.out.println();
	}
	
	/**
	 * 指定メッセージの後にラップ経過時間(msec) を表示します。
	 */
	public static void println(String msg) {
		System.out.print(msg);
		println();
	}
	
	/**
	 * ラップ経過時間(start() からの経過時間)を long 値で取得します。
	 * @return	経過時間(msec)
	 */
	public static long lap() {
		if (t0 == -1) start();
		return System.currentTimeMillis() - t0;
	}
}

