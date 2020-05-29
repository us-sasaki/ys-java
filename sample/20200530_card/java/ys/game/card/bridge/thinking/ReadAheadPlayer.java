package ys.game.card.bridge.thinking;

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
		
//		if (board.getTricks() == 13-6) {
//			if (board.getTrick().size() < 1) return base.draw();
//		}
		
		long t0 = System.currentTimeMillis();
		
		OptimizedBoard b = new OptimizedBoard(board);
int[] option = ShortCut.listOptions(b);
System.out.print("options = ");
for (int i = 0; i < option.length; i++) {
	if (option[i] == -1) break;
	System.out.print(OptimizedBoard.toString(option[i]));
}
System.out.println();
System.out.println("b.nsWins = " + b.nsWins);
		Conclusion2 c = Conclusion2.bestPlayOf(b);
System.out.print("best play(original) = ");
for (int i = 0; i < c.bestPlayCount; i++) {
	System.out.print(OptimizedBoard.toString(c.bestPlays[i]));
}
System.out.println();

		int[] bps = ShortCut.getEqualCards(b, c.bestPlays, c.bestPlayCount);
		
System.out.print("best play(Equally) = ");
for (int i = 0; i < bps.length; i++) {
	System.out.print(OptimizedBoard.toString(bps[i]));
}
System.out.println();
if (b.trickCount[b.tricks] == 0) {
int leader = b.leader[b.tricks];
System.out.println("���̃{�[�h�� ns ���g���b�N���ς��� = " + ShortCut.countApproximateNSWinners(b));
System.out.println("         leader Quick Tricks       = " + ShortCut.countApproximateWinners(b, leader));
System.out.println("         R.H.O. Quick Tricks       = " + ShortCut.countApproximateWinners(b, (leader+1)%4));
System.out.println("         L.H.O. Quick Tricks       = " + ShortCut.countApproximateWinners(b, (leader+3)%4));
System.out.println("         b.nsWins                  = " + b.nsWins);
System.out.println("         b.tricks                  = " + b.tricks);
}
		int bestPlay = bps[bps.length - 1]; // ���[�G�X�g
		
		int value	= (bestPlay % 14) + 2;
		if (value == 14) value = Card.ACE;
		int suit	= (bestPlay / 14) + 1;
		
		long t = System.currentTimeMillis();
		
		try {
			if ((t - t0) < 400)
				Thread.sleep(400 - (t - t0)); // 400msec �ɂȂ�܂ōl����ӂ�
		} catch (InterruptedException ignored) {
		}
		
		return getHand().peek(suit, value);
	}
	
}
