package ys.game.card.bridge;

/**
 * ���f�̗v�����󂯎�����ȂǂŊ��荞�݂������������Ƃ����� Exception �ł��B
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
