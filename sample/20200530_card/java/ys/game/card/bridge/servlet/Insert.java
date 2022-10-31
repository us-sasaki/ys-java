package ys.game.card.bridge.servlet;

import java.io.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Practice Applet �� Record �e�[�u���ɃC���T�[�g���� Servlet �ł��B
 * �܂��AintCode�̌��؂͂���Ă܂���B����Ă����v�����B
 * �܂��A��d�o�^���ł��Ă��܂��܂��B
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
	 * GET �́A�N���C�A���g����̎�M�v�����󂯁A���b�Z�[�W�𑗐M���܂��B
	 * ���b�Z�[�W���Ȃ��ꍇ�A�u���b�N���A���b�Z�[�W��POST���ꂽ���_��
	 * ������ԋp���܂��B
	 */
	public void doGet(	HttpServletRequest	request,
						HttpServletResponse	response)
							throws IOException, ServletException {
		
		response.setContentType("text/html; charset=Shift_JIS");
		
		PrintWriter p = response.getWriter();
		p.println("<html><head><title>�ł��܂���</title></head><body>���� URL �� HTTP GET �͎g�p�ł��܂���<br></body></html>");
	}
	
	/**
	 * POST �́A�N���C�A���g����̑��M���b�Z�[�W���󂯎��A���b�Z�[�W�҂�
	 * ��Ԃ̃N���C�A���g(GET ���g�p���Ă���)�ɒʒm���܂��B
	 */
	public void doPost(	HttpServletRequest	request,
						HttpServletResponse	response)
							throws IOException, ServletException {
		
		Hashtable param = ServletUtils.parsePostData(request.getContentLength(), request.getInputStream());
		
		// �p�����[�^�擾
		String	name		= ((String[])param.get("name"))[0];
		String	comment	= ((String[])param.get("comment"))[0];
		String	contstr	= ((String[])param.get("contracts"))[0];
		int		score	= Integer.parseInt( ((String[])param.get("score"))[0] );
		int		intcode	= Integer.parseInt( ((String[])param.get("intcode"))[0] );
		String	boardstr = ((String[])param.get("board"))[0];
		
		// check intcode
		// �܂��͂����Ă͂��Ȃ��B�\������̂�
		int		comparison = calculateIntCode(score, contstr+boardstr);
		
		//
		// insert �f�[�^�̍쐬
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
		
//		p.println("<html><head><title>���s���܂���</title></head><body>insert ���I�����܂���<br>");
		p.println("<html><head><title>���s���܂���</title></head><body>");
//		p.println("intcode = " + intcode);
//		p.println("<br>comparison = " + comparison);
		
		//
		// ���ʂ̃e�[�u����\������(jsp �̕����H)
		//
		p.println("<table border=0><tr><td>����</td><td>�X�R�A</td><td>���O</td><td>�R�����g</td><td>�R���g���N�g</td></tr>");
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
				p.print("�g���b�N)</td></tr>");
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
