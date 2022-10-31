package ys.game.card;

/**
 * Card 関連処理における Exception の規定クラス。
 *
 * @version		a-release	17, April 2000
 * @author		Yusuke Sasaki
 */
public class CardException extends Exception {
	public CardException() {
	}
	public CardException(String msg) {
		super(msg);
	}
}
