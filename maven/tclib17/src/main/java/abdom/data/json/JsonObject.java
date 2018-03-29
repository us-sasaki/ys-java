package abdom.data.json;

import java.util.Map;
import java.util.TreeMap;

/**
 * Json形式におけるオブジェクトを表します。
 * このクラスのオブジェクトはスレッドセーフではありません。
 */
public class JsonObject extends JsonType {
	/**
	 * オブジェクトの要素を保持します。null 値を含ませてはいけません。
	 * その場合、JsonValue(null) を挿入して下さい。
	 */
	protected Map<String, JsonType> map;
	
/*-------------
 * constructor
 */
	public JsonObject() {
		map = new TreeMap<String, JsonType>();
	}
	
/*------------------
 * instance methods
 */
	/**
	 * boolean 値を取得します。
	 * false となるのは、要素が空の場合のみです。
	 *
	 * @return		このオブジェクトの boolean としての値
	 */
	@Override
	public boolean booleanValue() {
		return (map.size() > 0);
	}

/*
 * add methods
 */
	@Override
	public JsonObject add(String name, Jsonizable obj) {
		if (obj == null) return this;
		return addImpl(name, obj.toJson());
	}
	@Override
	public JsonObject add(String name, String value) {
		if (value == null) return this;
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, boolean value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, byte value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, char value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, int value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, long value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, float value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, double value) {
		return addImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject add(String name, Jsonizable[] array) {
		return addImpl(name, new JsonArray((Object[])array));
	}
	
	private JsonObject addImpl(String name, Jsonizable j) {
		if (j == null) return this; // 何もしない
		JsonType t = j.toJson();
		int index = name.indexOf('.');
		if (index > -1) {
			// dot がある場合、recursive
			// 一段深いオブジェクト
			String next = name.substring(0, index);
			try {
				JsonObject jo = (JsonObject)map.get(next);
				if (jo == null) {
					jo = new JsonObject();
					map.put(next, jo);
				}
				jo.addImpl(name.substring(index + 1), j);
				return this;
			} catch (ClassCastException cce) {
				throw new IllegalArgumentException(toString() + "のキー"+ name + "中" + next +"には値追加できないオブジェクトがすでに設定されています");
			}
		}
		
		// dot がない場合
		if (map.containsKey(name)) {
			// 同一 name のエントリがすでにあった場合、value を JsonArray 化する
			JsonType v = map.get(name);
			if (v instanceof JsonArray) {
				// すでに JsonArray になっていた場合、要素追加
				JsonArray src = (JsonArray)v;
				src.array.add(t);
				return this;
			} else {
				// JsonArray になっていない場合、JsonArray化する
				JsonArray newArray = new JsonArray();
				newArray.array.add(v);
				newArray.array.add(t);
				
				map.put(name, newArray);
				return this;
			}
		} else {
			// 今回初めての追加(通例この場合となる)
			map.put(name, t);
			return this;
		}
	}
	
/*
 * put methods
 */
	@Override
	public JsonObject put(String name, Jsonizable obj) {
		return putImpl(name, obj);
	}
	@Override
	public JsonObject put(String name, String value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, boolean value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, byte value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, char value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, int value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, long value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, float value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, double value) {
		return putImpl(name, new JsonValue(value));
	}
	@Override
	public JsonObject put(String name, Jsonizable[] array) {
		return putImpl(name, new JsonArray((Object[])array));
	}
	
	private JsonObject putImpl(String name, Jsonizable t) {
		if (t == null) t = new JsonValue( null );
		// 上書き
		int index = name.indexOf('.');
		if (index == -1) {
			map.put(name, t.toJson());
			return this;
		}
		// 一段深いオブジェクト
		String next = name.substring(0, index);
		try {
			JsonObject j = (JsonObject)map.get(next);
			if (j == null) {
				j = new JsonObject();
				map.put(next, j);
			}
			j.putImpl(name.substring(index + 1), t);
			return this;
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException(toString() + "のキー"+ name + "中" + next +"には値追加できないオブジェクトがすでに設定されています");
		}
	}
	
/*-----------
 * overrides
 */
	@Override
	public JsonType get(String key) {
		int index = key.indexOf('.');
		if (index == -1) return map.get(key);
		JsonType jt = map.get(key.substring(0, index));
		if (jt == null) return null;
		return ((JsonObject)jt).get(key.substring(index+1));
	}
	
	@Override
	public JsonType cut(String key) {
		int index = key.indexOf('.');
		if (index == -1) return map.remove(key);
		JsonType jt = map.get(key.substring(0, index));
		if (jt == null) return null;
		return ((JsonObject)jt).cut(key.substring(index+1));
		
	}
	
	@Override
	public java.util.Set<String> keySet() {
		return map.keySet();
	}
	
	@Override
	public int getType() {
		return TYPE_OBJECT;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		boolean first = true;
		for (String name : map.keySet() ) {
			if (!first) sb.append(",");
			else first = false;
			sb.append('\"');
			sb.append(name);
			sb.append("\":");
			JsonType jt = map.get(name);
			sb.append(jt);
		}
		sb.append('}');
		
		return sb.toString();
	}
	
	@Override
	protected String toString(String indent, String indentStep,
						int textwidth, boolean objElement) {
		checkIndentIsWhiteSpace(indent);
		StringBuilder sb = new StringBuilder();
		
		// object で、"name": の後だけインデントをつけないためのフラグ
		if (!objElement) sb.append(indent);
		sb.append('{');
		boolean first = true;
		boolean elm1 = (map.size() == 1); // 1要素のみの場合、簡略表示
		for (String name : map.keySet() ) {
			if (!first) sb.append(',');
			else first = false;
			if (!elm1) {
				sb.append(JsonType.LS);
				sb.append(indent);
				sb.append(indentStep);
			}
			sb.append('\"');
			sb.append(name);
			sb.append("\": ");
			JsonType jt = map.get(name);
			if (jt instanceof JsonValue) sb.append(jt);
			else if (jt instanceof JsonObject) {
				// JsonObject の場合
				if (((JsonObject)jt).map.size() == 0) sb.append("{}");
				else if (textwidth > 0) {
					// JsonObject で、textWidth 指定の範囲で一行化を試みる
					int len = indent.length() + indentStep.length() +
								5 + name.length();
					String tryShort = jt.toString();
					if (len + tryShort.length() <= textwidth) {
						sb.append(tryShort);
					} else {
						sb.append(jt.toString(indent+indentStep, indentStep,
									textwidth, true));
					}
				} else {
					sb.append(jt.toString(indent+indentStep, indentStep,
									textwidth, true));
				}
			} else if (jt.size() == 0) {
				// JsonArray では、要素数が 0 の場合簡略表示
				// 1 以上の場合は textwidth 指定による
				sb.append("[]");
			} else if (textwidth > 0) {
				// JsonArray で、textwidth 指定の範囲で一行化を試みる
				
				// 一行化を試みる(toString() により、文字数カウント)
				int len = indent.length() + indentStep.length()
							+ 5 + name.length();
				String tryShort = jt.toString();
				if (len + tryShort.length() <= textwidth) {
					// textwidth 以内にはまる
					sb.append(tryShort);
				} else {
					sb.append(jt.toString(indent+indentStep, indentStep, textwidth, true));
				}
			} else {
				// textwidth に 0以下を指定すると、一行化を試みない(早い)
				sb.append(jt.toString(indent+indentStep, indentStep, textwidth, true));
			}
		}
		if (!elm1) {
			sb.append(JsonType.LS);
			sb.append(indent);
		}
		sb.append('}');
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object target) {
		if (!(target instanceof JsonObject)) return false;
		JsonObject jo = (JsonObject)target;
		return jo.map.equals(map);
	}
	
	@Override
	public int hashCode() {
		return map.hashCode();
	}
}
