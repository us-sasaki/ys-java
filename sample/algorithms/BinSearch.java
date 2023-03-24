import java.util.Arrays;
import java.util.Random;
import java.util.function.LongUnaryOperator;

/*------------------------------------------------------------
 *     Binary Search
 */
class BinSearch {
	/**
	 * バイナリサーチのサンプル実装(jdk より)
	 * f : [s, e) → long  単調増加関数(long→long) としたとき、
	 * 与えられた y に対し、f(x)=y となる x をバイナリサーチで求める。
	 * f(x)=y となる x が存在しない場合、f(x)>y となる最初の(最小の) x に
	 * 対し、-x-1 の値を返却する。
	 * ∀x∈[s,e) に対し、f(x) > n の場合、-s-1 を返す
	 * ∀x∈[s,e) に対し、f(x) < n の場合、-e-1 を返す
	 *
	 * @param		s			定義域の最小値
	 * @param		eExclusive	定義域の上界
	 * @param		f			long → long 関数(広義単調増加)
	 * @param		y			目的値。f(x) = y となる x を求める
	 * @return		求める x
	 */
	static long binarySearch(long s, long eExclusive, LongUnaryOperator f, long y) {
		long l = s;
		long r = eExclusive - 1;
		
		while (l <= r) {
			long m = (l + r) >> 1; // ここを /2 とするとだめ(↓参照)
			long mv = f.applyAsLong(m);
			
			if (mv < y) l = m + 1;
			else if (mv > y) r = m - 1;
			else return m;
		}
		return -(l+1);
	}
	// 上でsが負となる場合、除算でなく符号付シフトを使うのは必須
	// -3 / 2 = -1
	// -3 >> 1 = -2 であり、/2 は 0 に近い数を選ぶが、>> は小さい数を選ぶ
	// この差により、答えにずれが生じる場合がある。
	
	/**
	 * f[x] ≧ v となる最初(最小の) x の値を返却する。
	 * このような x が存在しない場合、eExclusive を返す。
	 */
	static int binarySearch(int s, int eExclusive, int[] f, int v) {
		int l = s;
		int r = eExclusive - 1;
		
		while (l <= r) {
			int m = (l + r) >> 1;
			int mv = f[m];
			
			if (mv < v) l = m + 1;
			else r = m - 1;
		}
		return l;
	}
	
	/**
	 * p が false, false, ...., false, true, true, .. となっているとき、
	 * p[x] = true となる最初(最小の) x の値を返却する。
	 * このような x が存在しない場合、eExclusive を返す。
	 */
	static int binarySearch(int s, int eExclusive, p[] array, int v) {
		int l = s;
		int r = eExclusive - 1;
		
		while (l <= r) {
			int m = (l + r) >> 1;
			
			if (!p[m]) l = m + 1;
			else r = m - 1;
		}
		return l;
	}
	
	public static void main(String[] args) throws Exception {
		final int N = 100000;
		int[] array = new int[N];
		Random r = new Random(102);
		for (int i = 0; i < N; i++) {
			array[i] = r.nextInt(N/10)*2;
		}
		Arrays.sort(array);
		
		for (int i = 0; i < N/10; i++) {
			int i1 = - Arrays.binarySearch(array, 2*i-1) - 1;
			int i2 = binarySearch(0, N, array, 2*i);
			if (i1 != i2) System.out.println("mismatch: "+i1+","+i2+" for "+i);
		}
	}
}