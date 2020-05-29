package ys.game.card.bridge.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import ys.game.card.gui.*;

/**
 * トリックが終了した後、勝ったカードのブリンク、消去、WinnerGui への表示を行うクラスです。
 *
 * @version		making		17, May 2000
 * @author		Yusuke Sasaki
 */
public class TrickAnimation implements MouseListener {
	protected GuiedBoard	board;
	protected BridgeField	field;
	protected WinnerGui		winnerGui;
	protected volatile boolean	clicked;
	
/*--------
 * 設定値
 */
	/** トリック終了時の点滅を行うときの待ち時間(msec) */
	protected static int		blinkWaitTime	= 300;
	
	/** トリック終了時の点滅回数 */
	protected static int		blinkCount		= 12;
	
	/** 裏返し待ち時間 */
	protected static int		turnWaitTime	= 500;
	
/*-------------
 * constructor
 */
	public TrickAnimation(GuiedBoard board) {
		Field f = board.getField();
		if (f == null)
			throw new RuntimeException("GuiedBoard が Field に add されていません。");
		if (!(f instanceof BridgeField))
			throw new RuntimeException("GuiedBoard の Field が BridgeField ではありません。");
		
		this.board	= board;
		field		= (BridgeField)f;
		winnerGui	= board.winnerGui;
		clicked		= false;
	}
	
	/**
	 * トリックを取ったあとのアニメーションを表示します。
	 */
	public void start() {
		field.getCanvas().addMouseListener(this);
		
		GuiedTrick trickGui = board.trickGui;
		GuiedTrick newTrickGui = (GuiedTrick)(board.getTrick()); // 現在のトリック
		
		if (trickGui != null) {
			//------------
			// 向きの設定
			//------------
			int d = (4 + trickGui.getLeader() - trickGui.getDirection() ) % 4;
			for (int i = 0; i < trickGui.size(); i++) {
				Entity ent = trickGui.getEntity(i);
				ent.setDirection((10 - i - d )%4);
			}
			board.layout();
			trickGui.layout();
			field.repaint();
		}
		
		if ( (trickGui != null)&&(trickGui.isFinished()) ) {
			//--------------------------------------
			// 表示中のトリックが終了した場合の処理
			//--------------------------------------
			
			//
			// クリックを待つ
			//
			waitClick((GuiedCard)(trickGui.getWinnerCard()));
			clicked = false;
			
			//
			// 裏返し、向きを設定する
			//
			int dir = ((trickGui.getWinner() ^ board.getDirection()) & 1) + 2;
			for (int i = 0; i < 4; i++) {
				GuiedCard card = (GuiedCard)(trickGui.peek(i));
				card.turn(false);
				card.setDirection(dir);
			}
			trickGui.layout();
			field.repaint();
			
			//
			// 待つ
			//
			sleep(turnWaitTime);
			
			//
			// 消去して TrickGui に追加
			//
			board.removeEntity(trickGui);
			
			if (( (trickGui.getWinner() ^ board.getDirection()) & 1) == 0)
				board.winnerGui.add(WinnerGui.WIN);
			else
				board.winnerGui.add(WinnerGui.LOSE);
			
			//
			// 次の trickGui を設定
			//
			if (trickGui != newTrickGui) { // 最終トリックのみ == となる
				trickGui = board.trickGui = newTrickGui;
				board.addEntity(newTrickGui);
			}
		}
		board.layout();
		if (trickGui != null) trickGui.layout();
		field.repaint();
		
		//
		// 終了処理
		//
		field.getCanvas().removeMouseListener(this);
	}
	
	/**
	 * 指定された時間 sleep します。ただし、clicked の場合すぐリターンします。
	 *
	 * @param		time		sleep時間(msec)
	 */
	private void sleep(long time) {
		if (clicked) return;
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void waitClick(GuiedCard toBlink) {
		synchronized (this) {
			clicked = false;
			int blinkcnt = 0;
			for (int i = 0; i < blinkCount; i++) { // 最大で 300 * 12 msec 待つ
				try {
					wait(blinkWaitTime);
					if (blinkcnt < (blinkCount / 3)) {
						toBlink.setVisibility(!toBlink.getVisibility());
						field.repaint();
						blinkcnt++;
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				if (clicked) break;
			}
			toBlink.setVisibility(true);
		}
	}
	
	public static void setBlinkWaitTime(int msec) {
		blinkWaitTime	= msec;
	}
	
	public static void setBlinkCount(int times) {
		blinkCount		= times;
	}
	
	public static void setTurnWaitTime(int msec) {
		turnWaitTime	= msec;
	}
	
/*----------------------------
 * implements (MouseListener)
 */
	public void mouseClicked(MouseEvent me) {
	}
	
	public void mousePressed(MouseEvent me) {
		synchronized (this) {
			clicked = true;
			notifyAll();
		}
	}
	
	public void mouseReleased(MouseEvent me) {
	}
	
	public void mouseEntered(MouseEvent me) {
	}
	
	public void mouseExited(MouseEvent me) {
	}
	
}
