package abdom.data.json.object;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abdom.data.json.*;

/**
 * JDouble テスト。
 */
class JDoubleTest {
	@Nested
	class コンストラクタ {
		@Nested
		class 値がない場合 {
			@Test void NULL値が生成される() {
				JDouble j = new JDouble();
				assertEquals(JsonType.NULL, j.toJson());
			}
			@Test void doubleValueはNan() {
				JDouble j = new JDouble();
				assertEquals(Double.NaN, j.doubleValue());
			}
		}
		@Nested
		class double値の場合 {
			@Test void 指定値となる() {
				JDouble j2 = new JDouble(0d);
				assertEquals(0d, j2.doubleValue());
				assertEquals("0.0", j2.toString());
			}
		}
		@Nested
		class 文字列値の場合 {
			@Test void 数値と認識できる場合その値となる() {
				JDouble j3 = new JDouble("1");
				assertEquals(1d, j3.doubleValue());
				assertEquals(1d, j3.toJson().doubleValue());
			}
			@Test void 数値でない文字列はNumberFormatExceptionとなる() {
				assertThrows(NumberFormatException.class,
								() -> new JDouble("x"));
			}
			@Test void 空文字列はTYPE_VOIDとなる() {
				JDouble j5 = new JDouble("");
				assertEquals(JsonType.TYPE_VOID, j5.toJson().getType());
			}
		}
	}
	@Nested
	class fillメソッド {
		@Test void fillできる() {
			JDouble j = new JDouble();
			j.fill("2");
			assertEquals("2.0", j.toString());
		}
		@Test void nullでfillすると初期化される() {
			JDouble j = new JDouble(5);
			j.fill(JsonType.NULL);
			assertEquals("null", j.toString());
		}
	}
	
	@Nested
	class toStringメソッド {
		@Test void 整数でfillしてもdouble表現となる() {
			JDouble j5 = new JDouble();
			j5.fill(new JsonValue(-1));
			assertEquals("-1.0", j5.toString());
		}
	}
	
	@Nested
	class cache挙動 {
		@Test void 整数値ではcacheされない() {
			JsonValue integer = new JsonValue(5);
			assertEquals(JsonType.TYPE_INT, integer.getType());
			
			JDouble j1 = new JDouble();
			j1.fill(integer);
			JsonType cached = j1.toJson();
			
			assertNotSame(integer, cached);
		}
		
		@Test void double値ではcacheされる() {
			JsonValue integer = new JsonValue(5d);
			assertEquals(JsonType.TYPE_DOUBLE, integer.getType());
			
			JDouble j1 = new JDouble();
			j1.fill(integer);
			JsonType cached = j1.toJson();
			
			assertSame(integer, cached);
		}
	}
}
