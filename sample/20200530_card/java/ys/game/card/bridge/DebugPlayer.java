package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;

/**
 * 可能なプレイを行うコンピュータプレイヤーです。
 * デバッグ用のため、同じハンドについては同じ手をプレイします。
 * ビッドはつねにパスします。ディクレアラーとしてもプレイできます。
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
	 * パスします。
	 *
	 * @return		パス
	 */
	public Bid bid() throws InterruptedException {
		return new Bid(Bid.PASS, 0, 0);
	}
	
	/**
	 * 可能なプレイをランダムに選択し、返却します。
	 *
	 * @return		ランダムなプレイ
	 */
	public Card draw() throws InterruptedException {
//		Thread.sleep(400); // 考えた振り
		
		Board board = getBoard();
		Packet hand = getHand();
		
		Card played = null;
		
		// 混ぜられたハンドの下から順にプレイ可能なカードを検索する
		for (int i = 0; i < hand.size(); i++) {
			played = hand.peek(i);
			if (board.allows(played)) break;
		}
		if (played == null) throw new InternalError();
		
		return played;
	}
	
}
