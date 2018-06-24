package abdom.data.json.object;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abdom.data.json.*;

/**
 * JData テスト。JUnit5
 */
class JData5Test {
	/** テスト用のクラス定義 */
	static class JD1 extends JData {
		public byte a;
		public short b;
		public char c;
		public int d;
		public long e;
		public float f;
		public double g;
		public String h;
		public JsonObject i;
	}
	
	@Nested
	class JData基本機能 {
		JD1 j1, jext;
		@BeforeEach void init() {
			j1 = new JD1();
			j1.a = 1;
			j1.b = 2;
			j1.c = '3';
			j1.d = 4;
			j1.e = 5;
			j1.f = 6;
			j1.g = 7;
			j1.h = "89";
			j1.i = new JsonObject().put("key", "value");
			
			jext = new JD1();
			jext.putExtra("ex1",1);
			jext.putExtra("ex2", "2");
		}
		@Test void toStringでJSON化できる() {
			assertEquals("{\"a\":1,\"b\":2,\"c\":\"3\",\"d\":4,\"e\":5,\"f\":6.0,\"g\":7.0,\"h\":\"89\",\"i\":{\"key\":\"value\"}}", j1.toString());
		}
		
		@Test void toStringでpretty_JSON化できる() {
			String r = System.getProperty("line.separator");
			assertEquals("{"+r+
			"  \"a\": 1,"+r+
			"  \"b\": 2,"+r+
			"  \"c\": \"3\","+r+
			"  \"d\": 4,"+r+
			"  \"e\": 5,"+r+
			"  \"f\": 6.0,"+r+
			"  \"g\": 7.0,"+r+
			"  \"h\": \"89\","+r+
			"  \"i\": {\"key\":\"value\"}"+r+
			"}", j1.toString("  "));
		}
		
		@Test void hasExtrasでextraの有無をチェックできる() {
			JD1 j = new JD1();
			assertFalse(j.hasExtras());
			j.putExtra("extra", "extraValue");
			assertTrue(j.hasExtras());
		}
		
		@Test void getExtraでextraを取得できる() {
			assertEquals(new JsonValue(1), jext.getExtra("ex1"));
			assertEquals(new JsonValue("2"), jext.getExtra("ex2"));
		}
		
		@Test void getExtraKeySetでkeySetを取得できる() {
			java.util.Set<String> s = jext.getExtraKeySet();
			assertTrue(s.contains("ex1"));
			assertTrue(s.contains("ex2"));
		}
		
		@Test void getExtrasでJsonObjectを取得できる() {
			JsonObject jo = jext.getExtras();
			assertEquals("{\"ex1\":1,\"ex2\":\"2\"}", jo.toString());
		}
		
		@Test void getでフィールド値が取得できる() {
			assertEquals(new JsonValue(1), j1.get("a"));
			assertEquals(new JsonValue("89"), j1.get("h"));
		}
		
		@Test void getはextra値でも取得できる() {
			assertEquals(new JsonValue(1), jext.get("ex1"));
			assertEquals(new JsonValue("2"), jext.get("ex2"));
		}
		
		@Test void setでフィールド値が設定できる() {
			JD1 j = new JD1();
			j.set("a",-1);
			assertEquals(-1, j.a);
		}
		
		@Test void setでフィールド値をJsonValueで設定できる() {
			JD1 j = new JD1();
			j.set("b",new JsonValue(5));
			assertEquals(5, j.b);
		}
		
		@Test void setでフィールド値を異なる型のJsonValue値でも設定できる() {
			JD1 j = new JD1();
			j.set("b", new JsonValue("5"));
			assertEquals(5, j.b);
		}
		
		@Test void setでフィールド値を文字列では設定できない() {
			JD1 j = new JD1();
			try {
				j.set("b", new JsonValue("ahi"));
				fail("JData#set でshortフィールドを文字列で設定できてしまった");
			} catch (IllegalFieldTypeException ifte) {
			}
			// assertThrows(IllegalFieldTypeException.class, () -> j.set(...
			// でもよい
		}
		
		@Test void setでフィールド値をオブジェクトで設定できない() {
			JD1 j = new JD1();
			try {
				j.set("b", new JsonObject().put("ahi", 5));
				fail("JData#set でshortフィールドをobjectで設定できてしまった");
			} catch (IllegalFieldTypeException ifte) {
			}
		}
		
		@Test void setでint値を小数値でも設定できる() {
			JD1 j = new JD1();
			j.set("d", new JsonValue(1.5));
			assertEquals(1, j.d);
		}
		
		@Test void fillで設定できる() {
			JD1 j = new JD1();
			j.fill("{\"b\":53}");
			assertEquals(53, j.b);
		}
		
		@Test void fillでprimitive型にnullは設定可能だが値は変わらない() {
			JD1 j = new JD1();
			j.b = 55;
			j.fill("{\"b\":null, \"c\":\"あ\"}");
			assertEquals(55, j.b);
			assertEquals('あ', j.c); // char は文字列で設定できる
		}
		
	}
	
}
			