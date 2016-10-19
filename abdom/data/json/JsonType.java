package abdom.data.json;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Json形式における型一般を表します(composite pat.)
 * キャストせずに利用できるよう、アクセスメソッドを提供します。
 * アクセスできない型であった場合、ClassCastException が発生します。
 */
public abstract class JsonType {

	public String getValue() {
		return ((JsonValue)this).value; // may throw ClassCastException
	}
	public JsonType get(String key) {
		JsonObject jo = (JsonObject)this; // may throw ClassCastException
		return jo.map.get(key);
	}
	public JsonType get(int index) {
		JsonArray ja = (JsonArray)this; // may throw ClassCastException
		return ja.array[index]; // may throw ArrayIndexOutOfBoundsException
	}
	public int size() {
		JsonArray ja = (JsonArray)this; // may throw ClassCastException
		return ja.array.length;
	}
	public JsonObject add(String name, JsonType t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, String t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, boolean t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, byte t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, char t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, short t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, int t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, long t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, float t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, double t) {
		return ((JsonObject)this).add(name, t);
	}
	public JsonObject add(String name, JsonType[] t) {
		return ((JsonObject)this).add(name, t);
	}
	
	/**
	 * 人が見やすい indent に対応するためのメソッド
	 */
	public String toString(String indent) {
		return indent + toString(); // デフォルトの実装
	}
	
/*---------------
 * class methods
 */
	public static JsonType parse(String str) {
		try {
			return parse(new StringReader(str));
		} catch (IOException e) {
			throw new InternalError("StringReader で IOException が発生しました"+e);
		}
	}
	
	/**
	 * 指定された InputStream から JSON value を１つ読み込みます。
	 * InputStream はJSON value終了位置まで読み込まれ、close() されません。
	 * InputStream は内部的に PushbackInputStream として利用されます。
	 */
	public static JsonType parse(Reader in) throws IOException {
		PushbackReader pr = new PushbackReader(in);
		return parseValue(pr);
	}
	
	/**
	 * スペース文字をスキップします。
	 * スペース文字は、space, tab, cr, lf です。
	 */
	private static void skipspaces(PushbackReader pr) throws IOException {
		while (true) {
			int c = pr.read();
			switch (c) {
			case ' ': continue;
			case '\t': continue;
			case '\r': continue;
			case '\n': continue;
			case -1: return;
			default:
				pr.unread(c);
				return;
			}
		}
	}
	
	/**
	 * 指定された PushbackInputStream から JSON value を１つ読み込み、
	 * JsonType として返却します。
	 */
	private static JsonType parseValue(PushbackReader pr) throws IOException {
		skipspaces(pr);
		int c = pr.read();
		JsonType jt = null;
		switch (c) {
		case '-':
			pr.unread(c);
			jt = parseNumber(pr);
			break;
		case '{':
			jt = parseObject(pr);
			break;
		case '[':
			jt = parseArray(pr);
			break;
		case 't':
			expect(pr, "true");
			jt = new JsonValue(true);
			break;
		case 'f':
			expect(pr, "false");
			jt = new JsonValue(false);
			break;
		case 'n':
			expect(pr, "null");
			jt = new JsonValue(null);
			break;
		case '\"':
			jt = parseString(pr);
			break;
		default:
			if (c >= '0' && c <= '9') {
				pr.unread(c);
				jt = parseNumber(pr);
			}
		}
		if (jt == null)	throw new JsonParseException("value の先頭文字が不正です : " + (char)c);
		return jt;
	}
	
	/**
	 * 指定した文字列となっていることをチェックします。
	 * ストリームの終わりを検出したり、指定した文字列と異なっている場合、
	 * JsonParseException をスローします。
	 */
	private static void expect(PushbackReader pr, String expected) throws IOException {
		for (int i = 1; i < expected.length(); i++) {
			int c = pr.read();
			if (c == -1) throw new JsonParseException("予期しない終了を検出しました。予期した文字列:"+expected);
			if (expected.charAt(i) != (char)c) throw new JsonParseException("予期しない文字を検出しました:"+(char)c+" 予期した文字:"+expected.charAt(i)+" 予期した文字列:"+expected);
		}
	}
	
	/**
	 * 数値の可能性のあるトークン(0-9, -+.eE からなる文字列)を抽出する。
	 */
	private static String readNumberToken(PushbackReader pr) throws IOException {
		StringBuilder result = new StringBuilder();
		while (true) {
			int c = pr.read();
			if (c == -1) break;
			if ((c >= '0' && c <= '9') || (c == '-' || c == '+' ||
					c == '.' || c == 'e' || c == 'E')) {
				result.append((char)c);
			} else {
				pr.unread(c);
				break;
			}
		}
		return result.toString();
	}
	
	/**
	 * 数値を読み込みます。数値でない場合、JsonParseException をスローします。
	 */
	private static JsonValue parseNumber(PushbackReader pr) throws IOException {
		String token = readNumberToken(pr);
		try {
			if (token.indexOf('.')>-1||token.indexOf('e')>-1||token.indexOf('E')>-1) {
				double v = Double.parseDouble(token);
				return new JsonValue(v);
			} else {
				int v = Integer.parseInt(token);
				return new JsonValue(v);
			}
		} catch (NumberFormatException nfe) {
			throw new JsonParseException("数値フォーマット異常 : " + token);
		}
	}
	
	/**
	 * " の次の文字にストリームがある前提で、続く文字列を取得します。
	 */
	private static JsonValue parseString(PushbackReader pr) throws IOException {
		return new JsonValue(readString(pr));
	}
	
	private static String readString(PushbackReader pr) throws IOException {
		StringBuilder result = new StringBuilder();
		while (true) {
			int c = pr.read();
			if (c == -1) throw new JsonParseException("文字列の途中で予期しない終了を検知しました");
			if (c == '\"') return result.toString();
			if (c < 32) throw new JsonParseException("文字列の途中で改行などのコントロールコードを検知しました。code = " + c);
			if (c == '\\') {
				c = pr.read();
				if (c == -1) throw new JsonParseException("\\ の次に予期しない終了を検知しました");
				switch (c) {
				case '\"':
				case '\\':
				case '/':
				case 'b':
				case 'f':
				case 'n':
				case 'r':
				case 't':
					result.append('\\');
					result.append((char)c);
					continue;
				case 'u':
					result.append('\\');
					result.append((char)c);
					for (int i = 0; i < 4; i++) {
						c = pr.read();
						if (c >= '0' && c <= '9') result.append( (char)c );
						else if (c >= 'A' && c <= 'F') result.append( (char)c );
						else if (c >= 'a' && c <= 'f') result.append( (char)c );
						else throw new JsonParseException("\\uの後の文字列が不正です : " + (char)c);
					}
				}
			}
			result.append((char)c);
		}
	}
	/**
	 * [ がある前提で、続く配列を取得します。
	 */
	private static JsonArray parseArray(PushbackReader pr) throws IOException {
		List<JsonType> array = new ArrayList<JsonType>();
		skipspaces(pr);
		int c = pr.read();
		if (c == ']') {
			return new JsonArray(array.toArray(new JsonType[0]));
		}
		pr.unread(c);
		while (true) {
			skipspaces(pr);
			JsonType j = parseValue(pr);
			array.add(j);
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("配列の終りの前に終了を検知しました");
			if (c == ']') {
				return new JsonArray(array.toArray(new JsonType[0]));
			}
			if (c != ',') throw new JsonParseException("配列内に不正な文字を検出しました : " + (char)c);
		}
	}
	
	/**
	 * { がある前提で、続くオブジェクトを取得します。
	 */
	private static JsonObject parseObject(PushbackReader pr) throws IOException {
		JsonObject result = new JsonObject();
		skipspaces(pr);
		int c = pr.read();
		if (c == '}') return result;
		pr.unread(c);
		
		while (true) {
			skipspaces(pr);
			c = pr.read();
			if (c != '\"') throw new JsonParseException("オブジェクト内の要素名が \" で始まっていません");
			String name = readString(pr);
			// ここで name として入っていてはならない文字をチェック
			// しかし、規則が書いてないので手抜き
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("オブジェクトの要素名の後に予期しない終了を検知しました");
			if (c != ':') throw new JsonParseException("オブジェクトの要素名の後に予期しない文字を検知しました : "+(char)c);
			skipspaces(pr);
			JsonType jt = parseValue(pr);
			result.add(name, jt);
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("オブジェクトの終りの前に予期しない終了を検知しました");
			if (c == ',') continue;
			if (c == '}') return result;
			throw new JsonParseException("オブジェクト内に不正な文字を検出しました : " + (char)c);
		}
	}

}
