/*
 * binary dump stream
 *
 *		ストリームをバイナリとみなしてダンプリスト形式で出力する。
 *
 * 2002/4/5 文字列出力のセパレータを '|' から '//' に変更
 */
package abdom.util;

import java.io.*;

/**
 * 指定した出力をダンプリスト形式に変換する FilterOutputStream.
 * 一行に表示する列数(バイト数)の変更、サイズカウント機能をサポートする.
 *
 * @version		1.21		5, April 2002
 * @author		Yusuke Sasaki
 */
public class BinaryDumpStream extends FilterOutputStream {
	protected final static byte[] SIZE_LETTERS = "size = ".getBytes();
	protected final static byte[] THREE_SPACES = "   ".getBytes();
	protected static final String LS = System.getProperty("line.separator");

	private int counter;	// counter
	private int columns;	// 列数
	private byte[] ascii;	// 右端に表示するアスキー文字列
	private int lineNumber; // 行番号
	private int size;
	
	private boolean lineNumberIsActive;
	private boolean sizePrintIsActive;
	private boolean columnsSet;
	private boolean asciiIs7Bit;
	private boolean putsAsciis;
/*-------------
 * Constructor
 */
	/**
	 * 指定した OutputStream にダンプリスト形式で
	 * 出力するオブジェクトを生成する.
	 * 一行のバイト数はデフォルト値である 16 が設定される.
	 * また、行番号出力は on, サイズ出力は off が設定される.
	 *
	 * @param		out		出力ストリーム
	 */
	public BinaryDumpStream(OutputStream out) {
		super(out);
		lineNumber = 0;
		size = 0;
		lineNumberIsActive = true;
		sizePrintIsActive = false;
		asciiIs7Bit = false;
		setColumns(16);
		columnsSet = false;
		putsAsciis = true;
	}

/*
 * instance methods
 */
	/**
	 * ascii 部分を表示し、一行を終了する.
	 */
	private void finishLine() throws IOException {
		lineNumber += columns;
		counter = 0;
		if (putsAsciis) {
			out.write('/');
			out.write('/');
			out.write(ascii);
			out.write(THREE_SPACES);
		}
		out.write(LS.getBytes());
		out.flush();
	}
	
	/**
	 * out に数値を出力する.
	 */
	private void writeNumber(String str, int d) throws IOException {
		int s = str.length();
		for (int i = 0; i < d-s; i++) {
			out.write('0');
		}
		for (int i = 0; i < s; i++) {
			out.write(str.charAt(i));
		}
	}
	
	public void write(int b) throws IOException {
		if ((lineNumberIsActive)&&(counter == 0)) {
			writeNumber(Integer.toHexString(lineNumber), 8);
			out.write(": ".getBytes());
		}
		b &= 255;
		writeNumber(Integer.toHexString(b), 2);
		if (b < 32) ascii[counter] = (byte)'.';
		else if (b == 127) ascii[counter] = (byte)'.';
		else if ((b > 127)&&(asciiIs7Bit)) ascii[counter] = (byte)'.';
		else ascii[counter] = (byte)b;
		counter++;
		size++;
		out.write(' ');
		if (counter == columns) finishLine();
	}

	public void write(byte[] b, int off, int len) throws IOException {

		for (int i = 0; i < len; i++)
			write((int)b[i + off]);
	}
	
	public void flush() throws IOException {
		
		if (counter > 0) {
			while (counter < columns) {
				ascii[counter] = (byte)' ';
				out.write(THREE_SPACES);
				counter++;
			}
			finishLine();
		}
		out.flush();
	}
	
	/**
	 * リソースの開放、setSizing を指定しているときは、サイズ表示を行う.
	 *
	 */
	public void close() throws IOException {
		try {
			flush();
			if (sizePrintIsActive) {
				out.write(SIZE_LETTERS);
				writeNumber(Integer.toHexString(size), 8);
				out.write('(');
				writeNumber(Integer.toString(size), 9);
				out.write((")"+LS).getBytes());
				out.flush();
			}
		}
		catch (IOException ignored) {
		}
		out.close();
	}
	
	/**
	 * 列数を変更する. これは、一オブジェクトにつき、一度しか設定できない.
	 *
	 * @param	c	列数
	 */
	public void setColumns(int c) {
		if (columnsSet) return;
		columnsSet = true;
		lineNumber = 0;
		columns = c;
		counter = 0;
		ascii = new byte[columns];
	}
	
	/**
	 * 行番号表示モードを変更する.
	 *
	 * @param	b	行番号を表示するときは true を指定する.
	 */
	public void setLineNumbering(boolean b) {
		this.lineNumberIsActive = b;
	}
	
	/**
	 * close後にサイズを表示するかどうかを指定する.
	 *
	 * @param	b		出力後にサイズ情報を負荷するときは true を指定する.
	 */
	public void setSizing(boolean b) {
		this.sizePrintIsActive = b;
	}
	
	/**
	 * WZ Editor 等 バイナリ文字を出力するとハングるエディタのための
	 * オプション.
	 * 
	 * @param	b	asciiコードで128以上のキャラクタを表示しないときは false
	 */
	public void set7Bit(boolean b) {
		this.asciiIs7Bit = b;
	}
	
	public void setPutsAsciis(boolean b) {
		this.putsAsciis = b;
	}
	
/*--------------------
 * main (Sample Code)
 */
	public static void main(String[] args) throws IOException {
		BinaryDumpStream b = new BinaryDumpStream(System.out);
		
		b.setSizing(true);
		
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 255; j++)
				b.write(j);
		
		b.write(1);
		b.write(2);
		b.close();
	}
	
		
}