import java.util.List;
import java.util.ArrayList;

/*------------------------------------------------------------
 *     K M P �@
 */

/**
 * �����񒆁A�ʂ̕����񂪊܂܂�� index �����߂� KMP �@
 */
class KnuthMorrisPratt {
	/**
	 * KMP(�N�k�[�X�E�����X�E�v���b�g)�@�ɂ�蕶���񒆂̕����񌟍����s���B
	 * Wikipedia �̋^���R�[�h���B
	 *
	 * @param		S		�����Ώۂ̃e�L�X�g
	 * @param		W		�P��
	 * @param		S����W�̈ʒu(�Ȃ��ꍇ -1)
	 */
	public static int findIndex(String S, String W) {
		int m = 0;
		int i = 0;
		List<Integer> T = kmpTable(W);
		
		while (m+i < S.length()) {
			if (W.charAt(i) == S.charAt(m+i)) {
				i++;
				if (i == W.length()) return m;
			} else {
				m = m + i - T.get(i);
				if (i > 0) i = T.get(i);
			}
		}
		return -1;
	}
	
	private static List<Integer> kmpTable(String W) {
		List<Integer> t = new ArrayList<>();
		int i = 2;
		int j = 0;
		t.add(-1);
		t.add(0);
		while (i < W.length()) {
			if (W.charAt(i-1) == W.charAt(j)) {
				t.add(j+1);
				i++;
				j++;
			} else if (j > 0) {
				j = t.get(j);
			} else {
				t.add(0);
				i++;
			}
		}
		return t;
	}
}
