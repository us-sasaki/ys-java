package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;

/**
 * Board情報を表示するウィンドウ
 * Singleton パターン
 */
public class TextInfoWindow extends Frame implements WindowListener {
	private static TextInfoWindow theInstance = null;
	
	protected TextArea area;
	
	private TextInfoWindow() {
		super("ハンド情報");
		setLayout(new FlowLayout());
		area = new TextArea("", 25, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
		area.setEditable(false);
		add(area);
		addWindowListener(this);
		setResizable(false);
		pack();
	}
	
	public void setText(String text) {
		this.area.setText(text);
	}
	
/*-----------------------------
 * implements (WindowListener)
 */
	public void windowActivated(WindowEvent e) { }
	public void windowOpened(WindowEvent e) { }
	public void windowClosing(WindowEvent e) {
		show();
		hide();
	}
	public void windowClosed(WindowEvent e) { }
	public void windowIconified(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	
	
	public static synchronized TextInfoWindow getInstance(String text) {
		if (theInstance == null) theInstance = new TextInfoWindow();
		theInstance.setText(text);
		theInstance.show();
		theInstance.setState(Frame.NORMAL);
		theInstance.requestFocus();
		return theInstance;
	}
	
}

