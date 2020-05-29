package ys.game.card.bridge.gui;

import java.awt.*;
import java.awt.event.*;

/**
 * �m�F�𑣂���_�C�A���O�ł��B
 * �R���X�g���N�^�̒��Ɋm�F������ݒ肷��ƁA���̓��e���\������܂��B
 * �\�����e�͂P�s�ŕ\������܂��B
 *
 * @version		a-release		30, September 2000
 * @author		Yusuke Sasaki
 */
public class MessageDialog extends Dialog implements WindowListener, ActionListener {
	protected Label		message;
	protected Button	ok;
	protected boolean	done;
	
/*-------------
 * Constructor
 */
	public MessageDialog(Component parent, String message) {
		super(topLevelFrame(parent), "�m�F", true);
		
		setLayout(null); //new GridLayout(2, 1));
		
		this.message = new Label(message);
		add(this.message);
		ok = new Button("OK");
		add(ok);
		
		pack();
		
		Dimension d = this.message.getPreferredSize();
		setSize(d.width + 100, d.height + 100);
		this.message.setBounds(50, 35, d.width, d.height);
		ok.setBounds( (d.width + 100 - 100)/2, 70, 100, 24 );
		
		setResizable(false);
		
		Rectangle r = getParent().getBounds();
		Dimension b = getSize();
		
		setLocation(r.x + (r.width - b.width) / 2, r.y + (r.height - b.height) / 2);
		
		addWindowListener(this);
		ok.addActionListener(this);
		
		done = false;
	}
	
/*-----------------------------
 * implements (WindowListener)
 */
	public void windowActivated(WindowEvent e) { }
	public void windowOpened(WindowEvent e) { }
	public void windowClosing(WindowEvent e) {
		done = true;
		dispose();
	}
	public void windowClosed(WindowEvent e) { }
	public void windowIconified(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	
/*-----------------------------
 * implements (ActionListener)
 */
	public void actionPerformed(ActionEvent e) {
		done = true;
		dispose();
	}
	
/*-----------
 * overrides
 */
	/**
	 * IE�ɂ����āAinterrupt()�ɂ����ă��[�_���_�C�A���O�� show() �������Ă��܂�
	 * ���ۂ�������邽�߂̃I�[�o�[���C�h�B
	 */
	public void show() {
		while (!done) {
			super.show();
		}
	}
	
	public void disposeDialog() {
		// IE JavaVM 4.0 �Ή�(�т���w�E���ADialog �� dispose() ���g�p�ł��Ȃ�)
		done = true;
		hide();
		//dispose();
	}
	
/*---------------
 * class methods
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
	
}
