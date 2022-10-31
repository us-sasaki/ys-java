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
	protected SimplePlayer2 base;
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
		
		if (board.getTricks() < 13-6)
			return base.draw();
		if (board.getTricks() == 13-6) {
			if (board.getTrick().size() < 1) return base.draw();
		}
		
		long t0 = System.currentTimeMillis();
		
		OptimizedBoard b = new OptimizedBoard(board);
		Conclusion2 c = Conclusion2.bestPlayOf(b);
System.out.print("best play(original) = ");
for (int i = 0; i < c.bestPlayCount; i++) {
	System.out.print(OptimizedBoard.toString(c.bestPlays[i]));
}
System.out.println();

		int[] bps = b.getEqualCards(c.bestPlays, c.bestPlayCount);
		
System.out.print("best play(Equally) = ");
for (int i = 0; i < bps.length; i++) {
	System.out.print(OptimizedBoard.toString(bps[i]));
}
System.out.println();
		int bestPlay = bps[bps.length - 1]; // ���[�G�X�g
		
		int value	= (bestPlay % 14) + 2;
		if (value == 14) value = Card.ACE;
		int suit	= (bestPlay / 14) + 1;
		
		long t = System.currentTimeMillis();
		
		try {
			if ((t - t0) < 400)
				Thread.sleep(400 - (t - t0)); // 400msec �ɂȂ�܂ő҂����ӂ�
		} catch (InterruptedException ignored) {
		}
		
		return getHand().peek(suit, value);
	}
	
}
