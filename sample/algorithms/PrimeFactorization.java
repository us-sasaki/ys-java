import java.util.*;

/**
 * �f���������̌`�Ő�����ێ�����N���X
 */
class PF {
	/** �f������N���X */
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
	
	/** �f��������Ώۂ̐��̏�� */
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
	 * n ��f�����������A���ʂ� Map �ŕԋp���܂��B�v�Z�ʂ� O(log n) �ł��B
	 *
	 * @param	n	�f��������Ώۂ̐�(LIMIT�ȉ�)
	 * @return	�f������������(key: �f����, value: �w��)
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
		if (n > 1) f.put(n, 1); // p �͑f��
		return f;
	}
	
	/**
	 * n ��f�����������Along[] �Ō��ʂ�ԋp���܂��B�v�Z�ɂ� pf() �𗘗p���Ă���A
	 * �z��`���ɕϊ����Ă��܂��B
	 * �f�����͏����Ƃ͌���Ȃ����߁A�����Ƃ���ꍇ
	 * Arrays.sort(pfa, p -> p[0]) �Ƃ��ĉ������B
	 * 
	 * @param n �f��������Ώۂ̐�(LIMIT�ȉ�)
	 * @return	�f������������ [i][0]..�f����, [i][1]..�w��
	 */
	static long[][] pfa(long n) {
		return pfa(pf(n));
	}
	
	/**
	 * n ��f�����������Along[] �Ō��ʂ�ԋp���܂��B
	 * �f�����͏����Ƃ͌���Ȃ����߁A�����Ƃ���ꍇ
	 * Arrays.sort(pfa, p -> p[0]) �Ƃ��ĉ������B
	 * 
	 * @param n �f��������Ώۂ̐�(LIMIT�ȉ�)
	 * @return	�f������������ [i][0]..�f����, [i][1]..�w��
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
	 * �w�肳�ꂽ�f�������� map �� long �ɕϊ����܂��B
	 * n ���v�Z���ʂƂ��āA�v�Z�ʂ͂��悻 O(log n) �ł��B
	 *
	 * @param	f	�f��������\��
	 * @return	���̐�
	 */
	static long longValue(Map<Long, Integer> f) {
		long r = 1;
		for (long p : f.keySet()) {
			 r *= pow(p, f.get(p));
		}
		return r;
	}

	/**
	 * �����ׂ̂�����v�Z���܂��B
	 * 
	 * @param p	�
	 * @param x �ׂ�
	 * @return p �� x ���ԋp
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
	 * �J�[�}�C�P���֐��̒l�����߂܂��B
	 * �J�[�}�C�P���֐� ��(n) �͎��𖞂����֐�
	 * �@a �� n �ƌ݂��ɑf�Ȏ��R���Ƃ����Ƃ��A
	 *     ��(n) = e   s.t.   ��a, a^e �� 1 (mod n) �𖞂����ŏ��� e
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
	 * �w�肳�ꂽ�f������������A�񐔂����������Ɋi�[�������X�g�`����
	 * �ԋp���܂��B
	 * @param	pfa		�Ώۂ̑f��������
	 * @return	���ׂĂ̖񐔂������Ƀ��X�g����������
	 */
	static List<Long> divs(long[][] pfa) {
		if (p == null) p = new long[PRIMES.size()];
		List<Long> a = new ArrayList<>();
		int[] c = new int[pfa.length];
		
		long v = 1;
		Arrays.fill(p, 0, pfa.length, 1);
		while (true) {
			a.add(v);
			// c[i] �J��グ����
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
	 * test1: �J�[�}�C�P���֐�
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
			// �J�[�}�C�P���֐��́A�C�ӂ� a �ɂ��Đ��藧�ŏ��̐��ł���A
			// ����� a �ɑ΂��Ă͂���ɏ������������肤��
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
	 * test2 ��
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
