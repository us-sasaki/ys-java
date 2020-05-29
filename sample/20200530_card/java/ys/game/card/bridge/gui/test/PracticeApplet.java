/*
 * 22, July 2001	Practice ���[�h�ǉ�
 */
import java.awt.*;
import java.awt.image.*;
import java.net.URL;
import java.net.MalformedURLException;

import ys.game.card.*;
import ys.game.card.gui.*;
import ys.game.card.bridge.*;
import ys.game.card.bridge.gui.*;

/**
 * �e�X�g�p�̃A�v���b�g�ł������A�{���Ƃ��Ďg����悤�ɂȂ�܂����B
 *
 * @version		release		23, July 2001
 * @author		Yusuke Sasaki
 */
public class PracticeApplet extends AppletCardImageHolder implements Runnable {
	
	Thread runner = null;
	PracticeModePlayMain main;
	int handno;
	boolean recordIsValid = false;
	
/*------------------------------
 * �O��̃{�[�h�̏����i�[����
 */
	String[] boardString = new String[4];
	int totalScore;
	String[] contractString = new String[4];
	int[] made = new int[4];
	
/*-----------
 * overrides
 */
	/**
	 * �A�v���b�g�����[�h���ꂽ�Ƃ��ɌĂ΂�܂��B
	 * PlayMain �� PARAM �^�O�Ŏw�肳�ꂽ����ǉ����Ainitialize() ���Ăяo���܂��B
	 */
	public void init() {
		super.init();
		String handnoStr = getParameter("handNumber");
		handno = 0;
		if (handnoStr != null) {
			try {
				handno = Integer.parseInt(handnoStr);
			} catch (NumberFormatException ignored) {
			}
		}
		String thinker = getParameter("Thinker");
		main = new PracticeModePlayMain(this, thinker);
		main.initialize();
	}
	
	
	public void start() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}
	
	public void stop() {
		if (runner != null) {
			runner = null;
			main.stop();
		}
//		
//		if (main != null) main.dispose();
	}
	
	public void destroy() {
//System.out.println("destrot called");
		super.destroy();
		if (main != null) main.dispose();
	}
	
	public void update(Graphics g) {
		paint(g);
	}
	
/*----------------------------------------
 * JavaScript�Ƀf�[�^��n�����߂̃��\�b�h
 */

	private synchronized void copyData() {
		synchronized (this) {
			totalScore = 0;
			for (int i = 0; i < boardString.length; i++) {
				Board b = main.getBoard(i);
				
				boardString[i]		= Converter.serialize(b);
				made[i]				= Score.countWinners(b);
				contractString[i]	= Converter.bidStr(b.getContract());
				totalScore += Score.calculate(b, Board.SOUTH);
			}
		}
	}
	
	public String recordIsAvailable() {
		if (recordIsValid) return "yes";
		return "no";
	}
	public int getTotalScore() { return totalScore; }
	public String getContractString() { return contractString[0]+" "+made[0]+" "+contractString[1]+" "+made[1]+" "+contractString[2]+" "+made[2]+" "+contractString[3]+" "+made[3]; }
	public String getBoard() { return boardString[0]+" "+boardString[1]+" "+boardString[2]+" "+boardString[3]; }
	public int getIntCode() { return calculateIntCode(); }
	
	private int calculateIntCode() {
		if (!recordIsValid) return -1;
		int result = 0;
		String str = getContractString() + getBoard();
		
		result = getTotalScore() + 1297321;
		for (int i = 0; i < str.length(); i++) {
			int c = (int)(str.charAt(i));
			result = result * 11157 * c + c + 1;
		}
		result = result * (getTotalScore() + 12497321);
		
		return result;
	}
	
/*----------------
 * �A�v���b�g����
 */
	/**
	 * ���C���X���b�h�����ł��B
	 * PlayMain �� start(), dispose() ���J��Ԃ��Ăт܂��B
	 */
	public void run() {
		try {
			while (runner != null) {
				// ���{�[�h�������Ď��s����
				main.start();
				//
				// ��x������Ƃ���AIllegalStatusException �������B
				// �r���ł�߂��Ƃ��� copyData() �����A�Ō�܂ł�����Ƃ��͗L���ƂȂ�
				// �悤�ɂȂ��Ă��邩�H  ���낢��ȏꍇ������̂ŁA�v�����B
				//
				try {
					recordIsValid = false;
					copyData();
					recordIsValid = true;
					
					//
					// �X�R�A�o�^��ʂ��J��
					//
//					showRecordURL();
					
				} catch (IllegalStatusException ignored) {
				}
				// �I������
				main.stop();
				if (main.isFinished()) break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("interrupted.. finish : " + e.getMessage() );
			throw new InternalError(e.toString()); // �����[�X���͍폜
		}
		jumpQuitURL();
		runner = null;
	}
	
	/**
	 * QuitURL �Ŏw�肳���y�[�W�ɃW�����v���܂��B
	 */
	protected void jumpQuitURL() {
		String quitURL = getParameter("QuitURL");
		URL url = null;
		try {
			url = new URL(getDocumentBase(), quitURL);
System.out.println("url = " + url);
			getAppletContext().showDocument(url);
		} catch (Exception e) {
System.out.println("url = " + url);
			
			try {
				if (quitURL.toUpperCase().startsWith("JAVASCRIPT:"))
					tryConnectJavaScript(quitURL.substring(11));
			} catch (Exception f) {
				System.out.println("PARAM �^�O�� QuitURL ���w�肵�Ă��Ȃ����A�����ȃt�@�C�����w�肵�Ă��܂� "+e+" --- "+f);
			}
		}
	}
	
	/**
	 * JavaScript: �ɃW�����v�ł��Ȃ��Ƃ��ɂ́AJSObject ���g�p���ăW�����v�����݂܂��B
	 */
	protected void tryConnectJavaScript(String methodName) throws Exception {
		netscape.javascript.JSObject win = netscape.javascript.JSObject.getWindow(this);
		netscape.javascript.JSObject doc = (netscape.javascript.JSObject) win.getMember("document");
		doc.call(methodName, new Object[] {} );
	}
	
	protected void showRecordURL() {
		try {
			URL url = new URL(getDocumentBase(), "record.html");
			getAppletContext().showDocument(url, "recordForm");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
