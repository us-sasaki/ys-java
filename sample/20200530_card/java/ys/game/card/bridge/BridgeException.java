package ys.game.card.bridge;

import ys.game.card.CardException;

/**
 * コントラクトブリッジ処理における例外の規定クラス。
 *
 * @version		a-release		17, April 2000
 * @author		Yusuke Sasaki
 */
public class BridgeException extends CardException {
	public BridgeException() {
	}
	public BridgeException(String msg) {
		super(msg);
	}
}
