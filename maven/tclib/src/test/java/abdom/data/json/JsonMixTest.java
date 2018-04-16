package abdom.data.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Json 複合テスト。
 */
public class JsonMixTest {

	@Nested class jsonモンキーテスト {
		@Test void junit確認() {
			assertEquals(1, 1);
			assertFalse( false );
			assertNotNull( "hoe" );
			String h = "hoe";
			assertNotSame(h, new String(h.getBytes()));
			assertNull( null );
			assertSame( h, "hoe" ); // !! 同じ文字定数はコンパイル時同じobjにな	る
			// Jsonのテストではない
			assertTrue( true );
			if (false) fail("VM異常");
		}
		
		@Test void jsonArrayとjsonObjectのtoString() {
			//json変換 ja
			JsonType ja = new JsonArray();
				ja.push("hogeo0123456789012345678901");
			ja.push("hogehoge");
			ja.push(123);
			ja.push(new JsonObject().add("name", "yusuke").add("name2", "sasaki"));
			
			String ls = System.getProperty("line.separator");
			assertEquals(ja.toString("  "), "["+ls+"  \"hogeo0123456789012345678901\","+ls+"  \"hogehoge\","+ls+"  123,"+ls+"  {"+ls+"    \"name\": \"yusuke\","+ls+"    \"name2\": \"sasaki\""+ls+"  }"+ls+"]");
			
		}
		
		@Test void jsonObjectで様々な型を格納しjson化() {
			//json変換 ja
			JsonType jo = new JsonObject();
			jo.add("name", "yusuke").add("name2", "sasaki");
			jo.add("hoe", new JsonObject().add("pi", 3.14).add("x", 35).add("str", "0123456789012345678901234567890123456789012"));
			
			String ls = System.getProperty("line.separator");
			assertEquals(jo.toString("  "), "{"+ls+"  \"hoe\": {\"pi\":3.14,\"str\":\"0123456789012345678901234567890123456789012\",\"x\":35},"+ls+"  \"name\": \"yusuke\","+ls+"  \"name2\": \"sasaki\""+ls+"}");
		}
		
		@Test void jsonValueのparseとエスケープ() {
			//json変換 jb
			JsonValue jv = new JsonValue("ab\t\r\nc");
			assertEquals(jv.toString(), "\"ab\\t\\r\\nc\"");
			assertEquals(jv.getValue(), "ab\t\r\nc");
			
			JsonType jt = JsonType.parse("\"\\"+"u3044\\ta\"");
			assertEquals(jt.toString(), "\"\u3044\\ta\"");
			assertEquals(jt.getValue(), "い\ta");
		}
		
		@Test void objectにはarrayを含められる() {
			// Object 変換
			JsonType[] array = new JsonType[2];
			array[0] = new JsonValue(5);
			array[1] = new JsonValue("hoe");
			
			JsonObject jo = new JsonObject();
			jo.put("array", array);
			
			assertEquals(jo.toString(), "{\"array\":[5,\"hoe\"]}");
			
			JsonType jt = JsonType.parse(jo.toString());
			assertEquals(jt.toString(), "{\"array\":[5,\"hoe\"]}");
			assertEquals(jt.get("array").get(1).getValue(), "hoe");
			
		}
		
		@Test void objectのgetで階層的なキー値が使える() {
			JsonType j = JsonType.o("prop1", JsonType.o("prop2", JsonType.o("prop3", 5)));
			
			assertEquals(j.get("prop1").toString(), "{\"prop2\":{\"prop3\":5}}");
			assertEquals(j.get("prop1.prop2").toString(), "{\"prop3\":5}");
			assertEquals(j.get("prop1.prop2.prop3").toString(), "5");
			assertEquals(j.toString(), "{\"prop1\":{\"prop2\":{\"prop3\":5}}}");
		}
		
		@Test void objectのputやcutで階層的なキー値が使える() {
			JsonObject jo = new JsonObject();
			jo.put("a", new JsonValue(1));
			try {
				jo.put("a.b", new JsonValue(2));
				fail("JsonObject のエントリ a にすでに JsonValue が格納されているにも関わらず、a.b への追加ができてしまいました");
			} catch (IllegalArgumentException ok) {
			}
			jo.put("b.c.d", new JsonObject().put("some","what"));
			jo.put("b.c.d.e", new JsonValue(true));
			jo.put("b.c.d2", new JsonValue(3));
			assertEquals(jo.toString(), "{\"a\":1,\"b\":{\"c\":{\"d\":{\"e\":true,\"some\":\"what\"},\"d2\":3}}}");
			JsonType j2 = jo.cut("b.c.d");
			assertEquals(j2.toString(), "{\"e\":true,\"some\":\"what\"}");
			assertEquals(jo.toString(), "{\"a\":1,\"b\":{\"c\":{\"d2\":3}}}");
		}
	
		// 階層的なキー値を持つ場合の add / cut
		@Test void objectのaddやcutで階層的なキー値が使える() {
			JsonObject jo = new JsonObject();
			jo.add("a", new JsonValue(1));
			try {
				jo.add("a.b", new JsonValue(2));
				fail("JsonObject のエントリ a にすでに JsonValue が格納されているにも関わらず、a.b への追加ができてしまいました");
			} catch (IllegalArgumentException ok) {
			}
			jo.add("b.c.d", new JsonObject().add("some","what"));
			jo.add("b.c.d.e", new JsonValue(true));
			jo.add("b.c.d2", new JsonValue(3));
			jo.add("b.c.d", new JsonObject().add("value",5)); // add
			assertEquals(jo.toString(), "{\"a\":1,\"b\":{\"c\":{\"d\":[{\"e\":true,\"some\":\"what\"},{\"value\":5}],\"d2\":3}}}");
			JsonType j2 = jo.cut("b.c.d");
			assertEquals(j2.toString(), "[{\"e\":true,\"some\":\"what\"},{\"value\":5}]");
			assertEquals(jo.toString(), "{\"a\":1,\"b\":{\"c\":{\"d2\":3}}}");
		}
		
		@Test void 階層的なキー値を指定しても連続putできる() {
			JsonType j = new JsonObject().put("a", "a").put("b.c", "b.c").put("d.e.f","d.e.f");
			assertEquals(j.toString(), "{\"a\":\"a\",\"b\":{\"c\":\"b.c\"},\"d\":{\"e\":{\"f\":\"d.e.f\"}}}");
		}
		
		// 階層的なキーを指定した場合の連続 add
		@Test void 階層的なキー値を指定しても連続addできる() {
			JsonType j = new JsonObject().add("a", "a").add("b.c", "b.c").add("d.e.f","d.e.f");
			assertEquals(j.toString(), "{\"a\":\"a\",\"b\":{\"c\":\"b.c\"},\"d\":{\"e\":{\"f\":\"d.e.f\"}}}");
		}
	}
}
