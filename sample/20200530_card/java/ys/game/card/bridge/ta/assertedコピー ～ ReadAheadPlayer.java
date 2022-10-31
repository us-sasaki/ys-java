package ys.game.card.bridge.ta;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.bridge.Player;
import ys.game.card.bridge.Board;
import ys.game.card.bridge.Bid;
import ys.game.card.bridge.IllegalStatusException;
import ys.game.card.bridge.SimplePlayer2;

/**
 * ダブルダミー状態で、最後まで読みきって最善手を打つプレイヤー。
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
	 * パスします。
	 *
	 * @return		パス
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * 可能なプレイをランダムに選択し、返却します。
	 * ただし、残り５トリック以上は時間がかかりすぎるため、SimplePlayer2 が使用されます。
	 *
	 * @return		最善手
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
			b.setDepthBorder(5); // 2トリック以上読む
		} else {
System.out.println("読みきりモード");
			b.setDepthBorder(100);
		}
		
if (board.getPlayOrder() == Board.LEAD) {
b.calcPropData();
System.out.println("------------リード時の和美アルゴリズム計算結果-------------");
System.out.println("●●Xs (NS)");
for (int i = 0; i < 4; i++) {
	System.out.print("  " + i + ":" + b.calcXs(0, i));
}
System.out.println();

System.out.println("●●Xs (EW)");
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
				Thread.sleep(800 - (t - t0)); // 400msec になるまで考えるふり
		} catch (InterruptedException ignored) {
		}
		
		return getHand().peek(suit, value);
	}
}
