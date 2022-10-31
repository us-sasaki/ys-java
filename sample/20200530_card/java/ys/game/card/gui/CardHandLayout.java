package ys.game.card.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

/**
 * �J�[�h�̃n���h��\�����邽�߂̃��C�A�E�g�B�J�[�h���d�Ȃ��ĕ\�������B
 * �J�[�h(GuiedCard)�ȊO�� Entity �ɑ΂��Ďg�p���邱�Ƃ͍l���Ă��Ȃ��B
 * �܂��AEntities �Ɋ܂܂�Ă��� Card �̌����͂��ׂĂ���layout�̌�������
 * ���ďc�����ł���Ƃ݂Ȃ��B
 *
 * @version		a-release		8, May 2000
 * @author		Yusuke Sasaki
 */
public class CardHandLayout implements EntityLayout {
	
	/** �J�[�h���c�Ɍ����Ƃ��̍ŏ����� */
	protected static final int XSTEP = GuiedCard.XSTEP;
	
	/** �J�[�h���c�Ɍ����Ƃ��̍ŏ��c�� */
	protected static final int YSTEP = GuiedCard.YSTEP;
	
	/** �J�[�h���c�Ɍ����Ƃ��̉��� */
	protected static final int XSIZE = GuiedCard.XSIZE;
	
	/** �J�[�h���c�Ɍ����Ƃ��̏c�� */
	protected static final int YSIZE = GuiedCard.YSIZE;
	
/*-------------
 * Constructor
 */
	public CardHandLayout() {
	}
	
/*----------------------------
 * implements (EntityLayout)
 */
	public void layout(Entities target) {
		int xpos, ypos, n;
		
		n = target.getEntityCount();
		
		int direction = target.getDirection();
		
		switch (direction) {
		
		case Entity.UPRIGHT:
			// direction == 0 �̏ꍇ�̎���
			xpos = target.getX();
			ypos = target.getY();
			
			for (int i = 0; i < n; i++) {
				Entity ent = target.getEntity(i);
				ent.setPosition(xpos, ypos);
				ent.setSize(XSIZE, YSIZE);
				xpos += XSTEP;
			}
			target.setSize(XSIZE + (n-1)*XSTEP, YSIZE);
			break;
			
		case Entity.RIGHT_VIEW:
			// direction == 1 �̏ꍇ�̎���
			xpos = target.getX();
			ypos = target.getY() + (n-1) * XSTEP;
			
			for (int i = 0; i < n; i++) {
				Entity ent = target.getEntity(i);
				ent.setPosition(xpos, ypos);
				ent.setSize(YSIZE, XSIZE);
				ypos -= XSTEP;
			}
			target.setSize(YSIZE, XSIZE + (n-1)*XSTEP);
			break;
			
		case Entity.UPSIDE_DOWN:
			// direction == 2 �̏ꍇ�̎���
			xpos = target.getX() + (n-1) * XSTEP;
			ypos = target.getY();
			
			for (int i = 0; i < n; i++) {
				Entity ent = target.getEntity(i);
				ent.setPosition(xpos, ypos);
				ent.setSize(XSIZE, YSIZE);
				xpos -= XSTEP;
			}
			target.setSize(XSIZE + (n-1)*XSTEP, YSIZE);
			break;
			
		case Entity.LEFT_VIEW:
			// direction == 1 �̏ꍇ�̎���
			xpos = target.getX();
			ypos = target.getY();
			
			for (int i = 0; i < n; i++) {
				Entity ent = target.getEntity(i);
				ent.setPosition(xpos, ypos);
				ent.setSize(YSIZE, XSIZE);
				ypos += XSTEP;
			}
			target.setSize(YSIZE, XSIZE + (n-1)*XSTEP);
			break;
		
		default:
			throw new InternalError("direction �̒l���s���ł��F" + direction);
		}
	}
	
	/**
	 * size ���v�Z����B
	 */
	public Dimension layoutSize(Entities target) {
		int n = target.getEntityCount();
		
		if (n == 0) return new Dimension(0, 0);
		
		int direction = target.getDirection();
		
		switch (direction) {
		
		case Entity.UPRIGHT:
		case Entity.UPSIDE_DOWN:
			return new Dimension( (n-1)*XSTEP + XSIZE, YSIZE );
			
		case Entity.RIGHT_VIEW:
		case Entity.LEFT_VIEW:
			return new Dimension( YSIZE, (n-1)*XSTEP + XSIZE );
		
		default:
			throw new InternalError("direction �̒l���s���ł��F" + direction);
		}
	}
}
