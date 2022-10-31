package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;

/**
 * PlayMain �𐮗����� Board ��i�߂�I�u�W�F�N�g���쐬���邱�ƂƂ���B
 * ���̃I�u�W�F�N�g�̓A�v���b�g�Ŏg�p����邱�Ƃ�O��Ƃ���B
 * �����I�� GUI �I�v�V�����A�v���C���e����@�\��񋟂��������B
 * �Ƃ肠�����A��{�@�\�݂̂���������B
 *
 * �����I�ɁA
 * GUI �I�v�V�����A�v���C���e����@�\�Ƃ��ẮA�����܂߂���悤�ɂ�����
 * �E�v���C�i�r�Q�[�V����(����B����C�x���g�ł��݂ꂪ��������B)
 * �����f
 * ���_�u���_�~�[���[�h
 * �E���_�̐ݒ�
 * �E�����I�I�[�v�����[�h
 * �E�J�[�h�̗��̖͗l�ݒ�
 * �E�_�Ŏ��ԂȂǁAwait �̐ݒ�
 * �EUndo
 * �E�����̋@�\�̗L�����A������
 */
public class BoardManager implements MouseListener, ActionListener, Runnable {

/*-----------
 * Constants
 */
	/** interrupt() �̗��R�������萔�ŁA���f��\���܂� */
	static final int		QUIT			= 1;
	
	static final int		PLAYER_CHANGE	= 2;
	
/*--------------------
 * instance variables
 */
	/**
	 * �e Container �ŁA�\�������ꏊ�ł���B
	 */
	protected Container		display;
	
	/**
	 * �\���G���A�ɓ\��t������ BridgeField �ł���
	 */
	protected BridgeField	field;
	
	/**
	 * �i�s�ΏۂƂȂ�{�[�h
	 */
	protected GuiedBoard	board;
	
	/**
	 * �v���C���[�A���S���Y��
	 */
	protected Player[]		player;
	
	protected Button		quit;
	protected Thread		runner;
	protected int			interruptReason;
	
	protected YesNoDialog	confirmDialog;
	
/*-------------
 * Constructor
 */
	public BoardManager(Container display) {
		player = new Player[4];
		
		this.display	= display;
		
		// ��ʂ̑�g�ł��� BridgeField ��ݒ肷��B
		field	= new BridgeField(display);
		
		board = new GuiedBoard(new BoardImpl(1));
		field.addEntity(board);
		board.setPosition(0, 0);
		board.setDirection(0);
		
		//
		// ���C�A�E�g�ݒ�
		//
//		display.setLayout(null);
		
		// BridgeField ��e Container �ɒǉ�����
		display.add(field.getCanvas());
		field.getCanvas().requestFocus();
		
		// �X���b�h���J�n����
		runner = new Thread(this);
		runner.start();
	}
	
/*----------------------------
 * implements (MouseListener)
 */
	/**
	 * �}�E�X���N���b�N���ꂽ�ꍇ�A���̃I�u�W�F�N�g�� wait() ���J�����܂��B
	 */
	public void mouseClicked(MouseEvent me) {
		synchronized (this) {
			notifyAll();
		}
	}
	
	public void mousePressed(MouseEvent me) { }
	public void mouseReleased(MouseEvent me) { }
	public void mouseEntered(MouseEvent me) { }
	public void mouseExited(MouseEvent me) { }
	
/*-----------------------------
 * implements (ActionListener)
 */
	public void actionPerformed(ActionEvent e) {
	}
	
/*--------------
 * �ݒ胁�\�b�h
 */
	/**
	 * ���������s���܂��B
	 */
	public void init() {
	}
	
	/**
	 * init()�Ƒ΂ɂȂ郁�\�b�h
	 */
	public void destroy() {
	}
	
	/**
	 * new �Ƒ΂ɂȂ郁�\�b�h
	 */
	public void dispose() {
		if (field != null)
			field.removeEntity(board);
		if (confirmDialog != null) confirmDialog.disposeDialog();
		if (player != null) {
			for (int i = 0; i < player.length; i++) {
				if ((player[i] != null)&&(player[i] instanceof HumanPlayer)) {
					((HumanPlayer)player[i]).dispose();
				}
			}
		}
		
		if (runner != null) runner = null;
	}
	
	/**
	 * �v���C�𒆒f���܂�
	 */
	public void quit() {
		if (runner != null) {
			interruptReason |= QUIT;
			runner.interrupt();
		}
	}
	
	/**
	 * ���� BoardManager GUI �𗘗p���� HumanPlayer �̃C���X�^���X���쐬���܂��B
	 *
	 * HumanPlayer�̔j���̃^�C�~���O�͒N�������Ă���̂��H�H
	 * finalize() �݂̂Ǝv����B
	 */
	public HumanPlayer getHumanPlayerInstance() {
		if (field == null) return null;
		return new HumanPlayer(board, field, Board.NORTH);
	}
	
	/**
	 * �v���C���[��ݒ肵�܂��B
	 * �ݒ肳�ꂽ�v���C���[�C���X�^���X�� setBoard(), setMySeat() �������I�ɌĂ΂�܂��B
	 */
	public void setPlayer(Player p, int seat) {
		p.setMySeat(seat);
		p.setBoard(board);
		
		player[seat] = p;
		
		interruptReason |= PLAYER_CHANGE;
		runner.interrupt();
	}
	
	public GuiedBoard getBoard() {
		return board;
	}
	
/*------------
 * �{�[�h�i�s
 */
	/**
	 * board ��i�s�����܂��B
	 */
	public void run() {
		while (true) {
			Thread.interrupted(); // interrupt�X�e�[�^�X�N���A
			
			try {
				deal();
				bid();
				play();
				scoring();
			} catch (InterruptedBridgeException e) {
				// ���f�{�^����������A�I�����I�����ꂽ�ꍇ
				field.removeSpot();
				field.repaint();
				break;
			}
		}
	}
	
	protected void deal() {
		board.reset(1);
		board.deal();
	}
	
	protected void bid() {
		// �K���ɂP�r�ƂȂ������Ƃɂ��Ă����B
		board.setContract(new Bid(Bid.BID, 1, Bid.SPADE), Board.EAST);
	}
	
	/**
	 * ���C�����[�v
	 */
	protected void play() throws InterruptedBridgeException {
		while (true) {
			//
			// Spot ���w�肷��
			//
			field.setSpot(board.getTurn());
			field.repaint();
			
			Object c = null;
			while (c == null) {
				try {
					Player p = null;
					// player �� null �̊ԁA�u���b�N����
					synchronized (player) {
						while ( (p = player[board.getPlayer()]) == null) {
							player.wait(); // may throw InterruptedException();
						}
					}
					c = p.play(); // �u���b�N����
				} catch (InterruptedException e) {
					if ((interruptReason & QUIT) != 0) {
						interruptReason ^= QUIT;
						if (confirmQuit()) throw new InterruptedBridgeException();
					}
					// PLAYER_CHANGE �̏ꍇ�A������x
					if ((interruptReason & PLAYER_CHANGE) != 0)
						interruptReason ^= PLAYER_CHANGE;
				}
			}
			board.play(c);
			field.repaint();
			
			if (board.getStatus() == Board.SCORING) break;
		}
	}
	
	/**
	 *
	 */
	protected void scoring() {
	}
	
	/**
	 * �ĕ`����s�킹�܂��B
	 */
	public void repaint() {
		if (field != null) field.repaint();
	}
	
	/**
	 * ���f�{�^���������ꂽ�Ƃ��ɕ\������A���f�m�F�_�C�A���O�\������
	 */
	protected boolean confirmQuit() {
		try {
			confirmDialog = new YesNoDialog(
								field.getCanvas(),
								"���̃{�[�h��j�����Ē��f���܂����H");
			confirmDialog.show();
			boolean yes = confirmDialog.isYes();
			confirmDialog.disposeDialog();
			confirmDialog = null;
			return yes;
		} catch (Exception e) {
			// �u���E�U�̋����I���̏ꍇ
			return true;
		}
	}
	
	/**
	 * �N���b�N�����܂ő҂��܂��B
	 * ���̃��\�b�h�́A�T�u�N���X�Ńi�r�Q�[�V�����ȂǂɎg�p���邱�Ƃ����҂��Ă��܂��B
	 * �{�N���X���ł͎g�p���܂���B
	 * interrupt() ���\�b�h���R�[�����ꂽ�ꍇ�AInterruptedException ���X���[����܂��B
	 */
	protected void waitClick() throws InterruptedException {
		synchronized (this) {
			Canvas canvas = field.getCanvas();
			canvas.addMouseListener(this);
			try {
				wait();
				canvas.removeMouseListener(this);
			} catch (InterruptedException e) {
				canvas.removeMouseListener(this);
				throw new InterruptedException();
			}
		}
	}
	
}
