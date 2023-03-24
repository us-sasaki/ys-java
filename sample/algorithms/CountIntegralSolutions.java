/**
 * ax + by + cz + du = k の形の整数解 (x, y, z, u) の個数を dp で求める。
 * ただし、0 ≦ x,y,z,u ≦ X, Y, Z, U
 *
 * ABC222 e における計算量評価
 *   ループ回数 : a.length × xx × k
 *   a.length は、通過回数でのグルーピングの数で最大 100。
 *   xx は、最大 1000 で、i で分類されるので、i,x ループを合わせて最大1000回。
 *   k は最大 100000 なので、100000000 程度のオーダー。危ない。
 *   a を昇順とし、ループを解の最大値までで切るとかなり減らせる。
 *   悪い例を考える。
 *   99x + 100y = k (0≦x≦500, 0≦y≦500) 99 で最大値 100000 に達し、
 *   100 で 100*100000 = 10000000 で大丈夫。
 */
public class CountIntegralSolutions {
	/** 係数 */
	int[] a = new int[] {1, 2, 4, 5};
	/** 変数の最大値 */
	int[] X = new int[] {2, 6, 3, 6};
	/** 定数項 */
	int k = 50;
	
	int[][] dp = new int[a.length][k+1];
	
	/**
	 * dp 処理メイン
	 */
	void main() {
		// dp[i][j] : 変数を i+1 個使った時、j となる解の個数
		int sm; // 解の最大値
		{
			int xx = Math.min(k/a[0], X[0]);
			sm = xx;
			for (int x = 0; x <= xx; x++) {
				dp[0][ a[0]*x ] = 1;
			}
		}
		for (int i = 1; i < a.length; i++) {
			int xx = Math.min(k/a[i], X[i]);
			sm = Math.min(k, sm + xx * a[i]);
			for (int x = 0; x <= xx; x++) {
				// 
				for (int j = sm; j >= a[i]*x; j--) {
					dp[i][j] += dp[i-1][j - a[i]*x];
				}
			}
		}
	}
	
	/**
	 * 結果表示
	 */
	private void print() {
		for (int j = 0; j <= k; j++) {
			System.out.printf("dp[%d][%d] = %d\n", a.length-1, j, dp[a.length-1][j]);
		}
	}
	
	// 検算(4 変数用)
	private void check() {
		int[] ans = new int[k+1];
		for (int x = 0; x <= X[0]; x++) {
			for (int y = 0; y <= X[1]; y++) {
				for (int z = 0; z <= X[2]; z++) {
					for (int u = 0; u <= X[3]; u++) {
						int cal = a[0]*x + a[1]*y + a[2]*z + a[3]*u;
						if (cal <= k) ans[cal]++;
					}
				}
			}
		}
		
		for (int j = 0; j <= k; j++) {
			if (dp[a.length-1][j] != ans[j]) System.out.println("wrong j = "+j);
		}
	}
	
	public static void main(String[] args) {
		CountIntegralSolutions c = new CountIntegralSolutions();
		c.main();
		c.print();
		c.check();
	}
}
