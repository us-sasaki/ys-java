package ys.game.card.bridge;

/**
 * Board, PlayHistory �ȂǂɌ��݂̃X�e�[�^�X��󂯕t�����Ȃ�������s������
 * �����ꍇ�ɃX���[������O�ł��B
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
