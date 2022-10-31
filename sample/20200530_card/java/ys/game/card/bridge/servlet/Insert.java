package ys.game.card.bridge.servlet;

import java.io.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Practice Applet の Record テーブルにインサートする Servlet です。
 * まだ、intCodeの検証はやってません。やっても大丈夫だが。
 * また、二重登録もできてしまいます。
 *
 * @author		Yusuke Sasaki
 * @version		making		17, November 2001
 */
public class Insert extends HttpServlet {
	
/*-----------
 * overrides
 */
	public void init(ServletConfig c) {
	}
	
	/**
	 * GET は、クライアントからの受信要求を受け、メッセージを送信します。
	 * メッセージがない場合、ブロックし、メッセージがPOSTされた時点で
	 * 応答を返却します。
	 */
	public void doGet(	HttpServletRequest	request,
						HttpServletResponse	response)
							throws IOException, ServletException {
		
		response.setContentType("text/html; charset=Shift_JIS");
		
		PrintWriter p = response.getWriter();
		p.println("<html><head><title>できません</title></head><body>この URL に HTTP GET は使用できません<br></body></html>");
	}
	
	/**
	 * POST は、クライアントからの送信メッセージを受け取り、メッセージ待ち
	 * 状態のクライアント(GET を使用している)に通知します。
	 */
	public void doPost(	HttpServletRequest	request,
						HttpServletResponse	response)
							throws IOException, ServletException {
		
		Hashtable param = ServletUtils.parsePostData(request.getContentLength(), request.getInputStream());
		
		// パラメータ取得
		String	name		= ((String[])param.get("name"))[0];
		String	comment	= ((String[])param.get("comment"))[0];
		String	contstr	= ((String[])param.get("contracts"))[0];
		int		score	= Integer.parseInt( ((String[])param.get("score"))[0] );
		int		intcode	= Integer.parseInt( ((String[])param.get("intcode"))[0] );
		String	boardstr = ((String[])param.get("board"))[0];
		
		// check intcode
		// まだはじいてはいない。表示するのみ
		int		comparison = calculateIntCode(score, contstr+boardstr);
		
		//
		// insert データの作成
		//
		String[]	contract	= new String[4];
		int[]		made		= new int[4];
		StringTokenizer st = new StringTokenizer(contstr, " \t", false);
		for (int i = 0; i < 4; i++) {
			contract[i]	= st.nextToken();
			made[i]		= Integer.parseInt(st.nextToken());
		}
		
		RecordDB r = new RecordDB();
		r.insert(score, contract, made, name, comment, intcode, boardstr);
		
		response.setContentType("text/html; charset=Shift_JIS");
		
		PrintWriter p = response.getWriter();
		
//		p.println("<html><head><title>実行しました</title></head><body>insert が終了しました<br>");
		p.println("<html><head><title>実行しました</title></head><body>");
//		p.println("intcode = " + intcode);
//		p.println("<br>comparison = " + comparison);
		
		//
		// 順位のテーブルを表示する(jsp の方が？)
		//
		p.println("<table border=0><tr><td>順位</td><td>スコア</td><td>名前</td><td>コメント</td><td>コントラクト</td></tr>");
		r.select();
		for (int i = 0; i < 20; i++) {
			Record d = r.nextRecord();
			if (d == null) continue;
			p.print("<tr><td>");
			p.print(i+1);
			p.print("</td><td>");
			p.print(d.score);
			p.print("</td><td>");
			p.print(d.name);
			p.print("</td><td>");
			p.print(d.comment);
			p.println("</td><td>");
			p.print("<table border=0>");
			for (int j = 0; j < 4; j++) {
				p.print("<tr><td>");
				p.print(d.contract[j]);
				p.print("(");
				p.print(d.made[j]);
				p.print("トリック)</td></tr>");
			}
			p.println("</table></td></tr>");
		}
		p.println("</table>");
		
		p.println("<br></body></html>");
	}
	
	private int calculateIntCode(int ts, String str) {
		int result = result = ts + 1297321;
		for (int i = 0; i < str.length(); i++) {
			int c = (int)(str.charAt(i));
			result = result * 11157 * c + c + 1;
		}
		result = result * (ts + 12497321);
		
		return result;
	}
	
}
