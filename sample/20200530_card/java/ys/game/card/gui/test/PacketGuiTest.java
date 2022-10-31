import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;

public class PacketGuiTest extends Frame {
	
/*-------------
 * Constructor
 */
	public PacketGuiTest() {
		super("Packet GUI Test");
		pack(); // create peer
		
	}
	
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) {
		PacketGuiTest t = new PacketGuiTest();
		GuiedCard.setCardImageHolder(new LocalCardImageHolder());
		
		Field field = new Field(t, 640, 480);
		
		t.add(field.getCanvas());
		t.pack();
		
		Packet pack = PacketFactory.provideDeck(PacketFactory.WITHOUT_JOKER);
		GuiedPacket p = new GuiedPacket(pack.subpacket(Card.SPADE));
		p.arrange();
		p.setPosition(100, 100);
		p.setDirection(3);
		p.layout();
		
		field.addEntity(p);
		
		t.show();
		
		for (int i = 0; i < 200; i++) {
			p.setX(p.getX() + 3);
			field.repaint();
			try {
				Thread.sleep(100);
				System.in.read();
			}
			catch (Exception e) {
			}
		}
	}
}
