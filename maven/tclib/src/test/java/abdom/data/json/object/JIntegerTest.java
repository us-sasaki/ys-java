package abdom.data.json.object;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abdom.data.json.*;

/**
 * JInteger テスト。JUnit5
 */
class JIntegerTest {
	@Nested
	class コンストラクタ {
		@Nested
		class 値がない場合 {
			@Test void NULL値が生成される() {
				JInteger j = new JInteger();
				assertEquals(JsonType.NULL, j.toJson());
			}
			@Test void intValueはMIN_VALUE() {
				JInteger j = new JInteger();
				assertEquals(Integer.MIN_VALUE, j.intValue());
			}
		}
		@Nested
		class int値の場合 {
			@Test void 指定値となる() {
				JInteger j2 = new JInteger(0);
				assertEquals(0, j2.intValue());
				assertEquals("0", j2.toString());
			}
		}
		@Nested
		class 文字列値の場合 {
			@Test void 数値と認識できる場合その値となる() {
				JInteger j3 = new JInteger("1");
				assertEquals(1, j3.intValue());
				assertEquals("1", j3.toString());
			}
			@Test void 数値でない文字列はNumberFormatExceptionとなる() {
				assertThrows(NumberFormatException.class,
								() -> new JInteger("x"));
			}
			@Test void 空文字列はTYPE_VOIDとなる() {
				JInteger j5 = new JInteger("");
				assertEquals(JsonType.TYPE_VOID, j5.toJson().getType());
			}
		}
	}
	@Nested
	class fillメソッド {
		@Test void fillできる() {
			JInteger j = new JInteger();
			j.fill("2");
			assertEquals("2", j.toString());
		}
		@Test void nullでfillすると初期化される() {
			JInteger j = new JInteger(5);
			j.fill(JsonType.NULL);
			assertEquals("null", j.toString());
		}
	}
	
	@Nested
	class toStringメソッド {
		@Test void 整数でfillできる() {
			JInteger j5 = new JInteger();
			j5.fill(new JsonValue(-1));
			assertEquals("-1", j5.toString());
		}
	}
	
	@Nested
	class cache挙動 {
		@Test void double値ではcacheされない() {
			JsonValue d = new JsonValue(5d);
			assertEquals(JsonType.TYPE_DOUBLE, d.getType());
			
			JInteger j1 = new JInteger();
			j1.fill(d);
			JsonType cached = j1.toJson();
			
			assertNotSame(d, cached);
		}
		
		@Test void int値ではcacheされる() {
			JsonValue integer = new JsonValue(5);
			assertEquals(JsonType.TYPE_INT, integer.getType());
			
			JInteger j1 = new JInteger();
			j1.fill(integer);
			JsonType cached = j1.toJson();
			
			assertSame(integer, cached);
		}
		
		@Test void long値ではcacheされない() {
			JsonValue integer = new JsonValue(Long.MAX_VALUE);
			assertEquals(JsonType.TYPE_INT, integer.getType());
			
			JInteger j1 = new JInteger();
			j1.fill(integer);
			JsonType cached = j1.toJson();
			
			assertNotSame(integer, cached);
		}
	}
	
}
			