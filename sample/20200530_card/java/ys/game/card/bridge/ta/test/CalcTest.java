import ys.game.card.bridge.ta.*;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.*;
import ys.game.card.bridge.*;

/**
 * 同格カードを除く処理がきちんとできているか確認する
 */
public class CalcTest {
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
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=S9432HQ73CAJDJ432&E=SK865HATC753DQT96&S=SQJ7HK865CT642DA5&W=SATHJ942CKQ98DK87&bid=1N-P-P-P&contract=1N&declarer=N", board);
		System.out.println("はじめの状態");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		OptimizedBoard b = null;
		
		// 手を進める
		for (int x = 0; x < 3 * 4; x++) {
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
			b = new OptimizedBoard(board);
		
		System.out.println(b);
		
		//
		// 状態の表示
		//
		System.out.println("calcPropData()");
		b.calcPropData();
		
		System.out.println("suitCount");
		printArray(b.suitCount);
		
		System.out.println("totalWinners");
		printArray(b.totalWinners);
		
		System.out.println("longerLength");
		printArray(b.longerLength);
		
		System.out.println("shorterLength");
		printArray(b.shorterLength);
		
		System.out.println("lowestCard");
		printArrayC(b.lowestCard);
		
		System.out.println("highestCard");
		printArrayC(b.highestCard);
		
		System.out.println("lowestCardOfShorterSuit");
		printArrayC(b.lowestCardOfShorterSuit);
		
		System.out.println("highestCardOfLongerSuit");
		printArrayC(b.highestCardOfLongerSuit);
		
		System.out.println("isWinner");
		for (int i = 0; i < b.isWinner.length; i++) {
			if (!b.isWinner[i]) continue;
			System.out.print(OptimizedBoard.getCardString(i) + ",");
		}
		System.out.println();
		
		System.out.println("Xs (NS)");
		for (int i = 0; i < 4; i++) {
			System.out.print("  " + i + ":" + b.calcXs(0, i));
		}
		System.out.println();
		
		System.out.println("Xs (EW)");
		for (int i = 0; i < 4; i++) {
			System.out.print("  " + i + ":" + b.calcXs(1, i));
		}
		System.out.println();
		
		System.out.println("calcX(north) = " + b.calcX(0));
		System.out.println("calcMaxX(EW) = " + b.calcMaxX(1));
	}
	
	
		

	private static void printArray(int[][] a) {
		for (int j = 0; j < a.length; j++) {
			for (int i = 0; i < a[j].length; i++) {
				System.out.print("[" + j + "]["+i+"]=" + a[j][i] + ", ");
			}
			System.out.println();
		}
	}
	private static void printArrayC(int[][] a) {
		for (int j = 0; j < a.length; j++) {
			for (int i = 0; i < a[j].length; i++) {
				System.out.print("[" + j + "]["+i+"]=" + OptimizedBoard.getCardString(a[j][i]) + ", ");
			}
			System.out.println();
		}
	}
}

