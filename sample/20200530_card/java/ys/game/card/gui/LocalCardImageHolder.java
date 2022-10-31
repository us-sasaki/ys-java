package ys.game.card.gui;

import java.awt.*;
import java.awt.image.*;

import ys.game.card.*;

public class LocalCardImageHolder implements CardImageHolder {
	protected static final String IMAGE_DIR =
					"G:\\programs\\ys\\game\\card\\gui\\images\\";
	
	static Image[][]	IMAGE;
	static Image[][]		BACK_IMAGE;
	int			backImageNum = 0;
	
	static {
		MediaTracker mt = new MediaTracker(new Canvas());
		
		Toolkit t = Toolkit.getDefaultToolkit();
		Frame f = new Frame();
		f.pack();
//		f.setBackground(new Color(0, 0, 0, 0));
		
		IMAGE		= new Image[4][53];
		BACK_IMAGE	= new Image[4][4];
		Image tmp;
		
		try {
		
		for (int i = 0; i < 13; i++) {
			tmp = t.getImage(IMAGE_DIR+"c" + (i+1) + ".gif");
			mt.addImage(tmp, i);
			mt.waitForAll();
			IMAGE[0][i] = f.createImage(GuiedCard.XSIZE, GuiedCard.YSIZE);
			IMAGE[0][i].getGraphics().drawImage(tmp, 0, 0, null);
		}
		for (int i = 0; i < 13; i++) {
			tmp = t.getImage(IMAGE_DIR+"d" + (i+1) + ".gif");
			mt.addImage(tmp, i+13);
			mt.waitForAll();
			IMAGE[0][i + 13] = f.createImage(GuiedCard.XSIZE, GuiedCard.YSIZE);
			IMAGE[0][i + 13].getGraphics().drawImage(tmp, 0, 0, null);
		}
		for (int i = 0; i < 13; i++) {
			tmp = t.getImage(IMAGE_DIR+"h" + (i+1) + ".gif");
			mt.addImage(tmp, i+26);
			mt.waitForAll();
			IMAGE[0][i + 26] = f.createImage(GuiedCard.XSIZE, GuiedCard.YSIZE);
			IMAGE[0][i + 26].getGraphics().drawImage(tmp, 0, 0, null);
		}
		for (int i = 0; i < 13; i++) {
			tmp = t.getImage(IMAGE_DIR+"s" + (i+1) + ".gif");
			mt.addImage(tmp, i+39);
			mt.waitForAll();
			IMAGE[0][i + 39] = f.createImage(GuiedCard.XSIZE, GuiedCard.YSIZE);
			IMAGE[0][i + 39].getGraphics().drawImage(tmp, 0, 0, null);
		}
		for (int i = 0; i < 4; i++) {
			tmp = t.getImage(IMAGE_DIR+"back"+i+".gif");
			mt.addImage(tmp, 53);
			mt.waitForAll();
			BACK_IMAGE[0][i] = f.createImage(GuiedCard.XSIZE, GuiedCard.YSIZE);
			BACK_IMAGE[0][i].getGraphics().drawImage(tmp, 0, 0, null);
		}
		
		} catch (InterruptedException ignored) {
		}
		// joker の絵はまだない
		IMAGE[0][52] = IMAGE[0][0];
		
		RotateImageFilter rot = new RotateImageFilter();
		
		int xsize = GuiedCard.YSIZE;
		int ysize = GuiedCard.XSIZE;
		
		for (int direction = 1; direction < 4; direction++) {
			for (int i = 0; i < 53; i++) {
				Image ori = IMAGE[direction - 1][i];
				tmp = t.createImage(new FilteredImageSource(ori.getSource(), rot) );
				IMAGE[direction][i] = f.createImage(tmp.getWidth(null), tmp.getHeight(null)); //xsize, ysize);
				IMAGE[direction][i].getGraphics().drawImage(tmp, 0, 0, null);
				
				int sw = xsize; xsize = ysize; ysize = sw;
			}
			for (int i = 0; i < 4; i++) {
				Image ori = BACK_IMAGE[direction - 1][i];
				tmp = t.createImage(new FilteredImageSource(ori.getSource(), rot) );
				BACK_IMAGE[direction][i] = f.createImage(tmp.getWidth(null), tmp.getHeight(null)); //xsize, ysize);
				BACK_IMAGE[direction][i].getGraphics().drawImage(tmp, 0, 0, null);
				
				int sw = xsize; xsize = ysize; ysize = sw;
			}
			
		}
	}
	
/*-------------
 * Constructor
 */
	public LocalCardImageHolder() {
	}
	
/*-----------------------------
 * implements(CardImageHolder)
 */
	/**
	 * スートとバリュー、向きを指定してカードの Image を取得します。
	 *
	 * @param		suit		スート
	 * @param		value		バリュー
	 * @param		direction	向き
	 *
	 * @return		カードのイメージ
	 */
	public Image getImage(int suit, int value, int direction) {
		if (suit == Card.JOKER) return IMAGE[direction][52];
		return IMAGE[direction][ (suit-1)*13 + value-1 ];
	}
	
	/**
	 * 向きを指定して、カード背面の Image を取得します。
	 *
	 * @param		direction	向き
	 *
	 * @return		カード背面のイメージ
	 */
	public Image getBackImage(int direction) {
		return BACK_IMAGE[direction][backImageNum];
	}
	
	/**
	 * Image に接続されている Graphics オブジェクトのリソースを開放します。
	 */
	public void dispose() {
		if (IMAGE != null) {
			for (int i = 0; i < IMAGE.length; i++) {
				for (int j = 0; j < IMAGE[i].length; j++) {
					if (IMAGE[i][j] != null) {
						Graphics g = IMAGE[i][j].getGraphics();
						if (g != null) g.dispose();
					}
				}
			}
		}
		if (BACK_IMAGE != null) {
			for (int i = 0; i < BACK_IMAGE.length; i++) {
				for (int j = 0; j < BACK_IMAGE[i].length; j++) {
					if (BACK_IMAGE[i][j] != null) {
						Graphics g = BACK_IMAGE[i][j].getGraphics();
						if (g != null) g.dispose();
					}
				}
			}
		}
	}
	
	/**
	 *
	 */
	public void setBackImage(int num) {
		backImageNum = num;
	}
	
}
