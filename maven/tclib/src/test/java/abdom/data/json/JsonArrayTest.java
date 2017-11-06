package abdom.data.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 練習で作った JsonArray テスト。
 */
public class JsonArrayTest extends TestCase{
	public void testNewArray() {
		//System.out.println("test new Array ....");
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
	
	public void testPush() {
		JsonType j = new JsonArray();
		j.push("str");
		j.push( (byte)1 );
		j.push( '2' );
		j.push( (short)3 );
		j.push( 4 );
		j.push( 5L );
		j.push( 6f );
		j.push( 7d );
		j.push( true );
		j.push( (Jsonizable)null );
		assertEquals(j.toString(), "[\"str\",1,\"2\",3,4,5,6.0,7.0,true,null]");
	}
	
	public void testShiftUnshift() {
		JsonType j = new JsonArray();
		j.shift("str");
		j.shift( (byte)1 );
		j.shift( '2' );
		j.shift( (short)3 );
		j.shift( 4 );
		j.shift( 5L );
		j.shift( 6f );
		j.shift( 7d );
		j.shift( true );
		j.shift( (Jsonizable)null );
		assertEquals(j.toString(), "[null,true,7.0,6.0,5,4,3,\"2\",1,\"str\"]");
		assertEquals(j.unshift(), new JsonValue(null));
		assertEquals(j.unshift(), new JsonValue(true));
		assertEquals(j.unshift(), new JsonValue(7d));
		assertEquals(j.unshift(), new JsonValue(6f));
		assertEquals(j.unshift(), new JsonValue(5L));
		assertEquals(j.unshift(), new JsonValue(4));
		assertEquals(j.unshift(), new JsonValue((short)3));
		assertEquals(j.unshift(), new JsonValue('2'));
		assertEquals(j.unshift(), new JsonValue((byte)1));
		assertEquals(j.unshift(), new JsonValue("str"));
		
	}
	
	public void testSlice() {
		JsonType j = new JsonArray(1, 2, 3, 4, 5, 6);
		assertEquals(j.slice(1, 3).toString(), "[2,3]");
	}
	
	public void testConcat() {
		JsonArray ja = new JsonArray(1, 3, 5);
		JsonArray ja2 = new JsonArray("2", "4");
		//printBytes(ja.concat(ja2).toString());
		//printBytes("[1,3,5,\"2\",\"4\"]");
		assertEquals(ja.concat(ja2), JsonType.parse("[1,3,5,\"2\",\"4\"]"));
		// 非破壊であることの確認
		assertEquals(ja.toString(), "[1,3,5]");
		assertEquals(ja2.toString(), "[\"2\",\"4\"]");
	}
	
	private void printBytes(String str) {
		for (int i = 0; i < str.length(); i++) {
			System.out.print( (int)str.charAt(i) );
		}
		System.out.println();
	}
	
	public void testGetCut() {
		JsonType jt = new JsonArray(1, 2, 3, 4, 5, 6, 7);
		jt.cut(1);
		jt.cut(2);
		jt.cut(3);
		for (int i = 0; i < jt.size(); i++) {
			assertEquals(jt.get(i).intValue(), i * 2 + 1);
		}
	}
	
	public void testIterator() {
		Integer[] a = new Integer[10];
		for (int i = 0; i < 10; i++) a[i] = i;
		JsonType jt = new JsonArray(a);
		int ind = 0;
		for (JsonType j : jt) {
			assertEquals(j.intValue(), ind++);
		}
	}
	
	public void testBoolean() {
		assertEquals(new JsonArray().booleanValue(), false);
		assertEquals(JsonType.a(1, "2").booleanValue(), true);
	}
}
