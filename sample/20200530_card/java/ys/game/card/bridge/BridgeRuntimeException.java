package ys.game.card.bridge;

/**
 * コントラクトブリッジ処理における実行時例外の基底クラス。
 *
 * @version		a-release		20, April 2000
 * @author		Yusuke Sasaki
 */
public class BridgeRuntimeException extends RuntimeException {
	public BridgeRuntimeException() {
	}
	public BridgeRuntimeException(String msg) {
		super(msg);
	}
}
