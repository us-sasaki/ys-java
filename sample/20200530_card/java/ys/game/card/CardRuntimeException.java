package ys.game.card;

/**
 * Card �֘A�����ɂ����� Exception �̋K��N���X�B
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
