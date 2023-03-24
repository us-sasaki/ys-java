import java.util.*;
import java.io.*;
import java.util.stream.*;

/**
 * 複素数による Convolution
 * https://atcoder.jp/contests/atc001/tasks/fft_c 提出版(Main とする)
 */
public class Convolution {
	/**
	 * FFT (正変換、逆変換)。結果は f 自身に入る。
	 * @param f 変換対象の関数(double[2][n] 形式)
	 * @param n 次数(2 のべきの形)
	 * @param sgn 正変換(1), 逆変換(-1)
	 */
	private static void fft(double[][] f, int sgn) {
		int n = f[0].length;
		if (n == 1) return;
		final int n2 = n / 2;
		double[][] a = new double[2][n2];
		double[][] b = new double[2][n2];
		for (int i = 0; i < n2; i++) {
			a[0][i] = f[0][i*2];
			a[1][i] = f[1][i*2];
			b[0][i] = f[0][i*2+1];
			b[1][i] = f[1][i*2+1];
		}
		fft(a, sgn);
		fft(b, sgn);
		double theta = sgn * Math.PI / n2;
		double[] z = new double[] { Math.cos(theta), Math.sin(theta) };
		double[] powZ = new double[] { 1, 0 };
		for (int i = 0; i < n; i++) {
			f[0][i] = a[0][i%n2] + b[0][i%n2]*powZ[0] - b[1][i%n2]*powZ[1];
			f[1][i] = a[1][i%n2] + b[0][i%n2]*powZ[1] + b[1][i%n2]*powZ[0];
			double reZ = powZ[0]*z[0] - powZ[1]*z[1];
			double imZ = powZ[0]*z[1] + powZ[1]*z[0];
			powZ[0] = reZ;
			powZ[1] = imZ;
		}
	}

	/**
	 * Convolution による sum(i=0, n, ai * b(k-i)) (∀k) 高速計算
	 * @param a 組み合わせの一方
	 * @param b 組み合わせの他方
	 * @return 結果
	 */
	public static long[] convolution(double[] a, double[] b) {
		int n = Integer.highestOneBit(a.length + b.length - 2) << 1;
		if (n == 0) return new long[] { Math.round(a[0]*b[0]) };
		return convImpl(Arrays.copyOf(a, n), Arrays.copyOf(b, n));
	}

	private static long[] convImpl(double[] a, double[] b) {
		int n = a.length;
		double[][] ca = new double[][] { a, new double[n] };
		double[][] cb = new double[][] { b, new double[n] };
		fft(ca, 1);
		fft(cb, 1);
		for (int i = 0; i < n; i++) {
			double reA = ca[0][i]*cb[0][i] - ca[1][i]*cb[1][i];
			double imA = ca[0][i]*cb[1][i] + ca[1][i]*cb[0][i];
			ca[0][i] = reA;
			ca[1][i] = imA;
		}
		fft(ca, -1);

		long[] r = new long[n];
		for (int i = 0; i < n; i++) r[i] = Math.round(ca[0][i]/n);
		return r;
	}

	public static long[] convolution(int[] a, int[] b) {
		int n = Integer.highestOneBit(a.length + b.length - 2) << 1;
		if (n == 0) return new long[] { (long)a[0] * b[0] };
		int la = a.length, lb = b.length;
		double[] da = new double[n]; Arrays.setAll(da, i -> (i < la)?a[i]:0 );
		double[] db = new double[n]; Arrays.setAll(db, i -> (i < lb)?b[i]:0 );
		return convImpl(da, db);
	}

	static int i(String s) { return Integer.parseInt(s); }
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int N = i(br.readLine());
		int[] A = new int[N];
		int[] B = new int[N];
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