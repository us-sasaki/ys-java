import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * p�i�̂�p���� Convolution �v�Z�B
 * Ck = sum(i=0, i <= k : Ai B(k-i)) �������ɋ��߂�B(k = 1, 2, .. , 2N)
 * a.length + b.length �� 2^21 �ŗ��p�\�B
 * �����ŁA�����W���Ō덷���Ȃ��B
 * �v�Z�ʂ� O(N log N)
 */
public class Convolutionp {
	static final long MOD = 1012924417L;
	static final long gen = 5;
	static final long genInv = 405169767L;

	private static long[] fft(long[] a, boolean inv) {
		int n = a.length;
		int c = 0;
		for (int i = 1; i < n; i++) {
			for (int j = n >> 1; j > (c ^= j); j >>= 1) { }
			if (c > i) {
				long d = a[c];
				a[c] = a[i];
				a[i] = d;
			}
		}

		long g = (inv)? genInv : gen;
		for (int i = 1; i < n; i <<= 1) {
			long z = modPow(g, (MOD - 1) / (2 * i));
			for (int j = 0; j < n; j += 2 * i) {
				long powZ = 1;
				for (int k = 0; k < i; k++) {
					long u = a[k + j];
					long v = a[k + j + i] * powZ % MOD;
					a[k + j] = (u + v) % MOD;
					a[k + j + i] = (u - v + MOD) % MOD;
					powZ = powZ * z % MOD;
				}
			}
		}
		return a;
	}

	/**
	 * p�i�̂�p���� Convolution �v�Z�B a.length + b.length �� 2^21 �ŗ��p�\
	 * @param a �g�ݍ��킹�̈��
	 * @param b �g�ݍ��킹�̑���
	 * @return a * b �̌W��
	 */
	public static long[] convolution(long[] a, long[] b) {
		int n = Integer.highestOneBit(a.length + b.length) << 1;
		a = fft(Arrays.copyOf(a, n), false);
		b = fft(Arrays.copyOf(b, n), false);
		for (int i = 0; i < n; ++i) a[i] = a[i] * b[i] % MOD;
		a = fft(a, true);
		long ninv = modPow(n, MOD - 2);
		for (int i = 0; i < n; ++i) a[i] = a[i] * ninv % MOD;
		return a;
	}

	private static long modPow(long a, long n) {
		long r = 1;
		for (; n > 0; n >>= 1) {
			if (n % 2 == 1) r = r * a % MOD;
			a = a * a % MOD;
		}
		return r;
	}

	static int i(String s) { return Integer.parseInt(s); }

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int N = i(br.readLine());
		long[] A = new long[N];
		long[] B = new long[N];
		for (int i = 0; i < N; i++) {
			String[] ab = br.readLine().split(" ");
			A[i] = i(ab[0]); B[i] = i(ab[1]);
		}
		long[] result = convolution(A, B);

		System.out.println(0);
		System.out.write(Arrays.stream(result).limit(2*N-1).mapToObj(String::valueOf).collect(Collectors.joining("\n")).getBytes());
		System.out.flush();
	}
}


