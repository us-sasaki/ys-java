package com.ntt.util;

import java.io.*;
import java.util.Vector;

/**
 * CSV形式で表現されたストリーム(Reader)を行ごとに読み込むクラスです。
 * このバージョンでは、コンマの前後にスペースが入ってはいけません。
 * また、最終行も改行が必要です。
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
	 * 渡された Reader は本クラス内部で close() を行うことはありません。
	 */
	public CsvReader(Reader in) {
		if (in instanceof BufferedReader) this.in = (BufferedReader)in;
		else this.in = new BufferedReader(in);
	}
	
/*-----------------
 * instance method
 */
	/**
	 * CSVファイルを一行読み、String配列として返却します。
	 * CSVでは ”でくくることによって改行をデータに含むことができるため、
	 * テキスト表現の一行と本関数で解釈する一行は必ずしも一致しません。
	 */
	public String[] readRow() throws IOException {
		// トークン分割
		int c;
		int r = 0;
		Vector v = new Vector();
		
		// 1行読み込む
		while (true) {
			// token を読み込む
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

