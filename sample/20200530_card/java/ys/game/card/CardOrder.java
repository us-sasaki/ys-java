package ys.game.card;

/**
 * Packet の arrange() メソッドにおいて、順序規則を規定するクラス。
 *
 * @version		a-release	15, April 2000
 * @author		Yusuke Sasaki
 */
public interface CardOrder {

	/**
	 * Card a と Card b の順序を比較する。
	 * a > b のとき 1, a = b のとき 0, a < b のとき -1 となる。
	 * = については、equals メソッドと互換性を持たせるべき。
	 *
	 * @param		a		比較対象１
	 * @param		b		比較対象２
	 * @return
	 */
	int compare(Card a, Card b);
}
