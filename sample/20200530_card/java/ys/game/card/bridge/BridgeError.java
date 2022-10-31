package ys.game.card.bridge;

import ys.game.card.CardError;

/**
 * コントラクトブリッジ処理において発生するエラーの規定クラス。
 *
 * @version		a-release	17, April 2000
 * @author		Yusuke Sasaki
 */
public class BridgeError extends CardError {
	public BridgeError() {
	}
	public BridgeError(String msg) {
		super(msg);
	}
}
