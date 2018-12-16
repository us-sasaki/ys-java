package abdom.data;

import java.io.*;
import java.util.*;
import abdom.util.CommentCutInputStream;

/**
 * ByteArray クラスは，byte[] に対する各種の便利なオペレーションを提供します。
 * abdom パッケージに移動しました。
 * 
 * @author		Yusuke Sasaki
 * @version		release		19, October 2016
 */
public final class ByteArray {
	
	private ByteArray() {
	}
	
	/**
	 * ２配列を連結した byte 配列を作成します。
	 *
	 * @param		a		先頭に位置する配列
	 * @param		b		後方に連結される配列
	 *
	 * @return		連結された配列
	 */
	public static byte[] concat(byte[] a, byte[] b) {
		if (a == null) return b;
		if (b == null) return a;
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		
		return result;
	}
	
	/**
	 * ２配列の内容が等しいかチェックします。
	 * 長さが異なる，もしくは内容が異なる場合 false を返します。
	 *
	 * @param		a		比較対象の配列
	 * @param		b		比較対象の配列
	 *
	 * @return		一致する場合 true, 一致しない場合 false
	 */
	public static boolean compare(byte[] a, byte[] b) {
		if (a.length != b.length) return false;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) return false;
		}
		return true;
	}
	
	public static byte[] subarray(byte[] target, int beginIndex, int finishIndex) {
		if ( (beginIndex < 0) || (finishIndex < beginIndex) )
			return null;
		
		byte[] result = new byte[finishIndex - beginIndex];
		System.arraycopy(target, beginIndex, result, 0, finishIndex - beginIndex);
		
		return result;
	}
	
	/**
	 * 16進ASCII文字列の内容を表す byte[] を返します。
	 *
	 * @param		str		16進ASCII文字列です。"0102EEDC" など。
	 * 
	 * @return		byte[] に変換された結果です。
	 */
	public static byte[] parse(String str)
					throws IllegalArgumentException, NumberFormatException {
		if ((str.length()%2) != 0)
			throw new IllegalArgumentException("16進ASCII表現は偶数文字数でなければなりません。");
		
		byte[] src = new byte[str.length()/2];
		
		for (int i = 0; i < src.length; i++) {
			src[i] = (byte)Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
		}
		
		return src;
	}
	
	/**
	 * byte[] の内容をヘキサ文字列に変換します。parse(String)の逆変換です。
	 * １バイトが２文字に変換され、変換後は 0 ～ 9, a ～ f(小文字) に変換されます。
	 * 
	 * @param		d		変換対象の byte[] です。
	 * 
	 * @return		16進ASCII文字列です。
	 */
	public static String toString(byte[] d) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < d.length; i++) {
			if ((d[i] & 0xF0) == 0) result.append("0");
			result.append(Integer.toString(d[i] & 0xFF, 16));
		}
		return result.toString();
	}
	
	/**
	 * byte[] の内容の指定した部分をヘキサ文字列に変換します。
	 * d[offset] ～ d[offset + len - 1] の各データが変換対象となります。
	 * １バイトが２文字に変換され、変換後は 0 ～ 9, a ～ f(小文字) に変換されます。
	 *
	 * @param		d		変換対象の byte[] です。
	 * @param		offset	変換を開始する d の添え字番号です。
	 * @param		len		変換を行う長さです。
	 *
	 * @return		16進ASCII文字列です。
	 */
	public static String toString(byte[] d, int offset, int len) {
		StringBuffer result = new StringBuffer();
		for (int i = offset; i < offset + len; i++) {
			if ((d[i] & 0xF0) == 0) result.append("0");
			result.append(Integer.toString(d[i] & 0xFF, 16));
		}
		return result.toString();
	}
	
	/**
	 * 指定されたバイトデータをヘキサ文字列に変換します。
	 * １バイトが２文字に変換され、変換後は 0 ～ 9, a ～ f(小文字) に変換されます。
	 * 
	 * @param		d		変換対象のデータを指定します。
	 * @return		16進ASCII文字列です。
	 */
	public static String toString(byte d) {
		String result = "";
		if ((d & 0xF0) == 0) result = result + "0";
		result = result + Integer.toString(d & 0xFF, 16);
		return result;
	}
	
	public static String toString(long v) {
		StringBuffer st = new StringBuffer();
		st.append(toString((byte)(v >> 56)));
		st.append(toString((byte)(v >> 48)));
		st.append(toString((byte)(v >> 40)));
		st.append(toString((byte)(v >> 32)));
		st.append(toString((byte)(v >> 24)));
		st.append(toString((byte)(v >> 16)));
		st.append(toString((byte)(v >> 8)));
		st.append(toString((byte)(v)));
		
		return st.toString();
	}
	
	public static String toString(int v) {
		StringBuffer st = new StringBuffer();
		st.append(toString((byte)(v >> 24)));
		st.append(toString((byte)(v >> 16)));
		st.append(toString((byte)(v >> 8)));
		st.append(toString((byte)(v)));
		
		return st.toString();
	}
	
	public static String toString(short v) {
		StringBuffer st = new StringBuffer();
		st.append(toString((byte)(v >> 8)));
		st.append(toString((byte)(v)));
		
		return st.toString();
	}
	
	public static String toString(char v) {
		StringBuffer st = new StringBuffer();
		st.append(toString((byte)(v >> 8)));
		st.append(toString((byte)(v)));
		
		return st.toString();
	}
	
	/**
	 * 指定された文字列の byte 列を抽出し、解釈結果の byte[] を返却します。
	 * 解釈は、C/C++ コメントを除外し、スペース、タブコード、または改行コード(CR, LF)で区切られた
	 * 文字トークンがヘキサ文字(0～9,A～F,a～f)のみで構成される部分を抽出します。
	 * 抽出した結果、奇数文字からなっていた場合、IllegalArgumentException がスローされます。
	 *
	 * @param		data		解釈を行う文字列
	 *
	 * @return		解釈結果
	 *
	 * @exception	java.lang.IllegalArgumentException	ヘキサ文字が奇数文字からなる
	 * @exception	java.lang.InternalError		動作環境で Shift-JIS が使用できない
	 */
	public static byte[] parseMessage(String data) {
		
		byte[] buff = null;
		try {
			buff = data.getBytes("SJIS");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError("SJISが使用できません");
		}
		try {
			ByteArrayInputStream	src		= new ByteArrayInputStream(buff);
			InputStream				src3	= new CommentCutInputStream(src);
			
			StringBuffer dst = new StringBuffer();
			while (true) {
				int c = src3.read();
				if (c == -1) break;
				dst.append( (char)c );
			}
			src3.close();
			
			StringTokenizer st = new StringTokenizer(dst.toString(), " \t\r\n");
			StringBuffer tmp = new StringBuffer();
			
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (checkValidity(token))
					tmp.append(token);
			}
			
			return ByteArray.parse(tmp.toString());
		} catch (IOException e) {
			throw new InternalError(e.toString());
		}
	}
	
	/**
	 * 与えられた文字列がヘキサ文字と見なせるかどうかをチェックします。
	 * 
	 * @param		target		チェック対象の文字列
	 * @return		ヘキサ文字とみなせる場合は true
	 */
	private static boolean checkValidity(String target) {
		for (int i = 0; i < target.length(); i++) {
			char c = target.charAt(i);
			
			if ( (c >= '0')&&(c <= '9') ) continue;
			if ( (c >= 'A')&&(c <= 'F') ) continue;
			if ( (c >= 'a')&&(c <= 'f') ) continue;
			return false;
		}
		return true;
	}
	
	/**
	 * 指定されたバイト配列をダンプリスト形式の文字列に変換する便利関数です。
	 * 結果全体をメモリ上に配置するため、大きなデータをダンプリストに変換する場合、
	 * com.ntt.io.BinaryDumpStream を使用した方がメモリ効率は上昇します。
	 * ダンプリストのフォーマットは、com.ntt.io.BinaryDumpStream において、set7Bit(true)
	 * setLineNumbering(true) を実行したものとなります。
	 *
	 * @param		data		ダンプリストに変換するデータ
	 * @return		ダンプリスト(改行文字を含む)
	 */
	public static String toDumpList(byte[] data) {
		return toDumpList(data, 0, data.length);
	}
	
	public static String toDumpList(byte[] data, int off, int len) {
		try {
			ByteArrayOutputStream		b	= new ByteArrayOutputStream();
			ByteArrayInputStream		src	= new ByteArrayInputStream(data, off, len);
			abdom.util.BinaryDumpStream	bds	= new abdom.util.BinaryDumpStream(b);
			bds.set7Bit(true);
			while (true) {
				int c = src.read();
				if (c == -1) break;
				bds.write(c);
			}
			bds.close();
			src.close();
			
			byte[] d = b.toByteArray();
			return new String(d, "SJIS");
		} catch (Exception e) {
			return "内部異常が発生しました";
		}
	}
	
	static Random r = new Random();
	
	/**
	 * 乱数ジェネレータを指定します。デフォルトでは java.util.Random なので、
	 * 精度を要求する場合に使用します。
	 *
	 * @param	newRandom	乱数ジェネレータ
	 */
	public static void setRandom(Random newRandom) {
		r = newRandom;
	}
	
	public static byte[] randomData(int minSize, int maxSize) {
		int i = (int)((r.nextDouble() * (double)(maxSize - minSize)) + minSize);
		
		byte[] result = new byte[i];
		r.nextBytes(result);
		return result;
	}
	
	public static byte[] xor(byte[] a, byte[] b) {
		byte[] result;
		byte[] t;
		if (b.length < a.length) {
			t = b;
			result = new byte[a.length];
			System.arraycopy(a, 0, result, 0, a.length);
		} else {
			t = a;
			result = new byte[b.length];
			System.arraycopy(b, 0, result, 0, b.length);
		}
		
		for (int i = 0; i < t.length; i++) {
			result[i] ^= t[i];
		}
		
		return result;
	}
	
	public static byte[] reverse(byte[] target) {
		byte[] result = new byte[target.length];
		for (int i = 0; i < target.length; i++) {
			result[i] = target[target.length - 1 - i];
		}
		return result;
	}
	
	/**
	 * 与えられたデータについて、奇数パリティを付加します。
	 *
	 * @param	d		パリティ付加対象データ
	 * @return	パリティ付加後のデータ
	 */
	public static byte setOddParity(byte d) {
		byte b = d;
		b ^= (b>>4);
		b ^= (b>>2);
		b ^= (b>>1);
		
		return ((b & 1)==0?(byte)(d ^ 1):d);
	}
	
	/**
	 * 与えられたデータについて、奇数パリティを付加します。
	 * 引数のデータは奇数パリティを付加したものにアップデートされます。
	 *
	 * @param	d		パリティ付加対象データ列
	 * @return	パリティ付加後のデータ列
	 */
	public static byte[] setOddParity(byte[] d) {
		for (int i = 0; i < d.length; i++) {
			byte b = d[i];
			b ^= (b>>4);
			b ^= (b>>2);
			b ^= (b>>1);
			
			if ((b & 1)==0) d[i] ^= 1;
		}
		return d;
	}
}
