/**
 * Main の下に貼り付けるため、package private class とする。
 */
class GCD {

/*------------------------------------------------------------
 *     G C D
 */

	/**
	 * 拡張ユークリッド互除法
	 * ax+by=gcd(a,b) となる gcd,x,y を求める。
	 * 逆元計算で利用するため、0 ≦ x ＜ gcd となる解を返却します。
	 * mod M での a の逆元を求めるためには、extgcd(a, M)[1] を利用します。
	 * extgcd(M, a)[2] では負の値を取る可能性があることに注意して下さい。
	 *
	 * @param		a	求める対象の値
	 * @param		b	求める対象の値２つ目
	 * @return		[0]..gcd  [1]..x  [2]..y
	 */
	static final long[] extgcd(long a, long b) {
		long aa = a, bb = b;
		long x0 = 1, x1 = 0;
		long y0 = 0, y1 = 1;
	
		while (b != 0) {
			long q = a / b;
			long r = a % b;
			long x2 = x0 - q * x1;
			long y2 = y0 - q * y1;
	
			a = b; b = r;
			x0 = x1; x1 = x2;
			y0 = y1; y1 = y2;
		}
		if (bb > 0 && x0 < 0) {
			long n = -x0/bb;
			if (-x0%bb != 0) n++;
			x0 += n*bb; y0 -= n*aa;
		}
	    return new long[]{a, x0, y0};
	}
	
	/**
	 * extgcd のうち、逆元計算用に I/F を変更したもの。
	 * a と mod は互いに素である必要があります。
	 *
	 * @param		a		逆元を求める対象
	 * @param		mod		法
	 * @return		法 mod における a の逆元(>0)
	 */
	static final long inv(long a, long mod) {
		long b = mod;
		long aa = a, bb = b;
		long x0 = 1, x1 = 0;
		long y0 = 0, y1 = 1;
	
		while (b != 0) {
			long q = a / b;
			long r = a % b;
			long x2 = x0 - q * x1;
			long y2 = y0 - q * y1;
	
			a = b; b = r;
			x0 = x1; x1 = x2;
			y0 = y1; y1 = y2;
		}
		if (bb > 0 && x0 < 0) {
			long n = -x0/bb;
			if (-x0%bb != 0) n++;
			x0 += n*bb; y0 -= n*aa;
		}
	    return x0;
	}
	
	/**
	 * ユークリッド互除法による gcd 計算
	 *
	 * @param	a	正数
	 * @param	b	もう一つの正数
	 * @return	最大公約数
	 */
	static long gcd(long a, long b) {
		if (a*b == 0) return a+b;
		long t;
		while ((t = b%a) != 0) { b = a; a = t; }
		return a;
	}
	/**
	 * a の p 乗(mod m) を計算します。0 の 0 乗は 1 を返します。
	 * p が概ね 31 より大きい場合に高速です。
	 *
	 * @param		a		底(正または 0 である必要があります)
	 * @param		p		指数(正または 0 である必要があります)
	 * @param		m		法(正である必要があります)
	 * @return		a の p 乗 (mod m)
	 */
	static int modPow(int a, int p, int mod) {
		long ans = 1;
		for (int m = Integer.highestOneBit(p); m > 0; m >>>= 1) {
			ans = ans * ans % mod;
			if ((p & m) > 0) ans = ans * a % mod;
		}
		return (int)ans;
	}
	
	/**
	 * (非推奨)mod 付き nCr 前処理
	 * n の最大値を与えて、テーブルを生成する。nCr = nCr[n][r]
	 * n = 10000 くらいで 1000 msec 程度になる(遅い)
	 */
	static int MOD = 1_000_000_007;
	static int[][] nCr;
	static int[] inv;
	static void provideNCR(int n) {
		nCr = new int[n+1][n+1];
		inv = new int[n+1];
		inv[1] = 1;
		for (int i = 2; i <= n; i++) inv[i] = (int)(MOD - (long)inv[MOD%i] * (MOD/i) %MOD);
		for (int i = 0; i <= n; i++) nCr[i][0] = 1;
		for (int i = 1; i < n+1; i++) {
			for (int j = 1; j <= i; j++) {
				nCr[i][j] = (int)((long)nCr[i][j-1] * (i-j+1) %MOD * inv[j] %MOD);
			}
		}
	}
	
	/**
	 * mod 付き nCr 前処理(fac, facinv 版)
	 * n の最大値を与えて、テーブルを生成する。nCr = nCr(n, r)
	 * provide の計算量 O(n) で速い
	 */
	static int[] fac;
	static int[] ifac;
	static void provideNCR2(int n) {
		fac = new int[n+1];
		ifac = new int[n+1];
		fac[0] = 1;
		for (int i = 1; i <= n; i++) fac[i] = (int)(((long)i * fac[i-1]) %MOD);
		ifac[n] = modPow(fac[n], MOD - 2, MOD);
		for (int i = n; i > 0; i--) ifac[i-1] = (int) ((long)ifac[i] * i %MOD);
	}

	static int nCr(int n, int r) {
		long ans = fac[n];
		ans = ans * ifac[r] %MOD;
		ans = ans * ifac[n-r] %MOD;
		return (int)ans;
	}
	
	public static void main(String[] args) {
		// int N = 10000000;
		// java.util.Random r = new java.util.Random(12345L);
		// for (int i = 0; i < N; i++) {
		// 	long a = Math.abs(r.nextLong());
		// 	long b = Math.abs(r.nextLong());
		// 	if (gcd(a,b) != gcd2(a,b)) {
		// 		System.out.println("error "+a + ","+b);
		// 	}
		// }
	}
}
