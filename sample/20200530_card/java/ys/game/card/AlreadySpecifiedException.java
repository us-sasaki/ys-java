package ys.game.card;

/**
 * すでに内容が確定しているカードに対して specify() を実行しようとした場合や、
 * すでに使用済みの内容を specify() したときにスローされる例外です。
 *
 * @version		a-release	17, April 2000
 * @author		Yusuke Sasaki
 */
public class AlreadySpecifiedException extends RuntimeException {
	public AlreadySpecifiedException() {
	}
	public AlreadySpecifiedException(String msg) {
		super(msg);
	}
}

