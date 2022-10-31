package ys.game.card.bridge.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import ys.game.card.gui.*;

/**
 * �g���b�N��\�����邽�߂̃��C�A�E�g�ł��B�J�[�h���㉺���E�ɔz�u����܂��B
 * �J�[�h(GuiedCard)�ȊO�� Entity �ɑ΂��Ďg�p���邱�Ƃ͍l���Ă��Ȃ��B
 *
 * @version		a-release		17, May 2000
 * @author		Yusuke Sasaki
 */
public class TrickLayout implements EntityLayout {
	
/*-------------
 * Constructor
 */
	public TrickLayout() {
	}
	
/*----------------------------
 * implements (EntityLayout)
 */
	/**
	 * TrickLayout �ł́Atarget �̃T�C�Y��ύX���܂���B
	 */
	public void layout(Entities target) {
		if (!(target instanceof GuiedTrick))
			throw new IllegalArgumentException("TrickLayout �� GuiedTrick ��p�ł��B");
		
		GuiedTrick trick = (GuiedTrick)target;
		
		int direction	= trick.getDirection();
		int leader		= trick.getLeader();
		int size		= trick.size();
		int xpos		= trick.getX();
		int ypos		= trick.getY();
		
		int d = (4 + leader - direction) % 4;
		
		for (int i = 0; i < size; i++) {
			Entity ent = trick.getEntity(i);
			
			switch ( (i+d)%4 ) {
			
			case 0: // ��
				ent.setPosition(xpos + (GuiedTrick.WIDTH - ent.getWidth())/2,
								ypos);
//				ent.setDirection(Entity.UPSIDE_DOWN);
//	���������ƁATrickAnimation �ł̏����C�����̂Ƃ��̌����̐ݒ肪�ł��Ȃ��Ȃ�
				break;
			case 1: // �E
				ent.setPosition(xpos + (GuiedTrick.WIDTH - ent.getWidth()),
								ypos + (GuiedTrick.HEIGHT - ent.getHeight())/2);
//				ent.setDirection(Entity.RIGHT_VIEW);
				break;
			case 2: // ��
				ent.setPosition(xpos + (GuiedTrick.WIDTH - ent.getWidth())/2,
								ypos + (GuiedTrick.HEIGHT - ent.getHeight()) );
//				ent.setDirection(Entity.UPRIGHT);
				break;
			case 3: // ��
				ent.setPosition(xpos,
								ypos + (GuiedTrick.HEIGHT - ent.getHeight())/2);
//				ent.setDirection(Entity.LEFT_VIEW);
				break;
			default:
				throw new InternalError();
			}
		}
	}
	
	/**
	 * size ���v�Z����B
	 */
	public Dimension layoutSize(Entities target) {
		return new Dimension( target.getWidth(), target.getHeight() );
	}
}
