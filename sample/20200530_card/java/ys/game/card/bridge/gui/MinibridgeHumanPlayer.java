package ys.game.card.bridge.gui;

import ys.game.card.Card;
import ys.game.card.Packet;

import ys.game.card.bridge.*;

/**
 * �f�t�h����Ńv���C���s��MiniBridge�̃q���[�}���v���C���[�ł��B
 *
 * @version		making		5, October 2003
 * @author		Yusuke Sasaki
 */
public class MinibridgeHumanPlayer extends HumanPlayer {
	
	protected MiniBiddingBox	biddingBox;
/*
 * 
 */
	public MinibridgeHumanPlayer(Board board, BridgeField field, int seat) {
		super(board, field, seat);
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
	
	
	public void dispose() {
		super.dispose();
		if (biddingBox != null) biddingBox.dispose();
	}
	
	
/*---------------
 * inner classes
 */
	protected class MiniBiddingBox extends java.awt.Dialog {
	}

}
