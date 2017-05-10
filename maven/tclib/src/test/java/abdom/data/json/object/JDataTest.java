package abdom.data.json.object;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import abdom.data.json.*;

/**
 * 練習で作った JsonValue テスト。
 */
public class JDataTest extends TestCase{
	public JDataTest(String testName) {
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
		return new TestSuite( JDataTest.class );
	}
	
	static class J1 {
		public boolean b;
		public int[] a;
		public double doublevalue;
		public transient short x;
		public String str;
		public JsonObject jo;
		
		protected String message;
		
		public String getMSG() {
//System.out.println("getMsg() called");
			return message;
		}
		public void setMSG(String msg) {
//System.out.println("setMsg() called");
			this.message = msg;
		}
		
		public String[] getMessages() {
//System.out.println("getMsgs() called");
			return new String[] { message, "fixed msg" };
		}
		
		public void setMessages(String[] args) {
//System.out.println("setMsgs() called");
			this.message = args[0];
		}
	}
    
	public void testJ1() {
		J1 j = new J1();
		
		JsonType jt = Jsonizer.toJson(j);
		String json = jt.toString();
		
		assertEquals(json.toString(), "{\"b\":false,\"doublevalue\":0.0,\"messages\":[null,\"fixed msg\"]}");
		jt.put("b", true);
		jt.put("a", JsonType.a(5,65) );
		jt.put("doublevalue", Math.PI);
		jt.put("str", "hoe");
		jt.put("jo", JsonType.o("key","value"));
		jt.put("MSG", "This is a message");
		jt.put("messages", JsonType.a("hogetarou", "hoe"));
		jt.put("fragment", "c8y?");
		assertEquals(jt.toString(), "{\"MSG\":\"This is a message\",\"a\":[5,65],\"b\":true,\"doublevalue\":3.141592653589793,\"fragment\":\"c8y?\",\"jo\":{\"key\":\"value\"},\"messages\":[\"hogetarou\",\"hoe\"],\"str\":\"hoe\"}");
		J1 k = new J1();
		JsonType frag = Jsonizer.fill(k, jt);
		
		assertEquals(Jsonizer.toJson(k).toString(), "{\"MSG\":\"hogetarou\",\"a\":[5,65],\"b\":true,\"doublevalue\":3.141592653589793,\"jo\":{\"key\":\"value\"},\"messages\":[\"hogetarou\",\"fixed msg\"],\"str\":\"hoe\"}");
		assertEquals(frag.toString(), "{\"fragment\":\"c8y?\"}");
	}
	
	static class J2 extends JData {
		public int a;
		public double doublevalue;
	}
	
	/**
	 * put extra のテスト
	 * すでにプロパティとして存在する key を使って putExtra() すると
	 * Exception が発生することを確認するテスト。
	 */
	public void testPutExtra() {
		J2 j = new J2();
		
		j.putExtra("extra", new JsonValue(1));
		try {
			j.putExtra("doublevalue", new JsonValue(0.2));
			fail();
		} catch (IllegalFieldTypeException e) {
		}
		assertEquals(j.toString(), "{\"a\":0,\"doublevalue\":0.0,\"extra\":1}");
	}
	
	/**
	 * set/get のテスト
	 */
	public void testGetSet() {
		J2 j = new J2();
		
		j.a = 5;
		j.doublevalue = 6.3d;
		
		JsonType b = new JsonValue(10.4f);
		j.putExtra("b", b);
		
		JsonType a = new JsonValue(25);
		j.set("a", a);
		assertEquals(j.a, 25);
		
		JsonType bb = new JsonValue(25.25f);
		j.set("b", bb);
		assertEquals(j.get("b"), bb);
		
		JsonType d = new JsonValue(2525.25d);
		j.set("doublevalue", d);
		assertEquals(j.get("doublevalue"), d);
		
		//System.out.println(j);
	}
}
