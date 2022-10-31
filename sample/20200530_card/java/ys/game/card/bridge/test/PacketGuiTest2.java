import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;

public class PacketGuiTest2 extends Frame {
	
/*-------------
 * Constructor
 */
	public PacketGuiTest2() {
		super("Packet GUI Test");
		pack(); // create peer
		
	}
	
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) {
		PacketGuiTest2 t = new PacketGuiTest2();
		GuiedCard.setCardImageHolder(new LocalCardImageHolder());
		
		Field field = new Field(t, 640, 480);
		
		t.add(field.getCanvas());
		t.pack();
		
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
		
		t.show();
		
		try {
			Thread.sleep(1000);
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
}
