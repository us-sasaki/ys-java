package abdom.data.json.object;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import abdom.data.json.*;

/**
 * JFloat テスト。
 */
class JFloatTest {
	@Nested
	class コンストラクタ {
		@Nested
		class 値がない場合 {
			@Test void NULL値が生成される() {
				JFloat j = new JFloat();
				assertEquals(JsonType.NULL, j.toJson());
			}
			@Test void floatValueはNan() {
				JFloat j = new JFloat();
				assertEquals(Float.NaN, j.floatValue());
			}
		}
		@Nested
		class float値の場合 {
			@Test void 指定値となる() {
				JFloat j2 = new JFloat(0f);
				assertEquals(0f, j2.floatValue());
				assertEquals("0.0", j2.toString());
			}
		}
		@Nested
		class 文字列値の場合 {
			@Test void 数値と認識できる場合その値となる() {
				JFloat j3 = new JFloat("1");
				assertEquals(1f, j3.floatValue());
				assertEquals(1f, j3.toJson().floatValue());
			}
			@Test void 数値でない文字列はNumberFormatExceptionとなる() {
				assertThrows(NumberFormatException.class,
								() -> new JFloat("x"));
			}
			@Test void 空文字列はTYPE_VOIDとなる() {
				JFloat j5 = new JFloat("");
				assertEquals(JsonType.TYPE_VOID, j5.toJson().getType());
			}
		}
	}
	@Nested
	class fillメソッド {
		@Test void fillできる() {
			JFloat j = new JFloat();
			j.fill("2");
			assertEquals("2.0", j.toString());
		}
		@Test void nullでfillすると初期化される() {
			JFloat j = new JFloat(5);
			j.fill(JsonType.NULL);
			assertEquals("null", j.toString());
		}
	}
	
	@Nested
	class toStringメソッド {
		@Test void 整数でfillしてもdouble表現となる() {
			JFloat j5 = new JFloat();
			j5.fill(new JsonValue(-1));
			assertEquals("-1.0", j5.toString());
		}
	}
	
	@Nested
	class cache挙動 {
		@Test void 整数値ではcacheされない() {
			JsonValue integer = new JsonValue(5);
			assertEquals(JsonType.TYPE_INT, integer.getType());
			
			JFloat j1 = new JFloat();
			j1.fill(integer);
			JsonType cached = j1.toJson();
			
			assertNotSame(integer, cached);
		}
		
		@Test void float値ではcacheされない() {
			JsonValue integer = new JsonValue(5f);
			assertEquals(JsonType.TYPE_DOUBLE, integer.getType());
			
			JFloat j1 = new JFloat();
			j1.fill(integer);
			JsonType cached = j1.toJson();
			
			assertNotSame(integer, cached);
		}
	}
}
