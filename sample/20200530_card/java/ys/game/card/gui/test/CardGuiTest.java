import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;

public class CardGuiTest extends Frame {
	
/*-------------
 * Constructor
 */
	public CardGuiTest() {
		super("Card GUI Test");
		pack(); // create peer
		
	}
	
	
	public static void main(String[] args) {
		CardGuiTest t = new CardGuiTest();
		GuiedCard.setCardImageHolder(new LocalCardImageHolder());
		
		Field field = new Field(t, 640, 480);
		
		t.add(field.getCanvas());
		t.pack();
		
		GuiedCard c = new GuiedCard(Card.SPADE, Card.ACE);
		c.setPosition(10, 50);
		field.addEntity(c);
		
		for (int i = 1; i < 5; i++) {
			for (int j = 1; j < 14; j++) {
				GuiedCard c2 = new GuiedCard(i, j);
				int x = (int)(Math.random() * 600 + 20);
				int y = (int)(Math.random() * 400 + 40);
				int dir = (int)(Math.random() * 4);
				c2.setPosition(x, y);
				field.addEntity(c2);
				c2.setDirection(dir);
			}
		}
		t.show();
		
		for (int i = 0; i < 200; i++) {
			c.setX(c.getX() + 3);
			field.repaint();
			try {
				Thread.sleep(20);
			}
			catch (InterruptedException e) {
			}
		}
		t.dispose();
		System.exit(0);
	}
}