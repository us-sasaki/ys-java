import java.util.Arrays;
import java.util.Random;
import java.util.function.LongUnaryOperator;

/*------------------------------------------------------------
 *     Binary Search
 */
class BinSearch {
	/**
	 * �o�C�i���T�[�`�̃T���v������(jdk ���)
	 * f : [s, e) �� long  �P�������֐�(long��long) �Ƃ����Ƃ��A
	 * �^����ꂽ y �ɑ΂��Af(x)=y �ƂȂ� x ���o�C�i���T�[�`�ŋ��߂�B
	 * f(x)=y �ƂȂ� x �����݂��Ȃ��ꍇ�Af(x)>y �ƂȂ�ŏ���(�ŏ���) x ��
	 * �΂��A-x-1 �̒l��ԋp����B
	 * ��x��[s,e) �ɑ΂��Af(x) > n �̏ꍇ�A-s-1 ��Ԃ�
	 * ��x��[s,e) �ɑ΂��Af(x) < n �̏ꍇ�A-e-1 ��Ԃ�
	 *
	 * @param		s			��`��̍ŏ��l
	 * @param		eExclusive	��`��̏�E
	 * @param		f			long �� long �֐�(�L�`�P������)
	 * @param		y			�ړI�l�Bf(x) = y �ƂȂ� x �����߂�
	 * @return		���߂� x
	 */
	static long binarySearch(long s, long eExclusive, LongUnaryOperator f, long y) {
		long l = s;
		long r = eExclusive - 1;
		
		while (l <= r) {
			long m = (l + r) >> 1; // ������ /2 �Ƃ���Ƃ���(���Q��)
			long mv = f.applyAsLong(m);
			
			if (mv < y) l = m + 1;
			else if (mv > y) r = m - 1;
			else return m;
		}
		return -(l+1);
	}
	// ���s�����ƂȂ�ꍇ�A���Z�łȂ������t�V�t�g���g���͕̂K�{
	// -3 / 2 = -1
	// -3 >> 1 = -2 �ł���A/2 �� 0 �ɋ߂�����I�Ԃ��A>> �͏���������I��
	// ���̍��ɂ��A�����ɂ��ꂪ������ꍇ������B
	
	/**
	 * f[x] �� v �ƂȂ�ŏ�(�ŏ���) x �̒l��ԋp����B
	 * ���̂悤�� x �����݂��Ȃ��ꍇ�AeExclusive ��Ԃ��B
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
	 * p �� false, false, ...., false, true, true, .. �ƂȂ��Ă���Ƃ��A
	 * p[x] = true �ƂȂ�ŏ�(�ŏ���) x �̒l��ԋp����B
	 * ���̂悤�� x �����݂��Ȃ��ꍇ�AeExclusive ��Ԃ��B
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