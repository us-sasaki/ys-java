package abdom.util;

import java.io.Closeable;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;

/**
 * CSV形式で表現されたストリーム(Reader)を行ごとに読み込むクラスです。
 * コンマの前後にスペースが入ってはいけません。
 * クオーテーション中、クオーテーションでエスケープできます。
 * CSV ファイルを for ループで使うための Iterable を返すメソッドがあります。
 *
 * @version		7, July 2017
 * @author		Yusuke Sasaki
 */
public class CsvReader implements Closeable {
	protected static final int BUFFER_SIZE = 1024;
	
	protected BufferedReader in;
	protected List<String> list;
	
/*-------------
 * Constructor
 */
	/**
	 * 渡された Reader は、最後まで読み込まれたとき close() されます。
	 */
	public CsvReader(Reader in) {
		if (in instanceof BufferedReader) this.in = (BufferedReader)in;
		else this.in = new BufferedReader(in);
		
		list = new ArrayList<String>();
	}
	
/*-----------------
 * instance method
 */
	/**
	 * CSVファイルを一行読み、String配列として返却します。
	 * CSVでは ”でくくることによって改行をデータに含むことができるため、
	 * テキスト表現の一行と本関数で解釈する一行は必ずしも一致しません。
	 *
	 * @return	一行をカラム値を値として持つ String 配列で返却します。
	 *			カラム値はヌル文字 "" である場合があります。
	 *			読み込むべき行がない場合(Reader の終了) null が返却され、
	 *			Reader はクローズされます。
	 *			改行のみの行は、ヌル文字１つからなる String 配列が
	 *			返却されます。
	 */
	public String[] readRow() throws IOException {
		// トークン分割
		int c;
		list.clear();
		
		// 1行読み込む
		while (true) {
			// token を読み込む
			String token = readToken(in);
			if (token != null) list.add(token);
			
			c = in.read();
			if (c == -1) break;
			if (c == '\r') {
				c = in.read();
				if (c == -1) break;
				if (c != '\n') throw new IOException("Illegal CR");
				break;
			}
			if (c == '\n') break; // LF のみも可
			if (c != ',') throw new InternalError("内部エラー");
		}
		int size = list.size();
		if (size == 0) {
			try {
				in.close();
			} catch (IOException ignored) {
			}
			return null;
		}
		
		return list.toArray(new String[size]);
	}
	
	/**
	 * CSV 形式のカラムを読み込み、文字列として返却します。
	 */
	private String readToken(BufferedReader r) throws IOException {
		StringBuilder result = new StringBuilder();
		
		r.mark(BUFFER_SIZE);
		int c = r.read();
		switch(c) {
		case -1:
			return null; // 要素はない
			
		case '\r':
		case '\n':
			r.reset();
			return "";
		
		case '\"':
		case '\'':
			// クオートつき
			int quote = c;
			
			// 終わりのクオートがくるまで読み込み続ける
			while (true) {
				c = r.read();
				if (c == -1) return result.toString();
				if (c == quote) {
					r.mark(BUFFER_SIZE);
					c = r.read();
					if (c != quote) {
						r.reset();
						break;
					}
				}
				result.append((char)c);
			}
			// 終わりのクオートが来た
			r.mark(BUFFER_SIZE);
			c = r.read();
			if (c == -1) return result.toString();
			if ( (c == ',')||(c == '\r')||(c == '\n') ) {
				r.reset();
				return result.toString();
			}
			// 終わりのクオートの後がコンマでも改行でもない
			throw new IOException("format error");
		
		default:
			// クオートのない通常のカラム
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
	
	@Override
	public void close() throws IOException {
		in.close();
	}
	
/*-----------------------
 * inner class(Iterator)
 */
	private static class CsvRowIterator
					implements Iterator<String[]>, Closeable {
		private CsvReader reader;
		private String[] next;
		private String fname;
		
		private CsvRowIterator(String fname) {
			try {
				FileReader fr = new FileReader(fname);
				reader = new CsvReader(fr);
				this.fname = fname;
			} catch (IOException ioe) {
				try {
					close();
				} catch (IOException ignored) {
				}
				throw new IllegalArgumentException("指定されたファイル" + fname + "が読み込めませんでした", ioe);
			}
			try {
				next = reader.readRow();
			} catch (IOException ioe) {
				try {
					close();
				} catch (IOException ignored) {
				}
				throw new IllegalArgumentException("指定されたファイル" + fname + "の読み込み中にエラーが発生しました", ioe);
			}
		}
		
		@Override
		public boolean hasNext() {
			return (next != null);
		}
		
		@Override
		public String[] next() {
			String[] ret = next;
			try {
				next = reader.readRow();
			} catch (IOException ioe) {
				try {
					close();
				} catch (IOException ignored) {
				}
				throw new IllegalArgumentException("指定されたファイル" + fname + "の読み込み中にエラーが発生しました", ioe);
			}
			return ret;
		}
		
		@Override
		public void close() throws IOException {
			if (reader != null) reader.close();
		}
		
	}
	
/*----------------
 * class methods
 */
	/**
	 * Iterator として CSV ファイルを扱うための便利メソッドです。
	 * IOException は IllegalArgumentException に変換されます。
	 * <pre>
	 * 使用例
	 * for (String[] row : CsvReader.rows("file.csv") {
	 *     // row に関する処理
	 *     for (String column : row) {
	 *         // column に関する処理
	 *     }
	 * }
	 * </pre>
	 */
	public static Iterable<String[]> rows(final String fname) {
		return new Iterable<String[]>() {
			@Override
			public Iterator<String[]> iterator() {
				return new CsvRowIterator(fname);
			}
		};
	}
	
	/**
	 * CSV ファイルをすべて読み込み、List<String[]> 形式で返却します。
	 */
	public static List<String[]> readAll(String filename) throws IOException {
		List<String[]> result = new ArrayList<String[]>();
		CsvReader cr = new CsvReader(new FileReader(filename));
		while (true) {
			String[] row = cr.readRow();
			if (row == null) break;
			result.add(row);
		}
		return result;
	}
	
	/**
	 * CSV ファイルをすべて読み込み、JsonArray 形式で返却します。
	 * 一行目をカラム名と認識します。
	 *
	 * @param	filename	CSVファイル名
	 * @return	CSV ファイル内容の JSON 変換
	 */
	public static JsonArray readAllasJson(String filename) throws IOException {
		JsonArray ja = new JsonArray();
		String[] columnName = null;
		boolean first = true;
		
		for (String[] row : rows(filename)) {
			if (first) {
				columnName = row;
				first = false;
				continue;
			}
			JsonObject jo = new JsonObject();
			int c = 0;
			for (String column : row) {
				String cn = null;
				try {	cn = columnName[c++];}
				catch (ArrayIndexOutOfBoundsException e) {cn = "noname"+(c++);}
				jo.put(cn, column);
			}
			ja.push(jo);
		}
		return ja;
	}
}

