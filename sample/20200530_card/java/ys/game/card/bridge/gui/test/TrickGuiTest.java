import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;


public class TrickGuiTest extends Frame {
	
/*-------------
 * Constructor
 */
	public TrickGuiTest() {
		super("Trick GUI Test");
		pack(); // create peer
		
	}
	
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) {
		TrickGuiTest t = new TrickGuiTest();
		GuiedCard.setCardImageHolder(new LocalCardImageHolder());
		
		Field field = new Field(t, 640, 480);
		
		t.add(field.getCanvas());
		t.pack();
		
		GuiedPacket pack = new GuiedPacket(PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER));
		field.addEntity(pack);
		pack.setPosition(0, 30);
		
		t.show();
		
		for (int j = 0; j < 13; j++) {
			GuiedTrick p = new GuiedTrick(j/4, Bid.NO_TRUMP);
			field.addEntity(p);
			
			p.setPosition(100, 100);
			p.setDirection(j % 4);
			
			for (int i = 0; i < 4; i++) {
				
				try {
						Thread.sleep(300);
				}
				catch (InterruptedException e) {
				}
				
				Card card = pack.draw();
				
				p.add(card);
				p.layout();
				
				field.repaint();
			}
			try {
					Thread.sleep(300);
			}
			catch (InterruptedException e) {
			}
			GuiedCard win = (GuiedCard)(p.getWinnerCard());
			
			win.setPosition(500, 200);
			field.addEntity(win);
			field.removeEntity(p);
			field.repaint();
		}
		
	}
}
