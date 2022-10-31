/*
 * 22, July 2001	Practice ���[�h�ǉ�
 */
import java.awt.*;
import java.awt.image.*;

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
public class AppTest extends AppletCardImageHolder implements Runnable {
	
	Thread runner = null;
	PlayMain main;
	int handno;
	
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
		main = new PlayMain(this);
		setProblem();
		main.initialize();
	}
	
	/**
	 * ���� PlayMain �ɒǉ����܂��B
	 * init() �̒��ŌĂ΂�܂��B
	 * ���݃T�|�[�g���Ă���^�O�͈ȉ��̒ʂ�ł��B
	 * <PRE>
	 *    NAME     |                  ���e
	 * ------------+---------------------------------------------
	 * Practice    | ���K���[�h��ǉ����܂��B
	 * Title(n)    | ���̃^�C�g���������܂��B
	 * Contract(n) | 4NT, 3HX �ȂǂŃR���g���N�g�������܂��B
	 * North(n)    | North �̃n���h�������܂��B S:AKQT9 H:.... �ȂǁB
	 * East(n)     | East �̃n���h�������܂��B�����̃n���h�͏ȗ�����ƃ����_���ɂȂ�܂��B
	 * South(n)    | South �̃n���h�������܂��B
	 * West(n)     | West �̃n���h�������܂��B
	 * Desc(n)     | �͂��߂ɂ��݂ꂪ����ׂ�����ł��B
	 * O.L.(n)     | �I�[�v�j���O���[�h�̎w�肪����Ƃ��ɂ����Ŏw�肵�܂��B
	 * Thinker     | �f�B�t�F���_�[�̎v�l���[�`�����w�肵�܂��B
	 *             |     �ȗ����� SimplePlayer �ƂȂ�܂��B
	 * </PRE>
	 */
	private void setProblem() {
		int index = 1;
		while (true) {
			String practice = getParameter("Practice");
			if ( (practice != null)&&(!practice.equals("")) ) {
				main.addProblem(new RandomProblem());
				continue;
			}
			String title = getParameter("Title"+index);
			String contract = getParameter("Contract"+index);
			String north	= getHandString("North"+index);
			String east		= getHandString("East"+index);
			String south	= getHandString("South"+index);
			String west		= getHandString("West"+index);
			String desc		= getDescString("Desc"+index);
			String opening	= getParameter("O.L."+index);
			
			// 2002/2/3 �v���C���[�̒ǉ�
			String thinker	= getParameter("Thinker"+index);
			
			if ((title == null)||(title.equals(""))) break;
			
//			title = title + "(" + contract + ")";
			
			int kind = Bid.BID;
			if (contract.endsWith("XX")) {
				kind = Bid.REDOUBLE;
				contract = contract.substring(0, contract.length() - 2);
			} else if (contract.endsWith("X")) {
				kind = Bid.DOUBLE;
				contract = contract.substring(0, contract.length() - 1);
			}
			
			int level = contract.charAt(0) - '0';
			
			int denom = Bid.NO_TRUMP;
			contract = contract.substring(1);
			if (contract.startsWith("NT")) denom = Bid.NO_TRUMP;
			else if (contract.startsWith("S")) denom = Bid.SPADE;
			else if (contract.startsWith("H")) denom = Bid.HEART;
			else if (contract.startsWith("D")) denom = Bid.DIAMOND;
			else if (contract.startsWith("C")) denom = Bid.CLUB;
			
			main.addProblem(new RegularProblem(title, kind, level, denom, new String[] {north, east, south, west}, desc, opening, thinker));
			index++;
		}
		if (index == 1) {
			main.addProblem(new RegularProblem("7NT", Bid.BID, 7, Bid.NO_TRUMP, new String[] {"S:AKQ H:T987 D:53 C:A753", "Rest", "S: H:AK D:AKQJT92 C:8642", "Rest"}, "�R���g���N�g�F�V�m�sby �r\nS((�f�B�N���A���[)��N(�_�~�[)�̎��\n�������삵�� 13�g���b�N�Ƃ��ĂˁB\n�؂�D�X�[�c�͂���܂���B", null));
		}
	}
	
	/**
	 * �n���h�̕�������擾���܂��B�ȗ�����Ă���ꍇ�A"Rest" ��Ԃ��܂��B
	 */
	private String getHandString(String name) {
		String result = getParameter(name);
		if ((result == null)||(result.equals(""))) result = "Rest";
		return result;
	}
	
	/**
	 * ���������擾���܂��B"\n" �� LF �ɕϊ����܂��B
	 */
	private String getDescString(String name) {
		String src = getParameter(name);
		if ( (src == null)||(src.equals("")) ) return "����΂��Ă�";
		StringBuffer buff = new StringBuffer();
		
		for (int i = 0; i < src.length(); i++) {
			char c = src.charAt(i);
			if (i < src.length() - 1) {
				if ((c == '\\')&&(src.charAt(i+1) == 'n')) {
					buff.append('\n');
					i++;
					continue;
				}
			}
			buff.append(c);
		}
		
		return buff.toString();
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
	}
	
	public void destroy() {
		if (main != null) main.dispose();
	}
	
	/**
	 * ���C���X���b�h�����ł��B
	 * PlayMain �� start(), dispose() ���J��Ԃ��Ăт܂��B
	 */
	public void run() {
		try {
			while (runner != null) {
				main.start();
				if (main.exitSignal) break;
				main.stop();
			}
		} catch (Exception e) {
			System.out.println("interrupted... finish");
			e.printStackTrace(System.out);
			throw new InternalError(e.toString()); // �����[�X���͍폜
		}
		if (main.exitSignal) {
			jumpQuitURL();
			runner = null;
			main.stop();
		}
	}
	
	/**
	 * QuitURL �Ŏw�肳���y�[�W�ɃW�����v���܂��B
	 */
	protected void jumpQuitURL() {
		String quitURL = getParameter("QuitURL");
		try {
			getAppletContext().showDocument(new java.net.URL(getDocumentBase(), quitURL));
		} catch (Exception e) {
			try {
				if (quitURL.toUpperCase().startsWith("JAVASCRIPT:"))
					tryConnectJavaScript(quitURL.substring(11));
			} catch (Exception f) {
				System.out.println("PARAM �^�O�� QuitURL ���w�肵�Ă��Ȃ����A�����ȃt�@�C�����w�肵�Ă��܂�");
			}
		}
	}
	
	protected void tryConnectJavaScript(String methodName) throws Exception {
		netscape.javascript.JSObject win = netscape.javascript.JSObject.getWindow(this);
		netscape.javascript.JSObject doc = (netscape.javascript.JSObject) win.getMember("document");
		doc.call(methodName, new Object[] {} );
	}
	
	public void update(Graphics g) {
		paint(g);
	}
}
