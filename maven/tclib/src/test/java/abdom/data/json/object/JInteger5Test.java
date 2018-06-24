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
class JInteger5Test {
	
	@Nested
	class JInteger値のないJInteger {
		JInteger j = new JInteger();
		
		@Test void JSONとしてNULL() {
			assertEquals(JsonType.NULL, j.toJson());
			assertEquals("null", j.toString());
		}
		
		@Test void intValueはMIN_VALUE() {
			assertEquals(Integer.MIN_VALUE, j.intValue());
		}
	}
	
	@Nested
	class JInteger基本機能 {
		@Test void toJsonではnumber型() {
			JInteger j = new JInteger(5);
			assertEquals(JsonType.TYPE_INT, j.toJson().getType());
		}
		
		@Test void fillできる() {
			JInteger j = new JInteger(5);
			j.fill("8");
			assertEquals(8, j.intValue());
		}
		
		@Test void nullでfillすると初期化される() {
			JInteger j = new JInteger(5);
			j.fill(JsonType.NULL);
			assertEquals("null", j.toString());
			
		}
	}
}
			