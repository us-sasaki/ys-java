public class JsonArray extends JsonType {
	public JsonType[] array;
	
	public JsonArray(JsonType[] array) {
		this.array = array;
	}
	
	public void set(JsonType[] array) {
		this.array = array;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("[ ");
		boolean first = true;
		for (JsonType obj : array) {
			if (!first) sb.append(", ");
			else first = false;
			sb.append(obj);
		}
		sb.append(" ]");
		
		return sb.toString();
	}
}
