package abdom.data.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 練習で作った JsonArray テスト。JUnit5
 */
class JsonArrayTest {
	@Nested
	class arrayの基本機能 {
		JsonArray a1;
		JsonArray a2;
		
		@BeforeEach void init() {
			a1 = new JsonArray("true");
			a2 = new JsonArray(1, 0.5, false, null);
		}
		
		@Test void 型() {
			assertEquals(JsonType.TYPE_ARRAY, a1.getType());
			assertEquals(JsonType.TYPE_ARRAY, a2.getType());
		}
		
		@Test void JSON化() {
			assertEquals("[\"true\"]", a1.toString());
			assertEquals("[1,0.5,false,null]", a2.toString());
		}
		
		@Test void sizeの取得() {
			assertEquals(1, a1.size());
			assertEquals(4, a2.size());
		}
		
		@Test void getによる取得() {
			assertEquals(new JsonValue("true"), a1.get(0));
			assertEquals(0.5d, a2.get(1).doubleValue());
		}
		@Test void boolean値() {
			assertEquals(new JsonArray().booleanValue(), false);
			assertEquals(JsonType.a(1, "2").booleanValue(), true);
		}
	}
	
	@Nested class Spliceテスト {
		JsonArray ja;
		
		@BeforeEach void init() {
			ja = new JsonArray(1,2,3,4,5);
		}
		
		@Test void 単一要素の挿入() {
			assertEquals("[1,5,2,3,4,5]",
							ja.splice(1,0,new JsonValue(5)).toString());
		}
		
		@Test void 削除して単一要素を挿入() {
			assertEquals("[1,6,3,4,5]",
							ja.splice(1,1,new JsonValue(6)).toString());
		}
		
		@Test void たくさん削除して単一要素を挿入() {
			assertEquals("[1,9]",
							ja.splice(1,4,new JsonValue(9)).toString());
		}
		
		@Test void 削除して複数要素を挿入() {
			assertEquals("[1,2,10,11,4,5]",
							ja.splice(2,1, new JsonArray(10,11)).toString());
		}
		
		@Test void spliceは破壊的() {
			ja.splice(1,0,new JsonValue(5));
			assertNotEquals("[1,2,3,4,5]", ja.toString());
		}
	}
	
	@Nested class popテスト {
		@Test void 連続してpopができる() {
			JsonArray ja = new JsonArray(10,100,null,300,400);
			assertEquals("400", ja.pop().toString());
			assertEquals("300", ja.pop().toString());
			assertEquals("null", ja.pop().toString());
			assertEquals(2, ja.size());
		}
	}
	
	@Nested class pushテスト {
		@Test void 連続してpushができる() {
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
	}
	
	@Nested class shiftテスト {
		@Test void 連続してshiftできる() {
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
			assertEquals(10, j.size());
		}
	}
	
	@Nested class unshiftテスト {
		@Test void 連続してunshiftできる() {
			JsonType j = JsonType.parse("[null,true,7.0,6.0,5,4,3,\"2\",1,\"str\"]");
			assertEquals(JsonType.NULL, j.unshift());
			assertEquals(JsonType.TRUE, j.unshift());
			assertEquals(new JsonValue(7d), j.unshift());
			assertEquals(new JsonValue(6f), j.unshift());
			assertEquals(new JsonValue(5L), j.unshift());
			assertEquals(new JsonValue(4), j.unshift());
			assertEquals(new JsonValue((short)3), j.unshift());
			assertEquals(new JsonValue('2'), j.unshift());
			assertEquals(new JsonValue((byte)1), j.unshift());
			assertEquals(new JsonValue("str"), j.unshift());
		}
	}
	
	@Nested class sliceテスト {
		JsonType j;
		
		@BeforeEach void init() {
			j = new JsonArray(1, 2, 3, 4, 5, 6);
		}
		
		@Test void sliceできる() {
			assertEquals("[2,3]", j.slice(1, 3).toString());
		}
		
		@Test void sliceは非破壊的() {
			j.slice(1, 3);
			assertEquals("[1,2,3,4,5,6]", j.toString());
		}
	}
	
	@Nested class concatテスト {
		JsonArray ja, ja2;
		
		@BeforeEach void init() {
			ja = new JsonArray(1, 3, 5);
			ja2 = new JsonArray("2", "4");
		}
		
		@Test void concatできる() {
			assertEquals(JsonType.parse("[1,3,5,\"2\",\"4\"]"),
							ja.concat(ja2) );
		}
		
		@Test void concatは非破壊的() {
			ja.concat(ja2);
			
			assertEquals(ja.toString(), "[1,3,5]");
			assertEquals(ja2.toString(), "[\"2\",\"4\"]");
		}
	}
	
	private void printBytes(String str) {
		for (int i = 0; i < str.length(); i++) {
			System.out.print( (int)str.charAt(i) );
		}
		System.out.println();
	}
	
	@Nested class cutテスト {
		@Test void cutできること() {
			JsonType jt = new JsonArray(1, 2, 3, 4, 5, 6, 7);
			jt.cut(1);
			jt.cut(2);
			JsonType cut = jt.cut(3);
			for (int i = 0; i < jt.size(); i++) {
				assertEquals(jt.get(i).intValue(), i * 2 + 1);
			}
			assertEquals(new JsonValue(6), cut);
		}
	}
	
	@Nested class iteratorテスト {
		@Test void forで使える() {
			Integer[] a = new Integer[10];
			for (int i = 0; i < 10; i++) a[i] = i;
			JsonType jt = new JsonArray(a);
			int ind = 0;
			for (JsonType j : jt) {
				assertEquals(j.intValue(), ind++);
			}
		}
	}
	
}
