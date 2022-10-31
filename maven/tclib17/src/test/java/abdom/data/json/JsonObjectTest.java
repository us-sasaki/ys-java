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
	
	public void testHasKey() {
		JsonType jt = new JsonObject().put("key1", 5);
		jt.put("newKey", "String Value");
		jt.put("nullKey", JsonType.NULL);
		jt.put("nKey", "null");
		
		assertTrue(jt.hasKey("key1"));
		assertTrue(jt.hasKey("newKey"));
		assertFalse(jt.hasKey("key"));
		assertTrue(jt.hasKey("nullKey"));
		assertTrue(jt.hasKey("nKey"));
	}
}
