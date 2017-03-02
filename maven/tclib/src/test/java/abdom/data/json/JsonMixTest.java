package abdom.data.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * 練習で作った JsonValue テスト。
 * Unit test for simple App.
 * http://www.mitchy-world.jp/java/test/junit3.htm
 */
public class JsonMixTest extends TestCase{
	public JsonMixTest(String testName) {
		super(testName);
		//System.out.println("JsonMixTest コンストラクタが呼ばれました");
	}
	protected void setUp() throws Exception {
		super.setUp();
		//System.out.println("setUp()が呼ばれました");
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		//System.out.println("tearDown()が呼ばれました");
	}
    public static Test suite() {
    	//System.out.println("suite() が呼ばれました");
        return new TestSuite( JsonMixTest.class );
    }
	public void test0() {
		assertEquals(1, 1);
		assertFalse( false );
		assertNotNull( "hoe" );
		String h = "hoe";
		assertNotSame(h, new String(h.getBytes()));
		assertNull( null );
		assertSame( h, "hoe" ); // !! 同じ文字定数はコンパイル時同じobjになる
								// Jsonのテストではない
		assertTrue( true );
		if (false) fail();
	}
	public void test1() {
		//json変換 ja
		JsonType ja = new JsonArray();
		ja.push("hogeo0123456789012345678901");
		ja.push("hogehoge");
		ja.push(123);
		ja.push(new JsonObject().add("name", "yusuke").add("name2", "sasaki"));
		
		String ls = System.getProperty("line.separator");
		assertEquals(ja.toString("  "), "["+ls+"  \"hogeo0123456789012345678901\","+ls+"  \"hogehoge\","+ls+"  123,"+ls+"  {"+ls+"    \"name\": \"yusuke\","+ls+"    \"name2\": \"sasaki\""+ls+"  }"+ls+"]");
		
	}
	public void test2() {
		//json変換 ja
		JsonType jo = new JsonObject();
		jo.add("name", "yusuke").add("name2", "sasaki");
		jo.add("hoe", new JsonObject().add("pi", 3.14).add("x", 35).add("str", "0123456789012345678901234567890123456789012"));
		
		String ls = System.getProperty("line.separator");
		assertEquals(jo.toString("  "), "{"+ls+"  \"hoe\": {\"pi\":3.14,\"str\":\"0123456789012345678901234567890123456789012\",\"x\":35},"+ls+"  \"name\": \"yusuke\","+ls+"  \"name2\": \"sasaki\""+ls+"}");
	}
	
	public void test3() {
		//json変換 jb
		JsonValue jv = new JsonValue("ab\t\r\nc");
		assertEquals(jv.toString(), "\"ab\\t\\r\\nc\"");
		assertEquals(jv.getValue(), "ab\t\r\nc");
		
		JsonType jt = JsonType.parse("\"\\"+"u3044\\ta\"");
		assertEquals(jt.toString(), "\"\u3044\\ta\"");
		assertEquals(jt.getValue(), "い\ta");
	}
	public void test4() {
		// Object... 変換
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
	public void test5() {
		JsonType j = JsonType.o("prop1", JsonType.o("prop2", JsonType.o("prop3", 5)));
		
		assertEquals(j.get("prop1").toString(), "{\"prop2\":{\"prop3\":5}}");
		assertEquals(j.get("prop1.prop2").toString(), "{\"prop3\":5}");
		assertEquals(j.get("prop1.prop2.prop3").toString(), "5");
		assertEquals(j.toString(), "{\"prop1\":{\"prop2\":{\"prop3\":5}}}");
	}
}
