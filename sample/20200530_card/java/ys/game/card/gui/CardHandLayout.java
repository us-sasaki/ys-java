package ys.game.card.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

/**
 * カードのハンドを表示するためのレイアウト。カードが重なって表示される。
 * カード(GuiedCard)以外の Entity に対して使用することは考えていない。
 * また、Entities に含まれている Card の向きはすべてこのlayoutの向きから
 * 見て縦向きであるとみなす。
 *
 * @version		a-release		8, May 2000
 * @author		Yusuke Sasaki
 */
public class CardHandLayout implements EntityLayout {
	
	/** カードを縦に見たときの最小横幅 */
	protected static final int XSTEP = GuiedCard.XSTEP;
	
	/** カードを縦に見たときの最小縦幅 */
	protected static final int YSTEP = GuiedCard.YSTEP;
	
	/** カードを縦に見たときの横幅 */
	protected static final int XSIZE = GuiedCard.XSIZE;
	
	/** カードを縦に見たときの縦幅 */
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
			// direction == 0 の場合の実装
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
			// direction == 1 の場合の実装
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
			// direction == 2 の場合の実装
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
			// direction == 1 の場合の実装
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
			throw new InternalError("direction の値が不正です：" + direction);
		}
	}
	
	/**
	 * size を計算する。
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
			throw new InternalError("direction の値が不正です：" + direction);
		}
	}
}
