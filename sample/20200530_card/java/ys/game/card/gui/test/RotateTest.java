import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;
import ys.game.card.gui.*;

public class RotateTest extends Frame {
	Image ori, rot;
	Canvas c;
	
	public RotateTest() throws Exception {
		super("Test");
		pack();	// create peer
		
		setSize(640,480);
		
		MediaTracker mt = new MediaTracker(this);
		Toolkit t = Toolkit.getDefaultToolkit();
		ori = t.getImage("images/s1.gif");
		mt.addImage(ori, 0);
		mt.waitForAll();
		
		RotateImageFilter r = new RotateImageFilter();
		
		rot = createImage(new FilteredImageSource(ori.getSource(), r) );
		c = new ImageCanvas(ori, rot);
		add(c);
		pack();
	}
	
	public static void main(String[] args) throws Exception {
		RotateTest f = new RotateTest();
		
		f.show();
		
		while (true) {
			f.c.repaint();
			Thread.sleep(50);
		}
	}
}

class ImageCanvas extends Canvas {
	Image ori, rot;
	
	public ImageCanvas(Image ori, Image rot) {
		this.ori = ori;
		this.rot = rot;
		setSize(600, 400);
		
	}
	
	public void update(Graphics g) {
		paint(g);
	}
	
	public void paint(Graphics g) {
		Rectangle r = getBounds();
		
		g.setColor(Color.black);
		
		g.fillRect(r.x, r.y, r.width, r.height);
		
		g.drawImage(ori, 100, 100, this);
		g.drawImage(rot, 160, 100, this);
	}
}
