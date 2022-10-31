import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;


public class BoardGuiTest extends Frame {
	
/*-------------
 * Constructor
 */
	public BoardGuiTest() {
		super("Board GUI Test");
		pack(); // create peer
		
	}
	
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) {
		BoardGuiTest t = new BoardGuiTest();
		GuiedCard.setCardImageHolder(new LocalCardImageHolder());
		
		BridgeField field = new BridgeField(t);
		
		t.add(field.getCanvas());
		t.pack();
		
		GuiedBoard board = new GuiedBoard(new BoardImpl(1));
		
		t.show();
		
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// ディール
		//
		board.deal();
		
		sleep(300);
		board.layout();
		field.repaint();
		
		//
		// ビッドを行う
		//
/*		board.play(new Bid(Bid.BID, 1, Bid.CLUB));
		board.play(new Bid(Bid.PASS, 0, 0));
		board.play(new Bid(Bid.PASS, 0, 0));
		board.play(new Bid(Bid.DOUBLE, 1, Bid.CLUB));
		board.play(new Bid(Bid.REDOUBLE, 1, Bid.CLUB));
		board.play(new Bid(Bid.PASS, 0, 0));
		board.play(new Bid(Bid.BID, 1, Bid.SPADE));
		board.play(new Bid(Bid.PASS, 0, 0));
		board.play(new Bid(Bid.BID, 1, Bid.NO_TRUMP));
		board.play(new Bid(Bid.PASS, 0, 0));
		board.play(new Bid(Bid.PASS, 0, 0));
		board.play(new Bid(Bid.PASS, 0, 0));*/

		board.setContract(new Bid(Bid.BID, 1, Bid.NO_TRUMP), Board.SOUTH);
		
		while (true) {
			int st = board.getStatus();
			int pl = board.getTurn();
			ys.game.card.Packet hand = board.getHand(pl);
			
			hand.shuffle();
			ys.game.card.Card c = null;
			for (int i = 0; i < hand.size(); i++) {
				c = hand.peek(i);
				if (board.allows(c)) break;
			}
			if (c == null) throw new RuntimeException("????");
			hand.arrange();
			
			board.play(c);
			
			if (board.getStatus() == Board.SCORING) break;
			
			sleep(100);
			board.layout();
			field.repaint();
		}
	}
	
	private static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ignored) {
		}
	}
}
