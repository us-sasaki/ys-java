package ys.game.card.bridge.gui;

import java.awt.*;
import java.util.Vector;
import java.util.StringTokenizer;

import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * ブリッジシミュレータにおける開始時の説明の絵を生成する Entity です。
 * この Entity には、コントラクトの内容などの説明書きが表示されます。
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
	
	/** 描画を開始するｘ座標 */
	protected int			x0;
	
	/** 描画を開始するｙ座標 */
	protected int			y0;
	
	/** 次の行までのステップ */
	protected int			yStep;
	
	/** 文字部分の幅と高さ */
	protected int			width, height;
	
	/** ふきだしの幅と高さ */
	protected int			mw, mh;
	
	/** ふきだしの頂点の長さ */
	protected int[]			xp, yp;
	
	/** すみれ */
	public static MediaLoader	media;
	
/*-------------
 * Constructor
 */
	/**
	 * 指定したコントラクトであることを説明する Entity を作成します。
	 *
	 * @param		msg		表示するメッセージ
	 */
	public Explanation(BridgeField field, String msg) {
		super();
		this.field = field;
		
		//
		// 与えられた文字列を改行で区切り、配列に変換する
		//
		Vector v = new Vector();
		StringTokenizer t = new StringTokenizer(msg, "\n", false);
		while (t.hasMoreTokens()) {
			v.addElement(t.nextToken());
		}
		message = new String[v.size()];
		v.copyInto(message);
		
		//
		// 大きさを決定する
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
	 * アニメーションで使用していたスレッドを終了させます。
	 */
	public void removed(Entities parent) {
		animated = false;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * わらったり、泣いたりのアニメーションを表示する。
	 * アニメーションスレッドは remove された後に実行を停止する。
	 *
	 * @param		face		顔(1..笑い顔, 2..泣き顔)
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
