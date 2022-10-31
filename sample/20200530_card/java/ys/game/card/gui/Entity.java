package ys.game.card.gui;

import java.awt.*;

/**
 * Field ��̃I�u�W�F�N�g�B���W�n�́AField �I�u�W�F�N�g�̂��̂���Ƃ��܂��B
 * �{�I�u�W�F�N�g�� GUI �R���|�[�l���g���`���܂����AGraphics ���\�[�X��
 * Field (�̒��� FieldCanvas) �ȊO�ɂ͕ێ����܂���B
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
	
	/** ���� Entity �̏������Ă��� Field ��ێ����Ă��܂� */
	protected Field field;
	protected boolean visible;
	
/*-------------
 * Constructor
 */
	/**
	 * Entity �̃C���X�^���X�𐶐����܂��B
	 * ������ UPRIGHT, ����ɔz�u����A30x30�̃T�C�Y�ɋK�肳��܂��B
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
	 * �T�u�N���X�ł͂��̃��\�b�h���I�[�o�[���C�h���邱�Ƃɂ���ăC���[�W��\�����܂��B
	 * Entity �N���X�ł̓f�t�H���g�̎����Ƃ��āA�ΐF�̑ȉ~��`�悵�܂��B
	 */
	public void draw(Graphics g) {
		g.setColor(Color.green);
		g.drawOval(x, y, w, h);
	}
	
	/**
	 * ���� Entity ����������e Entities ��ݒ肵�܂��B�����ɁA�e Entities �Ɠ��� field
	 * ���ݒ肵�܂��B
	 * �ʏ�AEntities.addEntity() �̒��Ŏ����I�ɌĂ΂�A�O�����疾���I�ɌĂԕK�v�͂���܂���B
	 *
	 * @param		parent		�e Entities�Anull �Ƃ���ƁAfield �� null ���ݒ肳���
	 */
	public void setParent(Entities parent) {
		this.parent = parent;
		if (parent == null) this.field = null;
		else this.field = parent.getField();
	}
	
	/**
	 * �n���ꂽ parent �� removeEntity �ɂ���� remove ���ꂽ�ʒm���󂯎��܂��B
	 * �T�u�N���X�ł̓X���b�h�I�������Ȃǂ̃��\�[�X�J�������������ōs���܂��B
	 *
	 * @param		parent		remove ���悤�Ƃ��Ă��� parent
	 */
	public void removed(Entities parent) {
	}
	
	/**
	 * �\���ۂ�ݒ肵�܂��B
	 */
	public void setVisibility(boolean visible) {
		this.visible = visible;
	}
	
	/**
	 * �ʒu(x)��ݒ肵�܂��B������ parent �� imageIsNoMoreCurrent() ���Ăт܂��B
	 */
	public void setX(int x) {
		this.x = x;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * �ʒu(y)��ݒ肵�܂��B������ parent �� imageIsNoMoreCurrent() ���Ăт܂��B
	 */
	public void setY(int y) {
		this.y = y;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * �傫��(w)��ݒ肵�܂��B������ parent �� imageIsNoMoreCurrent() ���Ăт܂��B
	 */
	public void setWidth(int w) {
		this.w = w;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * �傫��(h)��ݒ肵�܂��B������ parent �� imageIsNoMoreCurrent() ���Ăт܂��B
	 */
	public void setHeight(int h) {
		this.h = h;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * �ʒu(x,y)��ݒ肵�܂��B������ parent �� imageIsNoMoreCurrent() ���Ăт܂��B
	 */
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * �傫��(w,h)��ݒ肵�܂��B������ parent �� imageIsNoMoreCurrent() ���Ăт܂��B
	 */
	public void setSize(int w, int h) {
		this.w = w;
		this.h = h;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * �ʒu�Ƒ傫��(x,y,w,h)��ݒ肵�܂��B������ parent �� imageIsNoMoreCurrent() ���Ăт܂��B
	 */
	public void setBounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		if (parent != null) parent.imageIsNoMoreCurrent();
	}
	
	/**
	 * ������ݒ肵�܂��B�ʒu�͕ύX����܂���B�傫���͏c�A��������ւ��ꍇ����ւ��܂��B
	 * ������ parent �� imageIsNoMoreCurrent() ���Ăт܂��B
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
