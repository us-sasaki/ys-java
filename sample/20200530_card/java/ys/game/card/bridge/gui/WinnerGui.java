package ys.game.card.bridge.gui;

import java.awt.Panel;
import java.awt.Graphics;
import java.awt.Color;

import ys.game.card.gui.*;

/**
 * トリックの勝敗をカードの縦横で示す Entity です。
 *
 * @version		a-release		11, May 2000
 * @author		Yusuke Sasaki
 */
public class WinnerGui extends Entities {
	protected static final boolean	WIN			= true;
	protected static final boolean	LOSE		= false;
	protected static final int		LONGER_EDGE = 48;
	protected static final int		SHORTER_EDGE = 32;
	protected static final int		SLIDE_STEP	= 8;
//	protected static final Color	EDGE_COLOR	= new Color(100, 64, 0);
//	protected static final Color	COLOR		= new Color(255, 230, 200);
	
	protected WinnerCard[]	card;
	protected int			count;
	
/*-------------
 * Constructor
 */
	/**
	 * WinnerGUI を作成する.
	 */
	public WinnerGui() {
		card = new WinnerCard[13];
		count = 0;
		setLayout(null);
		setSize(SLIDE_STEP * 12 + LONGER_EDGE, LONGER_EDGE);
	}
	
/*------------------
 * instance methods
 */
	public void add(boolean win) {
		card[count] = new WinnerCard(win);
		card[count].setPosition(x+count * SLIDE_STEP,
								(win)?y:(y + LONGER_EDGE / 4));
		addEntity(card[count]);
		count++;
	}
	
/*-------------
 * inner class
 */
	/**
	 * この内部クラスは縦、または横の裏返されたカードを表す.
	 */
	protected class WinnerCard extends Entity {
		protected boolean win;
		
		protected WinnerCard(boolean win) {
			this.win = win;
			if (win) {
				// 縦
				setSize(SHORTER_EDGE, LONGER_EDGE);
			}
			else {
				// 横
				setSize(LONGER_EDGE, SHORTER_EDGE);
			}
		}
		
		public void draw(Graphics g) {
/*			if (win) {
				g.setColor(EDGE_COLOR);
				g.drawRect(x, y, SHORTER_EDGE - 1, LONGER_EDGE - 1);
				g.setColor(COLOR);
				g.fillRect(x+1, y+1, SHORTER_EDGE - 2, LONGER_EDGE - 2);
			}
			else {
				g.setColor(EDGE_COLOR);
				g.drawRect(x, y, LONGER_EDGE - 1, SHORTER_EDGE - 1);
				g.setColor(COLOR);
				g.fillRect(x+1, y+1, LONGER_EDGE - 2, SHORTER_EDGE - 2);
			}
*/
			CardImageHolder holder = GuiedCard.getCardImageHolder();
			
			if (holder != null) {
				if (win) {
					g.drawImage(holder.getBackImage(Entity.UPSIDE_DOWN), x, y, w, h, null);
				} else {
					g.drawImage(holder.getBackImage(Entity.RIGHT_VIEW), x, y, w, h, null);
				}
			}
		}
		
	}
}
