import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;


public class BoardManagerTest extends Frame {
	
/*-------------
 * Constructor
 */
	public BoardManagerTest() {
		super("BoardManager Test");
		pack(); // create peer
		
	}
	
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) {
		BoardManagerTest t = new BoardManagerTest();
		GuiedCard.setCardImageHolder(new LocalCardImageHolder());
		
//		TrickAnimation.setBlinkWaitTime(0);
//		TrickAnimation.setBlinkCount(0);
//		TrickAnimation.setTurnWaitTime(0);
		
		BoardManager bm = new BoardManager(t); // 自動的にスタートするはず
		
		t.pack();
		t.show();
		
		try {
			Thread.sleep(500L);
		} catch (InterruptedException ie) {
		}
		
//		bm.setPlayer(bm.getHumanPlayerInstance(), Board.NORTH);
		bm.setPlayer(new RandomPlayer(), Board.NORTH);
		bm.setPlayer(new RandomPlayer(), Board.EAST);
//		bm.setPlayer(bm.getHumanPlayerInstance(), Board.SOUTH);
		bm.setPlayer(new RandomPlayer(), Board.SOUTH);
		bm.setPlayer(new RandomPlayer(), Board.WEST);
		
		bm.getBoard().getHand(Board.EAST).turn(true);
	}
	
	private static void sleep(long time) {
	}
}
