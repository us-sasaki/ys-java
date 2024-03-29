package ys.game.card.bridge;

/**
 * 中断の要求を受け取ったなどで割り込みが発生したことを示す Exception です。
 *
 * @version		a-release		30, September 2000
 * @author		Yusuke Sasaki
 */
public class InterruptedBridgeException extends BridgeException {
	public InterruptedBridgeException() {
		super();
	}
	public InterruptedBridgeException(String msg) {
		super(msg);
	}
}
