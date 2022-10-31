package ys.game.card.bridge.gui;

import ys.game.card.Card;
import ys.game.card.Packet;

import ys.game.card.bridge.*;

/**
 * �f�t�h����Ńv���C���s���q���[�}���v���C���[�ł��B
 *
 * @version		making		7, May 2000
 * @author		Yusuke Sasaki
 */
public class HumanPlayer extends Player {
	
	protected BridgeField	field;
	protected MessageDialog	messageDialog;
/*
 * 
 */
	public HumanPlayer(Board board, BridgeField field, int seat) {
		setBoard(board);
		setMySeat(seat);
		this.field = field;
	}
	
/*------------
 * implements
 */
	/**
	 * �r�b�h�͂܂���������Ă��܂���B�����I�ɕK���p�X���܂��B
	 *
	 * @return		�p�X
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * �_�u���N���b�N���ꂽ�J�[�h�����[����\�ȃv���C�ł�������A���̃J�[�h��ԋp���܂��B
	 *
	 * @return		�����_���ȃv���C
	 */
	public Card draw() throws InterruptedException {
		Board board = getBoard();
		Card clicked = null;
		while (true) {
			clicked = field.waitCardSelect();
			if (clicked != null) {
				if (board.allows(clicked)) break;
				if (getHand().contains(clicked)) {
					messageDialog = new MessageDialog(field.getCanvas(), "���[�h�Ɠ����X�[�g��I�����Ă�������");
					messageDialog.show();
					messageDialog.dispose();
				} else {
					int turn = getBoard().getTurn();
					String seat;
					switch (turn) {
					case Board.NORTH:
						seat = "North"; break;
					case Board.EAST:
						seat = "East"; break;
					case Board.SOUTH:
						seat = "South"; break;
					case Board.WEST:
						seat = "West"; break;
					default:
						seat = "??";
					}
					
					messageDialog = new MessageDialog(field.getCanvas(), seat + "�̃n���h����J�[�h��I�����Ă�������");
					messageDialog.show();
					messageDialog.dispose();
				}
			}
		}
		
		return clicked;
	}
	
	public void dispose() {
		if (messageDialog != null) messageDialog.dispose();
	}
}
