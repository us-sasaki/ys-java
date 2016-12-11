package abdom.data.json.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;

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
 * boolean, int, double, String, JValue(,JData), JsonType
 * および、これらの型の配列 、List<JData>
 *
 * </pre>暗黙のフィールドとして、_extra (JsonObject型) を持っており
 * fill() の際に未定義のフィールド値はすべてここに格納されます。
 * また、toJson() では _extra フィールドは存在する(not null)場合のみJSON
 * メンバとして現れます。
 * 子クラスで、JSON形式との相互変換対象外とする変数を定義したい場合、
 * transient 修飾子をつけて下さい。
 *
 * @version	December 10, 2016
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
	private static Set<Class<?>> _fieldChecked;
	static {
		_fieldChecked = new HashSet<Class<?>>();
	}
	
	
/*-------------
 * constructor
 */
	/**
	 * インスタンス化の際に、インスタンス変数が JData カテゴリのものかどうか
	 * をチェックします。
	 */
	protected JData() {
		if (!_fieldChecked.contains(this.getClass())) {
			Field[] fields = this.getClass().getFields();
			
			for (Field f : fields) {
				// static は除外
				if (Modifier.isStatic(f.getModifiers())) continue;
				// transient も除外
				if (Modifier.isTransient(f.getModifiers())) continue;
				
				Class type = f.getType();
				
				if (isJDataCategory(type)) continue;
				throw new IllegalFieldTypeException("Illegal type \"" + type.getName() + "\" has found in field \""+ f.getName()+ "\" of class \"" + getClass().getName() + "\". JData field must consist of boolean, int, double, String, JValue, JsonType, their arrays, and List. To prevent the field from Jsonizing, set transient.");
			}
			_fieldChecked.add(this.getClass());
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 指定された Class オブジェクトが JData カテゴリに含まれるか
	 * チェックします。
	 */
	private boolean isJDataCategory(Class type) {
		// プリミティブ、JsonType, JValue
		if ( Boolean.TYPE.isAssignableFrom(type) ||
			Integer.TYPE.isAssignableFrom(type) ||
			Double.TYPE.isAssignableFrom(type) ||
			String.class.isAssignableFrom(type) ||
			JValue.class.isAssignableFrom(type) ||
			JsonType.class.isAssignableFrom(type) ) return true;
		
		// 配列
		if (boolean[].class.isAssignableFrom(type) ||
			int[].class.isAssignableFrom(type) ||
			double[].class.isAssignableFrom(type) ||
			String[].class.isAssignableFrom(type) ||
			JValue[].class.isAssignableFrom(type) ||
			JsonType[].class.isAssignableFrom(type) ) return true;
		
		// List<JData>
		if (List.class.isAssignableFrom(type)) return true;
		
		return false;
	}
	
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
		return _extra.map.keySet();
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
				throw new IllegalFieldTypeException(ie.toString());
			} catch (IllegalAccessException iae) {
				throw new IllegalFieldTypeException(iae.toString());
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
		JsonObject jobj = (JsonObject)json; // may throw ClassCastException
		
		Field[] fields = this.getClass().getFields();
		Map<String, Field> fmap = new TreeMap<String, Field>();
		for (Field f: fields) {
			// static 変数は除外
			if (Modifier.isStatic(f.getModifiers())) continue;
			// transient 変数も除外
			if (Modifier.isTransient(f.getModifiers())) continue;
			fmap.put(f.getName(), f);
		}
		
		for (String key : jobj.map.keySet()) {
			Field f = fmap.get(key);
			if (f == null) {
				// Field がない場合、_extra に格納
				if (_extra == null) _extra = new JsonObject();
				_extra.put(key, jobj.get(key));
			} else {
				fillMember(f, jobj.get(key));
			}
		}
	}
	
	/**
	 * メンバ変数１つの値を val で設定します。設定対象と JsonType の
	 * 型にミスマッチがあった場合、IllegalFieldTypeException がスロー
	 * されます。
	 *
	 * @param	f	設定対象の Field
	 * @param	val	設定対象に対応する値を持っている JsonType
	 */
	private void fillMember(Field f, JsonType val) {
		// フィールド名を取得する
		String name = f.getName();
		
		// 型を取得する
		Class type = f.getType();
		
		// 型により、適切に変換して格納
		if (Boolean.TYPE.isAssignableFrom(type)) {

			// boolean 型の場合
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + type.getName() + "\" expects type of JsonValue(boolean). Specified value: " + val);
			try {
				switch (val.toString()) {
				case "true":
					f.setBoolean(this, true);
					break;
				case "false":
					f.setBoolean(this, false);
					break;
				default:
					throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + type.getName() + "\" is boolean while json value is " + val);
				}
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException("boolean field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (Integer.TYPE.isAssignableFrom(type)) {
		
			// int 型の場合
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonValue(number/int). Specified value: " + val);
			try {
				f.setInt(this, Integer.parseInt(val.toString()));
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException("int field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (Double.TYPE.isAssignableFrom(type)) {
		
			// double 型の場合
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonValue(number/double). Specified value: " + val);
			try {
				f.setDouble(this, Double.parseDouble(val.toString()));
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException("double field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (String.class.isAssignableFrom(type)) {
			
			// String 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonValue(string). specified value: " + val);
			String str = val.toString(); // "" で囲まれているはず
			if (!str.startsWith("\"") || !str.endsWith("\"") )
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects Json string. specified value: " + str);
			try {
				f.set(this, str.substring(1, str.length() - 1));
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException("String field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (JValue.class.isAssignableFrom(type)) {
		
			// JValue 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonType))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonType. specified value: " + val);
			try {
				Object instance = f.get(this);
				try {
					if (instance == null) instance = type.newInstance();
				} catch (InstantiationException ie) {
					throw new JDataDefinitionException("Failed to instantiate \"" + getClass().getName() + "\". Default constructor of class \"" + type.getName() + "\" may not be accessible or defined.");
				}
				((JValue)instance).fill(val);
				f.set(this, instance);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (JsonType.class.isAssignableFrom(type)) {
			// JsonType 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			try {
				f.set(this, JsonType.parse(val.toString()));
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(type.toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (boolean[].class.isAssignableFrom(type)) {
		
			//
			// 配列の場合
			//
			
			// boolean[] 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonArray(boolean) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			boolean[] instance = new boolean[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException("\"" + name + "\" array-field of class \"" + getClass().getName() + "\" expects type of JsonValue(boolean) instead of type " + type);
				
				switch (j.toString()) {
				case "true":
					instance[i++] = true;
					break;
				case "false":
					instance[i++] = false;
					break;
				default:
					throw new IllegalFieldTypeException("\"" + name + "\" array-field of class \"" + getClass().getName() + "\" is boolean while json value is " + val);
				}
			}
			try {
				f.set(this, instance);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException("boolean[] field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (int[].class.isAssignableFrom(type)) {
			
			// int[] 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonArray(int) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			int[] instance = new int[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException("\"" + name + "\" array-field of class \"" + getClass().getName() + "\" expects type of JsonValue(int). Specified value: " + val);
				instance[i++] = Integer.parseInt(j.toString());
			}
			try {
				f.set(this, instance);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException("int[] field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (double[].class.isAssignableFrom(type)) {
			
			// double[] 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonArray(double). Specified value: " + val);
			JsonArray ja = (JsonArray)val;
			double[] instance = new double[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException("\"" + name + "\" array-field of class \"" + getClass().getName() + "\" expects type of JsonValue(double) instead of type " + type);
				instance[i++] = Double.parseDouble(j.toString());
			}
			try {
				f.set(this, instance);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException("double[] field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (String[].class.isAssignableFrom(type)) {
			
			// String[] 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonArray(String). specified value: " + val);
			JsonArray ja = (JsonArray)val;
			String[] instance = new String[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (j.getType() == JsonType.TYPE_VOID) {
					instance[i++] = null;
					continue;
				}
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException("\"" + name + "\" array-field of class \"" + getClass().getName() + "\" expects type of JsonValue(String). specified value: " + val);
				String str = j.toString();
				if (!str.startsWith("\"") || !str.endsWith("\"") )
					throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects Json string. specified value: " + str);
				instance[i++] = str.substring(1, str.length() - 1);
			}
			try {
				f.set(this, instance);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException("String[] field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (JValue[].class.isAssignableFrom(type)) {
			
			// JValue[] 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonArray. Specified value: " + val);
			JsonArray ja = (JsonArray)val;
			
			// 子クラスで宣言されている型での配列を生成し、とりあえず JValue[]
			// 型で保持する
			Class comptype = type.getComponentType();
			JValue[] instance = (JValue[])Array.newInstance(comptype, ja.size());
			int i = 0;
			try {
				for (JsonType j : ja.array) {
					if (j.getType() == JsonType.TYPE_VOID) {
						instance[i++] = null;
						continue;
					}
					JValue elm = (JValue)comptype.newInstance();
					elm.fill(j);
					instance[i++] = elm;
				}
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(comptype.toString() + "[] field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			} catch (InstantiationException ie) {
				throw new JDataDefinitionException("Failed to instantiate \"" + getClass().getName() + "\". Default constructor of class \"" + type.getName() + "\" may not be accessible or defined.");
			}
			try {
				f.set(this, instance);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(type.toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
			
		} else if (JsonType[].class.isAssignableFrom(type)) {
			
			// JsonType[] 型の場合
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonArray(JsonType). Specified value: " + val);
			JsonArray ja = (JsonArray)val;
			JsonType[] instance = new JsonType[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				// JsonType[] の場合、null が指定された場合、Java Object の
				// null ではなく、JsonValue の null (TYPE_VOID) を入れる
				JsonType elm = JsonType.parse(j.toString()); // deep copy
				instance[i++] = elm;
			}
			try {
				f.set(this, instance);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(type.toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else if (List.class.isAssignableFrom(type)) {
			
			// List 型の場合
			// List<JData> と子クラスで宣言されていても、JData 型を
			// 実行時に受け取ることはできない。(<JData>はコンパイル
			// コンテキストでのみ保持される属性のため)
			// したがって、本実装では　List型は List<JData> とみなす。
			//
			// List.toArray(new String[]{}) など、引数を渡したり、
			// ジェネリック型がバインドされるクラス内では、new E[]{} の
			// class.getComponentClass を呼ぶことで得ることは可能。
			if (val.getType() == JsonType.TYPE_VOID) {
				try {
					f.set(this, null);
					return;
				} catch (IllegalAccessException iae) {
					throw new JDataDefinitionException(type.getName().toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
				}
			}
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException("\"" + name + "\" field of class \"" + getClass().getName() + "\" expects type of JsonArray(JsonObject). Specified value: " + val);
			JsonArray ja = (JsonArray)val;
			List<JData> instance = new ArrayList<JData>();
			try {
				for (JsonType j : ja.array) {
					if (j.getType() == JsonType.TYPE_VOID) {
						instance.add(null);
						continue;
					}
					JData elm = (JData)type.getComponentType().newInstance();
					elm.fill(j);
					instance.add(elm);
				}
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(type.getComponentType().toString() + "[] field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			} catch (InstantiationException ie) {
				throw new JDataDefinitionException("Failed to instantiate \"" + getClass().getName() + "\". Default constructor of class \"" + type.getName() + "\" may not be accessible or defined.");
			}
			try {
				f.set(this, instance);
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(type.toString() + " field \"" + name + "\" of class \"" + getClass().getName() + "\" is not accessible");
			}
		} else {
			// コンストラクタでチェックしているため、ここには来ないはず
			new IllegalFieldTypeException("\"" + name + "\" field is not an element of JData category :" + type);
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
		try {
			return toJsonImpl();
		} catch (IllegalAccessException iae) {
			throw new IllegalFieldTypeException("..");
		}
	}
	
	/**
	 * toJson の実装本体です。
	 */
	private JsonObject toJsonImpl() throws IllegalAccessException {
		JsonObject result = new JsonObject();
		
		Field[] fields = this.getClass().getFields();
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers())) continue;
			if (Modifier.isTransient(f.getModifiers())) continue;
			if (f.get(this) == null) continue;
			String name = f.getName();
			Class type = f.getType();
			
			try {
				// プリミティブ型
				if (Boolean.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getBoolean(this)));
				} else if (Integer.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getInt(this)));
				} else if (Double.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getDouble(this)));
					
				// String, JValue, JsonType
				} else if (String.class.isAssignableFrom(type)) {
					result.put(name, new JsonValue((String)f.get(this)));
				} else if (JValue.class.isAssignableFrom(type)) {
					result.put(name, ((JValue)f.get(this)).toJson());
				} else if (JsonType.class.isAssignableFrom(type)) {
					result.put(name, (JsonType)f.get(this));
					
				// 配列型
				} else if (boolean[].class.isAssignableFrom(type)) {
					boolean[] v = (boolean[])f.get(this);
					result.put(name, new JsonArray(v));
				} else if (int[].class.isAssignableFrom(type)) {
					int[] v = (int[])f.get(this);
					result.put(name, new JsonArray(v));
				} else if (double[].class.isAssignableFrom(type)) {
					double[] v = (double[])f.get(this);
					result.put(name, new JsonArray(v));
				} else if (String[].class.isAssignableFrom(type)) {
					String[] v = (String[])f.get(this);
					result.put(name, new JsonArray(v));
				} else if (JValue[].class.isAssignableFrom(type)) {
					JValue[] v = (JValue[])f.get(this);
					JsonArray ja = new JsonArray();
					for (JValue jd : v) ja.push(jd.toJson());
					result.put(name, ja);
				} else if (JsonType[].class.isAssignableFrom(type)) {
					JsonType[] v = (JsonType[])f.get(this);
					result.put(name, new JsonArray(v));
					
				// List (List<JData> とみなす)
				} else if (List.class.isAssignableFrom(type)) {
					@SuppressWarnings("unchecked")
					List<JData> v = (List<JData>)f.get(this);
					JsonArray ja = new JsonArray();
					for (JData jd : v) ja.push(jd.toJson());
					result.put(name, ja);
				} else {
					throw new IllegalFieldTypeException("Unexpected type \"" + type.getName() + "\"is found.");
				}
			} catch (NullPointerException npe) {
				result.put(name, (String)null);
			}
		}
		// _extra を追加
		if (_extra != null) {
			for (String key : _extra.map.keySet()) {
				result.put(key, _extra.get(key));
			}
		}
		return result;
	}
	
	public String toString(String indent) {
		return toJson().toString(indent);
	}
	
	public String toString(String indent, int textwidth) {
		return toJson().toString(indent, textwidth);
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return toJson().toString();
	}
	
}
