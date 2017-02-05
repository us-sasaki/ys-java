package abdom.data.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 練習で作った JsonValue テスト。
 * Unit test for simple App.
 * http://www.mitchy-world.jp/java/test/junit3.htm
 */
public class JsonArrayTest extends TestCase{
	public void testNewArray() {
		System.out.println("test new Array ....");
		assertEquals(new JsonArray("true").toString(), "[\"true\"]");
		assertEquals(new JsonArray(1,0.5,false,null).toString(), "[1,0.5,false,null]");
	}
	public void testSplice() {
		JsonArray ja = new JsonArray(1,2,3,4,5);
		assertEquals(ja.toString(), "[1,2,3,4,5]");
		
		assertEquals(ja.splice(1,0,new JsonValue(5)).toString(), "[1,5,2,3,4,5]");
		assertEquals(ja.splice(1,1,new JsonValue(6)).toString(), "[1,6,2,3,4,5]");
		assertEquals(ja.splice(1,4,new JsonValue(9)).toString(), "[1,9,5]");
		//System.out.println(ja.splice(2,1, new JsonArray(10,11)));
		assertEquals(ja.splice(2,1, new JsonArray(10,11)).toString(), "[1,9,10,11]");
	}
	public void testPop() {
		JsonType jt = JsonType.parse("[10,100,null,300,400]");
		assertTrue( jt instanceof JsonArray );
		assertEquals( jt.pop().toString(), "400" );
		assertEquals( jt.pop().toString(), "300" );
		JsonType poped = jt.pop();
		assertEquals( poped.toString(), "null");
		assertEquals( poped.getType(), JsonType.TYPE_VOID );
		assertEquals( jt.size() , 2 );
	}
}
