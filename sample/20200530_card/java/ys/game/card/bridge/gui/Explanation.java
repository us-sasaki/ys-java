package ys.game.card.bridge.gui;

import java.awt.*;
import java.util.Vector;
import java.util.StringTokenizer;

import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * �u���b�W�V�~�����[�^�ɂ�����J�n���̐����̊G�𐶐����� Entity �ł��B
 * ���� Entity �ɂ́A�R���g���N�g�̓��e�Ȃǂ̐����������\������܂��B
 *
 * @version		a-release		August 10, 2000
 * @author		Yusuke Sasaki
 */
public class Explanation extends Entity implements Runnable {
	public static final int DELIGHTED = 1;
	public static final int SAD = 2;
	
	protected static final Font FONT = new Font("Dialog", Font.PLAIN, 13); //MS Gothic
	protected static final Color MSG_COLOR = new Color(255, 255, 200);
	
	protected BridgeField	field;
	protected String[]		message;
	protected int			picNumber;
	
	protected Thread		animate;
	protected int			face;
	protected volatile boolean animated;
	
	/** �`����J�n���邘���W */
	protected int			x0;
	
	/** �`����J�n���邙���W */
	protected int			y0;
	
	/** ���̍s�܂ł̃X�e�b�v */
	protected int			yStep;
	
	/** ���������̕��ƍ��� */
	protected int			width, height;
	
	/** �ӂ������̕��ƍ��� */
	protected int			mw, mh;
	
	/** �ӂ������̒��_�̒��� */
	protected int[]			xp, yp;
	
	/** ���݂� */
	public static MediaLoader	media;
	
/*-------------
 * Constructor
 */
	/**
	 * �w�肵���R���g���N�g�ł��邱�Ƃ�������� Entity ���쐬���܂��B
	 *
	 * @param		msg		�\�����郁�b�Z�[�W
	 */
	public Explanation(BridgeField field, String msg) {
		super();
		this.field = field;
		
		//
		// �^����ꂽ����������s�ŋ�؂�A�z��ɕϊ�����
		//
		Vector v = new Vector();
		StringTokenizer t = new StringTokenizer(msg, "\n", false);
		while (t.hasMoreTokens()) {
			v.addElement(t.nextToken());
		}
		message = new String[v.size()];
		v.copyInto(message);
		
		//
		// �傫�������肷��
		//
		Graphics g = field.getCanvas().getGraphics();
		g.setFont(FONT);
		FontMetrics metrics = g.getFontMetrics();
		
		for (int i = 0; i < message.length; i++) {
			int wid = metrics.stringWidth(message[i]);
			if (width < wid) width = wid;
		}
		width	+= 20;
		height	= metrics.getHeight() * message.length + 20;
		yStep	= metrics.getHeight();
		
		setBounds(140, 120, 360, 240);
		x0 = 130 + 40;
		y0 = 90 + 20 + metrics.getAscent() + 100 - yStep * message.length * 2 / 3;
		int msgy0 = 100 + 100 - yStep * message.length * 2 / 3;
		mw = 380 - 40;
		mh = yStep * message.length + 20;
		xp = new int[] {x0-20,	x0-20+mw,	x0-20+mw,	410,	405,	390,	x0-20};
		yp = new int[] {msgy0,	msgy0,		msgy0+mh,	msgy0+mh,	msgy0+mh+10,	msgy0+mh	,msgy0+mh};
		
		picNumber = 0;
	}
	
/*-----------
 * Overrides
 */
	protected static final Color LINE_COLOR = new Color(200, 255, 200);
	public void draw(Graphics g) {
		g.setColor(LINE_COLOR);
		for (int y0 = y; y0 < y + h; y0 += 2) {
			g.drawLine(x, y0, x+w, y0);
		}
//		g.setColor(Color.white);
//		g.fillRect(x, y, w, h);
//		g.setColor(Color.black);
//		g.drawRect(x, y, w, h);
		
		g.setColor(MSG_COLOR);
		g.fillPolygon(xp, yp, xp.length);
		g.setColor(Color.black);
		g.drawPolygon(xp, yp, xp.length);
		
		g.setColor(Color.black);
		g.setFont(FONT);
		int y = y0;
		
		for (int i = 0; i < message.length; i++) {
			g.drawString(message[i], x0, y);
			y += yStep;
		}
		java.awt.image.ImageObserver obs = null;
		if (field != null) obs = field.getCanvas();
		
		g.drawImage(media.getImage("sumire"+picNumber), 400, 260, obs);
	}
	
	/**
	 * �A�j���[�V�����Ŏg�p���Ă����X���b�h���I�������܂��B
	 */
	public void removed(Entities parent) {
		animated = false;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ��������A��������̃A�j���[�V������\������B
	 * �A�j���[�V�����X���b�h�� remove ���ꂽ��Ɏ��s���~����B
	 *
	 * @param		face		��(1..�΂���, 2..������)
	 */
	public void animate(int face) {
		if (animate == null) {
			this.face = face;
			animate = new Thread(this);
			animate.start();
		}
	}
	
/*-----------------------
 * implements (Runnable)
 */
	public void run() {
		animated = true;
		while (true) {
			picNumber ^= face;
			Field f = getField();
			if (f != null) f.repaint();
			if (picNumber > 0) sleep(500);
			else sleep(1000);
			if (!animated) break;
		}
		animate = null;
	}
	
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ignored) {
		}
	}
}
