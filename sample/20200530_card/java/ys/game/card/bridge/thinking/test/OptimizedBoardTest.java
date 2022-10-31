import ys.game.card.bridge.thinking.*;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.*;
import ys.game.card.bridge.*;

public class OptimizedBoardTest {
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
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=SQJT6HKQT653CQ98&E=S742H84CAJ6D96543&S=SK98HJ7C432DAKT72&W=SA53HA92CKT75DQJ8&bid=1N-P-P-P&contract=1N&declarer=N", board);
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=S9432HQ73CAJDJ432&E=SK865HATC753DQT96&S=SQJ7HK865CT642DA5&W=SATHJ942CKQ98DK87&bid=1N-P-P-P&contract=1N&declarer=N", board);
		System.out.println("はじめの状態");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		
		
		OptimizedBoard b = new OptimizedBoard(board);
		
		int[] list;
		
		//
		// 攪拌
		//
		for (int i = 1; i < 52; i++) {
			for (int n = 0; n < 1000; n++) {
				for (int j = 0; j < i; j++) b.play(b.listOptions()[0]);
				for (int j = 0; j < i; j++) b.undo();
			}
		}
		for (int i = 0; i < 52; i++) {
			System.out.println("プレイリスト");
			list = b.listOptions();
			print(list);
			System.out.println("leader = " + DIR[b.leader[b.tricks]]);
			System.out.println("winner = " + DIR[b.leader[b.tricks]]);
		
			b.play(list[0]);
		}
		b.undo();
			System.out.println("プレイリスト");
			list = b.listOptions();
			print(list);
			System.out.println("leader = " + DIR[b.leader[b.tricks]]);
			System.out.println("winner = " + DIR[b.leader[b.tricks]]);
		
			b.play(list[0]);
		
	}
	public static final String[] DIR = new String[] { "N","E","S","W"};
	public static final String[] SUIT = new String[] { "C", "D", "H", "S", "*" };
	public static final String[] VALUE =
			new String[] { "2","3","4","5","6","7","8","9","T","J","Q","K","A"};
	
	
	private static void print(int[] t) {
		for (int i = 0; i < t.length; i++) {
			if (t[i] == -1) break;
			System.out.print(SUIT[t[i]/14]+VALUE[t[i]%14]);
		}
		System.out.println();
	}
}

