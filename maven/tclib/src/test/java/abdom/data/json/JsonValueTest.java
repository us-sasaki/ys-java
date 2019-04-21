package abdom.data.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * JsonValue テスト。
 */
class JsonValueTest {
	@Nested
	class type別の基本機能 {
		@Nested
		class nullテスト {
			JsonValue n;
			
			@BeforeEach
			void init() {
				n = new JsonValue(null);
			}
			
			@Test void json値() {
				assertEquals("null", n.toString());
			}
			
			@Test void 型() {
				assertEquals(JsonType.TYPE_VOID, n.getType());
			}
			
			@Test void 値() {
				assertEquals(false, n.booleanValue());
			}
		}
		
		@Nested
		class booleanテスト {
			JsonValue t;
			JsonValue f;
			
			@BeforeEach
			void init() {
				t = new JsonValue(true);
				f = new JsonValue(false);
			}
			
			@Test void json値() {
				assertEquals("true", t.toString());
			}
			
			@Test void 型() {
				assertEquals(JsonType.TYPE_BOOLEAN, t.getType());
			}
			
			@Test void 値() {
				assertEquals("true", t.getValue());
				assertEquals("false",f.getValue());
				assertEquals(true, t.booleanValue());
				assertEquals(false, f.booleanValue());
			}
		}
		
		@Nested
		class numberテスト {
			JsonValue n1;
			JsonValue n2;
			
			@BeforeEach
			void init() {
				n1 = new JsonValue(100);
				n2 = new JsonValue(-0.9);
			}
			
			@Test void json値() {
				assertEquals("100", n1.toString());
				assertEquals("-0.9", n2.toString());
			}
			
			@Test void 型() {
				assertEquals(JsonType.TYPE_INT, n1.getType());
				assertEquals(JsonType.TYPE_DOUBLE, n2.getType());
			}
			
			@Test void 値() {
				assertEquals(100, n1.intValue());
				assertEquals(100L, n1.longValue());
				assertEquals(-0.9, n2.doubleValue());
				assertEquals(true, n1.booleanValue());
				assertEquals(true, n2.booleanValue());
			}
		}
		
		@Nested
		class stringテスト {
			JsonValue s;
			
			@BeforeEach
			void init() {
				s = new JsonValue("文字列");
			}
			
			@Test void json値() {
				assertEquals("\"文字列\"", s.toString());
			}
			
			@Test void 型() {
				assertEquals(JsonType.TYPE_STRING, s.getType());
			}
			
			@Test void 値() {
				assertEquals("文字列", s.getValue());
				assertEquals(true, s.booleanValue());
			}
			
			@Test void コントロールコードエスケープ() {
				JsonValue esc = new JsonValue("\r\n\t\"/\\");
				assertEquals("\"\\r\\n\\t\\\"/\\\\\"", esc.toString());
				assertEquals("\r\n\t\"/\\", esc.getValue());
			}
			
			@Test void 数値化() {
				JsonValue num = new JsonValue("123");
				assertEquals(123, num.intValue());
				assertEquals(123d, num.doubleValue());
			}
		}
	}
}
