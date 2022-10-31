package ys.game.card.bridge.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import ys.game.card.gui.*;

/**
 * トリックを表示するためのレイアウトです。カードが上下左右に配置されます。
 * カード(GuiedCard)以外の Entity に対して使用することは考えていない。
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
	 * TrickLayout では、target のサイズを変更しません。
	 */
	public void layout(Entities target) {
		if (!(target instanceof GuiedTrick))
			throw new IllegalArgumentException("TrickLayout は GuiedTrick 専用です。");
		
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
			
			case 0: // 上
				ent.setPosition(xpos + (GuiedTrick.WIDTH - ent.getWidth())/2,
								ypos);
//				ent.setDirection(Entity.UPSIDE_DOWN);
//	これをすると、TrickAnimation での勝ち，負けのときの向きの設定ができなくなる
				break;
			case 1: // 右
				ent.setPosition(xpos + (GuiedTrick.WIDTH - ent.getWidth()),
								ypos + (GuiedTrick.HEIGHT - ent.getHeight())/2);
//				ent.setDirection(Entity.RIGHT_VIEW);
				break;
			case 2: // 下
				ent.setPosition(xpos + (GuiedTrick.WIDTH - ent.getWidth())/2,
								ypos + (GuiedTrick.HEIGHT - ent.getHeight()) );
//				ent.setDirection(Entity.UPRIGHT);
				break;
			case 3: // 左
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
	 * size を計算する。
	 */
	public Dimension layoutSize(Entities target) {
		return new Dimension( target.getWidth(), target.getHeight() );
	}
}
