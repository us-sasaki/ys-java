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
			this.array[i] = new JsonValue(String.valueOf(array[i]));
		}
	}
	public void set(char[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(String.valueOf(array[i]));
		}
	}
	public void set(int[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(String.valueOf(array[i]));
		}
	}
	public void set(long[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(String.valueOf(array[i]));
		}
	}
	public void set(float[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(String.valueOf(array[i]));
		}
	}
	public void set(double[] array) {
		this.array = new JsonValue[array.length];
		for (int i = 0; i < array.length; i++) {
			this.array[i] = new JsonValue(String.valueOf(array[i]));
		}
	}
	
/*-----------
 * overrides
 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("[ ");
		boolean first = true;
		for (JsonType obj : array) {
			sb.append("\n");
			if (!first) sb.append(", ");
			else first = false;
			sb.append(obj);
		}
		sb.append("\n ]");
		
		return sb.toString();
	}
}
