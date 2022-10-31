package ys.game.card.gui;

import java.awt.*;

/**
 * Field クラスは、Entity の表示される領域となるトップレベルの Entities
 * です。このクラスは FieldCanvas と対に生成され、実際の画面表示リソースに
 * 接続して画面表示を行います。
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
	 * 指定された大きさのアニメーション表示領域を作成します。
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
	 * オフスクリーン領域に描画を行います。
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
	 * サブクラスにおいて、本メソッドをオーバーライドすることで背景を
	 * 設定することができます。
	 */
	protected void drawBackground(Graphics g) {
		Rectangle	r = canvas.getBounds();
		
		g.setColor(Color.black);
		g.fillRect(r.x, r.y, r.width, r.height);
	}
}

