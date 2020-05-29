package ys.game.card;

/**
 * Card 関連処理における処理エラーの基底クラス。
 *
 * @version		a-release	16, April 2000
 * @author		Yusuke Sasaki
 */
public class CardError extends Error {
	public CardError() {
	}
	public CardError(String msg) {
		super(msg);
	}
}
