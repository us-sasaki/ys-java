import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;


public class AppTest extends AppletCardImageHolder {
	
	public void start() {
		Field field = new Field(this, 640, 480);
		
		add(field.getCanvas());
		
		Packet pack = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		GuiedPacket p = new GuiedPacket(pack.subpacket(Card.HEART));
		GuiedPacket q = new GuiedPacket(pack.subpacket(Card.CLUB));
		field.addEntity(p);
		field.addEntity(q);
		
		p.arrange();
		q.arrange();
		p.setPosition(100, 100);
		q.setPosition(120, 380);
		p.setDirection(3);
		q.setDirection(0);
		
		try {
			Thread.sleep(10000);
		}
		catch (InterruptedException e) {
		}
		
		p.redraw();
		q.redraw();
		field.repaint();
		
		q.add(p.draw());
		p.layout();
		q.layout();
		field.repaint();
	}
	
	public void update(Graphics g) {
		paint(g);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
	}
}
