package ys.game.card;

/**
 * ���łɓ��e���m�肵�Ă���J�[�h�ɑ΂��� specify() �����s���悤�Ƃ����ꍇ��A
 * ���łɎg�p�ς݂̓��e�� specify() �����Ƃ��ɃX���[������O�ł��B
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

