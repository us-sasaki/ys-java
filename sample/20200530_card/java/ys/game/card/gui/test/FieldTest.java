import java.awt.*;

import ys.game.card.gui.*;

public class FieldTest {
	public static void main(String[] args) {
		Frame fr = new Frame("Field Test");
		Panel pa = new Panel();
		fr.add(pa);
		fr.pack();
		
		Field f = new Field(fr, 640, 480);
		Entity[] e = new Entity[10];
		
		for (int i = 0; i < e.length; i++) {
			e[i] = new Entity();
			e[i].setPosition(20, i * 30 + 30);
			f.addEntity(e[i]);
		}
		
		pa.add(f.getCanvas());
		fr.pack();
		fr.show();
		
		while (true) {
			for (int i = 0; i < e.length; i++) {
				e[i].setX(e[i].getX()+1);
			}
			f.repaint();
			try {
				Thread.sleep(20);
			}
			catch(InterruptedException ie) {
			}
		}
	}
}
