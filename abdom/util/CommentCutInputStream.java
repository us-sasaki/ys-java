package abdom.util;

import java.io.*;

/**
 * C, C++�`���̃R�����g������ FilterInputStream �ł��B
 * // �ɑ����s�̓R�����g�ƌ��Ȃ���܂��B�P�s�́ALF �� CR ���I�[�Ƃ��܂��B
 * // ���s�̐擪�ɂ������ꍇ�A�s���ׂĂ��X�L�b�v����܂����A����ȊO�̏ꍇ�A
 * ���̍s�̉��s�͊܂܂�܂��B
 * ���݁A����������� " �� // �����������Ă��Ă��A�R�����g�ƌ��Ȃ���܂��B
 *
 * @version		a-release		14, August 2001 / 19, Oct 2016
 * @author		Yusuke Sasaki
 */
public class CommentCutInputStream extends FilterInputStream {
	protected int pushBacked;
	protected int lastInput;
	protected int mode;
	protected boolean emptyLine =true;
	
/*-------------
 * Constructor
 */
	public CommentCutInputStream(InputStream in) {
		super(in);
		pushBacked = -2;
	}
	
/*-----------
 * Overrides
 */
	public int read() throws IOException {
		int c;
		if (pushBacked != -2) {
			c = pushBacked;
		} else {
			c = in.read();
		}
		pushBacked = -2;
		
		if (c == -1) return c;
		int tmp = lastInput;
		lastInput = c;
		if ((tmp == '/')&&(c == '/')) {	// C++ Style Comment
			// �s���܂ŃX�L�b�v����
			while (true) {
				int cc = in.read();
				lastInput = cc;
				if (cc == -1) return cc;
				if (cc == '\n') {
					if (!emptyLine) {
						emptyLine = true;
						return cc;
					} else {
						return read();
					}
				}
				if (cc == '\r') {
					if (!emptyLine) {
						emptyLine = true;
						return cc;
					} else {
						int ccc = in.read();
						lastInput = ccc;
						if (ccc == '\n') return read();
						else {
							pushBacked = ccc;
							lastInput = cc;
							emptyLine = true;
							return cc;
						}
					}
				}
			}
		}
		if ((tmp == '/')&&(c == '*')) { // C Style Comment
			// */ �܂ŃX�L�b�v����
			boolean astaed = false;
			while (true) {
				int cc = in.read();
				lastInput = cc;
				if (cc == '*') astaed = true;
				else {
					if ((astaed)&&(cc == '/')) return read();
					astaed = false;
				}
			}
		}
		if (c == '/') {
			int cc = in.read();
			pushBacked = cc;
			if ((cc == '/')||(cc == '*'))
				return read();
		}
		if ( (c != '\r')&&(c != '\n') ) emptyLine = false;
		return c;
	}
	
	public int read(byte[] buf, int offst, int len) throws IOException {
		int i;
		for (i = offst; i < offst + len; i++) {
			int c = read();
			if (c == -1) {
				if (i == offst) return -1;
				else return i - offst;
			}
			buf[i] = (byte)c;
		}
		return i - offst;
	}
	
	public static void main(String[] args) throws Exception {
		InputStream in = new CommentCutInputStream(new FileInputStream("AcqAccInfo_90.txt"));
		OutputStream out = new FileOutputStream("CommentCut.txt");
		
		while (true) {
			int c = in.read();
			if (c == -1) break;
			out.write(c);
		}
		in.close();
		out.close();
	}
}
