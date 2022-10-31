package ys.game.card.gui;

import java.awt.*;

class FieldCanvas extends Canvas {
	private Image		off;
	private Graphics	offg;
	private Field		field;
	private long		lastDrawn;
	private long		waiting		= 100L;
/*-------------
 * Constructor
 */
	/**
	 * �w�肳�ꂽ�傫���̃A�j���[�V�����\���̈���쐬���܂��B
	 *
	 */
	FieldCanvas(Component peeredComponent, int width, int height, Field parent) {
		setSize(width, height);
		
		off		= peeredComponent.createImage(width, height);
		offg	= off.getGraphics();
		field	= parent;
		
		lastDrawn = 0;
	}
	
/*-----------
 * overrides
 */
	public void update(Graphics g) {
		paint(g);
	}
	
	public void paint(Graphics g) {
		// �O�ɕ`�悵�����Ԃ��� waiting (msec) �ȏソ���Ȃ��ƍĕ`�悵�Ȃ��悤�ɂ���
//		if (System.currentTimeMillis() - lastDrawn < waiting) return;
		
		synchronized (this) {
			field.draw();
			// ���݂͏�ɕ`����s�����A�����I�ɕ\���t���[�����̃T�|�[�g������B
			g.drawImage(off, 0, 0, this);
			
//			lastDrawn = System.currentTimeMillis();
		}
	}
	
	protected void finalize() throws Throwable {
		dispose();
	}
	
/*------------------
 * instance methods
 */
	public void dispose() {
		offg.dispose();
	}
	
	public Graphics getOffGraphics() {
		return offg;
	}
}
