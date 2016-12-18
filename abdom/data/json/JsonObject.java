package abdom.data.json;

import java.util.Map;
import java.util.TreeMap;

/**
 * Json形式におけるオブジェクトを表します。
 * このクラスのオブジェクトはスレッドセーフではありません。
 */
public class JsonObject extends JsonType {
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

/*
 * add methods
 */
	@Override
	public JsonObject add(String name, JsonType obj) {
		return addImpl(name, obj);
	}
	@Override
	public JsonObject add(String name, String value) {
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
	public JsonObject add(String name, JsonType[] array) {
		return addImpl(name, new JsonArray((Object[])array));
	}
	
	private JsonObject addImpl(String name, JsonType t) {
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
	public JsonObject put(String name, JsonType obj) {
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
	public JsonObject put(String name, JsonType[] array) {
		return putImpl(name, new JsonArray((Object[])array));
	}
	
	private JsonObject putImpl(String name, JsonType t) {
		// 上書き
		map.put(name, t);
		return this;
	}
	
/*-----------
 * overrides
 */
	@Override
	public JsonType get(String key) {
		return map.get(key);
	}
	
	@Override
	public JsonType cut(String key) {
		return map.remove(key);
	}
	
	@Override
	public java.util.Set<String> keySet() {
		return map.keySet();
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (String name : map.keySet() ) {
			if (!first) sb.append(",");
			else first = false;
			sb.append("\"");
			sb.append(name);
			sb.append("\":");
			JsonType jt = map.get(name);
			sb.append(jt);
		}
		sb.append("}");
		
		return sb.toString();
	}
	
	@Override
	protected String toString(String indent, String indentStep,
						int textwidth, boolean objElement) {
		StringBuilder sb = new StringBuilder();
		
		// object で、"name": の後だけインデントをつけないためのフラグ
		if (!objElement) sb.append(indent);
		sb.append('{');
		boolean first = true;
		for (String name : map.keySet() ) {
			if (!first) sb.append(',');
			else first = false;
			sb.append(JsonType.LS);
			sb.append(indent);
			sb.append(indentStep);
			sb.append("\"");
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
			} else if (((JsonArray)jt).array.size() == 0) {
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
		sb.append(JsonType.LS);
		sb.append(indent);
		sb.append('}');
		
		return sb.toString();
	}
}
