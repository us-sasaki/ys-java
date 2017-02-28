package abdom.data.json;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Json形式における型一般(var)を表します。また、ストリーム、文字列からの parse 
 * メソッドを提供します。
 * 利便性のため、キャストせずに利用するアクセスメソッドを定義しています。
 * これらのメソッドの JsonType でのデフォルトの実装は ClassCastException 
 * のスローであり、継承した各クラスで可能なオペレーションを実装します。
 * 利用できないオペレーションでは、ClassCastException が発生します。
 *
 * @version		November 19, 2016
 * @author		Yusuke Sasaki
 */
public abstract class JsonType extends Number
								implements Iterable<JsonType>, Jsonizable {
	/** getType() で返却される、JavaScript での型 void(null) を表す定数です */
	public static final int TYPE_VOID = 0;
	
	/** getType() で返却される、JavaScript での型 boolean を表す定数です */
	public static final int TYPE_BOOLEAN = 1;
	
	/** getType() で返却される、JavaScript での型 number(int) を表す定数です */
	public static final int TYPE_INT = 2;
	
	/**
	 * getType() で返却される、JavaScript での型 number(double) を表す定数です
	 */
	public static final int TYPE_DOUBLE = 3;
	
	/** getType() で返却される、JavaScript での型 string を表す定数です */
	public static final int TYPE_STRING = 4;
	
	/** getType() で返却される、JavaScript での型 array を表す定数です */
	public static final int TYPE_ARRAY = 10;
	
	/** getType() で返却される、JavaScript での型 object を表す定数です */
	public static final int TYPE_OBJECT = 20;
	
	/**
	 * getType() で返却される、どの型でもないことを表す定数です。
	 * この値が返却されることは通常ありません。JsonType を継承した新しい
	 * クラスを作成したり、JsonValue を継承して value, quote に新しい値を
	 * 定義した場合に返却される可能性があります。
	 */
	public static final int TYPE_UNKNOWN = 99;
	
	/**
	 * 高速化のため、System.getProperty("line.separator")
	 * の値を保持します。
	 */
	protected static final String LS = System.getProperty("line.separator");
	
	/**
	 * JsonValue としての値を文字列で取得します。このオブジェクトが
	 * JsonValue でない場合、ClassCastException がスローされます。
	 * 文字列の場合、JSON におけるダブルクオーテーション括りを除去した
	 * 形式になります。また、コントロールコードのエスケープシーケンスが
	 * 解除されます。
	 *
	 * @return	JsonValue としての文字列値
	 */
	public String getValue() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、getValue できません");
	}
	
/*--------------------
 * overrides (Number)
 */
	/**
	 * JsonValue としての値を整数値で取得します。このオブジェクトが
	 * JsonValue でない場合、ClassCastException がスローされます。
	 * また、JsonValue でも整数として認識できない場合(Integer.parseInt が
	 * 失敗)、NumberFormatException がスローされます。
	 *
	 * @return	JsonValue としての int 値
	 */
	@Override
	public int intValue() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、intValue を持ちません");
	}
	
	/**
	 * JsonValue としての値を整数値で取得します。このオブジェクトが
	 * JsonValue でない場合、ClassCastException がスローされます。
	 * また、JsonValue でも整数として認識できない場合(Long.parseLong が
	 * 失敗)、NumberFormatException がスローされます。
	 *
	 * @return	JsonValue としての long 値
	 */
	@Override
	public long longValue() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、longValue を持ちません");
	}
	
	/**
	 * JsonValue としての値をdouble値で取得します。このオブジェクトが
	 * JsonValue でない場合、ClassCastException がスローされます。
	 * また、JsonValue でも float として認識できない場合
	 * (Float.parseFloat が失敗)、NumberFormatException がスローされます。
	 *
	 * @return	JsonValue としての float 値
	 */
	@Override
	public float floatValue() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、floatValue を持ちません");
	}
	
	/**
	 * JsonValue としての値をdouble値で取得します。このオブジェクトが
	 * JsonValue でない場合、ClassCastException がスローされます。
	 * また、JsonValue でも double として認識できない場合
	 * (Double.parseDouble が失敗)、NumberFormatException がスローされます。
	 *
	 * @return	JsonValue としての double 値
	 */
	@Override
	public double doubleValue() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、doubleValue を持ちません");
	}
	
/*------------------
 * instance methods
 */
	/**
	 * JsonObject として、指定されたキーの値を持っているかテストします。
	 * JsonObject でない場合、false が返却されます。
	 *
	 * @return	指定されたキーの値を持っている場合 true、キーがあっても
	 *			値が JsonValue(null) である場合、またはキーがない場合、
	 *			またはこのインスタンスが JsonObject でない場合 false
	 */
	public boolean hasKey(String key) {
		if (!(this instanceof JsonObject)) return false;
		JsonType val = get(key);
		return ( (val != null) && (!(val instanceof JsonValue)) &&
				(!val.toString().equals("null")) );
	}
	
	/**
	 * JsonObject として、指定されたキーの値を取得します。
	 * JsonObject でない場合、ClassCastException がスローされます。
	 * キー値には、階層的なオブジェクト構造を辿るための . (dot)表記が
	 * サポートされます。
	 *
	 * @param	key		値を取得したいキー名
	 * @return	取得される値(JsonType)
	 */
	public JsonType get(String key) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、get(String) はサポートされません");
	}
	
	/**
	 * JsonObject として、指定されたキーの値を取得し、削除します。(cut)
	 * JsonObject でない場合、ClassCastException がスローされます。
	 *
	 * @param	key		値を取得し、削除したいキー名
	 * @return	取得される値(JsonType)。キーが存在しない場合、null
	 */
	public JsonType cut(String key) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、cut(String) はサポートされません");
	}
	
	/**
	 * JsonArray として、指定された index の値を取得します。
	 * JsonArray でない場合、ClassCastException がスローされます。
	 *
	 * @param	index	index値( 0 ? size()-1 )
	 * @return	取得される値(JsonType)
	 */
	public JsonType get(int index) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、get(int) はサポートされません");
	}
	
	/**
	 * JsonArray として、指定された index の値を取得し、削除します。
	 * JsonArray でない場合、ClassCastException がスローされます。
	 *
	 * @param	index	index値( 0 ? size()-1 )
	 * @return	取得される値(JsonType)
	 */
	public JsonType cut(int index) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、cut(int) はサポートされません");
	}
	
	/**
	 * JsonArray として、要素数を返却します。
	 * JsonArray でない場合、ClassCastException がスローされます。
	 *
	 * @return	要素数
	 */
	public int size() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、size() はサポートされません");
	}
	
	/**
	 * この JsonType が JavaScript のどの型であるかを示す定数を返却します。
	 * Number 型については TYPE_INT, TYPE_DOUBLE のいずれかに分類されますが、
	 * Long.parseLong が成功する場合、TYPE_INT が返却されます。
	 * (TYPE_INT が TYPE_DOUBLE に優先します)
	 *
	 * @return	型を示す定数
	 * @see		#TYPE_VOID
	 * @see		#TYPE_BOOLEAN
	 * @see		#TYPE_INT
	 * @see		#TYPE_DOUBLE
	 * @see		#TYPE_STRING
	 * @see		#TYPE_ARRAY
	 * @see		#TYPE_OBJECT
	 * @see		#TYPE_UNKNOWN
	 */
	public int getType() {
		return TYPE_UNKNOWN;
	}

/*
 * set(array)
 */
	/**
	 * 配列値を指定された引数で設定します。
	 * 元々持っていた配列値は削除されます。
	 * このオブジェクトが JsonArray でない場合、ClassCastException
	 * がスローされます。
	 *
	 * @param	array	JsonArray の要素として設定する値
	 */
	public void set(Object... array) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、set できません");
	}
	
/*
 * add methods
 */
	/**
	 * この JsonObject に要素を追加します。
	 * put との違いは、すでに name で指定される要素が存在した場合、
	 * name の値を JsonArray に変換して値を追加する点と、null 値を
	 * 指定していた場合、何もしない点です。
	 * すでに name で指定される要素が JsonArray であった場合、
	 * その JsonArray に指定された要素が追加(push)されます。
	 *
	 * @param	name	要素名
	 * @param	t		値
	 * @return	要素が追加された JsonObject (this)
	 * @see		#put(String, boolean)
	 */
	public JsonObject add(String name, boolean t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, Jsonizable t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, String t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, byte t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, char t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, short t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, int t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, long t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, float t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, double t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
	public JsonObject add(String name, Jsonizable[] t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、add できません");
	}
/*
 * put methods (add と同等だが、値を上書き)
 */
	/**
	 * この JsonObject に要素を追加します。
	 * add との違いは、すでに name で指定される要素が存在した場合、
	 * name の値を上書きする点と、null 値を指定した場合、JsonValue(null)
	 * で上書きする点です。
	 *
	 * @param	name	要素名
	 * @param	t		値
	 * @return	要素が追加された JsonObject (this)
	 * @see		#add(String, boolean)
	 */
	public JsonObject put(String name, Jsonizable t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, String t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, boolean t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, byte t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, char t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, short t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, int t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, long t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, float t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, double t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
	public JsonObject put(String name, Jsonizable[] t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、put できません");
	}
/*
 * push methods (配列の最後尾に値追加)
 */
	/**
	 * この JsonArray の最後尾(index が size() - 1 の後ろ)に要素を追加します。
	 *
	 * @param	t		値
	 * @return	要素が追加された JsonArray (this)
	 */
	public JsonArray push(boolean t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(Jsonizable t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(String t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(byte t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(char t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(short t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(int t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(long t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(float t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	public JsonArray push(double t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	}
	//public JsonArray push(Jsonizable[] t) {
	//	throw new ClassCastException("この JsonType は " + getClass() + " のため、push できません");
	//}
	
/*
 * pop methods (配列の最後の要素を取得し、削除)
 */
	/**
	 * この JsonArray の最後尾(index が size() - 1)の要素を取得し、
	 * 削除します。配列長は１少なくなります。
	 *
	 * @return	要素が少なくなった JsonArray (this)
	 */
	public JsonType pop() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、pop できません");
	}
	
/*
 * shift methods (配列の最初に値追加)
 */
	/**
	 * この JsonArray の最初(index が 0)に要素を追加し、後続のインデックスを
	 * +1 します。
	 *
	 * @param	t		値
	 * @return	要素が追加された JsonArray (this)
	 */
	public JsonArray shift(boolean t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(Jsonizable t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(String t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(byte t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(char t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(short t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(int t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(long t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(float t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(double t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	public JsonArray shift(Jsonizable[] t) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、shift できません");
	}
	
/*
 * unshift methods (配列の最初の要素を取得し、削除)
 */
	/**
	 * この JsonArray の最初(index が 0)の要素を取得し、削除します。
	 * 後続の要素の index は -1 され、配列長は１少なくなります。
	 *
	 * @return	要素が少なくなった JsonArray (this)
	 */
	public JsonType unshift() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、unshift できません");
	}
	
	/**
	 * JavaScript における slice 操作(部分配列の切り出し)です。
	 * 
	 * @param	s	コピーする最初のインデックス(含みます)
	 * @param	e	コピーする末尾のインデックス(含みません)
	 * @return	切り取った JsonArray (要素は参照です(shallow copy))
	 */
	public JsonArray slice(int s, int e) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、slice できません");
	}
	
	/**
	 * JavaScript における concat (結合、非破壊的で元の値を保つ) です。
	 * JsonArray 以外を指定すると、ClassCastException がスローされます。
	 *
	 * @param	target	結合する JsonArray
	 * @return	結合後の JsonArray。元の JsonArray (this) は変更されません。
	 */
	public JsonArray concat(Jsonizable target) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、concat できません");
	}
	
	/**
	 * JavaScript における splice (継ぎ合わせ) です。
	 * 元のオブジェクトは push 同様変更されます(破壊的)。
	 *
	 * @param	index	挿入するインデックス
	 * @param	delete	削除する要素数
	 * @param	toAdd	index の位置に挿入する要素(JsonArray)
	 */
	public JsonArray splice(int index, int delete, Jsonizable toAdd) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、splice できません");
	}
	
	/**
	 * JavaScript における splice (継ぎ合わせ) です。
	 * 元のオブジェクトは push 同様変更されます(破壊的)。
	 * toAdd として、JsonType を１つだけ指定した場合、splice(int,int,JsonType)
	 * が呼ばれ、JsonType が JsonArray だった場合に配列の解除が行われます。
	 *
	 * @param	index	挿入するインデックス
	 * @param	delete	削除する要素数
	 * @param	toAdd	index の位置に挿入する複数要素
	 */
	public JsonArray splice(int index, int delete, Object... toAdd) {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、splice できません");
	}
	/**
	 * JsonObject としてのキー(keySet)を取得します。
	 *
	 * @return	キー集合(Set<String>)
	 */
	public Set<String> keySet() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、keySet を持ちません");
	}
	
/*---------------
 * class methods
 */
	/*
	 * new JsonObject を得るための便利関数です。
	 * 文字数(タイプ数)を減らす目的で設定されています。
	 * new JsonObject().add("name", "value") を
	 * JsonType.o("name", "value") で取得できます。
	 *
	 * @param	name	キー名
	 * @param	t		バリュー
	 * @return	新しく生成されたJsonObject
	 */
	public static JsonObject o(String name, Jsonizable t) {
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
	public static JsonObject o(String name, Jsonizable[] t) {
		return new JsonObject().put(name, t);
	}
	
	/**
	 * new JsonArray を得るための便利関数です。タイプ数を減らす目的で
	 * 設定されています。
	 * new JsonArray().push(5).push("hoe") または
	 * new JsonArray().set(5, "hoe");
	 * new JsonArray().splice(0,0,5,"hoe") を
	 * JsonType.a(5, "hoe")
	 * のように取得できます。
	 *
	 * @param		param	配列を構成する要素
	 */
	public static JsonArray a(Object... param) {
		JsonArray result = new JsonArray();
		for (Object t : param) {
			if (t == null) result.push(new JsonValue(null));
			else if (t instanceof JsonType) result.push((JsonType)t);
			else if (t instanceof Jsonizable) result.push(((Jsonizable)t).toJson());
			else if (t instanceof String) result.push((String)t);
			else if (t instanceof Byte) result.push((Byte)t);
			else if (t instanceof Character) result.push((Character)t);
			else if (t instanceof Short) result.push((Short)t);
			else if (t instanceof Integer) result.push((Integer)t);
			else if (t instanceof Long) result.push((Long)t);
			else if (t instanceof Float) result.push((Float)t);
			else if (t instanceof Double) result.push((Double)t);
			else if (t instanceof Boolean) result.push((Boolean)t);
			else throw new ClassCastException(t.getClass() + " は JsonArray の要素に指定できません");
		}
		return result;
	}
	
	/**
	 * 指定された JSON 文字列から JsonType を生成します。
	 * このメソッドは共有状態を持たずスレッドセーフです。
	 *
	 * @param	str		JSON 文字列
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
	 * Reader は JSON value 終了位置まで読み込まれ、close() されません。
	 * Reader は内部的に PushbackReader として利用されます。
	 * このメソッドは共有状態を持たずスレッドセーフです。
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
	 * スペース文字は、space, tab, CR, LF です。
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
	 * (比較は２文字目から行われます)
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
				long v = Long.parseLong(token);
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
						if (c >= '0' && c <= '9') u = 16*u + (c-'0');
						else if (c >= 'A' && c <= 'F') u = 16*u + (c-'A') +10;
						else if (c >= 'a' && c <= 'f') u = 16*u + (c-'a') +10;
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
		skipspaces(pr);
		int c = pr.read();
		if (c == ']') {
			return new JsonArray(); // 空のJsonArray
		}
		pr.unread(c);
		JsonArray result = new JsonArray();
		while (true) {
			skipspaces(pr);
			JsonType j = parseValue(pr);
			result.push(j);
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("配列の終りの前に終了を検知しました");
			if (c == ']') {
				return result;
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
			// -> RFC 7159 によると、string とあり、なんでもOK
			// 　　特に、"." も OK
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
	@Override
	public java.util.Iterator<JsonType> iterator() {
		throw new ClassCastException("この JsonType は " + getClass() + " のため、iterator を持ちません");
	}
	
/*------------------------
 * implements(Jsonizable)
 */
	/**
	 * JsonType では toJson() はこのオブジェクト自身を返却します。
	 *
	 * @return	このオブジェクト
	 */
	@Override
	public JsonType toJson() {
		return this;
	}
	
	/**
	 * JSON 文字列表現を返却します。
	 * toString() の文字列表現は、改行やスペース文字を含まない JSON 形式です。
	 * string 型 (JsonValue で保持する値が String の場合) では
	 * 結果は ""(ダブルクオーテーション) で括られることに注意してください。
	 *
	 * @return	このオブジェクトの JSON 形式(文字列)
	 */
	@Override
	public abstract String toString();
	
	/**
	 * 人が見やすいインデントを含んだ JSON 形式で文字列化します。
	 * 最大横幅はデフォルト値(80)が設定されます。
	 * 最大横幅は JsonArray, JsonObject の各要素が収まる場合に一行化する幅
	 * であり、すべての行が最大横幅以内に収まるわけではありません。
	 * (JSON では文字列要素の途中改行記法がありません)
	 *
	 * @param	indent	インデント(複数のスペースやタブ)
	 * @return	インデント、改行を含む JSON 文字列
	 */
	@Override
	public String toString(String indent) {
		return toString(indent, 80);
	}
	
	/**
	 * 人が見やすいインデントを含んだ JSON 形式で文字列化します。
	 * JsonObject, JsonArray 値を一行で表せるなら改行させないための、一行の
	 * 文字数を指定します。
	 *
	 * @param	indent		インデント(複数のスペースやタブ)
	 * @param	textwidth	object, array に関し、この文字数に収まる場合
	 *						複数行に分けない処理を行うための閾値。
	 *						0 以下を指定すると、一行化を試みず、常に複数行化
	 *						されます。(この方が高速)
	 * @return	インデント、改行を含む JSON 文字列
	 */
	@Override
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
}
