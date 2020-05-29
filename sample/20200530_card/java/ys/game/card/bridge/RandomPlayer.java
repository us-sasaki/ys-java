package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;

/**
 * �\�ȃv���C�������_���ɍs���R���s���[�^�v���C���[�ł��B
 * �r�b�h�͂˂Ƀp�X���܂��B�f�B�N���A���[�Ƃ��Ă��v���C�ł��܂��B
 *
 * @version		making		6, May 2000
 * @author		Yusuke Sasaki
 */
public class RandomPlayer extends Player {
	
/*
 * 
 */
	public RandomPlayer() {
	}
	
	public RandomPlayer(Board board, int seat) {
		setBoard(board);
		setMySeat(seat);
	}
	
/*------------
 * implements
 */
	/**
	 * �p�X���܂��B
	 *
	 * @return		�p�X
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * �\�ȃv���C�������_���ɑI�����A�ԋp���܂��B
	 *
	 * @return		�����_���ȃv���C
	 */
	public Card draw() throws InterruptedException {
//		Thread.sleep(400); // �l�����U��
		
		Board board = getBoard();
		Packet hand = getHand();
		
		// �v���C���ׂ��n���h��������
		hand.shuffle();
		Card played = null;
		
		// ������ꂽ�n���h�̉����珇�Ƀv���C�\�ȃJ�[�h����������
		for (int i = 0; i < hand.size(); i++) {
			played = hand.peek(i);
			if (board.allows(played)) break;
		}
		if (played == null) throw new InternalError();
		
		// �n���h��߂��Ă���
		hand.arrange();
		
		return played;
	}
	
}
