package abdom.data.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

/**
 * ランダムに生成した大きな JsonType に対し、文字列化/parse
 * しても変化しないことを確認します。
 */
public class JsonRushTest extends TestCase{
	private static final int MAX_DEPTH = 5;
	Random r;
	
	public JsonRushTest(String testName) {
		super(testName);
	}
	protected void setUp() throws Exception {
		super.setUp();
		//System.out.println("setUp()が呼ばれました");
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		//System.out.println("tearDown()が呼ばれました");
	}
    public static Test suite() {
    	//System.out.println("suite() が呼ばれました");
        return new TestSuite( JsonRushTest.class );
    }
	public void test() {
		for (int i = 0; i < 20; i++) {
			r = new Random(i + 12345L);
			JsonType j = createJson(0);
			JsonType c = JsonType.parse(j.toString());
			assertEquals(j, c);
			assertEquals(j.hashCode(), c.hashCode());
			//System.out.println(j.toString().length());
		}
	}
	
	// ランダムな JsonType を生成します。
	// object / array の最大階層は MAX_DEPTH までとします
	private JsonType createJson(int depth) {
		if (depth >= MAX_DEPTH) return createValue();
		double rand = r.nextDouble();
		if (rand < 0.3) return createObject(depth);
		if (rand < 0.5) return createArray(depth);
		return createValue();
	}
	
	// ランダムな JsonValue を生成します。
	private JsonType createValue() {
		double rand = r.nextDouble();
		if (rand < 0.1) return JsonType.NULL;
		if (rand < 0.3) {
			// boolean
			if (r.nextDouble() < 0.5) return JsonType.TRUE;
			return JsonType.FALSE;
		}
		if (rand < 0.4) {
			// double
			return new JsonValue(r.nextDouble());
		}
		if (rand < 0.6) {
			// int
			return new JsonValue(r.nextInt());
		}
		// string
		int len = r.nextInt(1000);
		StringBuilder sb = new StringBuilder();
		String chars = "\r\n\b\f\t 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\"\'\\-()$%&+*;:あいうえおかきくけこさしすせそ阿伊宇絵尾化気苦家子！”αβθ∴＠：。、";
		for (int i = 0; i < len; i++) {
			int c = r.nextInt(chars.length());
			sb.append(chars.charAt(c));
		}
		return new JsonValue(sb.toString());
	}
	
	// ランダムな JsonObject を生成します。
	private JsonType createObject(int depth) {
		int objs = r.nextInt(10);
		JsonType result = new JsonObject();
		for (int i = 0; i < objs; i++) {
			String key = "key" + String.valueOf(r.nextInt(10000000));
			result.put(key, createJson(depth+1));
		}
		return result;
	}
	
	// ランダムな JsonArray を生成します。
	private JsonType createArray(int depth) {
		int objs = r.nextInt(10);
		JsonType result = new JsonArray();
		for (int i = 0; i < objs; i++) {
			result.push(createJson(depth+1));
		}
		return result;
	}
	
}
