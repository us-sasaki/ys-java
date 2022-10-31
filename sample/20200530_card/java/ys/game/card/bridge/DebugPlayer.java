package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;

/**
 * �\�ȃv���C���s���R���s���[�^�v���C���[�ł��B
 * �f�o�b�O�p�̂��߁A�����n���h�ɂ��Ă͓�������v���C���܂��B
 * �r�b�h�͂˂Ƀp�X���܂��B�f�B�N���A���[�Ƃ��Ă��v���C�ł��܂��B
 *
 * @version		making		13, October 2002
 * @author		Yusuke Sasaki
 */
public class DebugPlayer extends Player {
	
/*
 * 
 */
	public DebugPlayer(Board board, int seat) {
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
		
		Card played = null;
		
		// ������ꂽ�n���h�̉����珇�Ƀv���C�\�ȃJ�[�h����������
		for (int i = 0; i < hand.size(); i++) {
			played = hand.peek(i);
			if (board.allows(played)) break;
		}
		if (played == null) throw new InternalError();
		
		return played;
	}
	
}
