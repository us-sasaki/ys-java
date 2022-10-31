package ys.game.card.bridge.gui;

import java.awt.Dimension;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;

/**
 * ダミーハンドの並べ方によってカードをレイアウトします。
 *
 * @version		a-release		8, May 2000
 * @author		Yusuke Sasaki
 */
public class DummyHandLayout implements EntityLayout {
//	protected GuiedPacket parent;
	
/*-------------
 * Constructor
 */
	/**
	 */
	public DummyHandLayout() {
//		parent = null;
	}
	
/*----------------------------
 * implements (EntityLayout)
 */
	/**
	 * layoutを行う.
	 */
	public void layout(Entities target) {
		if (!(target instanceof GuiedPacket))
			throw new IllegalArgumentException("DummyHandLayout は GuiedPacket 専用です。");
		GuiedPacket pack = (GuiedPacket)target;
		
		// begin added 2010/7/18
		CardOrder co = pack.getCardOrder();
		if (!(co instanceof NaturalCardOrder))
			throw new IllegalArgumentException("target に設定されている CardOrder が NaturalCardOrder ではありません");
		int[] suitOrder = ((NaturalCardOrder)co).getSuitOrder();
		// end added 2010/7/18
		
//		if (pack == parent) return;
//		parent = pack;
		
		// added 2010/7/18
		int[] count = countSuits(pack, suitOrder);
		
		// commented out 2010/7/18
		// int[] count = countSuits(pack);
		
		if (count[0] + count[1] + count[2] + count[3] == 0) return;
		
		//
		// サイズ計算用
		//
		int maxCount = Integer.MIN_VALUE;
		for (int i = 0; i < 4; i++) {
			if (maxCount < count[i]) maxCount = count[i];
		}
		
		//
		int xpos, ypos, n = 0;
		int dir = ( pack.getDirection() + 2 ) % 4;
		
		switch (pack.getDirection()) {
		
		case Entity.UPRIGHT:
			 xpos = pack.getX() + 3 * (GuiedCard.XSIZE + 3);;
			
			// それぞれのカードの配置を行う
			for (int i = 0; i < 4; i++) {
				ypos = pack.getY() + (maxCount-1)*GuiedCard.YSTEP;
				for (int j = 0; j < count[i]; j++) {
					Entity ent = pack.getEntity(n++);
					ent.setPosition(xpos, ypos);
					ent.setDirection(dir);
					ypos -= GuiedCard.YSTEP;
				}
				xpos -= GuiedCard.XSIZE + 3;
			}
			pack.setSize( (GuiedCard.XSIZE + 3)*4, GuiedCard.YSIZE + (maxCount-1)*GuiedCard.YSTEP );
			break;
			
		case Entity.RIGHT_VIEW:
			ypos = pack.getY();
			
			for (int i = 0; i < 4; i++) {
				xpos = pack.getX() + (maxCount-1)*GuiedCard.YSTEP;
				
				for (int j = 0; j < count[i]; j++) {
					Entity ent = pack.getEntity(n++);
					ent.setPosition(xpos, ypos);
					ent.setDirection(dir);
					xpos -= GuiedCard.YSTEP;
				}
				ypos += GuiedCard.XSIZE + 3;
			}
			pack.setSize( GuiedCard.YSIZE + (maxCount-1)*GuiedCard.YSTEP, (GuiedCard.XSIZE + 3)*4 );
			break;
		
		case Entity.UPSIDE_DOWN:
			xpos = pack.getX();
			
			// それぞれのカードの配置を行う
			for (int i = 0; i < 4; i++) {
				ypos = pack.getY();
				for (int j = 0; j < count[i]; j++) {
					Entity ent = pack.getEntity(n++);
					ent.setPosition(xpos, ypos);
					ent.setDirection(dir);
					ypos += GuiedCard.YSTEP;
				}
				xpos += GuiedCard.XSIZE + 3;
			}
			pack.setSize( (GuiedCard.XSIZE + 3)*4, GuiedCard.YSIZE + (maxCount-1)*GuiedCard.YSTEP );
			break;
			
		case Entity.LEFT_VIEW:
			ypos = pack.getY() + 3 * (GuiedCard.XSIZE + 3);
			
			for (int i = 0; i < 4; i++) {
				xpos = pack.getX();
				for (int j = 0; j < count[i]; j++) {
					Entity ent = pack.getEntity(n++);
					ent.setPosition(xpos, ypos);
					ent.setDirection(dir);
					xpos += GuiedCard.YSTEP;
				}
				ypos -= GuiedCard.XSIZE + 3;
			}
			pack.setSize( GuiedCard.YSIZE + (maxCount-1)*GuiedCard.YSTEP, (GuiedCard.XSIZE + 3)*4 );
			break;
		
		default:
			throw new InternalError("Direction が不正です");
		}
	}
	
	/**
	 * それぞれのスートの枚数を数える.
	 */
	private int[] countSuits(GuiedPacket pack, int[] suitOrder) {
		// それぞれのスートの枚数を数える
		int[] count = new int[4];
		
		int lastSuit = Integer.MIN_VALUE;
		int suit = -1;
		
		for (int i = 0; i < pack.size(); i++) {
			Card card = pack.peek(i);
			// begin commented out 2010/7/18
			//if (card.getSuit() != lastSuit) {
			//	lastSuit = card.getSuit();
			//	suit++;
			//	if (suit >= 4) suit = 3;
			//}
			//count[suit]++;
			// end commented out 2010/7/18
			count[4 - suitOrder[card.getSuit()]]++;
		}
		
		return count;
	}
	
	/**
	 * 最小の layout サイズを返す.
	 */
	public Dimension layoutSize(Entities target) {
		if (!(target instanceof GuiedPacket))
			throw new IllegalArgumentException("DummyHandLayout は GuiedPacket 専用です。");
		GuiedPacket pack = (GuiedPacket)target;
		
		// begin added 2010/7/18
		CardOrder co = pack.getCardOrder();
		if (!(co instanceof NaturalCardOrder))
			throw new IllegalArgumentException("target に設定されている CardOrder が NaturalCardOrder ではありません");
		int[] suitOrder = ((NaturalCardOrder)co).getSuitOrder();
		// end added 2010/7/18
		
		// added 2010/7/18
		int[] count = countSuits(pack, suitOrder);
		
		// commented out 2010/7/18
		// int[] count = countSuits(pack);
		
		//
		// サイズ計算用
		//
		int maxCount = Integer.MIN_VALUE;
		for (int i = 0; i < 4; i++) {
			if (maxCount < count[i]) maxCount = count[i];
		}
		switch (pack.getDirection()) {
		
		case Entity.UPRIGHT:
		case Entity.UPSIDE_DOWN:
			return new Dimension( (GuiedCard.XSIZE + 3)*4, GuiedCard.YSIZE + (maxCount-1)*GuiedCard.YSTEP );
			
		case Entity.RIGHT_VIEW:
		case Entity.LEFT_VIEW:
			return new Dimension( GuiedCard.YSIZE + (maxCount-1)*GuiedCard.YSTEP, (GuiedCard.XSIZE + 3)*4 );
		
		default:
			throw new InternalError("Direction が不正です");
		}
	}
	
}
