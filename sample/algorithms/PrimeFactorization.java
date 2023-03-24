import java.util.*;

/**
 * 素因数分解の形で整数を保持するクラス
 */
class PF {
	/** 素数判定クラス */
	static List<Integer> primes = new ArrayList<>();
	static {
		primes.add(2);
	}
	static int testedMax = 1;
	static boolean isPrime(long n) {
		for (int p: primes) {
			if ( (long)p*p > n) return true;
			if (n%p == 0) return false;
		}
	lp:
		for (long x = testedMax + 2; x * x <= n; x += 2) {
			for (int p: primes) {
				if ( (long)p*p > x) break;
				if (x%p == 0) {
					testedMax = x;
					continue lp;
				}
			}
			primes.add(x);
			if (n%x == 0) {
				testedMax = x;
				return false;
			}
		}
		return true;
	}
	static List<Integer> getPrimes() {
		return primes;
	}
	
	/** 素因数分解対象の数の上限 */
	final static long LIMIT = 1_000_000_000_000L;
	static List<Integer> PRIMES = new ArrayList<>();
	static {
		int n = (int)(Math.sqrt(LIMIT));
		PRIMES.add(2);
	lp:
		for (int i = 3; i <= n; i += 2) {
			double sqr = Math.sqrt(i);
			for (int p : PRIMES) {
				if (p > sqr) break;
				if (i%p == 0) continue lp;
			}
			PRIMES.add(i);
		}
	}
	
	/**
	 * n を素因数分解し、結果を Map で返却します。計算量は O(log n) です。
	 *
	 * @param	n	素因数分解対象の数(LIMIT以下)
	 * @return	素因数分解結果(key: 素因数, value: 指数)
	 */
	static Map<Long, Integer> pf(long n) {
		Map<Long, Integer> f = new HashMap<>();
		for (int p : PRIMES) {
			int x = 0;
			while (n%p == 0) {
				n /= p;
				x++;
			}
			if (x > 0) f.put((long)p, x);
			if (n == 1) break;
		}
		if (n > 1) f.put(n, 1); // p は素数
		return f;
	}
	
	/**
	 * n を素因数分解し、long[] で結果を返却します。計算には pf() を利用しており、
	 * 配列形式に変換しています。
	 * 素因数は昇順とは限らないため、昇順とする場合
	 * Arrays.sort(pfa, p -> p[0]) として下さい。
	 * 
	 * @param n 素因数分解対象の数(LIMIT以下)
	 * @return	素因数分解結果 [i][0]..素因数, [i][1]..指数
	 */
	static long[][] pfa(long n) {
		return pfa(pf(n));
	}
	
	/**
	 * n を素因数分解し、long[] で結果を返却します。
	 * 素因数は昇順とは限らないため、昇順とする場合
	 * Arrays.sort(pfa, p -> p[0]) として下さい。
	 * 
	 * @param n 素因数分解対象の数(LIMIT以下)
	 * @return	素因数分解結果 [i][0]..素因数, [i][1]..指数
	 */
	static long[][] pfa(Map<Long, Integer> pf) {
		long[][] result = new long[pf.size()][2];
		int i = 0;
		for (long p : pf.keySet()) {
			result[i][0] = p;
			result[i][1] = pf.get(p);
			i++;
		}
		return result;
	}
	
	/**
	 * 指定された素因数分解 map を long に変換します。
	 * n を計算結果として、計算量はおよそ O(log n) です。
	 *
	 * @param	f	素因数分解表現
	 * @return	元の数
	 */
	static long longValue(Map<Long, Integer> f) {
		long r = 1;
		for (long p : f.keySet()) {
			 r *= pow(p, f.get(p));
		}
		return r;
	}

	/**
	 * 整数のべき乗を計算します。
	 * 
	 * @param p	基数
	 * @param x べき
	 * @return p の x 乗を返却
	 */
	static long pow(long p, long x) {
		if ( p == 2 ) return 1L<<x;
		long r = 1;
		for (; x > 0; x >>>= 1) {
			if ( (x&1) > 0 ) r *= p;
			p = p*p;
		}
		return r;
	}
	
	/**
	 * カーマイケル関数の値を求めます。
	 * カーマイケル関数 λ(n) は次を満たす関数
	 * 　a を n と互いに素な自然数としたとき、
	 *     λ(n) = e   s.t.   ∀a, a^e ≡ 1 (mod n) を満たす最小の e
	 */
	static long carmichael(long n) {
		Map<Long, Integer> pf = pf(n);
		long lcm = 1;
		for (long p: pf.keySet()) {
			lcm = lcm(lcm, cm(p, pf.get(p)));
		}
		return lcm;
	}

	static long cm(long p, int e) {
		if (p == 2)
			return (e == 1)?1:(e == 2)?2:pow(2, e-2);
		return pow(p, e-1) * (p-1);
	}

	static long gcd(long a, long b) {
		if (a*b == 0) return a+b;
		long t;
		while ((t = b%a) != 0) { b = a; a = t; }
		return a;
	}

	static long lcm(long a, long b) {
		return a * b / gcd(a, b);
	}
	
	private static long[] p;
	/**
	 * 指定された素因数分解から、約数を小さい順に格納したリスト形式で
	 * 返却します。
	 * @param	pfa		対象の素因数分解
	 * @return	すべての約数を昇順にリスト化したもの
	 */
	static List<Long> divs(long[][] pfa) {
		if (p == null) p = new long[PRIMES.size()];
		List<Long> a = new ArrayList<>();
		int[] c = new int[pfa.length];
		
		long v = 1;
		Arrays.fill(p, 0, pfa.length, 1);
		while (true) {
			a.add(v);
			// c[i] 繰り上げ処理
			int i = 0;
			for (; i < c.length; i++) {
				c[i]++;
				if (c[i] <= pfa[i][1]) {
					v *= pfa[i][0];
					p[i] *= pfa[i][0];
					break;
				}
				v /= p[i];
				c[i] = 0;
				p[i] = 1;
			}
			if (i == c.length) break;
		}
		a.sort(Comparator.naturalOrder());
		return a;
	}
	
/*------
 * test
 */
	public static void main(String[] args) {
		test1();
		test2();
		List<Long> divs = divs(pfa(60));
		divs.forEach(System.out::println);
	}
	/**
	 * test1: カーマイケル関数
	 */
	private static void test1() {
		System.out.println("carmichael() test");
		Random r = new Random(111115L);
		int limit = 2_000_000_000;
		for (int i = 0; i < 1000; i++) {
			int n;
			do {
				n = r.nextInt(limit);
			} while (n < 2);
			int a;
			do {
				a = r.nextInt(n);
			} while (a < 2 || gcd(a, n) != 1);
			int cm = (int)carmichael(n);
			if (modPow(a, cm, n) != 1)
				System.out.println("wrong a="+a+" n="+n);
			
			// this check takes much time
			// カーマイケル関数は、任意の a について成り立つ最小の数であり、
			// 特定の a に対してはさらに小さい数もありうる
			//checkMinimality(a, n, cm);
		}
	}
	// private static void checkMinimality(int a, int n, int cm) {
	// 	System.out.println("a="+a+" n="+n+" carmichael="+cm);
	// 	long x = 1;
	// 	for (int b = 1; b < cm; b++) {
	// 	  x = (x * a) %n;
	// 		if (x == 1)
	// 			System.out.println("smaller ans. found: a="+a+" n="+n+ " b="+b);
	// 	}
	// }
	private static int modPow(int a, int p, int mod) {
		long ans = 1;
		for (int m = Integer.highestOneBit(p); m > 0; m >>>= 1) {
			ans = ans * ans % mod;
			if ((p & m) > 0) ans = ans * a % mod;
		}
		return (int)ans;
	}
	
	/**
	 * test2 約数
	 */
	private static void test2() {
		System.out.println("divs() test");
		Random r = new Random(1616161L);
		int limit = 100_000;
		for (int i = 0; i < 1000; i++) {
			int n;
			do {
				n = r.nextInt(limit);
			} while (n == 0);
			List<Long> divs = divs(pfa(n));
			checkDivs(divs, n);
		}
	}
	
	private static void checkDivs(List<Long> divs, int n) {
		//System.out.println("check divisors: divs.size="+divs.size()+" n="+n);
		int i = 0;
		for (int d = 1; d <= n; d++) {
			if (n%d != 0) continue;
			if (divs.get(i++) != d)
				System.out.println("wrong divs d="+d+" divs="+divs.get(i-1));
		}
	}
}
