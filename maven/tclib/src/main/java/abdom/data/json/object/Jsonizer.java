package abdom.data.json.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

/**
 * Java オブジェクトと JSON の相互変換に関する static メソッドを提供します。
 * Java オブジェクトにおいて次に定義する「プロパティ」が変換対象となります。<br>
 * 1.public メンバ変数。プロパティ名は変数名になります。<br>
 * 2.public getter, setter メソッドの対。プロパティ名は Java Beans 命名規則<br>
 *   によります。さらに対は getter は引数なし、setter は引数ありで getter <br>
 * 　の返値型と setter の引数型が一致するもの<br>
 * <br>
 *
 * 2017/9/8 JData カテゴリの制約をなくしました
 *
 * </pre>
 * JSON形式との相互変換対象外とする変数を定義したい場合、
 * transient 修飾子をつけて下さい。また、メソッドでは strictfp 修飾子が
 * getter, setter のいずれかに含まれると変換対象外となります(裏技)。
 * <pre>
 * null 値については、次のように取り扱います。
 *       Java Object                    toJson()
 *  Object null;              ->   現れない
 *  JsonObject null;          ->   現れない
 *  JsonValue(null)           ->   null
 *
 *       　　JSON                    fill()
 *  現れない                  ->   設定しない
 *  null                      ->   Object null; を設定
 *                                 JsonObject null; を設定
 *                                 JsonValue null; を設定
 * </pre>
 * Javaオブジェクトに JsonValue フィールドを持っており、JsonValue(null), null
 * を設定する場合を考えます。
 * この状態を示す JSON文字列としては "null" 以外の表記がなく、いずれかを暗黙
 * 的に決めておく必要があります。
 * この実装では、JSON文字列 "null" は、一律 Javaオブジェクトの null を設定
 * します。
 *
 * @version	December 24, 2016
 * @author	Yusuke Sasaki
 */
public class Jsonizer {

	/**
	 * Field, Method Accessor の生成はクラスごとに１度だけ行えばよいため、
	 * 行った結果をクラス単位で保持する。
	 * この Map に含まれる Class は生成済み。
	 */
	private static Map<Class<?>, Map<String, Accessor>> _fieldAccessors;
	static {
		_fieldAccessors = new HashMap<Class<?>, Map<String, Accessor>>();
	}
	
	private static class MethodPair {
		Method getter;
		List<Method> setter = new ArrayList<Method>();
	}
	
/*---------------
 * class methods
 */
	/**
	 * Java オブジェクトのプロパティを、指定された Jsonizable で設定します。
	 *
	 * @param	instance	設定対象の Java オブジェクト
	 * @param	arg			設定値を持つ Jsonizable オブジェクト
	 * @return	設定値の中で、Java オブジェクトに対応するプロパティがなく
	 *			設定しなかった項目。すべて設定された場合、null。
	 *			ただし、instance が JValue 直接の子クラスの場合(JDataの
	 *			子クラスでない場合)は、常に null。
	 */
	public static JsonObject fill(Object instance, Jsonizable arg) {
		if (instance instanceof JValue && !(instance instanceof JData)) {
			((JValue)instance).fill(arg);
			return null; // JValue は extra を持たない
		}
		
		Map<String, Accessor> accessors = getAccessors(instance);
		
		JsonObject extra = null;
		JsonObject jobj = (JsonObject)arg.toJson(); // may throw ClassCastException
		
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
	
	/**
	 * 指定された名称の単一フィールドを設定します。
	 *
	 * @param	instance	設定対象の Java オブジェクト
	 * @param	name		設定フィールド名(dot 記法が使えます)
	 * @param	arg			設定値を持つ Jsonizable オブジェクト
	 * @return	null(設定された場合) / arg(設定するフィールドがなかった場合)
	 */
	public static JsonType set(Object instance, String name, Jsonizable arg) {
		Map<String, Accessor> accessors = getAccessors(instance);
		int index = name.indexOf('.');
		if (index > -1) {
			// dot がある場合
			String nextName = name.substring(0, index);
			if (accessors.keySet().contains(nextName)) {
				// フィールドを持つ場合
				Accessor a = accessors.get(nextName);
				Object nextObj = a.getProp().getObj(instance);
				if (nextObj == null) {
					// 宣言型Classのオブジェクトを生成
					// 情報取得できないため、子クラスは生成できない
					try {
						nextObj = a.getProp().getType().newInstance();
					} catch (ReflectiveOperationException roe) {
						throw new JDataDefinitionException(
							"Failed to instantiate \"" +
							a.getProp().getName() +
							"\". Default constructor of class \"" +
							a.getProp().getType().getName() +
							"\" may not be accessible and defined.", roe);
					}
				}
				JsonType j = set(nextObj, name.substring(index + 1), arg);
				if (j == null) {
					// nextObj に設定成功なら、instance に obj を設定
					// 失敗のとき、instance 値は変更されてはならない
					a.getProp().setObj(instance, nextObj);
					return null;
				} else {
					return arg.toJson();
				}
			}
		} else {
			// dot がない場合
			if (accessors.keySet().contains(name)) {
				Accessor a = accessors.get(name);
				a.set(instance, arg.toJson());
				return null;
			}
		}
		// dot があって dot 以前で示されるプロパティがない場合
		// dot がなく、プロパティがない場合
		if (instance instanceof JData) {
			// JData なら putExtra で設定する
			JData jd = (JData)instance;
			jd.putExtra(name, arg);
			return null;
		} else {
			return arg.toJson();
		}
	}
	
	/**
	 * 指定された Java オブジェクトのプロパティ値に基づいて JsonType に
	 * 変換します。変換規則は、このパッケージの説明を参照してください。
	 *
	 * @param	instance	JsonType に変換する Java オブジェクト
	 * @return	変換された JsonType
	 */
	public static JsonType toJson(Object instance) {
		if (instance instanceof JValue && !(instance instanceof JData))
			return ((JValue)instance).toJson();
		
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
	 * 指定された Java オブジェクトの指定された名称のフィールドを
	 * JsonType として返却します。
	 * Java オブジェクトが JData のインスタンスの場合、Extra の
	 * フィールドも探索します。
	 *
	 * @param	instance	取得対象の Java オブジェクト
	 * @param	name		取得フィールド名(dot 記法が使えます)
	 * @return	取得された値(フィールドが存在しない場合、null)
	 */
	public static JsonType get(Object instance, String name) {
		Map<String, Accessor> accessors = getAccessors(instance);
		
		int index = name.indexOf('.');
		if (index > 0) {
			// dot がある場合
			String nextName = name.substring(0, index);
			if (accessors.keySet().contains(nextName)) {
				// フィールドを持つ場合
				Accessor a = accessors.get(nextName);
				Object nextObj = a.getProp().getObj(instance);
				if (nextObj == null) return null;
				return get(nextObj, name.substring(index + 1));
			}
		} else {
			if (accessors.keySet().contains(name)) {
				Accessor a = accessors.get(name);
				return a.get(instance);
			}
		}
		// dot があって dot 以前で示されるプロパティがない場合
		// dot がなく、プロパティがない場合
		if (instance instanceof JData) {
			JData jd = (JData)instance;
			return jd.getExtra(name);
		}
		return null;
	}
	
	/**
	 * 指定された Java オブジェクトの JSON 文字列表現を返却します。
	 * toString() の文字列表現は、改行やスペース文字を含まない JSON 形式です。
	 * string 型 (JsonValue で保持する値が String の場合) では
	 * 結果は ""(ダブルクオーテーション) で括られることに注意してください。
	 *
	 * @param	instance	JSON 文字列化する対象のインスタンス
	 * @return	このオブジェクトの JSON 形式(文字列)
	 */
	public static String toString(Object instance) {
		return toJson(instance).toString();
	}
	
	/**
	 * 指定された Java オブジェクトを人が見やすいインデントを含んだ JSON 
	 * 形式で文字列化します。
	 * 最大横幅はデフォルト値(80)が設定されます。
	 * 最大横幅は array, object の各要素が収まる場合に一行化する幅
	 * であり、すべての行が最大横幅以内に収まるわけではありません。
	 * (JSON では文字列要素の途中改行記法がありません)
	 *
	 * @param	instance	JSON 文字列化する対象のインスタンス
	 * @param	indent	インデント(複数のスペースやタブ)
	 * @return	インデント、改行を含む JSON 文字列
	 */
	public static String toString(Object instance, String indent) {
		return toJson(instance).toString(indent);
	}
	
	/**
	 * 指定された Java オブジェクトを人が見やすいインデントを含んだ JSON
	 * 形式で文字列化します。
	 * array, object 値を一行で表せるなら改行させないための、一行の
	 * 文字数を指定します。
	 *
	 * @param	instance	JSON 文字列化する対象のインスタンス
	 * @param	indent		インデント(複数のスペースやタブ)
	 * @param	textwidth	object, array に関し、この文字数に収まる場合
	 *						複数行に分けない処理を行うための閾値。
	 *						0 以下を指定すると、一行化を試みず、常に複数行化
	 *						されます。(この方が高速)
	 * @return	インデント、改行を含む JSON 文字列
	 */
	public static String toString(Object instance, String indent, int maxwidth) {
		return toJson(instance).toString(indent, maxwidth);
	}
	
	/**
	 * 指定されたオブジェクトのプロパティとして、key が含まれるかテストします。
	 * JData のインスタンスの場合、extra は探索しません。
	 *
	 * @param	instance	テスト対象のオブジェクト
	 * @param	key			プロパティ名
	 * @return	key で示される名のプロパティを持つ場合 true
	 */
	public static boolean hasProperty(Object instance, String key) {
		synchronized (_fieldAccessors) {
			return (getAccessors(instance).get(key) != null);
		}
	}
	
	/**
	 * 指定されたオブジェクトのプロパティ名のリストを Set で返却します。
	 * JData のインスタンスの場合、extra は対象外です。
	 *
	 * @param	instance	テスト対象のオブジェクト
	 * @return	プロパティ名のリスト
	 */
	public static Set<String> getPropertyNames(Object instance) {
		synchronized (_fieldAccessors) {
			// keySet() は不変オブジェクトのため synchronized 不要
			return getAccessors(instance).keySet();
		}
	}
	
	/**
	 * 指定されたインスタンスのクラスに関連する Accessor
	 *  (値取得/設定オブジェクト) を取得します。
	 * ない場合、生成します。
	 *
	 * @param	instance	Json変換を行うインスタンス
	 * @return	instance に対応する Accessor
	 */
	static Map<String, Accessor> getAccessors(Object instance) {
		Class<?> cls = instance.getClass();
		
		// JValue のインスタンスかつ、JData のインスタンスでない
		// 場合(JValue を直接継承)、Accessor による設定でなく、
		// fill(), toJson() による変換を行うため、null が返却される。
		if (JValue.class.isAssignableFrom(cls) &&
			!JData.class.isAssignableFrom(cls) ) return null;
		
		// _fieldAccessors は複数スレッドから操作される可能性がある
		// 一度取得されたインスタンスは不変のため、外部で synchronized
		// は不要
		synchronized (_fieldAccessors) {
			Map<String, Accessor> accessors = _fieldAccessors.get(cls);
			if (accessors != null) return accessors;
			
			//System.out.println("generate accessor of " + instance.getClass());
			//
			// Accessors を生成する
			//
			accessors = new HashMap<String, Accessor>();
			
			// Accessor を設定する。
			// 以下のメソッドは同一名で上書きするため、同一名称では
			// method が field に優先することとなる		
			addFieldAccessors(accessors, cls);
			addMethodAccessors(accessors, cls);
			
			_fieldAccessors.put(cls, accessors);
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
			
			String name = f.getName();
			if (type.isArray()) {
				//System.out.println("field put array " + name);
				accessors.put(name, new ArrayAccessor(f));
			} else {
				//System.out.println("field put " + name);
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
			// strictfp も除外(裏技用)
			if (Modifier.isStrict(m.getModifiers())) continue;
			
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
				
				// set とペアになるまでは除外
				// 引数のない get メソッドは1つしかない(overloadがない)
				MethodPair mp = pairs.get(name);
				if (mp == null) mp = new MethodPair();
				mp.getter	= m;
				pairs.put(name, mp); // getter を登録
				
			} else if (methodName.startsWith("set")) { // set
			
				if (params.length != 1) continue; // 引数は１つ限定
				// get とペアになるまでは除外
				//
				MethodPair mp = pairs.get(name);
				if (mp == null) mp = new MethodPair();
				mp.setter.add(m);
				pairs.put(name, mp);
			}
		}
		
		// get の returnType と set の argType が同一のものを選択
		// Number getNumber() と
		// setNumber(Integer) はマッチしないことと決める。
		// 同様に、int getCount()  void setCount(Integer) もマッチしない
		for (String name : pairs.keySet()) {
			MethodPair mp = pairs.get(name);
			
			// ペアがない場合スキップ
			if (mp.getter == null || mp.setter.size() == 0) continue;
			
			// ペアとなっていた場合、get の retType と set の paramType が
			// 一致する組み合わせを検索
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
					//System.out.println("method put array " + name);
					accessors.put(name, new ArrayAccessor(mp.getter, theOther));
				} else {
					//System.out.println("method put " + name);
					accessors.put(name, new SimpleAccessor(mp.getter, theOther));
				}
			}
		}
	}
	
	/**
	 * JsonType(JsonArray) から、Java オブジェクト配列を生成する便利関数です。
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
	public static <T> T[] toArray(Jsonizable json, T[] array) {
		JsonType source = json.toJson();
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
				if (JValue.class.isAssignableFrom(compType)) {
					// compType が JValue の場合、fill を呼ぶ
					((JValue)result[i]).fill(source.get(i));
				} else {
					// result[i] が JValue のサブクラスでも、
					// この処理では extra が設定されない
					fill(result[i], source.get(i));
				}
			} catch (InstantiationException ie) {
				throw new JDataDefinitionException("Failed to instantiate \"" + compType.getName() + "\". Default constructor may not be accessible and defined.", ie);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(iae.toString(), iae);
			}
		}
		return result;
	}
	
	/**
	 * JSON 文字列から、Java オブジェクトの配列を生成する便利関数です。
	 * 指定する JSON 文字列は、object の array である必要があります。
	 * 返される配列の実行時の型は、指定された配列の型になります。
	 * 指定された配列にリストが収まる場合は、その配列で返されます。それ
	 * 以外の場合は、指定された配列の実行時の型と JSON arrayのサイズを
	 * 使って新しい配列が割り当てられます。 
	 *
	 * @param	source	値を保持している JsonType
	 * @param	array	値を格納する配列(の型)
	 * @return	JsonType の値が入れられた Java オブジェクトの配列
	 */
	public static <T> T[] toArray(String source, T[] array) {
		return toArray(JsonType.parse(source), array);
	}
	
	/**
	 * 与えられた JsonType から Java オブジェクトを生成します。
	 * 指定された Class オブジェクトが JValue のサブクラスの場合、
	 * JValue.fill() が呼ばれます。
	 * 一方、JValue のサブクラスでない場合、Jsonizer.fill() が
	 * 使用され、指定された Class オブジェクトに格納できない値を
	 * JsonType 値が持っていた場合、捨てられます。
	 * <pre>
	 * 利用例
	 *   JDataSubClass jd = Jsonizer.fromJson(jsonType, JDataSubClass.class);
	 *
	 * または
	 *
	 *   Pojo pj = Jsonizer.fromJson(jsonType, Pojo.class);
	 * </pre>
	 *
	 * @param	source	パラメータを持つ JsonType 値
	 * @param	clazz	生成するインスタンスの Class オブジェクト
	 * @return	生成された Java オブジェクト
	 */
	public static <T> T fromJson(Jsonizable json, Class<T> clazz) {
		JsonType source = json.toJson();
		try {
			T instance = clazz.newInstance();
			if (JValue.class.isAssignableFrom(clazz)) {
				((JValue)instance).fill(source);
			} else {
				fill(instance, source);
			}
			return instance;
		} catch (ReflectiveOperationException roe) {
			throw new JDataDefinitionException("Failed to instantiate \"" + clazz.getName() + "\". Default constructor may not be accessible and defined.", roe);
		}
	}
	
	/**
	 * 与えられた JSON 文字列から Java オブジェクトを生成します。
	 * 指定された Class オブジェクトが JValue のサブクラスの場合、
	 * JValue.fill() が呼ばれます。
	 * 一方、JValue のサブクラスでない場合、Jsonizer.fill() が
	 * 使用され、指定された Class オブジェクトに格納できない値を
	 * JsonType 値が持っていた場合、捨てられます。
	 * <pre>
	 * 利用例
	 *   JDataSubClass jd = Jsonizer.fromJson(jsonString, JDataSubClass.class);
	 *
	 * または
	 *
	 *   Pojo pj = Jsonizer.fromJson(jsonString, Pojo.class);
	 * </pre>
	 *
	 * @param	source	パラメータを持つ JsonType 値
	 * @param	clazz	生成するインスタンスの Class オブジェクト
	 * @return	生成された Java オブジェクト
	 */
	public static <T> T fromJson(String source, Class<T> clazz) {
		return fromJson(JsonType.parse(source), clazz);
	}
}
