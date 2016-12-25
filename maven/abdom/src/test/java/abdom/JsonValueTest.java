package abdom;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;

/**
 * Unit test for simple App.
 * http://www.mitchy-world.jp/java/test/junit3.htm
 */
public class JsonValueTest extends TestCase{
	public void testBoolean() {
		System.out.println("testBoolean....");
		assertTrue(new JsonValue(true).toString().equals("true"));
		assertTrue(new JsonValue(true).getValue().equals("true"));
		assertTrue(new JsonValue(false).getValue().equals("false"));
		assertTrue(new JsonValue(true).getType() == JsonType.TYPE_BOOLEAN);
	}
	public void testNumber() {
		System.out.println("testNumber....");
		assertTrue(new JsonValue(0).toString().equals("0"));
		assertTrue(new JsonValue((short)1).getValue().equals("1"));
		assertTrue(new JsonValue(-100).getIntValue() == -100);
		assertTrue(new JsonValue(15232).getDoubleValue() == 15232d);
		assertTrue(new JsonValue(25).getType() == JsonType.TYPE_INT);
		assertTrue(new JsonValue(15.3).getType() == JsonType.TYPE_DOUBLE);
	}
	public void testNull() {
		System.out.println("testNull....");
		assertTrue(new JsonValue(null).toString().equals("null"));
		assertTrue(new JsonValue(null).getValue() == null);
		assertTrue(new JsonValue(null).getType() == JsonType.TYPE_VOID);
	}
	public void testString() {
		System.out.println("testString....");
		assertTrue(new JsonValue("hogeo").toString().equals("\"hogeo\""));
		JsonValue str = new JsonValue("escape char \t\n\r\"\\");
		assertTrue(str.toString().equals("\"escape char \\t\\n\\r\\\"\\\\\""));
		assertTrue(str.getValue().equals("escape char \t\n\r\"\\"));
	}
}
