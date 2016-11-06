package com.ntt.util;

import java.io.*;
import java.util.Vector;

/**
 * CSV�`���ŕ\�����ꂽ�X�g���[��(Reader)���s���Ƃɓǂݍ��ރN���X�ł��B
 * ���̃o�[�W�����ł́A�R���}�̑O��ɃX�y�[�X�������Ă͂����܂���B
 * �܂��A�ŏI�s�����s���K�v�ł��B
 *
 * @version		a-release		21, July 2001
 * @author		Yusuke Sasaki
 */
public class CsvReader {
	protected static final int BUFFER_SIZE = 1024;
	
	protected BufferedReader in;
	
/*-------------
 * Constructor
 */
	/**
	 * �n���ꂽ Reader �͖{�N���X������ close() ���s�����Ƃ͂���܂���B
	 */
	public CsvReader(Reader in) {
		if (in instanceof BufferedReader) this.in = (BufferedReader)in;
		else this.in = new BufferedReader(in);
	}
	
/*-----------------
 * instance method
 */
	/**
	 * CSV�t�@�C������s�ǂ݁AString�z��Ƃ��ĕԋp���܂��B
	 * CSV�ł� �h�ł����邱�Ƃɂ���ĉ��s���f�[�^�Ɋ܂ނ��Ƃ��ł��邽�߁A
	 * �e�L�X�g�\���̈�s�Ɩ{�֐��ŉ��߂����s�͕K��������v���܂���B
	 */
	public String[] readRow() throws IOException {
		// �g�[�N������
		int c;
		int r = 0;
		Vector v = new Vector();
		
		// 1�s�ǂݍ���
		while (true) {
			// token ��ǂݍ���
			String token = readToken(in);
			if (token != null) v.addElement(token);
			r++;
			
			c = in.read();
			if (c == -1) break;
			if (c == '\r') {
				c = in.read();
				if (c != '\n') throw new IOException("Illegal CR");
				break;
			}
			if (c == '\n') break;
			if (c != ',') throw new InternalError("ahi-");
		}
		
		String[] array = new String[v.size()];
		v.copyInto(array);
		
		return array;
	}
	
	private String readToken(BufferedReader r) throws IOException {
		StringBuffer result = new StringBuffer();
		r.mark(BUFFER_SIZE);
		int c = r.read();
		switch(c) {
		case -1:
			return null;
			
		case '\r':
		case '\n':
			r.reset();
			return "";
		
		case '\"':
		case '\'':
			int delimiter = c;
			
			while (true) {
				c = r.read();
				if (c == -1) result.toString();
				if (c == delimiter) break;
				result.append((char)c);
			}
			r.mark(BUFFER_SIZE);
			c = r.read();
			if (c == -1) result.toString();
			if ( (c == ',')||(c == '\r')||(c == '\n') ) {
				r.reset();
				return result.toString();
			}
			throw new IOException("format error");
		
		default:
			r.reset();
			while (true) {
				r.mark(BUFFER_SIZE);
				c = r.read();
				if ((c == ',')||(c == '\r')||(c == '\n') ) break;
				if ( (c == '\"')||(c == '\'') )
					throw new IOException("format error: unexpected \"");
				if (c == -1) return result.toString();
				result.append((char)c);
			}
			r.reset();
			return result.toString();
		}
	}
	
}

