package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;

import ys.game.card.bridge.Board;

/**
 * ���v���C�A�n���h�I���Ȃǂ�I�ԃ_�C�A���O�ł��B
 *
 * @version		making		6, August 2000
 * @author		Yusuke Sasaki
 */
public class SelectDialog extends Dialog implements ActionListener, WindowListener {
	
	/** �r�f�I */
	protected Button	video;
	
	/** �����n���h�Ń��v���C */
	protected Button	sameHand;
	
	/** �V�����n���h�Ńv���C�̃��x�� */
	protected Button	newHandButton;
	
	/** �V�����n���h�Ńv���C */
	protected Choice	newHand;
	
	/** �I������ */
	protected String	result;
	
/*-------------
 * Constructor
 */
	public SelectDialog(Component parent, Board lastBoard) {
		super(topLevelFrame(parent), "�I��ł�������", true);
		
		setLayout(null);
		setSize(280, 180);
		
		video		= new Button("���̃v���C�������Đ�����");
		sameHand	= new Button("�����n���h��������x�v���C����");
		newHandButton= new Button("�V�K�v���C");
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
		// �ʒu����
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
	 * IE�ɂ����āAinterrupt()�ɂ����ă��[�_���_�C�A���O�� show() �������Ă��܂�
	 * ���ۂ�������邽�߂̃I�[�o�[���C�h�B
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
		// IE JavaVM 4.0 �Ή�(�т���w�E���ADialog �� dispose() ���g�p�ł��Ȃ�)
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
