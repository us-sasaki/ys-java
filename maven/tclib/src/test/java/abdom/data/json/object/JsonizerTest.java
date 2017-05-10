package abdom.data.json.object;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import abdom.data.json.*;

/**
 * Jsonizer のテスト
 */
public class JsonizerTest extends TestCase{
	
	static class Pojo {
		public boolean x;
		public int a;
		public long b;
		public float c;
		public double d;
		public String e;
	}
	
	static class J1 extends JData {
		private boolean bool;
		private int i;
		private long l;
		private float f;
		private double d;
		private String s;
		private String doubleCalled = "";
		
		public boolean getBool() { return bool; }
		public void setBool(boolean x) { bool = x; }
		public int getINTEGER() { return i; }
		public void setINTEGER(int x) { i = x; }
		public long getLonG() { return l; }
		public void setLonG(long x) { l = x; }
		public float getFloat() { return f; }
		public void setFloat(float x) { f = x; }
		public double getDouble() {
			//System.out.println("getDouble called");
			doubleCalled = doubleCalled + "get";
			return d;
		}
		public void setDouble(double x) {
			//System.out.println("setDouble called");
			doubleCalled = doubleCalled + "set";
		}
		public String getString() { return s; }
		public void setString(String x) { s = x; }
	}
	
	public JsonizerTest(String testName) {
		super(testName);
	}
    
	public void testPojo() {
		Pojo p = new Pojo();
		p.x = false;
		p.a = 55;
		p.b = -5000L;
		p.c = 4.33f;
		p.d = 5555555.555d;
		p.e = "pojo";
		
		String json = Jsonizer.toString(p);
		//System.out.println(json);
		
		assertEquals(json, "{\"a\":55,\"b\":-5000,\"c\":4.33,\"d\":5555555.555,\"e\":\"pojo\",\"x\":false}");
		
		Pojo q = Jsonizer.fromJson(json, Pojo.class);
		//System.out.println(Jsonizer.toJson(q));
		assertEquals(Jsonizer.toString(p), Jsonizer.toString(q));
	}
	
	public void testPojo2() {
		Pojo p = new Pojo();
		p.x = false;
		p.a = 55;
		p.b = -5000L;
		p.c = 4.33f;
		p.d = 5555555.555d;
		p.e = "pojo";
		
		JsonType x = new JsonValue(true);
		Jsonizer.set(p, "x", x);
		assertEquals(Jsonizer.get(p, "x"), x);
		JsonType a = new JsonValue(193);
		Jsonizer.set(p, "a", a);
		assertEquals(Jsonizer.get(p, "a"), a);
		JsonType b = new JsonValue(4193L);
		Jsonizer.set(p, "b", b);
		assertEquals(Jsonizer.get(p, "b"), b);
		JsonType c = new JsonValue(1.93f);
		Jsonizer.set(p, "c", c);
		assertEquals(Jsonizer.get(p, "c"), c);
		JsonType d = new JsonValue(19.3d);
		Jsonizer.set(p, "d", d);
		assertEquals(Jsonizer.get(p, "d"), d);
		JsonType e = new JsonValue("193");
		Jsonizer.set(p, "e", e);
		assertEquals(Jsonizer.get(p, "e"), e);
		
		//System.out.println(Jsonizer.toString(p));
		
	}
	
	public void testJ() {
		J1 j = new J1();
		j.setBool(true);
		j.setINTEGER(99);
		j.setLonG(-50);
		j.setFloat(-10.4f);
		j.setDouble(-123456789.9876d);
		j.setString("JData object");
		
		//System.out.println(j);
		
		assertEquals(j.toString(), "{\"INTEGER\":99,\"bool\":true,\"double\":0.0,\"float\":-10.4,\"lonG\":-50,\"string\":\"JData object\"}");
		
		J1 k = Jsonizer.fromJson(j.toString(), J1.class);
		
		assertEquals(j.toJson(), k.toJson());
	}
	
	public void testJ2() {
		java.util.Set<String> names = Jsonizer.getPropertyNames(new J1());
		java.util.Set<String> ans = new java.util.HashSet<String>();
		ans.addAll(java.util.Arrays.asList(new String[] {"string", "bool" ,"double", "float", "lonG", "INTEGER"}));
		assertEquals(names, ans);
	}
	
}
