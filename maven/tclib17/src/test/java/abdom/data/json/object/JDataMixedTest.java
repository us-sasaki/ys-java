package abdom.data.json.object;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import abdom.data.json.*;
import abdom.data.json.object.*;

/**
 * 練習で作った JsonValue テスト。
 * Unit test for simple App.
 * http://www.mitchy-world.jp/java/test/junit3.htm
 */
public class JDataMixedTest extends TestCase{
	public JDataMixedTest(String testName) {
		super(testName);
		//System.out.println("JDataTest コンストラクタが呼ばれました");
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
		return new TestSuite( JDataMixedTest.class );
	}
	
	static class J2 extends JData {
		public int[] ary;
		public J2 composite;
		
		protected String status;
		
		public String getStatus() {
			return status;
		}
		
		public void setStatus(String sts) {
			this.status = sts;
		}
		
		public void setHoge(int hoe) {
		}
		
	}
    
	public void testJ2() {
		J2 j = new J2();
		
		JsonType jt = j.toJson();
		assertEquals("{}", jt.toString());
		
		String ls = System.getProperty("line.separator");
		j.fill("{\"ary\":[], \"composite\":{\"ary\":[0,1,2,3]},\"frag\":35}");
		assertEquals(j.toString("  "), "{"+ls+"  \"ary\": [],"+ls+"  \"composite\": {\"ary\":[0,1,2,3]},"+ls+"  \"frag\": 35"+ls+"}");
		assertEquals(j.ary.length, 0);
		assertEquals(j.getExtras().get("frag").getValue(), "35");
		
		J2 k = new J2();
		k.composite = j;
		J2 l = new J2();
		l.composite = k;
		
		assertEquals(l.get("composite.composite.composite.ary").get(1).intValue(), 1);
		assertEquals(l.composite.get("composite").toString(), l.composite.composite.toString());
	}
	
	public void testObj() {
		java.util.Date date = new java.util.Date();
		Jsonizer.toJson(date);
	}
}
