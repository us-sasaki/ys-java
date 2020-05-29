/*
 * 22, July 2001	Practice モード追加
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
 * テスト用のアプレットでしたが、本物として使われるようになりました。
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
 * 前回のボードの情報を格納する
 */
	String[] boardString = new String[4];
	int totalScore;
	String[] contractString = new String[4];
	int[] made = new int[4];
	
/*-----------
 * overrides
 */
	/**
	 * アプレットがロードされたときに呼ばれます。
	 * PlayMain に PARAM タグで指定された問題を追加し、initialize() を呼び出します。
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
 * JavaScriptにデータを渡すためのメソッド
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
 * アプレット処理
 */
	/**
	 * メインスレッド処理です。
	 * PlayMain の start(), dispose() を繰り返し呼びます。
	 */
	public void run() {
		try {
			while (runner != null) {
				// 何ボードか続けて実行する
				main.start();
				//
				// 一度やったところ、IllegalStatusException が発生。
				// 途中でやめたときは copyData() せず、最後までやったときは有効となる
				// ようになっているか？  いろいろな場合があるので、要検討。
				//
				try {
					recordIsValid = false;
					copyData();
					recordIsValid = true;
					
					//
					// スコア登録画面を開く
					//
//					showRecordURL();
					
				} catch (IllegalStatusException ignored) {
				}
				// 終了処理
				main.stop();
				if (main.isFinished()) break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("interrupted.. finish : " + e.getMessage() );
			throw new InternalError(e.toString()); // リリース時は削除
		}
		jumpQuitURL();
		runner = null;
	}
	
	/**
	 * QuitURL で指定されるページにジャンプします。
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
				System.out.println("PARAM タグで QuitURL を指定していないか、無効なファイルを指定しています "+e+" --- "+f);
			}
		}
	}
	
	/**
	 * JavaScript: にジャンプできないときには、JSObject を使用してジャンプを試みます。
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
