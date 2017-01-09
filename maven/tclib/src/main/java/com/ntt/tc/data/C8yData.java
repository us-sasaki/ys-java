package com.ntt.tc.data;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import abdom.data.json.object.JData;
import abdom.data.json.object.JDataDefinitionException;
import abdom.data.json.JsonType;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;

/**
 * Cumulocity のデータのスーパークラスです。
 * カテゴリ分けのほか、JData の直列化、c8y 特有のルールに対応するメソッドを
 * 提供します。
 * C8yData は JData を継承しているため、JSON 直列化をサポートします。
 * 例えば、<pre>
 * System.out.println(new ManagedObject().toJson().toString("  "));
 * </pre>
 * を実行すると、ManagedObject の JSON 形式が得られます。
 */
public abstract class C8yData extends JData {
	private static final JsonValue CACHED_NULL = new JsonValue(null);
	
	/**
	 * このオブジェクトを指定されたオブジェクトに値が一致させる
	 * JsonObject を抽出します。返り値を ret とした場合、一般に
	 * another.fill(ret).equals(this) が成立します。ただし、fill の制約である
	 * 要素に JsonValue(null) を明示的に設定できないことは同様です。
	 *
	 * @param	another		比較対象
	 * @return	差分を表す JsonObject。
	 */
	public JsonObject getDifference(C8yData another) {
		if (getClass() != another.getClass())
			throw new IllegalArgumentException("getDifference() requires "+getClass() + " instance.");
		JsonType a = another.toJson();
		JsonType b = this.toJson();
		
		JsonObject result = new JsonObject();
		
		// キーをマージした map を生成
		Set<String> merged = new HashSet<String>(a.keySet());
//System.out.println("merged(a) = " + merged);
		for (String toAdd : b.keySet() ) {
			merged.add(toAdd);
		}
//System.out.println("merged(a,b) = " + merged);
		for (String field : merged) {
			JsonType ja = a.get(field);
			JsonType jb = b.get(field);
			
			if (ja == null) {
				if (jb != null) result.put(field, jb);
				continue;
			}
			if (ja.equals(jb)) continue;
			
			if (jb == null) result.put(field, CACHED_NULL);
			else result.put(field, jb);
		}
		return result;
	}
	
}
