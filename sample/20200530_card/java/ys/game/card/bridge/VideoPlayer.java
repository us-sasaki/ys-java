package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;

/**
 * �w�肳�ꂽ�{�[�h(�r�b�h�A�v���C����)�ɂ��������ăr�f�I�Đ�����
 * �R���s���[�^�v���C���[�ł��B
 *
 * @version		making		6, May 2000
 * @author		Yusuke Sasaki
 */
public class VideoPlayer extends Player {
	protected Board scenario;
	
/*-------------
 * Constructor
 */
	public VideoPlayer(Board board, Board scenario, int seat) {
		setBoard(board);
		setMySeat(seat);
		
		this.scenario = scenario;
	}
	
/*------------
 * implements
 */
	/**
	 * �r�b�h�������猻�݂̏�Ԃł̃r�b�h���擾���A�ԋp���܂��B(������)
	 *
	 * @return		�p�X
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * �v���C�������猻�݂̏�Ԃł̃v���C���擾���A�ԋp���܂��B
	 *
	 * @return		�����_���ȃv���C
	 */
	public Card draw() throws InterruptedException {
		Thread.sleep(400); // �l�����U��
		
		int trickCount = getBoard().getTricks();
		int num = getBoard().getTrick().size();
		
		Trick tr = scenario.getPlayHistory().getTrick(trickCount);
		
		return tr.peek(num);
	}
	
}
