package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;

/**
 * ユーザに対して、「はい」「いいえ」ボタンを表示し、決定を促するダイアログです。
 * コンストラクタの中に決定内容を設定すると、その内容が表示されます。
 * 表示内容は１行で表示されます。
 *
 * @version		a-release		15, July 2001
 * @author		Yusuke Sasaki
 */
public class YesNoDialog extends Dialog implements WindowListener, ActionListener {
	protected Label		message;
	protected Button	yes;
	protected Button	no;
	protected int		result;
	
/*-------------
 * Constructor
 */
	public YesNoDialog(Component parent, String message) {
		super(topLevelFrame(parent), "どうしますか", true);
		
		setLayout(null); //new GridLayout(2, 1));
		
		this.message = new Label(message);
		add(this.message);
		yes = new Button("はい");
		add(yes);
		no = new Button("いいえ");
		add(no);
		
		pack(); // create peer
		
		Dimension d = this.message.getPreferredSize();
		setSize(d.width + 100, d.height + 100);
		this.message.setBounds(50, 35, d.width, d.height);
		yes.setBounds( (d.width + 100)/2 - 110, 70, 100, 24 );
		no.setBounds( (d.width + 100)/2 + 10, 70, 100, 24 );
		
		setResizable(false);
		
		Rectangle r = getParent().getBounds();
		Dimension b = getSize();
		
		setLocation(r.x + (r.width - b.width) / 2, r.y + (r.height - b.height) / 2);
		
		addWindowListener(this);
		yes.addActionListener(this);
		no.addActionListener(this);
		
		result = -1;
	}
	
/*-----------------------------
 * implements (WindowListener)
 */
	public void windowActivated(WindowEvent e) { }
	public void windowOpened(WindowEvent e) { }
	public void windowClosing(WindowEvent e) {
//		dispose();
	}
	public void windowClosed(WindowEvent e) { }
	public void windowIconified(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	
/*-----------------------------
 * implements (ActionListener)
 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == yes) result = 1;
		else result = 0;
		hide();
	}
	
/*-----------
 * overrides
 */
	/**
	 * IEにおいて、interrupt()においてモーダルダイアログの show() が抜けてしまう
	 * 現象を回避するためのオーバーライド。
	 */
	public void show() {
		while (result == -1) {
			super.show();
		}
	}
	
	public void disposeDialog() {
		// IE JavaVM 4.0 対応(林さん指摘分、Dialog の dispose() が使用できない)
		result = 0;
		hide();
		//dispose();
	}
	
/*------------------
 * instance methods
 */
	public boolean isYes() {
		return (result == 1);
	}

/*---------------
 * class methods
 */
	private static Frame topLevelFrame(Component comp) {
		while (true) {
			Component parent = comp.getParent();
			if (parent instanceof Frame) return (Frame)parent;
			if (parent == null) {
				return null;
//				return (Frame)comp;
			}
			comp = parent;
		}
	}
	
}
