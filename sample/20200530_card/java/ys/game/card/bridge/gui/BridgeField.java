package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * GuiedBoard, GuiedPacket �Ȃǂ̃u���b�WGUI�� Field ��񋟂��܂��B
 * Field �ɒǉ������@�\�Ƃ��āA�N�̔Ԃ��������X�|�b�g���C�g�A�J�[�h�N���b�N
 * ���o������܂��B
 *
 * @version		a-release		21, May 2000
 * @author		Yusuke Sasaki
 */
public class BridgeField extends Field implements MouseListener {
	protected static final int WIDTH	= 640;
	protected static final int HEIGHT	= 480;
	
	protected volatile GuiedCard selectedCard;
	protected int spot = -1;
	
	public BridgeField(Component peeredComponent) {
		super(peeredComponent, WIDTH, HEIGHT);
		
		selectedCard = null;
		getCanvas().addMouseListener(this);
	}
	
/*----------------------------
 * implements (MouseListener)
 */
	public void mouseClicked(MouseEvent me) {
	}
	
	public void mousePressed(MouseEvent me) {
		synchronized (this) {
			int x = me.getX();
			int y = me.getY();
			Entity ent = getEntityAt(x, y);
			if (ent == null) return;
			if (!(ent instanceof GuiedCard)) return;
			selectedCard = (GuiedCard)ent;
			notify();
		}
	}
	
	public void mouseReleased(MouseEvent me) {
	}
	
	public void mouseEntered(MouseEvent me) {
	}
	
	public void mouseExited(MouseEvent me) {
	}
	
/*------------------
 * instance methods
 */
	/**
	 * BridgeField �ł́A�X�|�b�g���C�g�̂悤�Ȗ��邢�����ŒN�̔Ԃ��������܂��B
	 * spot �ɂ́ABoard.NORTH �Ȃǂ̒l���w�肵�܂��B
	 */
	public void setSpot(int spot) {
		this.spot = spot;
	}
	
	/**
	 * �X�|�b�g���C�g�������܂��B
	 */
	public void removeSpot() {
		setSpot(-1);
	}
	
	/**
	 * �J�[�h���N���b�N����鑀���҂��܂��B
	 * �{���\�b�h�ł̓N���b�N���s���邩�A���荞�݂���������܂ŏ������u���b�N����܂��B
	 * �u���b�N���Ainterrupt() ���ꂽ�ꍇ(���f�{�^��)�AInterruptedException ���X���[���܂��B
	 */
	public GuiedCard waitCardSelect() throws InterruptedException {
		synchronized (this) {
			selectedCard = null;
			
			while (selectedCard == null) {
				wait();
			}
		}
		return selectedCard;
	}
	
	private static final Color BACK_COLOR = new Color(60, 120, 30);
	
	/**
	 * �u���b�W�e�[�u����`�悵�܂��B
	 * �u���b�W�e�[�u���́A�΂̃O���f�[�V�����ŁA�N�̔Ԃ��������X�|�b�g���C�g������܂��B
	 *
	 * @param		g		�`����s���O���t�B�b�N�R���e�N�X�g
	 */
	protected void drawBackground(Graphics g) {
		Rectangle	rec = new Rectangle(0, 0, WIDTH, HEIGHT); //getCanvas().getBounds();
		
		//
		// �o�b�N(�΂̃O���f�[�V����)
		//
		float step = ((float)rec.height) / 40;
		for (int i = 0; i < 40; i++) {
			g.setColor(new Color(40+i/2, 80+i, 30));
			g.fillRect(rec.x, rec.y+(int)(i*step), rec.width, (int)(step+1));
		}
		
		//
		// ���Ԃ������X�|�b�g���C�g
		//
		if (spot == -1) return;
		
		int d = (spot + direction)%4;
		int r, gr, b, x, y;
		
		switch (d) {
		
		default:
		case 0: // ��
			x = WIDTH / 2; y = 40;
			r = 42; gr = 84; b = 30;
			break;
			
		case 1: // �E
			x = WIDTH - 40; y = HEIGHT /2;
			r = 50; gr = 100; b = 30;
			break;
			
		case 2: // ��
			x = WIDTH / 2; y = HEIGHT - 40;
			r = 48; gr = 116; b = 30;
			break;
			
		case 3: // ��
			x = 40; y = HEIGHT / 2;
			r = 50; gr = 100; b = 30;
			break;
			
		}
		
		for (int radius = 95; radius > 35; radius -= 4) {
			g.setColor(new Color(r, gr, b));
			g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
			r += 5; gr += 4; b += 4;
		}
	}

}
