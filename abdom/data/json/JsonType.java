package abdom.data.json;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Json形式における型一般を表します。また、ストリーム、文字列からの parse 
 * メソッドを提供します。
 * 利便性のため、なるべくキャストせずに利用できるアクセスメソッドを提供します。
 * アクセスできない型であった場合、ClassCastException が発生します。
 *
 * @version		November 19, 2016
 * @author		Yusuke Sasaki
 */
public abstract class JsonType implements Iterable<JsonType> {
	protected static final String LS = System.getProperty("line.separator");
	
	/**
	 * JsonValue としての値を文字列で取得します。このオブジェクトが
	 * JsonValue でない場合、ClassCastException がスローされます。
	 *
	 * @return	JsonValue としての文字列値
	 */
	public String getValue() {
		return ((JsonValue)this).getValue(); // may throw ClassCastException
	}
	
	/**
	 * JsonValue としての値を整数値で取得します。このオブジェクトが
	 * JsonValue でない場合、ClassCastException がスローされます。
	 * また、JsonValue でも整数として認識できない場合(Integer.parseInt が
	 * 失敗)、NumberFormatException がスローされます。
	 *
	 * @return	JsonValue としての int 値
	 */
	public int getIntValue() {
		return Integer.parseInt(((JsonValue)this).value); // may throw ClassCastException
	}
	
	/**
	 * JsonValue としての値をdouble値で取得します。このオブジェクトが
	 * JsonValue でない場合、ClassCastException がスローされます。
	 * また、JsonValue でも double として認識できない場合
	 * (Double.parseDouble が失敗)、NumberFormatException がスローされます。
	 *
	 * @return	JsonValue としての double 値
	 */
	public double getDoubleValue() {
		return Double.parseDouble(((JsonValue)this).value); // may throw ClassCastException
	}
	
	/**
	 * JsonType としての値を持っているかテストします。
	 * false となるのは以下の場合です。<pre>
	 * JsonObject で、空オブジェクトの場合
	 * JsonArray で、空配列の場合
	 * JsonValue で、値が false の場合
	 * </pre>
	 * ほかの場合、true が返却されます。
	 *
	 * @return	値を持っている、または false 値でない場合 true
	 */
	public boolean isTrue() {
		if (this instanceof JsonObject) {
			return (((JsonObject)this).keySet().size() > 0);
		} else if (this instanceof JsonArray) {
			return (((JsonArray)this).array.size() > 0);
		} else if (this instanceof JsonValue) {
			return !"\"false\"".equals(toString());
		} else {
			// never fall back here
			return true;
		}
	}
	
	/**
	 * JsonObject として、指定されたキーの値を取得します。
	 * JsonObject でない場合、ClassCastException がスローされます。
	 *
	 * @param	key		値を取得したいキー名
	 * @return	取得される値(JsonType)
	 */
	public JsonType get(String key) {
		JsonObject jo = (JsonObject)this; // may throw ClassCastException
		return jo.map.get(key);
	}
	
	/**
	 * JsonObject として、指定されたキーの値を取得し、削除します。(cut)
	 * JsonObject でない場合、ClassCastException がスローされます。
	 *
	 * @param	key		値を取得し、削除したいキー名
	 * @return	取得される値(JsonType)。キーが存在しない場合、null
	 */
	public JsonType cut(String key) {
		JsonObject jo = (JsonObject)this;
		return jo.map.remove(key);
	}
	
	/**
	 * JsonArray として、指定された index の値を取得します。
	 * JsonArray でない場合、ClassCastException がスローされます。
	 *
	 * @param	index	index値( 0 ～ size()-1 )
	 * @return	取得される値(JsonType)
	 */
	public JsonType get(int index) {
		JsonArray ja = (JsonArray)this; // may throw ClassCastException
		return ja.array.get(index); // may throw ArrayIndexOutOfBoundsException
	}
	
	/**
	 * JsonArray として、要素数を返却します。
	 * JsonArray でない場合、ClassCastException がスローされます。
	 *
	 * @return	要素数
	 */
	public int size() {
		JsonArray ja = (JsonArray)this; // may throw ClassCastException
		return ja.array.size();
	}
	
	public boolean isArray() {	return (this instanceof JsonArray);	}
	public boolean isObject() {	return (this instanceof JsonObject); }
	public boolean isValue() { return (this instanceof JsonValue); }
	
/*
 * add methods
 */
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
/*
 * put methods (add と同等だが、値を上書き)
 */
	public JsonObject put(String name, JsonType t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, String t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, boolean t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, byte t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, char t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, short t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, int t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, long t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, float t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, double t) {
		return ((JsonObject)this).put(name, t);
	}
	public JsonObject put(String name, JsonType[] t) {
		return ((JsonObject)this).put(name, t);
	}
/*
 * push methods (配列の最後尾に値追加)
 */
	public JsonArray push(JsonType t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(String t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(boolean t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(byte t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(char t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(short t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(int t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(long t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(float t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(double t) {
		return ((JsonArray)this).push(t);
	}
	public JsonArray push(JsonType[] t) {
		return ((JsonArray)this).push(t);
	}
	
/*
 * pop methods (配列の最後の要素を取得し、削除)
 */
	public JsonType pop() {
		return ((JsonArray)this).pop();
	}
	
/*
 * shift methods (配列の最初に値追加)
 */
	public JsonArray shift(JsonType t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(String t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(boolean t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(byte t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(char t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(short t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(int t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(long t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(float t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(double t) {
		return ((JsonArray)this).shift(t);
	}
	public JsonArray shift(JsonType[] t) {
		return ((JsonArray)this).shift(t);
	}
	
/*
 * unshift methods (配列の最初の要素を取得し、削除)
 */
	public JsonType unshift() {
		return ((JsonArray)this).unshift();
	}
	
	/**
	 * JsonObject としてのキー(keySet)を取得します。
	 *
	 * @return	キー集合(Set<String>)
	 */
	public Set<String> keySet() {
		return ((JsonObject)this).map.keySet(); // may throw ClassCastException
	}
	
	/**
	 * 文字列表現を返却します。文字列表現は、改行やスペース
	 * 文字を含まない JSON 形式です。
	 * string 型 (JsonValue で保持する値が String の場合) では
	 * 結果は ""(ダブルクオーテーション) で括られることに注意してください。
	 *
	 * @return	このオブジェクトの JSON 形式(文字列)
	 */
	public abstract String toString();
	
	/**
	 * 人が見やすいインデントを含んだ形式で文字列化します。
	 * 最大横幅はデフォルト値(80)が設定されます。
	 *
	 * @param	indent	インデント(複数のスペースやタブ)
	 * @return	インデント、改行を含む文字列
	 */
	public String toString(String indent) {
		return toString(indent, 80);
	}
	
	/**
	 * 人が見やすいインデントを含んだJSON形式で文字列化します。
	 * JsonObject, JsonArray 値を一行で表せるなら改行させないための、一行の
	 * 文字数を指定します。
	 *
	 * @param	indent		インデント(複数のスペースやタブ)
	 * @param	textwidth	object, array に関し、この文字数に収まる場合
	 *						複数行に分けない処理を行うための閾値。
	 *						0 以下を指定すると、一行化を試みず、常に複数行化
	 *						されます。(この方が高速)
	 * @return	インデント、改行を含む文字列
	 */
	public final String toString(String indent, int textwidth) {
		return toString("", indent, textwidth, false);
	}
	
	
	/**
	 * 人が見やすいインデントを含んだJSON形式で文字列化します。
	 * インデントをサポートするため、現在のインデントを示す indent,
	 * 次のインデントを作るための indentStep, 行が長くならない場合に
	 * 一行化するための textwidth, JsonObject にける "name" : 後に
	 * { を同行に配置する特例処理をするためのフラグ(objElement)を
	 * 持っています。
	 * 複数行に分けるための改行コードは、JsonType.LS として保持されています。
	 * <pre>
	 *
	 * [indent]*開始位置(objElement==true の時は indent をつけない)
	 * [indent][indentStep]*インデント付の次の行の開始位置
	 * -------------------------(textwidthまでは一行化されることあり)-----
	 * </pre>
	 * 
	 * @param	indent		インデント(いくつかのスペース)
	 * @param	indentStep	インデント一回分のスペースやタブ
	 * @param	textwidth	object, array に関し、この文字数に収まる場合
	 *						複数行に分けない処理を行うための閾値。
	 *						0 以下を指定すると、一行化を試みず、常に複数行化
	 *						されます。(この方が高速)
	 * @param	objElement	true..オブジェクトの要素名の後ろ
	 * @return	改行、スペースなどを含む String
	 */
	protected abstract String toString(String indent, String indentStep,
						int textwidth, boolean objElement);
	
/*---------------
 * class methods
 */
	/**
	 * new JsonObject を得るための便利関数です。
	 * 文字数(タイプ数)を減らす目的で設定されています。
	 * new JsonObject().add("name", "value") を
	 * JsonType.o("name", "value") で取得できます。
	 *
	 * @param	name	キー名
	 * @param	t		バリュー
	 * @return	新しく生成されたJsonObject
	 */
	public static JsonObject o(String name, JsonType t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, String t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, boolean t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, byte t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, char t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, short t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, int t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, long t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, float t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, double t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, JsonType[] t) {
		return new JsonObject().put(name, t);
	}
	
	/**
	 * 指定された JSON 文字列から JsonType を生成します。
	 *
	 * @param	str	Json文字列
	 * @return	指定された文字列の表す JsonType
	 */
	public static JsonType parse(String str) {
		try {
			return parse(new StringReader(str));
		} catch (IOException e) {
			throw new InternalError("StringReader で IOException が発生しました"+e);
		}
	}
	
	/**
	 * 指定された Reader から JSON value を１つ読み込みます。
	 * Reader はJSON value終了位置まで読み込まれ、close() されません。
	 * Reader は内部的に PushbackReader として利用されます。
	 *
	 * @param	in	Json文字列を入力する Reader。
	 * @return	生成された JsonType
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
	 * 指定された PushbackReader から JSON value を１つ読み込み、
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
	 * 数値の可能性のあるトークン(0-9, -+.eE からなる文字列)を抽出します。
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
				case '\"':	result.append(c); continue;
				case '\\':	result.append(c); continue;
				case '/':	result.append(c); continue;
				case 'b':	result.append('\b'); continue;
				case 'f':	result.append('\f'); continue;
				case 'n':	result.append('\n'); continue;
				case 'r':	result.append('\r'); continue;
				case 't':	result.append('\t'); continue;
				case 'u':
					int u = 0;
					for (int i = 0; i < 4; i++) {
						c = pr.read();
						if (c >= '0' && c <= '9') u = 256*u + (c-'0');
						else if (c >= 'A' && c <= 'F') u = 256*u + (c-'A') +10;
						else if (c >= 'a' && c <= 'f') u = 256*u + (c-'a') +10;
						else throw new JsonParseException("\\uの後の文字列が不正です : " + (char)c);
					}
					result.append((char)u);
					continue;
				}
			}
			result.append((char)c);
		}
	}
	/**
	 * [ がある前提(Readerの現在位置は [ の次)で、続く配列を取得します。
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
	 * { がある前提(Readerの現在位置は { の次)で、続くオブジェクトを取得
	 * します。
	 */
	private static JsonObject parseObject(PushbackReader pr) throws IOException {
		JsonObject result = new JsonObject();
		skipspaces(pr);
		int c = pr.read();
		if (c == '}') return result; // 空のオブジェクト
		pr.unread(c);
		
		while (true) {
			skipspaces(pr);
			c = pr.read();
			if (c != '\"') throw new JsonParseException("オブジェクト内の要素名が \" で始まっていません");
			String name = readString(pr);
			// ここで name として入っていてはならない文字をチェック
			// しかし、規則が書いてないので手抜き
			// RFC4627 によると、string とあり、なんでもOKらしい
			// "." も OK
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

/*----------------------
 * implements(Iterable)
 */
	public java.util.Iterator<JsonType> iterator() {
		try {
			JsonArray array = (JsonArray)this;
			return array.iterator();
		} catch (ClassCastException cce) {
			throw new ClassCastException("JsonType が JsonArray でないため、for などで iterator を取得できません: type="+ getClass() + ":" + cce);
		}
	}
}
