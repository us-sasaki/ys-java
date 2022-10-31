package ys.game.card;

/**
 * Card 関連処理における Exception の規定クラス。
 *
 * @version		a-release	17, April 2000
 * @author		Yusuke Sasaki
 */
public class CardRuntimeException extends RuntimeException {
	public CardRuntimeException() {
	}
	public CardRuntimeException(String msg) {
		super(msg);
	}
}
