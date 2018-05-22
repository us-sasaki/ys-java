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
		JD1 j1;
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
	}
	
}
			