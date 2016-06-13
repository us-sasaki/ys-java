import java.util.Map;
import java.util.HashMap;

public class JsonObject extends JsonType {
	public Map<String, JsonType> map;
	
/*-------------
 * constructor
 */
	public JsonObject() {
		map = new HashMap<String, JsonType>();
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
			// ���� name �̃G���g�������łɂ������ꍇ�Avalue �� JsonArray ������
			JsonType v = map.get(name);
			if (v instanceof JsonArray) {
				// ���ł� JsonArray �ɂȂ��Ă����ꍇ�A�v�f�ǉ�
				JsonArray src = (JsonArray)v;
				JsonType[] newArray = new JsonType[src.array.length + 1];
				System.arraycopy(src.array, 0, newArray, 0, src.array.length);
				newArray[src.array.length] = t;
				
				map.put(name, new JsonArray(newArray));
				return this;
			} else {
				// JsonArray �ɂȂ��Ă��Ȃ��ꍇ�AJsonArray������
				JsonType[] newArray = new JsonType[2];
				newArray[0] = v;
				newArray[1] = t;
				
				map.put(name, new JsonArray(newArray));
				return this;
			}
		} else {
			// ���񏉂߂Ă̒ǉ�(�ʗႱ�̏ꍇ�ƂȂ�)
			map.put(name, t);
			return this;
		}
	}
	
/*-----------
 * overrides
 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		boolean first = true;
		for (String name : map.keySet() ) {
			sb.append("\n");
			if (!first) sb.append(", ");
			else first = false;
			sb.append("\"");
			sb.append(name);
			sb.append("\" : ");
			sb.append(map.get(name));
		}
		sb.append("\n");
		sb.append(" }");
		
		return sb.toString();
	}
}
