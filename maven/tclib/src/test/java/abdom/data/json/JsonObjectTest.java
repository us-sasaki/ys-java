package abdom.data.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * JsonObject テスト。
 */
class JsonObjectTest {
	@Nested
	class objectの基本機能 {
		@Test void 空のときbooleanValueはfalse() {
			assertEquals(new JsonObject().booleanValue(), false);
		}
		
		@Test void 空でないときbooleanValueはtrue() {
			assertEquals(JsonType.o("1", "2").booleanValue(), true);
		}
		
		@Test void 当然後から空にしてもfalse() {
			JsonType jt = JsonType.o("1", "3");
			jt.cut("1");
			assertEquals(jt.booleanValue(), false); // poly-morphism
		}
		
		@Test void hasKeyでキーがあるかテストできる() {
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
		
		@Test void 値が複数あるときtoStringは複数行() {
			JsonType jt = JsonType.o("1","1").put("2","2");
			assertEquals("{"+JsonType.LS+"  \"1\": \"1\","+JsonType.LS+"  \"2\": \"2\""+JsonType.LS+"}", jt.toString("  "));
		}
		
		@Test void 値が単一でJsonValueのときtoStringは簡略表示() {
			JsonType jt = JsonType.o("1","1");
			assertEquals("{\"1\": \"1\"}", jt.toString("  "));
			jt = JsonType.o("1",1.2);
			assertEquals("{\"1\": 1.2}", jt.toString("  "));
			jt = JsonType.o("1",true);
			assertEquals("{\"1\": true}", jt.toString("  "));
			jt = JsonType.o("1",(String)null);
			assertEquals("{\"1\": null}", jt.toString("  "));
		}
	}
}
