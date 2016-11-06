package abdom.data.json;

/**
 * Json形式における配列を表します。
 */
public class JsonArray extends JsonType {
	public JsonType[] array;
	
/*-------------
 * constructor
 */
	public JsonArray(JsonType[] array) {
		this.array = array;
	}
	public JsonArray(byte[] array) {	set(array);	}
	public JsonArray(char[] array) {	set(array);	}
	public JsonArray(int[] array) {	set(array);	}
	public JsonArray(long[] array) {	set(array);	}
	public JsonArray(float[] array) {	set(array);	}
	public JsonArray(double[] array) {	set(array);	}
	public JsonArray(String[] array) {	set(array);	}
	
/*------------------
 * instance methods
 */
	public void set(JsonType[] array) {
		this.array = array;
	}
	public void set(String[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(array[i]);
		}
	}
	public void set(byte[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(array[i]);
		}
	}
	public void set(char[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(array[i]);
		}
	}
	public void set(int[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(array[i]);
		}
	}
	public void set(long[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(array[i]);
		}
	}
	public void set(float[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(array[i]);
		}
	}
	public void set(double[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(array[i]);
		}
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return toString("", false);
	}
	
	@Override
	protected String toString(String indent, boolean objElement) {
		StringBuffer sb = new StringBuffer();
		
		if (!objElement) sb.append(indent);
		sb.append("[");
		boolean first = true;
		for (JsonType obj : array) {
			if (!first) {
				sb.append(",");
				sb.append(JsonType.ls);
			} else {
				sb.append(JsonType.ls);
				first = false;
			}
			sb.append(obj.toString(indent+JsonType.indent, false));
		}
		sb.append(JsonType.ls);
		sb.append(indent);
		sb.append("]");
		
		return sb.toString();
	}
}
