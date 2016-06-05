import java.util.*;

public class JsonTest {
	public static void main(String[] args) {
		JsonType j = new JsonValue("hoehoe");
		System.out.println(j);
		
		JsonObject jo = new JsonObject();
		jo.add("name", j);
		System.out.println(jo);
		
		JsonValue[] a = new JsonValue[] { new JsonValue("1"), new JsonValue("2") };
		JsonArray ja = new JsonArray(a);
		System.out.println(ja);
		
		jo.add("array", ja);
		System.out.println(jo);
		
		JsonObject jo2 = new JsonObject();
		jo2.add("name2", new JsonValue("value2"));
		jo2.add("object", jo);
		jo2.add("int", 4);
		jo2.add("char", 'a');
		jo2.add("float", 0.5f);
		jo2.add("double", Math.PI);
		System.out.println(jo2);
	}
}
