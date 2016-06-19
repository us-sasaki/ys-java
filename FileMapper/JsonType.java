import java.util.*;

/**
 * Json形式における型一般を表します(composite pat.)
 * 利便性のため、アクセスメソッドを提供します。
 */
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
//	未実装。stack で実装すると良さそう
//	}

	/**
	 * 人が見やすい indent に対応するためのメソッド
	 */
	public abstract String toString(String indent);

}
