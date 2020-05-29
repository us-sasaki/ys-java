package ys.game.card.gui;

import java.awt.*;
import java.awt.image.*;

/**
 * 90���E��]������������ ImageFilter �ł��B
 *
 * @version		a-release		3, May 2000
 * @author		Yusuke Sasaki
 */
public class RightRotateImageFilter extends ImageFilter {
	/** ��]�������ɂ����镝 */
	protected int width;
	
	/** ��]�������ɂ����鍂�� */
	protected int height;

/*-----------
 * Overrides
 */
	/**
	 * ���炩���ߒʒm����镝�ƍ�����ϊ������Ŏg�p���邽�߁A�l���擾���܂��B
	 *
	 * @param		width		���Ƃ̉摜�̕��A���������摜�̍����ɂȂ�܂�
	 * @param		height		���Ƃ̉摜�̍����A���������摜�̕��ɂȂ�܂�
	 */
	public void setDimensions(int width, int height) {
		this.width = height;
		this.height = width;
//System.out.println("W,H=" + height + "  "+ width);
		super.setDimensions(height, width);
	}
	
	/**
	 * ImageSource ����̉摜�s�N�Z������90����]������ ImageConsumer ��
	 * �]�����܂��B
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
				newPixels[(h - 1 - yp) + xp * h] = pixels[xp + yp * scansize + off];
			}
		}
		int newOff = 0;
		
		// ���]�����f�[�^�𑗂�
		consumer.setPixels(width - y - h, x, h, w, model, newPixels, newOff, h);
	}
	
	/**
	 * ImageSource ����̉摜�s�N�Z������90����]������ ImageConsumer ��
	 * �]�����܂��B
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
				newPixels[(h - 1 - yp) + xp * h] = pixels[xp + yp * scansize + off];
//System.out.println( "" + (yp + (w - 1 - xp) * h) + (xp + yp * scansize + off) );
			}
		}
		int newOff = 0;
//System.out.println( "newOff" + newOff);
		
		// ���]�����f�[�^�𑗂�
		consumer.setPixels(width - y - h, x, h, w, model, newPixels, newOff, h);
	}
	
	/**
	 * RotateImageFilter �ł́AImageSource ���瑗����摜�s�N�Z�����̏�����
	 * �قȂ��������� ImageConsumer �ɑ����邽�߁AHint ��K�؂ɕϊ����܂��B
	 *
	 * @param		hint		�摜�s�N�Z�����̓]�������Ɋւ���q���g
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
	
}
