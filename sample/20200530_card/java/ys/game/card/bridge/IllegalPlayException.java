package ys.game.card.bridge;

/**
 * �s�\�ȃr�b�h�A�������̓v���C���s�����Ƃ����Ƃ��ɃX���[������O�ł��B
 * �u���b�WAPI �ł́Aallows ���\�b�h�ɂ����Ă��̗�O���X���[����邩�ǂ���
 * ���O���肪�\�ł��B
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
