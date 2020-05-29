package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;

import ys.game.card.bridge.Board;

/**
 * リプレイ、ハンド選択などを選ぶダイアログです。
 *
 * @version		making		6, August 2000
 * @author		Yusuke Sasaki
 */
public class SelectDialog extends Dialog implements ActionListener, WindowListener {
	
	/** ビデオ */
	protected Button	video;
	
	/** 同じハンドでリプレイ */
	protected Button	sameHand;
	
	/** 新しいハンドでプレイのラベル */
	protected Button	newHandButton;
	
	/** 新しいハンドでプレイ */
	protected Choice	newHand;
	
	/** 選択結果 */
	protected String	result;
	
/*-------------
 * Constructor
 */
	public SelectDialog(Component parent, Board lastBoard) {
		super(topLevelFrame(parent), "選んでください", true);
		
		setLayout(null);
		setSize(280, 180);
		
		video		= new Button("今のプレイを自動再生する");
		sameHand	= new Button("同じハンドをもう一度プレイする");
		newHandButton= new Button("新規プレイ");
		newHand		= new Choice();
		
		add(newHandButton); newHandButton.setBounds(20, 50, 70, 24);
		add(newHand);	newHandButton.addActionListener(this); newHand.setBounds(100, 50, 160, 24);
		add(video);		video.addActionListener(this); video.setBounds(20, 80, 240, 24);
		add(sameHand);	sameHand.addActionListener(this); sameHand.setBounds(20, 110, 240, 24);
		
		if (lastBoard == null) {
			video.setEnabled(false);
			sameHand.setEnabled(false);
		} else if (lastBoard.getStatus()!=Board.SCORING) {
			video.setEnabled(false);
		}
		
		//
		// 位置決定
		//
		Rectangle r = getParent().getBounds();
		Dimension b = getSize();
		
		setLocation(r.x + (r.width - b.width) / 2, r.y + (r.height - b.height) / 2);
		
		setResizable(false);
		
		result = null;
		
		addWindowListener(this);
	}
	
/*-----------------------------
 * implements (WindowListener)
 */
	public void windowActivated(WindowEvent we) { }
	public void windowClosed(WindowEvent we) { }
	public void windowClosing(WindowEvent we) {
		result = "disposed";
		hide();
	}
	public void windowDeactivated(WindowEvent we) { }
	public void windowDeiconified(WindowEvent we) { }
	public void windowIconified(WindowEvent we) { }
	public void windowOpened(WindowEvent we) { }
	
/*-----------
 * overrides
 */
	/**
	 * IEにおいて、interrupt()においてモーダルダイアログの show() が抜けてしまう
	 * 現象を回避するためのオーバーライド。
	 */
	public void show() {
		while (result == null) {
			super.show();
		}
	}
	
/*------------------
 * instance methods
 */
	private static Frame topLevelFrame(Component comp) {
		while (true) {
			Component parent = comp.getParent();
			if (parent == null) {
				return (Frame)comp;
			}
			comp = parent;
		}
	}
	
	public void addChoice(String title) {
		newHand.add(title);
	}
	
	public void disposeDialog() {
		// IE JavaVM 4.0 対応(林さん指摘分、Dialog の dispose() が使用できない)
		result = "disposed";
		hide();
		//dispose();
	}
	
/*-----------------------------
 * implements (ActionListener)
 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == video) {
			result = "Video";
			hide();
//			super.dispose();
		} else if (e.getSource() == sameHand) {
			result = "Same Hand";
			hide();
//			super.dispose();
//		} else if (e.getSource() == quit) {
//			result = "Quit";
		} else if (e.getSource() == newHandButton) {
			result = newHand.getSelectedItem();
			hide();
//			super.dispose();
		}
	}
	
}
