import java.util.List;
import java.util.ArrayList;

/*------------------------------------------------------------
 *     K M P 法
 */

/**
 * 文字列中、別の文字列が含まれる index を求める KMP 法
 */
class KnuthMorrisPratt {
	/**
	 * KMP(クヌース・モリス・プラット)法により文字列中の文字列検索を行う。
	 * Wikipedia の疑似コードより。
	 *
	 * @param		S		検索対象のテキスト
	 * @param		W		単語
	 * @param		S内のWの位置(ない場合 -1)
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
