package ys.game.card.gui;

import java.awt.*;

/**
 * Field �N���X�́AEntity �̕\�������̈�ƂȂ�g�b�v���x���� Entities
 * �ł��B���̃N���X�� FieldCanvas �Ƒ΂ɐ�������A���ۂ̉�ʕ\�����\�[�X��
 * �ڑ����ĉ�ʕ\�����s���܂��B
 *
 * @version		a-release		5, May 2000
 * @author		Yusuke Sasaki
 */
public class Field extends Entities {
	private FieldCanvas canvas;
	
/*-------------
 * Constructor
 */
	/**
	 * �w�肳�ꂽ�傫���̃A�j���[�V�����\���̈���쐬���܂��B
	 *
	 */
	public Field(Component peeredComponent, int width, int height) {
		canvas = new FieldCanvas(peeredComponent, width, height, this);
		
		this.parent	= null;
		this.field	= this;
	}
	
/*------------------
 * instance methods
 */
	public void repaint() {
		canvas.repaint();
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	/**
	 * �I�t�X�N���[���̈�ɕ`����s���܂��B
	 */
	public void draw() {
		Graphics	g = canvas.getOffGraphics();
		drawBackground(g);
		
		draw(g);
		
//		imageIsCurrent = true;
	}
	
	public void dispose() {
		canvas.dispose();
	}
	
	/**
	 * �T�u�N���X�ɂ����āA�{���\�b�h���I�[�o�[���C�h���邱�ƂŔw�i��
	 * �ݒ肷�邱�Ƃ��ł��܂��B
	 */
	protected void drawBackground(Graphics g) {
		Rectangle	r = canvas.getBounds();
		
		g.setColor(Color.black);
		g.fillRect(r.x, r.y, r.width, r.height);
	}
}

