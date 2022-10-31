package ys.game.card.gui;

import java.awt.*;

/**
 * Field 上のオブジェクト。座標系は、Field オブジェクトのものを基準とします。
 * 本オブジェクトは GUI コンポーネントを定義しますが、Graphics リソースは
 * Field (の中の FieldCanvas) 以外には保持しません。
 *
 * @version		remaking		5, August 2000
 * @author		Yusuke Sasaki
 */
public class Entity {
	public static final int UPRIGHT		= 0;
	public static final int RIGHT_VIEW	= 1;
	public static final int UPSIDE_DOWN	= 2;
	public static final int LEFT_VIEW	= 3;
	
	protected int x;
	protected int y;
	protected int w;
	protected int h;
	protected int direction;
	protected Entities parent;
	
	/** この Entity の所属している Field を保持しています */
	protected Field field;
	protected boolean visible;
	
/*-------------
 * Constructor
 */
	/**
	 * Entity のインスタンスを生成します。
	 * 向きは UPRIGHT, 左上に配置され、30x30のサイズに規定されます。
	 */
	public Entity() {
		x = 0;
		y = 0;
		h = 30;
		w = 30;
		direction	= UPRIGHT;
		visible		= true;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * サブクラスではこのメソッドをオーバーライドすることによってイメージを表現します。
	 * Entity クラスではデフォルトの実装として、緑色の楕円を描画します。
	 */
	public void draw(Graphics g) {
		g.setColor(Color.green);
		g.drawOval(x, y, w, h);
	}
	
	/**
	 * この Entity が所属する親 Entities を設定します。同時に、親 Entities と同一 field
	 * も設定します。
	 * 通常、Entities.addEntity() の中で自動的に呼ばれ、外部から明示的に呼ぶ必要はありません。
	 *
	 * @param		parent		親 Entities、null とすると、field も null が設定される
	 */
	public void setParent(Entities parent) {
		this.parent = parent;
		if (parent == null) this.field = null;
		else this.field = parent.getField();
	}
	
	/**
	 * 渡された parent の removeEntity によって remove された通知を受け取ります。
	 * サブクラスではスレッド終了処理などのリソース開放処理をここで行います。
	 *
	 * @param		parent		remove しようとしている parent
	 */
	public void removed(Entities parent) {
	}
	
	/**
	 * 表示可否を設定します。
	 */
	public void setVisibility(boolean visible) {
		this.visible = visible;
	}
	
	/**
	 * 位置(x)を設定します。内部で parent の imageIsNoMoreCurrent() を呼びます。
	 */
	public void setX(int x) {
		this.x = x;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * 位置(y)を設定します。内部で parent の imageIsNoMoreCurrent() を呼びます。
	 */
	public void setY(int y) {
		this.y = y;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * 大きさ(w)を設定します。内部で parent の imageIsNoMoreCurrent() を呼びます。
	 */
	public void setWidth(int w) {
		this.w = w;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * 大きさ(h)を設定します。内部で parent の imageIsNoMoreCurrent() を呼びます。
	 */
	public void setHeight(int h) {
		this.h = h;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * 位置(x,y)を設定します。内部で parent の imageIsNoMoreCurrent() を呼びます。
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * 大きさ(w,h)を設定します。内部で parent の imageIsNoMoreCurrent() を呼びます。
	 */
	public void setSize(int w, int h) {
		this.w = w;
		this.h = h;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * 位置と大きさ(x,y,w,h)を設定します。内部で parent の imageIsNoMoreCurrent() を呼びます。
	 */
	public void setBounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * 向きを設定します。位置は変更されません。大きさは縦、横が入れ替わる場合入れ替えます。
	 * 内部で parent の imageIsNoMoreCurrent() を呼びます。
	 */
	public void setDirection(int direction) {
		if (( (this.direction ^ direction) & 1) == 1) {
			int tmp = w;
			w = h;
			h = w;
		}
		this.direction = direction;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	public Field getField() { return field; }
	public boolean getVisibility() { return visible; }
	public boolean isVisible() { return visible; }
	public int getX() { return x; }
	public int getY() { return y; }
	public int getWidth() { return w; }
	public int getHeight() { return h; }
	public Point getPosition() { return new Point(x, y); }
	public Dimension getSize() { return new Dimension(w, h); }
	public Rectangle getBounds() { return new Rectangle(x, y, w, h); }
	
	public int getDirection() { return direction; }
	
}
