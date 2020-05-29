package ys.game.card.gui;

import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;
import java.util.Enumeration;

import ys.game.card.*;
import ys.game.card.bridge.gui.*;

public class AppletCardImageHolder extends java.applet.Applet implements CardImageHolder, MediaLoader {
	protected Image[][]		image;
	protected Image[][]		backImage;
	protected int			backImageNum;
	
	protected Hashtable table;

	private static final int CARDW = 59;
	private static final int CARDH = 87;
	private static final int SUMIREW = 66;
	private static final int SUMIREH = 97;
	private static final int CANW = (CARDW * 14 + SUMIREW);
	private static final int CANH = CARDH * 4;

	public void init() {
		//
		// Card Image を読み込む
		//
//System.out.println("1");
		MediaTracker mt = new MediaTracker(this);
		
		image = new Image[4][53];
		backImage = new Image[4][4];
		java.net.URL url = getCodeBase();
		
		// 一枚の絵から切り取る
		Image canvas = getImage(url, "images/images.gif");
		prepareImage(canvas, this);
		mt.addImage(canvas, 0);
		try {
			mt.waitForAll();
		}
		catch (InterruptedException ignored) {
		}
		
		int[] data = new int[CANW * CANH];
		PixelGrabber pg = new PixelGrabber(canvas, 0, 0, CANW, CANH, data, 0, CANW);
		try { pg.grabPixels(); }
		catch (InterruptedException e) { System.out.println(e); }

		for (int s = 0; s < 4; s++) {
			for (int i = 0; i < 13; i++) {
				image[0][s*13+i] = new BufferedImage(CARDW, CARDH, BufferedImage.TYPE_INT_ARGB);
				((BufferedImage)image[0][s*13+i]).setRGB(0, 0, CARDW, CARDH, data, i*CARDW+s*CANW*CARDH, CANW);
			}
			backImage[0][s] = new BufferedImage(CARDW, CARDH, BufferedImage.TYPE_INT_ARGB);
			((BufferedImage)backImage[0][s]).setRGB(0, 0, CARDW, CARDH, data, 13*CARDW+s*CANW*CARDH, CANW);
		}

		// joker の絵はまだない
		image[0][52] = image[0][0];
		
		// 回転
//System.out.println("6");
		RotateImageFilter		rot = new RotateImageFilter();
		UpsideDownImageFilter	ups = new UpsideDownImageFilter();
		RightRotateImageFilter	rig = new RightRotateImageFilter();
		
//System.out.println("7" + "   [" + new java.util.Date() + "]");
		for (int i = 0; i < 53; i++) {
//System.out.println("71");
			Image ori = image[0][i];
//System.out.println("72" + ori + " ///" + ori.getSource());
			image[1][i] = createImage(new FilteredImageSource(ori.getSource(), rot) );
			mt.addImage(image[1][i], 1);
//System.out.println("73");
			image[2][i] = createImage(new FilteredImageSource(ori.getSource(), ups) );
			mt.addImage(image[2][i], 1);
//System.out.println("74");
			image[3][i] = createImage(new FilteredImageSource(ori.getSource(), rig) );
			mt.addImage(image[3][i], 1);
		}
//System.out.println("8" + "   [" + new java.util.Date() + "]");
		for (int i = 0; i < 4; i++) {
			Image backori = backImage[0][i];
			backImage[1][i] = createImage(new FilteredImageSource(backori.getSource(), rot) );
			mt.addImage(backImage[1][i], 1);
			backImage[2][i] = createImage(new FilteredImageSource(backori.getSource(), ups) );
			mt.addImage(backImage[2][i], 1);
			backImage[3][i] = createImage(new FilteredImageSource(backori.getSource(), rig) );
			mt.addImage(backImage[3][i], 1);
		}
		
		GuiedCard.setCardImageHolder(this);
//System.out.println("9" + "   [" + new java.util.Date() + "]");
		backImageNum = 0;
		
		//
		// Sumire の絵を読み込む
		//
		table = new Hashtable();
		BufferedImage tmp;
		
		for (int i = 0; i < 3; i++) {
			tmp = new BufferedImage(SUMIREW, SUMIREH, BufferedImage.TYPE_INT_ARGB);
			tmp.setRGB(0, 0, SUMIREW, SUMIREH, data, 14*CARDW+i*CANW*SUMIREH, CANW);
			table.put("sumire"+i, tmp);
		}
//System.out.println("10" + "   [" + new java.util.Date() + "]");
		Explanation.media = this;
		
		try {
			mt.waitForAll();
		}
		catch (InterruptedException ignored) {
		}
	}
	
	public void destroy() {
		super.destroy();
		dispose();
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
		if (suit == Card.JOKER) return image[direction][52];
		return image[direction][ (suit-1)*13 + value-1 ];
	}
	
	/**
	 * 向きを指定して、カード背面の Image を取得します。
	 *
	 * @param		direction	向き
	 *
	 * @return		カード背面のイメージ
	 */
	public Image getBackImage(int direction) {
		return backImage[direction][backImageNum];
	}
	
	/**
	 * 本オブジェクトが保持しているイメージリソースを開放する。
	 */
	public void dispose() {
		// 本来不要な以下の dispose() について、エラーが発生するため
		// コメントアウトする(2002/9/17)
/*		if (image != null) {
			for (int i = 0; i < image.length; i++) {
				for (int j = 0; j < image[i].length; j++) {
					if (image[i][j] != null) {
						try {
							Graphics g = image[i][j].getGraphics();
							if (g != null) g.dispose();
							image[i][j] = null;
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		if (backImage != null) {
			for (int i = 0; i < backImage.length; i++) {
				for (int j = 0; j < backImage[i].length; j++) {
					if (backImage[i][j] != null) {
						try {
							Graphics g = backImage[i][j].getGraphics();
							if (g != null) g.dispose();
							backImage[i][j] = null;
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		if (table != null) {
			for (Enumeration e = table.keys(); e.hasMoreElements(); ) {
				Object key = e.nextElement();
				Object o = table.get(key);
				if (o instanceof Image) {
					try {
						Image image = (Image)o;
						Graphics g = image.getGraphics();
						if (g != null) g.dispose();
					} catch (Throwable ignored) {
					}
				}
			}
		}
*/	}
	
	/**
	 *
	 */
	public void setBackImage(int num) {
		backImageNum = num;
	}
	
/*-------------------------
 * implements(MediaLoader)
 */
	public Hashtable getMediaTable() {
		return table;
	}
	
	public Image getImage(String key) {
		Object val = table.get(key);
		if (!(val instanceof Image))
			throw new IllegalArgumentException("指定したキー"+
							key+
							"に対応するオブジェクトはImageのインスタンスではありません。");
		return (Image)val;
	}
	
/*-----------
 * Overrides
 */
	public void update(Graphics g) {
		paint(g);
	}
	
}
