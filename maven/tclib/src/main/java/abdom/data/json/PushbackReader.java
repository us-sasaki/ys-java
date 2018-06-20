package abdom.data.json;

import java.io.Reader;
import java.io.IOException;

/**
 * JsonType#parse で利用するための PushbackReader
 * java.io.PushbackReader はここでは不要な synchronized を行うため、最適化。
 * また、読み込み位置を返却し、例外に情報を付加できるようにした。
 *
 */
class PushbackReader {
	String src;
	int pos;
	
	PushbackReader() {
	}
	
	PushbackReader(String src) {
		this.src = src;
		pos = 0;
	}
	
	int read() throws IOException {
		if (pos == src.length()) return -1;
		return src.charAt(pos++);
	}
	
	void unread(int c) throws IOException {
		if (pos == 0)
			throw new IllegalStateException("at the beginning of String");
		pos--;
	}
	
	int bytesRead() {
		return pos;
	}
}

/**
 * Reader 用の PushbackReader
 */
class PushbackReader2 extends PushbackReader {
	Reader r;
	int b;
	
	PushbackReader2(Reader r) {
		this.r = r;
		b = -1;
		pos = 0;
	}
	
	@Override int read() throws IOException {
		if (b != -1) {
			int bb = b;
			b = -1;
			pos++;
			return bb;
		}
		pos++;
		return r.read();
	}
	
	@Override void unread(int c) throws IOException {
		if (b != -1)
			throw new IllegalStateException("already pushbacked:"+b);
		b = c;
		pos--;
	}
}
