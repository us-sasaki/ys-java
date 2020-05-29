import ys.game.card.bridge.ta.*;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.*;
import ys.game.card.bridge.*;

/**
 * OptimizedBoard の先読み処理の状況確認用
 */
public class ReadAheadTest {
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) throws Exception {
		Board board = new BoardImpl(1);
		board.deal();
		
		board.play(new Bid(Bid.BID, 1, Bid.CLUB));
		board.play(new Bid(Bid.PASS));
		board.play(new Bid(Bid.PASS));
		board.play(new Bid(Bid.PASS));
		
		// 前の手と同じ手を与える
		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=SQJT6HKQT653CQ98&E=S742H84CAJ6D96543&S=SK98HJ7C432DAKT72&W=SA53HA92CKT75DQJ8&bid=1C-P-P-P&contract=1N&declarer=N", board);
		System.out.println("はじめの状態");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		// 手を進める
		for (int x = 0; x < 6 * 4; x++) {
			Packet hand = board.getHand(board.getTurn());
			
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
			
			board.play(played);
		}
		
		System.out.println("手を進めた状態");
		System.out.println(board);
		
		ReadAheadPlayer rap = new ReadAheadPlayer(board, board.getTurn());
		
		long t0, t;
		t0 = System.currentTimeMillis();
		Card c = rap.draw();
		t = System.currentTimeMillis();
		
		System.out.println("所要時間(msec) " + (t-t0));
		System.out.println("ReadAheadPlayerの選んだプレイ: " + c);
		
	}
	
}

