import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;

public class AppTest2 extends AppletCardImageHolder implements Runnable {
	
	Thread runner = null;
	Field field;
	GuiedCard c;
	
	public void init() {
		super.init();
		
		field = new Field(this, 640, 480);
		
		add(field.getCanvas());
		
		c = new GuiedCard(Card.SPADE, Card.ACE);
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
	}
	
	public void start() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}
	
	public void stop() {
		if (runner != null) {
			runner.stop();
			runner = null;
		}
	}
	
	public void run() {
		for (int i = 0; i < 200; i++) {
			c.setX(c.getX() + 4);
			field.draw();
			try {
				Thread.sleep(120);
			}
			catch (InterruptedException e) {
			}
//			repaint();
			field.repaint();
		}
	}
	
//	public void update(Graphics g) {
//		paint(g);
//	}
//	
//	public void paint(Graphics g) {
//		field.getCanvas().paint(g);
//	}
}
