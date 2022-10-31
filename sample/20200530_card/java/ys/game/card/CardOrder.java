package ys.game.card;

/**
 * Packet �� arrange() ���\�b�h�ɂ����āA�����K�����K�肷��N���X�B
 *
 * @version		a-release	15, April 2000
 * @author		Yusuke Sasaki
 */
public interface CardOrder {

	/**
	 * Card a �� Card b �̏������r����B
	 * a > b �̂Ƃ� 1, a = b �̂Ƃ� 0, a < b �̂Ƃ� -1 �ƂȂ�B
	 * = �ɂ��ẮAequals ���\�b�h�ƌ݊�������������ׂ��B
	 *
	 * @param		a		��r�ΏۂP
	 * @param		b		��r�ΏۂQ
	 * @return
	 */
	int compare(Card a, Card b);
}
