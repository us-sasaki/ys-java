import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;


public class BGT3 extends Frame {
	
/*-------------
 * Constructor
 */
	public BGT3() {
		super("Board GUI Test");
		
		setLayout(new FlowLayout());
		pack(); // create peer
		
	}
	
/*------------------
 * デバッグ用メイン
 */
	public static void main(String[] args) {
		BGT3 t = new BGT3();
		t.setSize(640, 500);
		GuiedCard.setCardImageHolder(new LocalCardImageHolder());
		Explanation.media = new LocalMediaLoader();
		
		PlayMain main = new PracticeModePlayMain(t);
		main.initialize();
		t.pack();
		while (true) {
			main.start();
			main.dispose();
		}
	}
}
