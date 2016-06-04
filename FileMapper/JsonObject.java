import java.util.Map;
import java.util.HashMap;

public class JsonObject extends JsonType {
	public Map<String, JsonType> map;
	
	public JsonObject() {
		map = new HashMap<String, JsonType>();
	}
	
	public void add(String name, JsonType obj) {
		map.put(name, obj);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		boolean first = true;
		for (String name : map.keySet() ) {
			if (!first) sb.append(", ");
			else first = false;
			sb.append("\"");
			sb.append(name);
			sb.append("\" : ");
			sb.append(map.get(name));
		}
		sb.append(" }");
		
		return sb.toString();
	}
}
