package abdom.data.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * 練習で作った JsonValue テスト。
 * Unit test for simple App.
 * http://www.mitchy-world.jp/java/test/junit3.htm
 */
public class JsonTypeTest extends TestCase{
	public void testArrayAndObject() {
		//System.out.println("testBoolean....");
		assertEquals(new JsonArray().getType(), JsonType.TYPE_ARRAY);
		assertEquals(new JsonObject().getType(), JsonType.TYPE_OBJECT);
	}
}
