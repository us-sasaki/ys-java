import java.util.*;

public abstract class JsonType {

	public String getValue() {
		return ((JsonValue)this).value; // may throw ClassCastException
	}
	public JsonType get(String key) {
		JsonObject jo = (JsonObject)this; // may throw ClassCastException
		return jo.map.get(key);
	}
	public JsonType get(int index) {
		JsonArray ja = (JsonArray)this; // may throw ClassCastException
		return ja.array[index]; // may throw ArrayIndexOutOfBoundsException
	}
	public int size() {
		JsonArray ja = (JsonArray)this; // may throw ClassCastException
		return ja.array.length;
	}
	
//	public static JsonType parse(String json) {
//	
//	}
	public abstract String toString(String indent);

}
