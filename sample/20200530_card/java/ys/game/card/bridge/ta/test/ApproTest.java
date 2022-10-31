import ys.game.card.bridge.ta.*;

import ys.game.card.Card;
import ys.game.card.Packet;
import ys.game.card.PacketImpl;
import ys.game.card.*;
import ys.game.card.bridge.*;

public class ApproTest {
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) throws Exception {
	
	for (int a = 0; a < 100; a++) {
		Board board = new BoardImpl(1);
		board.deal();
		Packet[] hand = board.getHand();
		for (int i = 0; i < hand.length; i++) hand[i].turn(true);
		OptimizedBoard ob;
		Board b;
		
		// NT
		b = new BoardImpl(1);
		b.deal(hand);
		b.setContract(new Bid(Bid.BID, 1, Bid.NO_TRUMP), Board.SOUTH); // 1NT
		for (int i = 0; i < 4; i++) b.getHand()[i].turn(true);
		ob = new OptimizedBoard(b);
		System.out.println("---ボード情報---");
		System.out.println(b.toText());
		System.out.println("calcApproximateTricks(NT) : " + ob.calcApproximateTricks());
		
		// SPADE
		b = new BoardImpl(1);
		b.deal(hand);
		b.setContract(new Bid(Bid.BID, 1, Bid.SPADE), Board.SOUTH); // 1S
		for (int i = 0; i < 4; i++) b.getHand()[i].turn(true);
		ob = new OptimizedBoard(b);
//		System.out.println("---ボード情報---");
//		System.out.println(b.toText());
		System.out.println("calcApproximateTricks(S ) : " + ob.calcApproximateTricks());
		
		// HEART
		b = new BoardImpl(1);
		b.deal(hand);
		b.setContract(new Bid(Bid.BID, 1, Bid.HEART), Board.SOUTH); // 1H
		for (int i = 0; i < 4; i++) b.getHand()[i].turn(true);
		ob = new OptimizedBoard(b);
//		System.out.println("---ボード情報---");
//		System.out.println(b.toText());
		System.out.println("calcApproximateTricks(H ) : " + ob.calcApproximateTricks());
		
		// DIAMOND
		b = new BoardImpl(1);
		b.deal(hand);
		b.setContract(new Bid(Bid.BID, 1, Bid.DIAMOND), Board.SOUTH); // 1D
		for (int i = 0; i < 4; i++) b.getHand()[i].turn(true);
		ob = new OptimizedBoard(b);
//		System.out.println("---ボード情報---");
//		System.out.println(b.toText());
		System.out.println("calcApproximateTricks(D ) : " + ob.calcApproximateTricks());
		
		// CLUB
		b = new BoardImpl(1);
		b.deal(hand);
		b.setContract(new Bid(Bid.BID, 1, Bid.CLUB), Board.SOUTH); // 1C
		for (int i = 0; i < 4; i++) b.getHand()[i].turn(true);
		ob = new OptimizedBoard(b);
//		System.out.println("---ボード情報---");
//		System.out.println(b.toText());
		System.out.println("calcApproximateTricks(C ) : " + ob.calcApproximateTricks());
		
}		
	}
}

