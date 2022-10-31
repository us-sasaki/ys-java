package ys.game.card.bridge.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import ys.game.card.gui.*;

/**
 * ボードをデザインするレイアウトです。ハンドが上下左右に配置され、
 * 中央にはトリックが配置されます。
 *
 * @version		a-release		6, May 2000
 * @author		Yusuke Sasaki
 */
public class BoardLayout implements EntityLayout {
	
	/** ハンドの表示位置関連 */
	protected static final int SIDE_MARGIN = 40;
	protected static final int VSIDE_MARGIN = 20;
	
	/** トリックの表示位置 */
	protected static final int TRICK_X = (GuiedBoard.WIDTH - GuiedTrick.WIDTH)/2;
	protected static final int TRICK_Y = (GuiedBoard.HEIGHT - GuiedTrick.HEIGHT)/2;
	
	/** ウィナー表示位置 */
	protected static final int WINNER_X = 460;
	protected static final int WINNER_Y = 400;
	
/*-------------
 * Constructor
 */
	public BoardLayout() {
	}
	
/*----------------------------
 * implements (EntityLayout)
 */
	public void layout(Entities target) {
		if (!(target instanceof GuiedBoard))
			throw new IllegalArgumentException("BoardLayout は GuiedBoard 専用です。");
		
		GuiedBoard board = (GuiedBoard)target;
		
		int direction	= board.getDirection();
		int xpos		= board.getX();
		int ypos		= board.getY();
		
		//
		// ハンドの位置指定
		//
		int d = direction; // この式は意味なし
		
		if (board.handGui != null) {
			for (int i = 0; i < 4; i++) {
				Entities ent = board.handGui[i];
				if (ent == null) continue;
				ent.setDirection((10 - i - d )%4);
				Dimension lsize = ent.getSize();	// 大きさを計算させる
				
				int x, y;
				
				switch ( (i+d)%4 ) {
				case 0:		// 上のハンド
					x = (board.getWidth() - lsize.width) / 2;
					y = VSIDE_MARGIN;
					break;
				case 1:		// 右のハンド
					x = board.getWidth() - lsize.width - SIDE_MARGIN;
					y = (board.getHeight() - lsize.height) / 2;
					break;
				case 2:		// 下のハンド
					x = (board.getWidth() - lsize.width) / 2;
					y = board.getHeight() - lsize.height - VSIDE_MARGIN;
					break;
				case 3:		// 左のハンド
					x = SIDE_MARGIN;
					y = (board.getHeight() - lsize.height) / 2;
					break;
				default:
					throw new InternalError();
				}
				ent.setPosition(xpos + x, ypos + y);
				ent.layout();
			}
		}
		
		//
		// トリックの位置指定
		//
		GuiedTrick trick = board.trickGui;
		
		if (trick != null) {
			trick.setPosition(xpos + TRICK_X, ypos + TRICK_Y);
			trick.layout();
		}
		
		//
		// ウィナーの位置指定
		//
		WinnerGui winnerGui = board.winnerGui;
		if (winnerGui != null) {
			winnerGui.setPosition(xpos + WINNER_X, ypos + WINNER_Y);
		}
		
	}
	
	/**
	 * size を計算する。
	 */
	public Dimension layoutSize(Entities target) {
		return new Dimension( target.getWidth(), target.getHeight() );
	}
}
