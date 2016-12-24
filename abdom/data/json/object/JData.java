package abdom.data.json.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;

/**
 * JSON オブジェクトを Java オブジェクトによって模倣します。
 * このクラスを継承することで、Java オブジェクトと JSON 形式の相互変換が
 * 容易になります。つまり、Java オブジェクトのインスタンス変数が、
 * JSON 形式として直列化でき、また逆に JSON 形式から Java オブジェクトの
 * フィールドを設定できるようになります。
 * メンバ変数として次の型(JDataカテゴリ)が指定できます。<pre>
 *
 * boolean, int, double, String, JValue(,JData), JsonObject
 * および、これらの型の配列
 *
 * </pre>暗黙のフィールドとして、_extra (JsonObject型) を持っており
 * fill() の際に未定義のフィールド値はすべてここに格納されます。
 * また、toJson() では _extra フィールドは存在する(not null)場合のみJSON
 * メンバとして現れます。
 * 子クラスで、JSON形式との相互変換対象外とする変数を定義したい場合、
 * transient 修飾子をつけて下さい。
 * <pre>
 * null 値については、次のように取り扱う。
 *       Java Object                    JSON
 *  Object null;              ->   現れない
 *  JsonObject null;          ->   現れない
 *
 *       　　JSON                    Java Object
 *  現れない                  ->   設定しない
 *  null                      ->   Object null; を設定
 *                                 JsonObject null; を設定(JsonObject のため)
 * </pre>
 *
 * @version	December 23, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JData extends JValue {

	/** fill できなかった値を格納する予約領域 */
	protected transient JsonObject _extra;
	
	/**
	 * Field チェックはクラスごとに１度だけ行えばよいため、
	 * 行ったかどうかをクラス単位で保持する。
	 * この Set に含まれる Class はチェック済。
	 */
	private static Map<Class<?>, Map<String, Accessor>> _fieldAccessors;
	static {
		_fieldAccessors = new HashMap<Class<?>, Map<String, Accessor>>();
	}
	
	private static class MethodPair {
		Method getter;
		List<Method> setter = new ArrayList<Method>();
	}
	
/*-------------
 * constructor
 */
	protected JData() {
		getAccessors(this);
	}
/*------------------
 * instance methods
 */
	/**
	 * extra を持つかどうかテストします。
	 *
	 * @return	extra を持つ場合、true
	 */
	public boolean hasExtras() {
		return (_extra != null);
	}
	
	/**
	 * extra の keySet を返却します。ない場合、null となります。
	 *
	 * @return	extra のキー(extra が存在しない場合、null)
	 */
	public Set getExtraKeySet() {
		if (_extra == null) return null;
		return _extra.keySet();
	}
	
	/**
	 * extra アクセスメソッドで、JsonType 値を取得します。
	 * extra がない場合、あっても指定されたキーを持たない場合、
	 * null が返却されます。
	 *
	 * @param	key	extra の key 情報
	 * @return	key に対応する値(null の場合があります)
	 */
	public JsonType getExtra(String key) {
		if (_extra == null) return null;
		return _extra.get(key);
	}
	
	/**
	 * extra アクセスメソッドで、JsonType 値を設定します。
	 *
	 * @param	key	extra の key 情報
	 * @param	jt	設定する値
	 */
	public void putExtra(String key, JsonType jt) {
		if (_extra == null) _extra = new JsonObject();
		_extra.put(key, jt);
	}
	
	/**
	 * このインスタンスが持つ extra オブジェクト(JsonObject)
	 * の参照を返却します。内容の参照/変更を簡便に行うことを想定した
	 * メソッドです。
	 *
	 * @return	extra オブジェクト(JsonObject)。null の場合があります。
	 */
	public JsonObject getExtras() {
		return _extra;
	}
	
	
	/**
	 * 指定された JsonObject の内容をこのオブジェクトに設定します。
	 * 引数の型は、利便性のため JsonType としていますが、JsonObject
	 * 以外を指定すると、ClassCastException がスローされます。
	 * このメソッドは値を追加し、既存値は上書きされなければ保存される
	 * ことに注意してください。_extra も同様です。
	 *
	 * @param	json	このオブジェクトに値を与える JsonType
	 */
	@Override
	public void fill(JsonType json) {
		JsonType rest = fill(this, json);
		if (rest == null) return;
		if (_extra == null) _extra = (JsonObject)rest;
		else {
			for (String key : rest.keySet())
				_extra.put(key, rest.get(key));
		}
	}
	
	/**
	 * JSON形式の文字列でフィールドを埋めます。内部的には、文字列から
	 * JsonType を構成し、fill(JsonType) を呼んでいます。
	 *
	 * @param	jsonString	値を保持する JSON 文字列
	 */
	public void fill(String jsonString) {
		fill(JsonType.parse(jsonString));
	}
	
	/**
	 * このオブジェクトを JsonObject に変換します。
	 *
	 * @return	JsonObject
	 */
	@Override
	public JsonType toJson() {
		JsonType json = toJson(this);
		// _extra を追加
		if (_extra == null) return json;
		for (String key : _extra.keySet()) {
			json.put(key, _extra.get(key));
		}
		return json;
	}
	
	public String toString(String indent) {
		return toJson().toString(indent);
	}
	
	public String toString(String indent, int textwidth) {
		return toJson().toString(indent, textwidth);
	}
	
	@Override
	public String toString() {
		return toJson().toString();
	}
	
/*---------------
 * class methods
 */
	public static JsonObject fill(Object instance, JsonType arg) {
		Map<String, Accessor> accessors = getAccessors(instance);
		
		JsonObject extra = null;
		try {
			JsonObject hoe = (JsonObject)arg;
		} catch (ClassCastException cce) {
			throw new RuntimeException("instance = " + instance + " class = " + instance.getClass() + "arg = " + arg + " " + arg.getClass());
		}
		JsonObject jobj = (JsonObject)arg; // may throw ClassCastException
		
		for (String name : jobj.keySet()) {
			Accessor a = accessors.get(name);
			if (a == null) {
				// Field がない場合、_extra に格納
				if (extra == null) extra = new JsonObject();
				extra.put(name, jobj.get(name));
			} else {
				a.set(instance, jobj.get(name));
			}
		}
		return extra;
	}
	
	public static JsonType toJson(Object instance) {
		Map<String, Accessor> accessors = getAccessors(instance);
		
		JsonObject result = new JsonObject();
		for (String name : accessors.keySet()) {
			Accessor a = accessors.get(name);
			JsonType value = a.get(instance);
			if (value != null) result.put(name, value);
		}
		return result;
	}
	
	/**
	 * このインスタンスのクラスに関連する Accessor (値取得/設定オブジェクト)
	 * を取得します。
	 * ない場合、生成します。
	 *
	 * @param	instance	Json変換を行うインスタンス
	 */
	private static Map<String, Accessor> getAccessors(Object instance) {
		Class<?> cls = instance.getClass();
		synchronized (cls) {
			Map<String, Accessor> accessors = _fieldAccessors.get(cls);
			if (accessors != null) return accessors;
			
System.out.println("generate accessor of " + instance.getClass());
			//
			// Accessors を生成する
			//
			accessors = new HashMap<String, Accessor>();
			
			// Accessor を設定する。
			// 以下のメソッドは同一名で上書きするため、同一名称では
			// method が field に優先することとなる		
			addFieldAccessors(accessors, cls);
			addMethodAccessors(accessors, cls);
			synchronized (_fieldAccessors) {
				_fieldAccessors.put(cls, accessors);
			}
			return accessors;
		}
	}
	
	/**
	 * 与えられた Accessor に指定されたクラスの public フィールドに対する
	 * Accessor を追加します。
	 */
	private static void addFieldAccessors(
							Map<String, Accessor> accessors,
							Class<?> cls) {
		// public フィールドを走査
		Field[] fields = cls.getFields(); // public field を取得
		
		for (Field f : fields) {
			// static は除外
			if (Modifier.isStatic(f.getModifiers())) continue;
			// transient も除外
			if (Modifier.isTransient(f.getModifiers())) continue;
			
			Class type = f.getType();
			if (!isJDataCategory(type))
				throw new IllegalFieldTypeException("Illegal type \"" +
						type.getName() + "\" has found in field \""+
						f.getName()+ "\" of class \"" + cls.getName() +
						"\". JData field must consist of boolean, int, double, String, JValue, JsonObject, their arrays. To prevent the field from Jsonizing, set transient.");
			String name = f.getName();
			if (type.isArray()) {
System.out.println("field array put " + name);
				accessors.put(name, new ArrayAccessor(f));
			} else {
System.out.println("field put " + name);
				accessors.put(name, new SimpleAccessor(f));
			}
		}
	}
	
	/**
	 * 与えられた Accessor に指定されたクラスの public setter/getter メソッド
	 * で構成されるプロパティへの Accessor を追加します。
	 */
	private static void addMethodAccessors(
							Map<String, Accessor> accessors,
							Class<?> cls) {
		// getter/setter メソッドを走査
		Method[] methods = cls.getMethods(); // public methods を取得
		
		// ペア(候補)を格納
		Map<String, MethodPair> pairs = new HashMap<String, MethodPair>();
		
		for (Method m : methods) {
			// static は除外
			if (Modifier.isStatic(m.getModifiers())) continue;
			
			// 引数型、リターン型をチェック
			// 
			String methodName = m.getName();
			if (methodName.length() < 4) continue; // メソッド名４文字未満は除外
			char c = methodName.charAt(3);
			if (Character.isLowerCase(c)) continue; // 4文字目小文字は除外
			// geta() と getA() が異なるメソッドだが同一プロパティとなるため
			
			// プロパティ名を Java Beans 規則にのっとり生成
			String name;
			if (methodName.length() == 4) name = methodName;
			else {
				if (Character.isUpperCase(methodName.charAt(4))) {
					// 二文字目が大文字の場合、そのまま
					// 例 getURL() / setURL()　-> URL
					name = methodName.substring(3);
				} else {
					// 一文字目を小文字に
					// 例 getCount() / setCount() -> count
					name = ""+Character.toLowerCase(c)+methodName.substring(4);
				}
			}
			
			Class<?> retType  = m.getReturnType();
			Class<?>[] params = m.getParameterTypes();
			
			if (methodName.startsWith("get")) { // get
			
				if (params.length != 0) continue; // 引数付きは除外
				if (!isJDataCategory(retType)) continue; // JData catでないものは除外
				// set とペアになるまでは除外
				// 引数のない get メソッドは1つしかない(overloadがない)
				MethodPair mp = pairs.get(name);
				if (mp == null) mp = new MethodPair();
				mp.getter	= m;
				pairs.put(name, mp); // getter を登録
				
			} else if (methodName.startsWith("set")) { // set
			
				if (params.length != 1) continue; // 引数は１つ限定
				if (!isJDataCategory(params[0])) continue; // JData catでないものは除外
				// get とペアになるまでは除外
				//
				MethodPair mp = pairs.get(name);
				if (mp == null) mp = new MethodPair();
				mp.setter.add(m);
				pairs.put(name, mp);
			}
		}
for (String name : pairs.keySet())
	System.out.println(name + " " + pairs.get(name));
		
		// get の returnType と set の argType が同一のものを選択
		// Number getNumber() と
		// setNumber(Integer) はマッチしないことと決める。
		// 同様に、int getCount()  void setCount(Integer) もマッチしない
		for (String name : pairs.keySet()) {
			MethodPair mp = pairs.get(name);
			Class<?> retType = mp.getter.getReturnType();
			Method theOther = null;
			for (Method setter : mp.setter) {
				if (setter.getParameterTypes()[0] == retType) {
					theOther = setter;
					break;
				}
			}
			if (theOther != null) {
				if (retType.isArray()) {
System.out.println("method array put " + name);
					accessors.put(name, new ArrayAccessor(mp.getter, theOther));
				} else {
System.out.println("method put " + name);
					accessors.put(name, new SimpleAccessor(mp.getter, theOther));
				}
			}
		}
	}
	
	/**
	 * 指定された Class オブジェクトが JData カテゴリに含まれるか
	 * チェックします。
	 */
	private static boolean isJDataCategory(Class type) {
		// プリミティブ、String, JsonObject, JValue
		if ( boolean.class == type ||
			int.class == type ||
			double.class == type ||
			String.class == type ||
			JValue.class.isAssignableFrom(type) ||
			JsonObject.class.isAssignableFrom(type) ) return true;
		
		// 配列
		if (boolean[].class == type ||
			int[].class == type ||
			double[].class == type ||
			String[].class == type ||
			JValue[].class.isAssignableFrom(type) ||
			JsonObject[].class.isAssignableFrom(type) ) return true;
		
		return false;
	}
	
	/**
	 * JsonType(JsonArray) から、JData[] を生成する便利関数です。
	 * 指定する JsonType は、JsonObject を要素に持つ JsonArray である
	 * 必要があります。
	 * 返される配列の実行時の型は、指定された配列の型になります。
	 * 指定された配列にリストが収まる場合は、その配列で返されます。それ
	 * 以外の場合は、指定された配列の実行時の型と JsonArray のサイズを
	 * 使って新しい配列が割り当てられます。 
	 *
	 * @param	source	値を保持している JsonType
	 * @param	array	値を格納する配列(の型)
	 * @return	JsonType の値が設定された JData の子クラスのインスタンスの配列
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JData> T[] toArray(JsonType source, T[] array) {
		int size = source.size(); // may throw class cast exception
		T[] result = null;
		Class compType = array.getClass().getComponentType();
		if (array.length >= size) {
			result = array;
		} else {
			result = (T[])Array.newInstance(compType, size);
		}
		for (int i = 0; i < size; i++) {
			try {
				result[i] = (T)compType.newInstance();
				result[i].fill(source.get(i));
			} catch (InstantiationException ie) {
				throw new JDataDefinitionException("Failed to instantiate \"" + compType.getName() + "\". Default constructor may not be accessible or defined.");
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(iae.toString());
			}
		}
		return result;
	}
	
	/**
	 * JSON 文字列から、JData[] を生成する便利関数です。
	 * 指定する JSON 文字列は、object の array である必要があります。
	 * 返される配列の実行時の型は、指定された配列の型になります。
	 * 指定された配列にリストが収まる場合は、その配列で返されます。それ
	 * 以外の場合は、指定された配列の実行時の型と JSON arrayのサイズを
	 * 使って新しい配列が割り当てられます。 
	 *
	 * @param	source	値を保持している JsonType
	 * @param	array	値を格納する配列(の型)
	 * @return	JsonType の値が入れられた JData の子クラスの配列
	 */
	public static <T extends JData> T[] toArray(String source, T[] array) {
		return toArray(JsonType.parse(source), array);
	}
}
