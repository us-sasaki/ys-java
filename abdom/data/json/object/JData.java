package abdom.data.json.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;

/**
 * Json オブジェクトを Java オブジェクトによって模倣します。
 * このクラスを継承することで、Java オブジェクトと JSON 形式の相互変換が
 * 容易になります。
 * メンバ変数として次のフィールド(JDataカテゴリ)が指定できます。<pre>
 *
 * boolean, int, double, String, JData, JsonType
 * および、これらの型の配列 、List<JData>
 *
 * </pre>暗黙のフィールドとして、_fragment (JsonType型, JsonObject または
 * JsonArray) が存在し、fill() の際に未定義のフィールド値はすべてここに
 * 格納されます。
 * また、toJson() では _fragment フィールドは存在する(not null)場合のみJSON
 * メンバとして現れます。
 * 子クラスで、JSON形式との相互変換対象外の変数を定義したい場合、
 * transient 修飾子をつけて下さい。
 *
 * @version	November 12, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JData {

	/** fill できなかった値を格納する予約領域 */
	public JsonObject _fragment;
	
	/**
	 * 子クラスのコンストラクタで super() を呼び忘れたときに例外を
	 * 発生させるためのフラグ。
	 */
	public transient boolean fieldChecked = false;
	
/*-------------
 * constructor
 */
	/**
	 * このコンストラクタは、子クラスのコンストラクタで必ず呼んでください。
	 * 呼ばない場合、fill, toJson メソッドで IllegalStateException が
	 * スローされます。
	 * インスタンス化の際に、インスタンス変数が JData カテゴリのものかどうか
	 * をチェックします。
	 */
	protected JData() {
		Field[] fields = this.getClass().getFields();
		
		for (Field f : fields) {
			// static は除外
			if (Modifier.isStatic(f.getModifiers())) continue;
			// transient も除外
			if (Modifier.isTransient(f.getModifiers())) continue;
			
			Class type = f.getType();
			
			if (isJDataCategory(type)) continue;
			throw new IllegalFieldTypeException("Illegal type " + type + " has found. JData field must consist of boolean, int, double, String, JData, JsonType and their arrays.");
		}
		fieldChecked = true;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 指定された Class オブジェクトが JData カテゴリに含まれるか
	 * チェックします。
	 */
	private boolean isJDataCategory(Class type) {
		// プリミティブ、JData, JsonType
		if ( Boolean.TYPE.isAssignableFrom(type) ||
			Integer.TYPE.isAssignableFrom(type) ||
			Double.TYPE.isAssignableFrom(type) ||
			String.class.isAssignableFrom(type) ||
			JData.class.isAssignableFrom(type) ||
			JsonType.class.isAssignableFrom(type) ) return true;
		
		// 配列
		if (boolean[].class.isAssignableFrom(type) ||
			int[].class.isAssignableFrom(type) ||
			double[].class.isAssignableFrom(type) ||
			String[].class.isAssignableFrom(type) ||
			JData[].class.isAssignableFrom(type) ||
			JsonType[].class.isAssignableFrom(type) ) return true;
		
		// List<JData>
		if (List.class.isAssignableFrom(type)) return true;
		
		return false;
	}
	
	/**
	 * 指定された JsonObject の内容をこのオブジェクトに設定します。
	 * 引数の型は、利便性のため JsonType としていますが、JsonObject
	 * 以外を指定すると、ClassCastException がスローされます。
	 * このメソッドは値を追加し、既存値は上書きされなければ保存される
	 * ことに注意してください。_fragment も同様です。
	 *
	 * @param	json	このオブジェクトに値を与える JsonType
	 */
	public void fill(JsonType json) {
		if (!fieldChecked)
			throw new IllegalStateException("It is inevitable to invoke the constructor of super class(JData).");
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
				// Field がない場合、_fragment に格納
				if (_fragment == null) _fragment = new JsonObject();
				_fragment.put(key, jobj.get(key));
			} else {
				try {
					fillMember(f, jobj.get(key));
				} catch (InstantiationException ie) {
					throw new IllegalFieldTypeException(ie.toString());
				} catch (IllegalAccessException iae) {
					throw new IllegalFieldTypeException(iae.toString());
				}
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
	private void fillMember(Field f, JsonType val) throws InstantiationException, IllegalAccessException {
		// フィールド名を取得する
		String name = f.getName();
		
		// 型を取得する
		Class type = f.getType();
		
		// 型により、適切に変換して格納
		if (Boolean.TYPE.isAssignableFrom(type)) {

			// boolean 型の場合
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonValue(boolean) instead of type " + type);
			switch (val.toString()) {
			case "true":
				f.setBoolean(this, true);
				break;
			case "false":
				f.setBoolean(this, false);
				break;
			default:
				throw new IllegalFieldTypeException(name + " field is boolean while json value is " + val);
			}
		} else if (Integer.TYPE.isAssignableFrom(type)) {
		
			// int 型の場合
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonValue(number/int) instead of type " + type);
			f.setInt(this, Integer.parseInt(val.toString()));
		} else if (Double.TYPE.isAssignableFrom(type)) {
		
			// double 型の場合
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonValue(number/double) instead of type " + type);
			f.setDouble(this, Double.parseDouble(val.toString()));
			
		} else if (String.class.isAssignableFrom(type)) {
			
			// String 型の場合
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonValue(string) instead of type " + type);
			String str = val.toString(); // "" で囲まれているはず
			if (!str.startsWith("\"") || !str.endsWith("\"") )
				throw new IllegalFieldTypeException(name + " field is expected as Json string. The value: " + str);
			f.set(this, str.substring(1, str.length() - 1));
		} else if (JData.class.isAssignableFrom(type)) {
		
			// JData 型の場合
			if (!(val instanceof JsonObject))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonObject instead of type " + type);
			
			Object instance = f.get(this);
			if (instance == null) instance = type.newInstance();
			((JData)instance).fill(val);
			f.set(this, instance);
		} else if (JsonType.class.isAssignableFrom(type)) {
		
			// JsonType 型の場合
System.out.println("type = "+type+" f = " + f+" val = "+val);
			f.set(this, JsonType.parse(val.toString()));
		} else if (boolean[].class.isAssignableFrom(type)) {
		
			//
			// 配列の場合
			//
			
			// boolean[] 型の場合
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(boolean) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			boolean[] instance = new boolean[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonValue(boolean) instead of type " + type);
				
				switch (j.toString()) {
				case "true":
					instance[i++] = true;
					break;
				case "false":
					instance[i++] = false;
					break;
				default:
					throw new IllegalFieldTypeException(name + " array-field is boolean while json value is " + val);
				}
			}
			f.set(this, instance);
		} else if (int[].class.isAssignableFrom(type)) {
			
			// int[] 型の場合
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(int) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			int[] instance = new int[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonValue(int) instead of type " + type);
				instance[i++] = Integer.parseInt(j.toString());
			}
			f.set(this, instance);
		} else if (double[].class.isAssignableFrom(type)) {
			
			// double[] 型の場合
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(double) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			double[] instance = new double[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonValue(double) instead of type " + type);
				instance[i++] = Double.parseDouble(j.toString());
			}
			f.set(this, instance);
		} else if (String[].class.isAssignableFrom(type)) {
			
			// String[] 型の場合
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(String) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			String[] instance = new String[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonValue(String) instead of type " + type);
				String str = j.toString();
				if (!str.startsWith("\"") || !str.endsWith("\"") )
					throw new IllegalFieldTypeException(name + " field is expected as Json string. The value: " + str);
				instance[i++] = str.substring(1, str.length() - 1);
			}
			f.set(this, instance);
		} else if (JData[].class.isAssignableFrom(type)) {
			
			// JData[] 型の場合
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(JsonObject) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			
			// 子クラスで宣言されている型での配列を生成し、とりあえず JData[]
			// 型で保持する
			Class comptype = type.getComponentType();
			JData[] instance = (JData[])Array.newInstance(comptype, ja.size());
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonObject))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonObject[]) instead of type " + type);
				JData elm = (JData)comptype.newInstance();
				elm.fill(j);
				instance[i++] = elm;
			}
			f.set(this, instance);
		} else if (JsonType[].class.isAssignableFrom(type)) {
			
			// JsonType[] 型の場合
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(JsonType) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			JsonType[] instance = new JsonType[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				JsonType elm = JsonType.parse(j.toString()); // deep copy
				instance[i++] = elm;
			}
			f.set(this, instance);
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
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(JsonObject) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			List<JData> instance = new ArrayList<JData>();
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonObject))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonObject[]) instead of type " + type);
				JData elm = (JData)type.getComponentType().newInstance();
				elm.fill(j);
				instance.add(elm);
			}
			f.set(this, instance);
		} else {
			new IllegalFieldTypeException(name + " field is not an element of JData category :" + type);
		}
	}
	
	/**
	 * JSON形式の文字列でフィールドを埋めます。
	 */
	public void fill(String jsonString) {
		fill(JsonType.parse(jsonString));
	}
	
	/**
	 * このオブジェクトを JsonObject に変換します。
	 */
	public JsonObject toJson() {
		if (!fieldChecked)
			throw new IllegalStateException("It is inevitable to invoke the constructor of super class(JData).");
		try {
			return toJsonImpl();
		} catch (IllegalAccessException iae) {
			throw new IllegalFieldTypeException("..");
		}
	}
	private JsonObject toJsonImpl() throws IllegalAccessException {
		JsonObject result = new JsonObject();
		
		Field[] fields = this.getClass().getFields();
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers())) continue;
			if (Modifier.isTransient(f.getModifiers())) continue;
			if (f.get(this) == null) continue;
			String name = f.getName();
			if ("_fragment".equals(name)) continue; // 後で処理
			Class type = f.getType();
			
			try {
				// プリミティブ型
				if (Boolean.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getBoolean(this)));
				} else if (Integer.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getInt(this)));
				} else if (Double.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getDouble(this)));
					
				// String, JData, JsonType
				} else if (String.class.isAssignableFrom(type)) {
					result.put(name, new JsonValue((String)f.get(this)));
				} else if (JData.class.isAssignableFrom(type)) {
					result.put(name, ((JData)f.get(this)).toJson());
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
				} else if (JData[].class.isAssignableFrom(type)) {
					JData[] v = (JData[])f.get(this);
					JsonArray ja = new JsonArray();
					for (JData jd : v) ja.push(jd.toJson());
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
					throw new IllegalFieldTypeException("Unexpected type " + type + "is found.");
				}
			} catch (NullPointerException npe) {
System.out.println(npe);
				result.put(name, (String)null);
			}
		}
		// _fragment を追加
		if (_fragment != null) {
			for (String key : _fragment.map.keySet()) {
				result.put(key, _fragment.get(key));
			}
		}
		return result;
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return toJson().toString();
	}
	
}
