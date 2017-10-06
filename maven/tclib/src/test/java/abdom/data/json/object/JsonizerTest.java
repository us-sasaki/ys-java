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
	
	static class J3 {
		public int a;
	}
	
	static class J4 {
		public J3 b;
	}
	
	// 参照のオブジェクトが null でない場合、fill() で値が
	// 上書きされることを確認する。
	// 一方、null であった場合、新規インスタンスが生成されることを確認する。
	public void testJ3() {
		J4 c = new J4();
		Jsonizer.fill(c, JsonType.parse("{\"b\":{\"a\":5}}"));
		// 新しいオブジェクトが設定されるか
		assertEquals(c.b.a, 5);
		J3 x = c.b;
		Jsonizer.fill(c, JsonType.parse("{\"b\":{\"a\":15}}"));
		// c を fill して x が変更されるか
		assertEquals(x.a, 15);
	}
	
	static class J5 {
		public byte a;
		public short b;
		public char c;
		public byte[] d;
		public short[] e;
		public char[] f;
	}
	
	// 追加した primitive 型の対応テスト
	public void testJ5() {
		J5 j = new J5();
		j.a = (byte)255;
		j.b = (short)10;
		j.c = 'あ';
		j.d = new byte[] { 0,1,2 };
		j.e = new short[] { 0,-1,-2 };
		j.f = new char[] {'a', 'b', '漢', '甕' };
		assertEquals(Jsonizer.toString(j), "{\"a\":-1,\"b\":10,\"c\":\"あ\",\"d\":[0,1,2],\"e\":[0,-1,-2],\"f\":[\"a\",\"b\",\"漢\",\"甕\"]}");
		
		Jsonizer.fill(j, JsonType.parse("{\"a\":257,\"b\":-10,\"c\":\"あ\",\"d\":[257,258,259],\"e\":[-3,-4,-5],\"f\":[\"bcd\",\"g\",\"他\",\"薔薇\"]}"));
		
		assertEquals(Jsonizer.toString(j), "{\"a\":1,\"b\":-10,\"c\":\"あ\",\"d\":[1,2,3],\"e\":[-3,-4,-5],\"f\":[\"b\",\"g\",\"他\",\"薔\"]}");
	}

	static class J6 extends JData {
		public byte a;
		public short b;
		public char c;
		public byte[] d;
		public short[] e;
		public char[] f;
	}
	
	// 追加した primitive 型の対応テスト(JData版)
	public void testJ6() {
		J6 j = new J6();
		j.a = (byte)255;
		j.b = (short)10;
		j.c = 'あ';
		j.d = new byte[] { 0,1,2 };
		j.e = new short[] { 0,-1,-2 };
		j.f = new char[] {'a', 'b', '漢', '甕' };
		assertEquals(j.toString(), "{\"a\":-1,\"b\":10,\"c\":\"あ\",\"d\":[0,1,2],\"e\":[0,-1,-2],\"f\":[\"a\",\"b\",\"漢\",\"甕\"]}");
		
		j.fill(JsonType.parse("{\"a\":257,\"b\":-10,\"c\":\"あ\",\"d\":[257,258,259],\"e\":[-3,-4,-5],\"f\":[\"bcd\",\"g\",\"他\",\"薔薇\"]}"));
		
		assertEquals(j.toString(), "{\"a\":1,\"b\":-10,\"c\":\"あ\",\"d\":[1,2,3],\"e\":[-3,-4,-5],\"f\":[\"b\",\"g\",\"他\",\"薔\"]}");
	}

	// set の dot オペレーション
	static class J7 {
		public J5 j5;
		public J6 j6;
	}
	
	public void testJ7() {
		J7 j7 = new J7();
		// J5 にあるフィールドに設定できること
		JsonType jt = Jsonizer.set(j7, "j5.b", new JsonValue(5));
		assertEquals(j7.j5.b, 5);
		assertEquals(jt, null);
		
		// J5 にないフィールドに設定できないこと
		jt = Jsonizer.set(j7, "j5.g", new JsonValue("い"));
		assertEquals(jt.toString(), "\"い\"");
		
		// J6 にあるフィールドに設定できること
		jt = Jsonizer.set(j7, "j6.b", new JsonValue(6));
		assertEquals(j7.j6.b, 6);
		assertEquals(jt, null);
		
		// J6 にないフィールドも設定できる
		jt = Jsonizer.set(j7, "j6.g", new JsonValue("い"));
		assertEquals(jt, null);
		assertEquals(j7.j6.getExtra("g"), new JsonValue("い"));

		// J6 にない深いフィールドも設定できる
		jt = Jsonizer.set(j7, "j6.h.i", new JsonValue("い"));
		assertEquals(jt, null);
//		System.out.println(Jsonizer.toString(j7));
		assertEquals(j7.j6.getExtra("h.i"), new JsonValue("い"));
		
		assertEquals(Jsonizer.get(j7, "j5.b").shortValue(), (short)5);
		assertEquals(Jsonizer.get(j7, "j6.b").shortValue(), (short)6);
		assertEquals(Jsonizer.get(j7, "j6.g"), new JsonValue("い"));
		assertEquals(Jsonizer.get(j7, "j6.h").toString(), "{\"i\":\"い\"}");
		assertEquals(Jsonizer.get(j7, "j6.h.i").getValue(), "い");
	}
	
	// get の dot オペレーション
	public void testJ8() {
		J7 j7 = new J7();
		j7.j5 = new J5();
		j7.j6 = new J6();
		j7.j5.b = 5;
		j7.j6.b = 6;
		Jsonizer.set(j7, "j6.g", new JsonValue("い"));
		Jsonizer.set(j7, "j6.h.i", new JsonValue("い"));
		
		assertEquals(new JsonValue(5), Jsonizer.get(j7, "j5.b"));
		assertEquals(new JsonValue(6), Jsonizer.get(j7, "j6.b"));
		assertEquals(new JsonValue("い"), Jsonizer.get(j7, "j6.g"));
		assertEquals(new JsonValue("い"), Jsonizer.get(j7, "j6.h.i"));
		
	}
}
