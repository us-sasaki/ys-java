import ys.game.card.bridge.thinking.*;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.*;
import ys.game.card.bridge.*;

public class ShortCutTest {
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
		
		for (int i = 0; i < 4; i++) {
			board.getHand(i).arrange();
		}
		// 前の手と同じ手を与える
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=SQJT6HKQT653CQ98&E=S742H84CAJ6D96543&S=SK98HJ7C432DAKT72&W=SA53HA92CKT75DQJ8&bid=1N-P-P-P&contract=1N&declarer=N", board);
//		Converter.deserializeBoard("status=OPEN&dealer=N&vul=No&N=S9432HQ73CAJDJ432&E=SK865HATC753DQT96&S=SQJ7HK865CT642DA5&W=SATHJ942CKQ98DK87&bid=1N-P-P-P&contract=1N&declarer=N", board);
		System.out.println("はじめの状態");
		System.out.println(board);
		System.out.println(Converter.serialize(board));
		System.out.println();
		
		
		
		OptimizedBoard b = new OptimizedBoard(board);
		long t0 = System.currentTimeMillis();
		int trick = 0;
		for (int i = 0; i < 1000; i++) {
			trick = ShortCut.countApproximateNSWinners(b);
		}
		long te = System.currentTimeMillis();
		System.out.println("盤面評価による NS のトリック数 : " + trick);
		System.out.println("time : " + (te - t0));
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

