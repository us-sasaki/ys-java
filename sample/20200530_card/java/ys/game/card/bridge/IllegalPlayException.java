package ys.game.card.bridge;

/**
 * 不可能なビッド、もしくはプレイを行おうとしたときにスローされる例外です。
 * ブリッジAPI では、allows メソッドにおいてこの例外がスローされるかどうか
 * 事前判定が可能です。
 *
 * @version		a-release		20, April 2000
 * @author		Yusuke Sasaki
 */
public class IllegalPlayException extends IllegalStatusException {
	public IllegalPlayException() {
	}
	public IllegalPlayException(String msg) {
		super(msg);
	}
}
