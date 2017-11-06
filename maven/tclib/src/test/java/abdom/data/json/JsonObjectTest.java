package abdom.data.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JsonObject テスト。
 */
public class JsonObjectTest extends TestCase{
	public void testBoolean() {
		assertEquals(new JsonObject().booleanValue(), false);
		assertEquals(JsonType.o("1", "2").booleanValue(), true);
		
		JsonType jt = JsonType.o("1", "3");
		jt.cut("1");
		assertEquals(jt.booleanValue(), false); // poly-morphism
	}
}
