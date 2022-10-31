package ys.game.card.gui;

import java.awt.*;

/**
 * 複数の Entity をグルーピングし、一つの Entity とするための容器クラスです。
 *
 * @version		remaking		3, December 2000
 * @author		Yusuke Sasaki
 */
public class Entities extends Entity {
	protected Entity[]		entity;
	protected int			entities;
	protected EntityLayout	layout;
	
//	protected Image			image;		// 使われてないようだが？？
//	protected Graphics		graphics;	// 使われてないようだが？？
	protected boolean		imageIsCurrent;
	
/*-------------
 * Constructor
 */
	public Entities() {
		entities	= 0;
		imageIsCurrent = false;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * この Entities に Entity を追加し、適切な位置に配置します。
	 * 現在スレッドセーフではありません。
	 * null オブジェクトを挿入しようとすると何も行いません。
	 * 追加された Entity は、setParent(this), setDirection() が呼ばれます。
	 * また、layout() が自動的に呼ばれます。
	 *
	 * @param		e		追加される Entity
	 */
	public void addEntity(Entity e) {
		if (e == null) return;
		if (e instanceof Field)
			throw new IllegalArgumentException("Entities に Field は addEntity できません");
		
		if (contains(e)) return;
		if (entity == null) entity = new Entity[1];
		int i;
		for (i = 0; i < entity.length; i++) {
			if (entity[i] == null) break;
		}
		if (i == entity.length) {
			Entity[] tmp = new Entity[entity.length * 2];
			System.arraycopy(entity, 0, tmp, 0, entity.length);
			entity = tmp;
		}
		entity[i] = e;
		e.setParent(this);
		e.setDirection(getDirection());
		entities++;
//System.out.println("Entities.entities = " + entities + "  object id = " + this);
//		imageIsNoMoreCurrent();
		
		layout();
	}
	
	/**
	 * この Entities の指定された位置に Entity を追加し、
	 * 適切な位置に配置します。null オブジェクトを挿入しようとすると何も行いません。
	 * 現在スレッドセーフではありません。
	 * 追加された Entity は、setParent(), setDirection() が呼ばれます。
	 * また、layout() が自動的に呼ばれます。
	 *
	 * @param		e		追加される Entity
	 * @param		index	追加位置
	 */
	public void insertEntityAt(Entity e, int index) {
		if (index < 0) throw new IndexOutOfBoundsException("値範囲外です:"+index);
		if (index > entities) index = entities;
		
		if (e == null) return;
		
		if (contains(e)) return;
		if (entity == null) entity = new Entity[1];
		
		Entity[] src = entity;
		Entity[] dst;
		
		if (entity.length == entities) {
			int newSize = entity.length * 2;
			dst = new Entity[newSize];
			if (index > 0)
				System.arraycopy(entity, 0, dst, 0, index);
		}
		else {
			dst = entity;
		}
		if (entities > index)
			System.arraycopy(src, index, dst, index + 1, entities - index);
		dst[index] = e;
		entity = dst;
		entities++;
//System.out.println("Entities.entities = " + entities + "  object id = " + this);
		
		e.setParent(this);
		e.setDirection(getDirection());
		
//		imageIsNoMoreCurrent();
		
		layout();
	}
	
	/**
	 * 指定された Entity を削除します。削除後、layout() は実行されません。
	 * 現在スレッドセーフではありません。
	 * 削除された Entity は、setParent(null), removed(this) が呼ばれます。
	 *
	 * @param		e		削除する Entity
	 */
	public void removeEntity(Entity e) {
		if (e == null) return;
		int i;
		for (i = 0; i < entities; i++) {
			if (entity[i] == e) break;
		}
		if (i == entities) return;
		if (i < entities - 1)
			System.arraycopy(entity, i+1, entity, i, entities-i-1);
		entity[entities - 1] = null;
		e.setParent(null);
		entities--;
		
		e.removed(this);
		
		imageIsNoMoreCurrent();
	}
	
	/**
	 * 指定された位置の Entity を削除します。削除後、layout() は実行されません。
	 * 現在スレッドセーフではありません。
	 */
	public void removeEntity(int index) {
		if ( (index < 0)||(index >= entities) )
			throw new IndexOutOfBoundsException("removeEntity の引数は無効です。");
		
		System.arraycopy(entity, index+1, entity, index, entities - index - 1);
		
		Entity e = entity[entities - 1];
		entity[entities - 1] = null;
		e.setParent(null);
		entities--;
		
		e.removed(this);
		
		imageIsNoMoreCurrent();
	}
	
	/**
	 * 指定された EntityLayout を登録し、子 Entity をこの layout
	 * にしたがって適切な位置に再配置します。
	 *
	 * @param		layout		登録する layout
	 */
	public void setLayout(EntityLayout layout) {
		this.layout = layout;
		
//		imageIsNoMoreCurrent();
		
		layout();
	}
	
	public EntityLayout getLayout() {
		return layout;
	}
	
	/**
	 * この Entities に登録されている layout に従って子 Entity の再配置を
	 * 行います。この再配置の効果を画面上に反映するためには、このメソッド
	 * のあと、Field.repaint() を実行して下さい。
	 */
	public void layout() {
		if (layout == null) return;
		layout.layout(this);
		imageIsNoMoreCurrent();
	}
	
	/**
	 * 指定された Entity が含まれているかテストします。
	 * == の意味で判定を行います。
	 */
	public boolean contains(Entity e) {
		if (e == null) return true;
		for (int i = 0; i < entities; i++) {
			if (e == entity[i]) return true;
		}
		return false;
	}
	
	/**
	 * この Entities に含まれる子 Entity の数を取得します。
	 *
	 * @return		Entity の数
	 */
	public int getEntityCount() {
		return entities;
	}
	
	/**
	 * 指定された index の Entity を取得します。
	 * index の値が getEntityCount() を超えると IndexOutOfBoundsException
	 * がスローされます。
	 *
	 * @param		index
	 * @return		entity
	 */
	public Entity getEntity(int index) {
		if (index >= entities)
			throw new IndexOutOfBoundsException("index が値範囲外です。:"+index);
		return entity[index];
	}
	
	/**
	 * 指定された位置を表示位置として占有している Entity を取得します。
	 * このメソッドでは、上に位置するものを優先して返却します。
	 * Entities が返却されることはありません。
	 *
	 * @param		x		表示位置 X
	 * @param		y		表示位置 Y
	 */
	public Entity getEntityAt(int x, int y) {
		int n;
		for (n = entities - 1; n >= 0; n--) {
			if (entity[n].getBounds().contains(x, y)) {
				if (entity[n] instanceof Entities) {
					Entity ent =((Entities)entity[n]).getEntityAt(x, y);
					if (ent != null) return ent;
				} else {
					return entity[n];
				}
			}
		}
		return null;
	}
	
	/**
	 * バッファリングされた領域に再描画が必要であることを通知します。
	 * ただし現在は無効で、オブジェクトが変更されたか否かに関わらず、
	 * つねに再描画を行います。
	 */
//	public void redraw() {
		//
		// 将来的に、Entities でも Image のキャッシュを行う場合、上位から通知
		// される invalidate では redraw の必要がなくなり、invalidateChilds()
		// が不要になる。
		// 現在は、親子の関連性のある Entity ファミリーのいずれかが invalidate
		// されるとファミリー全体が invalid となる実装である。
		//
//		if (parent != null) parent.redraw();	// Not being top level, search top level.
//		else redrawChilds();	// this is top level, then invalidate.
//	}
	
//	void redrawChilds() {
//		isValid = false;
//		for (int i = 0; i < entities; i++) {
//			if ( entity[i] instanceof Entities ) {
//				Entities childEntities = (Entities)entity[i];
//				childEntities.redrawChilds();
//			}
//		}
//	}
	
/*-------------------
 * Overrides(Entity)
 */
	/**
	 * Entities での draw は、メンバの draw() を呼びます。
	 */
	public void draw(Graphics g) {
		if (!visible) return;
		
		for (int i = 0; i < entities; i++) {
			entity[i].draw(g);
		}
	}
	
	/**
	 * 子フィールドに対して Field がアップデートされたことを通知するため
	 * オーバーライドしています。
	 */
	public void setParent(Entities parent) {
		super.setParent(parent); // Field を設定する
		
		//
		// 子 Entity の Field を設定する
		//
		for (int i = 0; i < entities; i++)
			entity[i].setParent(this);
	}
	
/*	public void setX(int x) {
		super.setX(x);
		layout();
	}
	
	public void setY(int y) {
		super.setY(y);
		layout();
	}
	
	public void setWidth(int w) {
		super.setWidth(w);
		layout();
	}
	
	public void setHeight(int h) {
		super.setHeight(h);
		layout();
	}
	
	public void setPosition(int x, int y) {
		super.setPosition(x, y);
		layout();
	}
	
	public void setSize(int w, int h) {
		super.setSize(w, h);
		layout();
	}
	
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		layout();
	}
*/	
	/**
	 * このオブジェクトの向きを設定します。
	 * Entities では、収容している各 Entity の向きをこのオブジェクトと同じものに変更します。
	 * したがって、TrickGui のようなさまざまな向きの Entity を含む Entities では
	 * 本メソッドをオーバーライドする必要があります。
	 *
	 * @param		direction		向きを指定します(Entity.UPRIGHT/RIGHT_VIEW/UPSIDE_DOWN/LEFT_VIEW)
	 * @see			ys.game.card.gui.Entity#UPRIGHT
	 */
	public void setDirection(int direction) {
		// this.direction を設定する
		super.setDirection(direction);
		
		// TrickGui のようなさまざまな向きの Entity を包含する Entities は
		// このメソッドをオーバーライドしなければならない
		for (int i = 0; i < entities; i++) {
			entity[i].setDirection(direction);
		}

//		int step = (direction - oldDirection + 4)%4;
//		for (int i = 0; i < entities; i++) {
//			entity[i].setDirection((entity[i].getDirection() + step)%4);
//		}
		
//		layout();
	}
	
	public Dimension getSize() {
		if (layout == null) return super.getSize();
		Dimension d = layout.layoutSize(this);
		setSize(d.width, d.height);
		return d;
	}
	
	/**
	 * この Entities に Entity が追加削除された、layout が変更されたなど
	 * の理由によって保持しているバッファリングイメージの描き直しが必要で
	 * あることを通知します。
	 * draw(Graphics) において、実際の描き直しが行われます。
	 * 子 Entity のイメージが変更された際、子 Entity によってコールされる
	 * こともあります。
	 */
	public void imageIsNoMoreCurrent() {
		imageIsCurrent = false;
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
}
