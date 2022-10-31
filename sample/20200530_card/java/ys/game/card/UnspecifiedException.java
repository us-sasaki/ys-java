package ys.game.card;

/**
 * getValue(), equals(), contains() メソッドなどで、カードがUnspecifiedの
 * ため、結果が不定となる場合にスローされます。
 *
 * @version		a-release	17, April 2000
 * @author		Yusuke Sasaki
 */
public class UnspecifiedException extends RuntimeException {
	public UnspecifiedException() {
	}
	public UnspecifiedException(String msg) {
		super(msg);
	}
}

