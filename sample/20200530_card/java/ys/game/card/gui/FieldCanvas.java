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
	 * 指定された大きさのアニメーション表示領域を作成します。
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
		// 前に描画した時間から waiting (msec) 以上たたないと再描画しないようにする
//		if (System.currentTimeMillis() - lastDrawn < waiting) return;
		
		synchronized (this) {
			field.draw();
			// 現在は常に描画を行うが、将来的に表示フレーム数のサポートをする。
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
