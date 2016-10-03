package abdom.data.json;

import java.util.Map;
import java.util.TreeMap;

/**
 * Json形式におけるオブジェクトを表します。
 */
public class JsonObject extends JsonType {
	public Map<String, JsonType> map;
	
/*-------------
 * constructor
 */
	public JsonObject() {
		map = new TreeMap<String, JsonType>();
	}
	
/*------------------
 * instance methods
 */
	public JsonObject add(String name, JsonType obj) {
		return put(name, obj);
	}
	public JsonObject add(String name, String value) {
		return put(name, new JsonValue(value));
	}
	public JsonObject add(String name, byte value) {
		return put(name, new JsonValue(value));
	}
	public JsonObject add(String name, char value) {
		return put(name, new JsonValue(value));
	}
	public JsonObject add(String name, int value) {
		return put(name, new JsonValue(value));
	}
	public JsonObject add(String name, long value) {
		return put(name, new JsonValue(value));
	}
	public JsonObject add(String name, float value) {
		return put(name, new JsonValue(value));
	}
	public JsonObject add(String name, double value) {
		return put(name, new JsonValue(value));
	}
	public JsonObject add(String name, JsonType[] array) {
		return put(name, new JsonArray(array));
	}
	
	private JsonObject put(String name, JsonType t) {
		if (map.containsKey(name)) {
			// 同一 name のエントリがすでにあった場合、value を JsonArray 化する
			JsonType v = map.get(name);
			if (v instanceof JsonArray) {
				// すでに JsonArray になっていた場合、要素追加
				// 毎回heapを新規確保する実装で遅い
				JsonArray src = (JsonArray)v;
				JsonType[] newArray = new JsonType[src.array.length + 1];
				System.arraycopy(src.array, 0, newArray, 0, src.array.length);
				newArray[src.array.length] = t;
				
				map.put(name, new JsonArray(newArray));
				return this;
			} else {
				// JsonArray になっていない場合、JsonArray化する
				JsonType[] newArray = new JsonType[2];
				newArray[0] = v;
				newArray[1] = t;
				
				map.put(name, new JsonArray(newArray));
				return this;
			}
		} else {
			// 今回初めての追加(通例この場合となる)
			map.put(name, t);
			return this;
		}
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return toString("");
	}
	@Override
	public String toString(String indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent);
		sb.append("{");
		boolean first = true;
		for (String name : map.keySet() ) {
			if (!first) sb.append(",");
			else first = false;
			sb.append("\n");
			sb.append(indent);
			sb.append("  \"");
			sb.append(name);
			sb.append("\":");
			JsonType jt = map.get(name);
			if (jt instanceof JsonValue) sb.append(jt);
			else {
				sb.append("\n");
				sb.append(jt.toString("  "+indent));
			}
		}
		sb.append("\n");
		sb.append(indent);
		sb.append("}");
		
		return sb.toString();
	}
}
