package ys.game.card.gui;

import java.awt.*;
import java.awt.image.*;

/**
 * 90°左回転した像をつくる ImageFilter です。使用例は次の通りです。
 * <PRE>
 *  Image originalImage = ....   // オリジナルイメージの作成
 *	RotateImageFilter rotator = new RotateImageFilter();
 *	ImageSource src = new FilteredImageSource(originalImage.getSource(), rotator);
 *	Image rotatedImage = createImage(src);
 * </PRE>
 *
 * @version		a-release		3, May 2000
 * @author		Yusuke Sasaki
 */
public class RotateImageFilter extends ImageFilter {
	/** 回転した像における幅 */
	protected int width;
	
	/** 回転した像における高さ */
	protected int height;

/*-----------
 * Overrides
 */
	/**
	 * あらかじめ通知される幅と高さを変換処理で使用するため、値を取得します。
	 *
	 * @param		width		もとの画像の幅、生成される画像の高さになります
	 * @param		height		もとの画像の高さ、生成される画像の幅になります
	 */
	public void setDimensions(int width, int height) {
		this.width = height;
		this.height = width;
//System.out.println("W,H=" + height + "  "+ width);
		super.setDimensions(height, width);
	}
	
	/**
	 * ImageSource からの画像ピクセル情報を90°回転させて ImageConsumer に
	 * 転送します。
	 */
	public void setPixels(int x, int y, int w, int h,
								ColorModel model,
								byte[] pixels,
								int off, int scansize) {
//		print(x, y, w, h, off, scansize, pixels.length);
//		System.out.println("rotate called byte");
		
		byte[] newPixels = new byte[w * h];
		for (int yp = 0; yp < h; yp++) {
			for (int xp = 0; xp < w; xp++) {
				newPixels[yp + (w - 1 - xp) * h] = pixels[xp + yp * scansize + off];
			}
		}
		int newOff = 0;
		
		// 反転したデータを送る
		consumer.setPixels(y, height - x - w, h, w, model, newPixels, newOff, h);
	}
	
	/**
	 * ImageSource からの画像ピクセル情報を90°回転させて ImageConsumer に
	 * 転送します。
	 */
	public void setPixels(int x, int y, int w, int h,
								ColorModel model,
								int[] pixels,
								int off, int scansize) {
//		print(x, y, w, h, off, scansize, pixels.length);
//		System.out.println("rotate called");
		
		int[] newPixels = new int[w * h];
		for (int yp = 0; yp < h; yp++) {
			for (int xp = 0; xp < w; xp++) {
				newPixels[yp + (w - 1 - xp) * h] = pixels[xp + yp * scansize + off];
//System.out.println( "" + (yp + (w - 1 - xp) * h) + (xp + yp * scansize + off) );
			}
		}
		int newOff = 0;
//System.out.println( "newOff" + newOff);
		
		// 反転したデータを送る
		consumer.setPixels(y, height - x - w, h, w, model, newPixels, newOff, h);
	}
	
	/**
	 * RotateImageFilter では、ImageSource から送られる画像ピクセル情報の順序が
	 * 異なった順序で ImageConsumer に送られるため、Hint を適切に変換します。
	 *
	 * @param		hint		画像ピクセル情報の転送順序に関するヒント
	 */
	public void setHints(int hint) {
		int newHint = 0;
		if ((hint & ImageConsumer.SINGLEFRAME) != 0)
			newHint |= ImageConsumer.SINGLEFRAME;
		if ((hint & ImageConsumer.SINGLEPASS) != 0) {
			newHint |= ImageConsumer.SINGLEPASS |
						ImageConsumer.COMPLETESCANLINES |
						ImageConsumer.TOPDOWNLEFTRIGHT;
			super.setHints(newHint);
			return;
		}
		newHint |= ImageConsumer.RANDOMPIXELORDER;
		super.setHints(newHint);
	}
	
	private void print(int x, int y, int w, int h, int off, int scansize, int pix) {
		System.out.println("x="+x+" y="+y+" w="+w+" h="+h+" off="+off+" scan="+scansize+" pixlen="+pix);
	}
}
