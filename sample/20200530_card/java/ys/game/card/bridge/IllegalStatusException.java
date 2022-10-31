package ys.game.card.bridge;

/**
 * Board, PlayHistory などに現在のステータス上受け付けられない操作を行おうと
 * した場合にスローされる例外です。
 *
 * @version		a-release		4, May 2000
 * @author		Yusuke Sasaki
 */
public class IllegalStatusException extends BridgeRuntimeException {
	public IllegalStatusException() {
	}
	public IllegalStatusException(String msg) {
		super(msg);
	}
}
