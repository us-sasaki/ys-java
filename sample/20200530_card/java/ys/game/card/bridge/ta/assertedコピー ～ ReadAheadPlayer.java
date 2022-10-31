package ys.game.card.bridge.ta;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.bridge.Player;
import ys.game.card.bridge.Board;
import ys.game.card.bridge.Bid;
import ys.game.card.bridge.IllegalStatusException;
import ys.game.card.bridge.SimplePlayer2;

/**
 * �_�u���_�~�[��ԂŁA�Ō�܂œǂ݂����čőP���łv���C���[�B
 *
 * @version		making		20 October, 2002
 * @author		Yusuke Sasaki
 */
public class ReadAheadPlayer extends Player {
	protected SimplePlayer2	base;
	protected boolean		openingLeadSpecified;
	
	private byte[][] paths = new byte[5000][13];
	
/*
 * 
 */
	public ReadAheadPlayer(Board board, int seat) {
		setBoard(board);
		setMySeat(seat);
		
		base = new SimplePlayer2(board, seat);
	}
	
	public ReadAheadPlayer(Board board, int seat, String ol) {
		setBoard(board);
		setMySeat(seat);
		
		base = new SimplePlayer2(board, seat, ol);
		if ((ol != null)&&(!ol.equals(""))) openingLeadSpecified = true;
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
	 * �������A�c��T�g���b�N�ȏ�͎��Ԃ������肷���邽�߁ASimplePlayer2 ���g�p����܂��B
	 *
	 * @return		�őP��
	 */
	public Card draw() throws InterruptedException {
		Board board = getBoard();
		
		if (board.getStatus() == Board.OPENING) {
			if (openingLeadSpecified) return base.draw();
		}
		
		if (board.getTricks() < 13-13) // 13-6
			return base.draw();
		
		long t0 = System.currentTimeMillis();
		
		OptimizedBoard b = new OptimizedBoard(board);
		if (board.getTricks() < 8) {
			b.setDepthBorder(5); // 2�g���b�N�ȏ�ǂ�
		} else {
System.out.println("�ǂ݂��胂�[�h");
			b.setDepthBorder(100);
		}
		
if (board.getPlayOrder() == Board.LEAD) {
b.calcPropData();
System.out.println("------------���[�h���̘a���A���S���Y���v�Z����-------------");
System.out.println("����Xs (NS)");
for (int i = 0; i < 4; i++) {
	System.out.print("  " + i + ":" + b.calcXs(0, i));
}
System.out.println();

System.out.println("����Xs (EW)");
for (int i = 0; i < 4; i++) {
	System.out.print("  " + i + ":" + b.calcXs(1, i));
}
System.out.println();

System.out.println("calcX(north) = " + b.calcX(0));
System.out.println("calcX(east ) = " + b.calcX(1));
System.out.println("calcX(south) = " + b.calcX(2));
System.out.println("calcX(west ) = " + b.calcX(3));
System.out.println("calcMaxX(EW) = " + b.calcMaxX(1));
System.out.println("calcApproximate = " + b.calcApproximateTricks());
}
		
		int[] bps = b.getBestPlay();
		for (int i = 0; i < bps.length; i++) {
			if (bps[i] == -1) break;
			System.out.print(" " + i + ":" + OptimizedBoard.getCardString(bps[i]));
		}
		System.out.println();
		int bestPlay = bps[0];
		
		int value	= (bestPlay % 14) + 2;
		if (value == 14) value = Card.ACE;
		int suit	= (bestPlay / 14) + 1;
		
		long t = System.currentTimeMillis();
		
		try {
			if ((t - t0) < 800)
				Thread.sleep(800 - (t - t0)); // 400msec �ɂȂ�܂ōl����ӂ�
		} catch (InterruptedException ignored) {
		}
		
		return getHand().peek(suit, value);
	}
}
