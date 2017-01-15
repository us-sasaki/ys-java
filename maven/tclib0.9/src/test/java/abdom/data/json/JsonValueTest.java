package abdom.data.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * 練習で作った JsonValue テスト。
 * Unit test for simple App.
 * http://www.mitchy-world.jp/java/test/junit3.htm
 */
public class JsonValueTest extends TestCase{
	public void testBoolean() {
		//System.out.println("testBoolean....");
		assertEquals(new JsonValue(true).toString(), "true");
		assertEquals(new JsonValue(true).getValue(), "true");
		assertTrue(new JsonValue(false).getValue().equals("false"));
		assertTrue(new JsonValue(true).getType() == JsonType.TYPE_BOOLEAN);
	}
	public void testNumber() {
		//System.out.println("testNumber....");
		assertTrue(new JsonValue(0).toString().equals("0"));
		assertTrue(new JsonValue((short)1).getValue().equals("1"));
		assertTrue(new JsonValue(-100).intValue() == -100);
		assertEquals(new JsonValue(999999999999L).longValue(), 999999999999L);
		assertTrue(new JsonValue(-53.111f).floatValue() == -53.111f);
		assertTrue(new JsonValue(15232).doubleValue() == 15232d);
		assertTrue(new JsonValue(25).getType() == JsonType.TYPE_INT);
		assertTrue(new JsonValue(15.3).getType() == JsonType.TYPE_DOUBLE);
	}
	public void testNull() {
		//System.out.println("testNull....");
		assertTrue(new JsonValue(null).toString().equals("null"));
		assertTrue(new JsonValue(null).getValue() == null);
		assertTrue(new JsonValue(null).getType() == JsonType.TYPE_VOID);
	}
	public void testString() {
		//System.out.println("testString....");
		assertTrue(new JsonValue("hogeo").toString().equals("\"hogeo\""));
		JsonValue str = new JsonValue("escape char \t\n\r\"\\");
		assertTrue(str.toString().equals("\"escape char \\t\\n\\r\\\"\\\\\""));
		assertTrue(str.getValue().equals("escape char \t\n\r\"\\"));
	}
}
