package abdom.data.json;

import java.io.Reader;
import java.io.IOException;
import java.util.Arrays;

/**
 * JsonType#parse で利用するための PushbackReader
 * java.io.PushbackReader はここでは不要な synchronized を行うため、最適化。
 * また、読み込み位置を返却し、例外に情報を付加できるようにし、周辺の文字列を
 * 取得できるようにした。
 *
 * @author		Yusuke Sasaki
 * @version		November 18, 2018
 */
class PushbackReader {
	static final int BUF_SIZE = 20;
	String src;
	int pos;
	
/*-------------
 * constructor
 */
	PushbackReader() {
	}
	
	/**
	 * 指定した文字列を読み込む PushbackReader を作成します
	 *
	 * @param		src		読み込む文字列
	 */
	PushbackReader(String src) {
		this.src = src;
		pos = 0;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * １文字読み込みます。文字列の末尾に達していた場合、-1 が返却されます。
	 *
	 * @return		読み込んだ文字。末尾で読み込めない場合 -1。
	 */
	int read() throws IOException {
		if (pos == src.length()) return -1;
		return src.charAt(pos++);
	}
	
	/**
	 * 読み込んだ文字１字分を戻します。
	 * read() ではもう一度同じ文字が読み込まれます。
	 * ただし、文字列の末尾であった場合、次の read() は常に最後の文字を
	 * 返却します。
	 *
	 * @param		c		任意の値(戻される文字は常にもともと文字列にあった
	 *						ものになります)
	 */
	void unread(int c) throws IOException {
		if (pos == 0)
			throw new IllegalStateException("at the beginning of String");
		pos--;
	}
	
	/**
	 * これまでに読み込んだ文字数を返却します。
	 *
	 * @return		これまでに読み込んだ文字数
	 */
	int bytesRead() {
		return pos-1;
	}
	
	/**
	 * 最後に読み込んだ文字の周辺の文字列を返却します。
	 * JSON Parse 例外が出た場合、誤りを解析するために使われることを期待
	 * しています。
	 *
	 * @return		最後に読み込んだ文字が &gt; &lt; にくくられ、前後の
	 *				文字を含んだ文字列が返却されます。
	 */
	String neighborhood() {
		if (pos == 0)
			throw new IllegalStateException("at the beginning of String");
		int s = pos-1-BUF_SIZE/2;
		if (s < 0) s = 0;
		int e = s + BUF_SIZE;
		if (e > src.length()) e = src.length();
		return src.substring(s,pos-1)+">"+src.charAt(pos-1)+"<"
				+src.substring(pos,e);
	}
}

/**
 * Reader 用の PushbackReader
 */
class PushbackReader2 extends PushbackReader {
	
	Reader r;
	int b; // the pushbacked char, -1 if not pushbacked
	char[] buff; // ringed buffer
	int bufpos; // current position
	
	PushbackReader2(Reader r) {
		this.r = r;
		b = -1;
		pos = 0;
		buff = new char[BUF_SIZE];
		bufpos = BUF_SIZE - 1;
	}
	
	@Override int read() throws IOException {
		if (b != -1) {
			int bb = b;
			b = -1;
			bufpos++;
			if (bufpos > BUF_SIZE-1) bufpos = 0;
			pos++;
			return bb;
		}
		pos++;
		int read = r.read();
		if (read != 1) {
			bufpos++;
			if (bufpos > BUF_SIZE-1) bufpos = 0;
			buff[bufpos] = (char)read;
		}
		return read;
	}
	
	/**
	 * 指定された文字１字分を戻します。
	 * 次の read() では指定された文字が読み込まれます。
	 *
	 * @param		c		戻す１文字
	 */
	@Override void unread(int c) throws IOException {
		if (b != -1)
			throw new IllegalStateException("already pushbacked:"+b);
		b = c;
		pos--;
		bufpos--;
		if (bufpos < 0) bufpos = BUF_SIZE-1;
	}
	
	@Override int bytesRead() {
		return (b==-1?pos-1:pos-2);
	}
	
	/**
	 * 最後に読み込んだ文字の周辺の文字列を返却します。
	 * JSON Parse 例外が出た場合、誤りを解析するために使われることを期待
	 * しています。PushbackReader2 では Reader を利用しているため、
	 * Reader の現在位置を変えないよう後続の文字列は出力されません。
	 *
	 * @return		最後に読み込んだ文字が &gt; &lt; にくくられ、前の
	 *				文字を含んだ文字列が返却されます。
	 */
	@Override String neighborhood() {
		int firstIndex = bufpos + ( (b==-1)?1:2 );
		
		while ( firstIndex < BUF_SIZE-1 && buff[firstIndex] == 0) firstIndex++;
		char[] first = Arrays.copyOfRange(buff, firstIndex, BUF_SIZE);
		char[] second = Arrays.copyOfRange(buff, 0, bufpos);
		
		return new String(first)+new String(second)+">"+buff[bufpos]+"<";
	}
}
