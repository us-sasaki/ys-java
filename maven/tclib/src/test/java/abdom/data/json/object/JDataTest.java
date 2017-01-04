package abdom.data.json.object;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import abdom.data.json.*;

/**
 * 練習で作った JsonValue テスト。
 * Unit test for simple App.
 * http://www.mitchy-world.jp/java/test/junit3.htm
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
System.out.println("getMsg() called");
			return message;
		}
		public void setMSG(String msg) {
System.out.println("setMsg() called");
			this.message = msg;
		}
		
		public String[] getMessages() {
System.out.println("getMsgs() called");
			return new String[] { message, "fixed msg" };
		}
		
		public void setMessages(String[] args) {
System.out.println("setMsgs() called");
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
}
