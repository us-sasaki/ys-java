package ys.game.card.bridge;

import ys.game.card.Card;
import ys.game.card.Packet;

/**
 * 可能なプレイをランダムに行うコンピュータプレイヤーです。
 * ビッドはつねにパスします。ディクレアラーとしてもプレイできます。
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
		
		// プレイすべきハンドを混ぜる
		hand.shuffle();
		Card played = null;
		
		// 混ぜられたハンドの下から順にプレイ可能なカードを検索する
		for (int i = 0; i < hand.size(); i++) {
			played = hand.peek(i);
			if (board.allows(played)) break;
		}
		if (played == null) throw new InternalError();
		
		// ハンドを戻しておく
		hand.arrange();
		
		return played;
	}
	
}
