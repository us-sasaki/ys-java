package ys.game.card.bridge.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import ys.game.card.gui.*;

/**
 * �{�[�h���f�U�C�����郌�C�A�E�g�ł��B�n���h���㉺���E�ɔz�u����A
 * �����ɂ̓g���b�N���z�u����܂��B
 *
 * @version		a-release		6, May 2000
 * @author		Yusuke Sasaki
 */
public class BoardLayout implements EntityLayout {
	
	/** �n���h�̕\���ʒu�֘A */
	protected static final int SIDE_MARGIN = 40;
	protected static final int VSIDE_MARGIN = 20;
	
	/** �g���b�N�̕\���ʒu */
	protected static final int TRICK_X = (GuiedBoard.WIDTH - GuiedTrick.WIDTH)/2;
	protected static final int TRICK_Y = (GuiedBoard.HEIGHT - GuiedTrick.HEIGHT)/2;
	
	/** �E�B�i�[�\���ʒu */
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
			throw new IllegalArgumentException("BoardLayout �� GuiedBoard ��p�ł��B");
		
		GuiedBoard board = (GuiedBoard)target;
		
		int direction	= board.getDirection();
		int xpos		= board.getX();
		int ypos		= board.getY();
		
		//
		// �n���h�̈ʒu�w��
		//
		int d = direction; // ���̎��͈Ӗ��Ȃ�
		
		if (board.handGui != null) {
			for (int i = 0; i < 4; i++) {
				Entities ent = board.handGui[i];
				if (ent == null) continue;
				ent.setDirection((10 - i - d )%4);
				Dimension lsize = ent.getSize();	// �傫�����v�Z������
				
				int x, y;
				
				switch ( (i+d)%4 ) {
				case 0:		// ��̃n���h
					x = (board.getWidth() - lsize.width) / 2;
					y = VSIDE_MARGIN;
					break;
				case 1:		// �E�̃n���h
					x = board.getWidth() - lsize.width - SIDE_MARGIN;
					y = (board.getHeight() - lsize.height) / 2;
					break;
				case 2:		// ���̃n���h
					x = (board.getWidth() - lsize.width) / 2;
					y = board.getHeight() - lsize.height - VSIDE_MARGIN;
					break;
				case 3:		// ���̃n���h
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
		// �g���b�N�̈ʒu�w��
		//
		GuiedTrick trick = board.trickGui;
		
		if (trick != null) {
			trick.setPosition(xpos + TRICK_X, ypos + TRICK_Y);
			trick.layout();
		}
		
		//
		// �E�B�i�[�̈ʒu�w��
		//
		WinnerGui winnerGui = board.winnerGui;
		if (winnerGui != null) {
			winnerGui.setPosition(xpos + WINNER_X, ypos + WINNER_Y);
		}
		
	}
	
	/**
	 * size ���v�Z����B
	 */
	public Dimension layoutSize(Entities target) {
		return new Dimension( target.getWidth(), target.getHeight() );
	}
}
