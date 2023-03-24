/**
 * ax + by + cz + du = k �̌`�̐����� (x, y, z, u) �̌��� dp �ŋ��߂�B
 * �������A0 �� x,y,z,u �� X, Y, Z, U
 *
 * ABC222 e �ɂ�����v�Z�ʕ]��
 *   ���[�v�� : a.length �~ xx �~ k
 *   a.length �́A�ʉ߉񐔂ł̃O���[�s���O�̐��ōő� 100�B
 *   xx �́A�ő� 1000 �ŁAi �ŕ��ނ����̂ŁAi,x ���[�v�����킹�čő�1000��B
 *   k �͍ő� 100000 �Ȃ̂ŁA100000000 ���x�̃I�[�_�[�B��Ȃ��B
 *   a �������Ƃ��A���[�v�����̍ő�l�܂łŐ؂�Ƃ��Ȃ茸�点��B
 *   ��������l����B
 *   99x + 100y = k (0��x��500, 0��y��500) 99 �ōő�l 100000 �ɒB���A
 *   100 �� 100*100000 = 10000000 �ő��v�B
 */
public class CountIntegralSolutions {
	/** �W�� */
	int[] a = new int[] {1, 2, 4, 5};
	/** �ϐ��̍ő�l */
	int[] X = new int[] {2, 6, 3, 6};
	/** �萔�� */
	int k = 50;
	
	int[][] dp = new int[a.length][k+1];
	
	/**
	 * dp �������C��
	 */
	void main() {
		// dp[i][j] : �ϐ��� i+1 �g�������Aj �ƂȂ���̌�
		int sm; // ���̍ő�l
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
	 * ���ʕ\��
	 */
	private void print() {
		for (int j = 0; j <= k; j++) {
			System.out.printf("dp[%d][%d] = %d\n", a.length-1, j, dp[a.length-1][j]);
		}
	}
	
	// ���Z(4 �ϐ��p)
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
