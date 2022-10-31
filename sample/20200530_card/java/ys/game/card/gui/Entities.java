package ys.game.card.gui;

import java.awt.*;

/**
 * ������ Entity ���O���[�s���O���A��� Entity �Ƃ��邽�߂̗e��N���X�ł��B
 *
 * @version		remaking		3, December 2000
 * @author		Yusuke Sasaki
 */
public class Entities extends Entity {
	protected Entity[]		entity;
	protected int			entities;
	protected EntityLayout	layout;
	
//	protected Image			image;		// �g���ĂȂ��悤�����H�H
//	protected Graphics		graphics;	// �g���ĂȂ��悤�����H�H
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
	 * ���� Entities �� Entity ��ǉ����A�K�؂Ȉʒu�ɔz�u���܂��B
	 * ���݃X���b�h�Z�[�t�ł͂���܂���B
	 * null �I�u�W�F�N�g��}�����悤�Ƃ���Ɖ����s���܂���B
	 * �ǉ����ꂽ Entity �́AsetParent(this), setDirection() ���Ă΂�܂��B
	 * �܂��Alayout() �������I�ɌĂ΂�܂��B
	 *
	 * @param		e		�ǉ������ Entity
	 */
	public void addEntity(Entity e) {
		if (e == null) return;
		if (e instanceof Field)
			throw new IllegalArgumentException("Entities �� Field �� addEntity �ł��܂���");
		
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
	 * ���� Entities �̎w�肳�ꂽ�ʒu�� Entity ��ǉ����A
	 * �K�؂Ȉʒu�ɔz�u���܂��Bnull �I�u�W�F�N�g��}�����悤�Ƃ���Ɖ����s���܂���B
	 * ���݃X���b�h�Z�[�t�ł͂���܂���B
	 * �ǉ����ꂽ Entity �́AsetParent(), setDirection() ���Ă΂�܂��B
	 * �܂��Alayout() �������I�ɌĂ΂�܂��B
	 *
	 * @param		e		�ǉ������ Entity
	 * @param		index	�ǉ��ʒu
	 */
	public void insertEntityAt(Entity e, int index) {
		if (index < 0) throw new IndexOutOfBoundsException("�l�͈͊O�ł�:"+index);
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
	 * �w�肳�ꂽ Entity ���폜���܂��B�폜��Alayout() �͎��s����܂���B
	 * ���݃X���b�h�Z�[�t�ł͂���܂���B
	 * �폜���ꂽ Entity �́AsetParent(null), removed(this) ���Ă΂�܂��B
	 *
	 * @param		e		�폜���� Entity
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
	 * �w�肳�ꂽ�ʒu�� Entity ���폜���܂��B�폜��Alayout() �͎��s����܂���B
	 * ���݃X���b�h�Z�[�t�ł͂���܂���B
	 */
	public void removeEntity(int index) {
		if ( (index < 0)||(index >= entities) )
			throw new IndexOutOfBoundsException("removeEntity �̈����͖����ł��B");
		
		System.arraycopy(entity, index+1, entity, index, entities - index - 1);
		
		Entity e = entity[entities - 1];
		entity[entities - 1] = null;
		e.setParent(null);
		entities--;
		
		e.removed(this);
		
		imageIsNoMoreCurrent();
	}
	
	/**
	 * �w�肳�ꂽ EntityLayout ��o�^���A�q Entity ������ layout
	 * �ɂ��������ēK�؂Ȉʒu�ɍĔz�u���܂��B
	 *
	 * @param		layout		�o�^���� layout
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
	 * ���� Entities �ɓo�^����Ă��� layout �ɏ]���Ďq Entity �̍Ĕz�u��
	 * �s���܂��B���̍Ĕz�u�̌��ʂ���ʏ�ɔ��f���邽�߂ɂ́A���̃��\�b�h
	 * �̂��ƁAField.repaint() �����s���ĉ������B
	 */
	public void layout() {
		if (layout == null) return;
		layout.layout(this);
		imageIsNoMoreCurrent();
	}
	
	/**
	 * �w�肳�ꂽ Entity ���܂܂�Ă��邩�e�X�g���܂��B
	 * == �̈Ӗ��Ŕ�����s���܂��B
	 */
	public boolean contains(Entity e) {
		if (e == null) return true;
		for (int i = 0; i < entities; i++) {
			if (e == entity[i]) return true;
		}
		return false;
	}
	
	/**
	 * ���� Entities �Ɋ܂܂��q Entity �̐����擾���܂��B
	 *
	 * @return		Entity �̐�
	 */
	public int getEntityCount() {
		return entities;
	}
	
	/**
	 * �w�肳�ꂽ index �� Entity ���擾���܂��B
	 * index �̒l�� getEntityCount() �𒴂���� IndexOutOfBoundsException
	 * ���X���[����܂��B
	 *
	 * @param		index
	 * @return		entity
	 */
	public Entity getEntity(int index) {
		if (index >= entities)
			throw new IndexOutOfBoundsException("index ���l�͈͊O�ł��B:"+index);
		return entity[index];
	}
	
	/**
	 * �w�肳�ꂽ�ʒu��\���ʒu�Ƃ��Đ�L���Ă��� Entity ���擾���܂��B
	 * ���̃��\�b�h�ł́A��Ɉʒu������̂�D�悵�ĕԋp���܂��B
	 * Entities ���ԋp����邱�Ƃ͂���܂���B
	 *
	 * @param		x		�\���ʒu X
	 * @param		y		�\���ʒu Y
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
	 * �o�b�t�@�����O���ꂽ�̈�ɍĕ`�悪�K�v�ł��邱�Ƃ�ʒm���܂��B
	 * ���������݂͖����ŁA�I�u�W�F�N�g���ύX���ꂽ���ۂ��Ɋւ�炸�A
	 * �˂ɍĕ`����s���܂��B
	 */
//	public void redraw() {
		//
		// �����I�ɁAEntities �ł� Image �̃L���b�V�����s���ꍇ�A��ʂ���ʒm
		// ����� invalidate �ł� redraw �̕K�v���Ȃ��Ȃ�AinvalidateChilds()
		// ���s�v�ɂȂ�B
		// ���݂́A�e�q�̊֘A���̂��� Entity �t�@�~���[�̂����ꂩ�� invalidate
		// �����ƃt�@�~���[�S�̂� invalid �ƂȂ�����ł���B
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
	 * Entities �ł� draw �́A�����o�� draw() ���Ăт܂��B
	 */
	public void draw(Graphics g) {
		if (!visible) return;
		
		for (int i = 0; i < entities; i++) {
			entity[i].draw(g);
		}
	}
	
	/**
	 * �q�t�B�[���h�ɑ΂��� Field ���A�b�v�f�[�g���ꂽ���Ƃ�ʒm���邽��
	 * �I�[�o�[���C�h���Ă��܂��B
	 */
	public void setParent(Entities parent) {
		super.setParent(parent); // Field ��ݒ肷��
		
		//
		// �q Entity �� Field ��ݒ肷��
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
	 * ���̃I�u�W�F�N�g�̌�����ݒ肵�܂��B
	 * Entities �ł́A���e���Ă���e Entity �̌��������̃I�u�W�F�N�g�Ɠ������̂ɕύX���܂��B
	 * ���������āATrickGui �̂悤�Ȃ��܂��܂Ȍ����� Entity ���܂� Entities �ł�
	 * �{���\�b�h���I�[�o�[���C�h����K�v������܂��B
	 *
	 * @param		direction		�������w�肵�܂�(Entity.UPRIGHT/RIGHT_VIEW/UPSIDE_DOWN/LEFT_VIEW)
	 * @see			ys.game.card.gui.Entity#UPRIGHT
	 */
	public void setDirection(int direction) {
		// this.direction ��ݒ肷��
		super.setDirection(direction);
		
		// TrickGui �̂悤�Ȃ��܂��܂Ȍ����� Entity ���܂��� Entities ��
		// ���̃��\�b�h���I�[�o�[���C�h���Ȃ���΂Ȃ�Ȃ�
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
	 * ���� Entities �� Entity ���ǉ��폜���ꂽ�Alayout ���ύX���ꂽ�Ȃ�
	 * �̗��R�ɂ���ĕێ����Ă���o�b�t�@�����O�C���[�W�̕`���������K�v��
	 * ���邱�Ƃ�ʒm���܂��B
	 * draw(Graphics) �ɂ����āA���ۂ̕`���������s���܂��B
	 * �q Entity �̃C���[�W���ύX���ꂽ�ہA�q Entity �ɂ���ăR�[�������
	 * ���Ƃ�����܂��B
	 */
	public void imageIsNoMoreCurrent() {
		imageIsCurrent = false;
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
}
