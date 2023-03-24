/**
 * Main �̉��ɓ\��t���邽�߁Apackage private class �Ƃ���B
 */
class GCD {

/*------------------------------------------------------------
 *     G C D
 */

	/**
	 * �g�����[�N���b�h�ݏ��@
	 * ax+by=gcd(a,b) �ƂȂ� gcd,x,y �����߂�B
	 * �t���v�Z�ŗ��p���邽�߁A0 �� x �� gcd �ƂȂ����ԋp���܂��B
	 * mod M �ł� a �̋t�������߂邽�߂ɂ́Aextgcd(a, M)[1] �𗘗p���܂��B
	 * extgcd(M, a)[2] �ł͕��̒l�����\�������邱�Ƃɒ��ӂ��ĉ������B
	 *
	 * @param		a	���߂�Ώۂ̒l
	 * @param		b	���߂�Ώۂ̒l�Q��
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
	 * extgcd �̂����A�t���v�Z�p�� I/F ��ύX�������́B
	 * a �� mod �݂͌��ɑf�ł���K�v������܂��B
	 *
	 * @param		a		�t�������߂�Ώ�
	 * @param		mod		�@
	 * @return		�@ mod �ɂ����� a �̋t��(>0)
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
	 * ���[�N���b�h�ݏ��@�ɂ�� gcd �v�Z
	 *
	 * @param	a	����
	 * @param	b	������̐���
	 * @return	�ő����
	 */
	static long gcd(long a, long b) {
		if (a*b == 0) return a+b;
		long t;
		while ((t = b%a) != 0) { b = a; a = t; }
		return a;
	}
	/**
	 * a �� p ��(mod m) ���v�Z���܂��B0 �� 0 ��� 1 ��Ԃ��܂��B
	 * p ���T�� 31 ���傫���ꍇ�ɍ����ł��B
	 *
	 * @param		a		��(���܂��� 0 �ł���K�v������܂�)
	 * @param		p		�w��(���܂��� 0 �ł���K�v������܂�)
	 * @param		m		�@(���ł���K�v������܂�)
	 * @return		a �� p �� (mod m)
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
	 * (�񐄏�)mod �t�� nCr �O����
	 * n �̍ő�l��^���āA�e�[�u���𐶐�����BnCr = nCr[n][r]
	 * n = 10000 ���炢�� 1000 msec ���x�ɂȂ�(�x��)
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
	 * mod �t�� nCr �O����(fac, facinv ��)
	 * n �̍ő�l��^���āA�e�[�u���𐶐�����BnCr = nCr(n, r)
	 * provide �̌v�Z�� O(n) �ő���
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
