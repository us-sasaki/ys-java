package abdom.data.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;

/**
 * 練習で作った JsonValue テスト。
 */
class JsonTypeTest {
	
	@Nested
	class JSON文字列をparseできる {
		@Test
		void 数値parseでJsonValueのintとなる() {
			JsonType jnumber = JsonType.parse("123");
			assertEquals(JsonType.TYPE_INT, jnumber.getType());
			assertEquals(123, jnumber.intValue());
			assertEquals("123", jnumber.toString());
		}
		
		@Test
		void 文字parseでJsonValueのstringとなる() {
			JsonType jstring = JsonType.parse("\"123\"");
			assertEquals(JsonType.TYPE_STRING, jstring.getType());
			assertEquals("123", jstring.getValue());
			assertEquals("\"123\"", jstring.toString());
		}
		
		@Test
		void null_parseでJsonValueのnullとなる() {
			JsonType jnull = JsonType.parse("null");
			assertEquals(JsonType.TYPE_VOID, jnull.getType());
			assertEquals(JsonType.NULL, jnull);
			assertEquals("null", jnull.toString());
		}
		
		@Test
		void true_parseでJsonValueのtrueとなる() {
			JsonType jtrue = JsonType.parse("true");
			assertEquals(JsonType.TYPE_BOOLEAN, jtrue.getType());
			assertEquals(JsonType.TRUE, jtrue);
			assertEquals("true", jtrue.toString());
		}
		
		@Test
		void array_parseでJsonArrayとなる() {
			JsonType ja = JsonType.parse("[1,false,\"3\"]");
			assertEquals(JsonType.TYPE_ARRAY, ja.getType());
			assertEquals(JsonType.FALSE, ja.get(1));
			assertEquals(3, ja.size());
		}
		
		@Test
		void object_parseでJsonObjectとなる() {
			JsonType jo = JsonType.parse("{\"key\": \"string\"}");
			assertEquals(JsonType.TYPE_OBJECT, jo.getType());
			assertEquals("string", jo.get("key").getValue());
		}
	}
	
	@Nested
	class JSON文字列は数値の形式の場合intValueなどが使える {
		JsonType jstring;
		
		@BeforeEach
		void init() {
			jstring = JsonType.parse("\"123\"");
		}
		
		@Test
		void intValueが使える() {
			assertEquals(123, jstring.intValue());
		}
		
		@Test
		void floatValueが使える() {
			assertEquals(123f, jstring.floatValue());
		}
		
		@Test
		void doubleValueが使える() {
			assertEquals(123d, jstring.doubleValue());
		}
	}
	
	@Nested
	class Readerからparseできる {
		@Test
		void readerからparseできる() throws Exception {
			String json = "{\"key\":125}";
			ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes());
			Reader r = new InputStreamReader(bais);
			JsonType j = JsonType.parse(r);
			assertEquals(json, j.toString());
		}
	}
	
}
