package ys.game.card;

/**
 * getValue(), equals(), contains() ���\�b�h�ȂǂŁA�J�[�h��Unspecified��
 * ���߁A���ʂ��s��ƂȂ�ꍇ�ɃX���[����܂��B
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

