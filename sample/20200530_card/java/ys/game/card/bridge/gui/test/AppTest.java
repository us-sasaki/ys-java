/*
 * 22, July 2001	Practice モード追加
 */
import java.awt.*;
import java.awt.image.*;

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
public class AppTest extends AppletCardImageHolder implements Runnable {
	
	Thread runner = null;
	PlayMain main;
	int handno;
	
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
		main = new PlayMain(this);
		setProblem();
		main.initialize();
	}
	
	/**
	 * 問題を PlayMain に追加します。
	 * init() の中で呼ばれます。
	 * 現在サポートしているタグは以下の通りです。
	 * <PRE>
	 *    NAME     |                  内容
	 * ------------+---------------------------------------------
	 * Practice    | 練習モードを追加します。
	 * Title(n)    | 問題のタイトルを示します。
	 * Contract(n) | 4NT, 3HX などでコントラクトを示します。
	 * North(n)    | North のハンドを示します。 S:AKQT9 H:.... など。
	 * East(n)     | East のハンドを示します。これらのハンドは省略するとランダムになります。
	 * South(n)    | South のハンドを示します。
	 * West(n)     | West のハンドを示します。
	 * Desc(n)     | はじめにすみれがしゃべる説明です。
	 * O.L.(n)     | オープニングリードの指定があるときにここで指定します。
	 * Thinker     | ディフェンダーの思考ルーチンを指定します。
	 *             |     省略時は SimplePlayer となります。
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
			
			// 2002/2/3 プレイヤーの追加
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
			main.addProblem(new RegularProblem("7NT", Bid.BID, 7, Bid.NO_TRUMP, new String[] {"S:AKQ H:T987 D:53 C:A753", "Rest", "S: H:AK D:AKQJT92 C:8642", "Rest"}, "コントラクト：７ＮＴby Ｓ\nS((ディクレアラー)とN(ダミー)の手を\n両方操作して 13トリックとってね。\n切り札スーツはありません。", null));
		}
	}
	
	/**
	 * ハンドの文字列を取得します。省略されている場合、"Rest" を返します。
	 */
	private String getHandString(String name) {
		String result = getParameter(name);
		if ((result == null)||(result.equals(""))) result = "Rest";
		return result;
	}
	
	/**
	 * 説明文を取得します。"\n" を LF に変換します。
	 */
	private String getDescString(String name) {
		String src = getParameter(name);
		if ( (src == null)||(src.equals("")) ) return "がんばってね";
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
	 * メインスレッド処理です。
	 * PlayMain の start(), dispose() を繰り返し呼びます。
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
			throw new InternalError(e.toString()); // リリース時は削除
		}
		if (main.exitSignal) {
			jumpQuitURL();
			runner = null;
			main.stop();
		}
	}
	
	/**
	 * QuitURL で指定されるページにジャンプします。
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
				System.out.println("PARAM タグで QuitURL を指定していないか、無効なファイルを指定しています");
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
