package ys.game.card.bridge.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import ys.game.card.gui.*;

/**
 * �g���b�N���I��������A�������J�[�h�̃u�����N�A�����AWinnerGui �ւ̕\�����s���N���X�ł��B
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
 * �ݒ�l
 */
	/** �g���b�N�I�����̓_�ł��s���Ƃ��̑҂�����(msec) */
	protected static int		blinkWaitTime	= 300;
	
	/** �g���b�N�I�����̓_�ŉ� */
	protected static int		blinkCount		= 12;
	
	/** ���Ԃ��҂����� */
	protected static int		turnWaitTime	= 500;
	
/*-------------
 * constructor
 */
	public TrickAnimation(GuiedBoard board) {
		Field f = board.getField();
		if (f == null)
			throw new RuntimeException("GuiedBoard �� Field �� add ����Ă��܂���B");
		if (!(f instanceof BridgeField))
			throw new RuntimeException("GuiedBoard �� Field �� BridgeField �ł͂���܂���B");
		
		this.board	= board;
		field		= (BridgeField)f;
		winnerGui	= board.winnerGui;
		clicked		= false;
	}
	
	/**
	 * �g���b�N����������Ƃ̃A�j���[�V������\�����܂��B
	 */
	public void start() {
		field.getCanvas().addMouseListener(this);
		
		GuiedTrick trickGui = board.trickGui;
		GuiedTrick newTrickGui = (GuiedTrick)(board.getTrick()); // ���݂̃g���b�N
		
		if (trickGui != null) {
			//------------
			// �����̐ݒ�
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
			// �\�����̃g���b�N���I�������ꍇ�̏���
			//--------------------------------------
			
			//
			// �N���b�N��҂�
			//
			waitClick((GuiedCard)(trickGui.getWinnerCard()));
			clicked = false;
			
			//
			// ���Ԃ��A������ݒ肷��
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
			// �҂�
			//
			sleep(turnWaitTime);
			
			//
			// �������� TrickGui �ɒǉ�
			//
			board.removeEntity(trickGui);
			
			if (( (trickGui.getWinner() ^ board.getDirection()) & 1) == 0)
				board.winnerGui.add(WinnerGui.WIN);
			else
				board.winnerGui.add(WinnerGui.LOSE);
			
			//
			// ���� trickGui ��ݒ�
			//
			if (trickGui != newTrickGui) { // �ŏI�g���b�N�̂� == �ƂȂ�
				trickGui = board.trickGui = newTrickGui;
				board.addEntity(newTrickGui);
			}
		}
		board.layout();
		if (trickGui != null) trickGui.layout();
		field.repaint();
		
		//
		// �I������
		//
		field.getCanvas().removeMouseListener(this);
	}
	
	/**
	 * �w�肳�ꂽ���� sleep ���܂��B�������Aclicked �̏ꍇ�������^�[�����܂��B
	 *
	 * @param		time		sleep����(msec)
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
			for (int i = 0; i < blinkCount; i++) { // �ő�� 300 * 12 msec �҂�
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
