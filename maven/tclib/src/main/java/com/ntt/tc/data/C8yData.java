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
 * C8yData は JData を継承しているため、JSON 相互変換をサポートします。
 * 例えば、<pre>
 * System.out.println(new ManagedObject().toString("  "));
 * </pre>
 * を実行すると、ManagedObject の (pretty)JSON 形式が得られます。
 */
public class C8yData extends JData {
	
	/**
	 * 指定されたオブジェクトをこのオブジェクトに値を一致させる
	 * JsonObject を抽出します(差分抽出)。
	 * 例えとして、(返り値) = this - another のような振る舞いをします：
	 * 返り値を ret とした場合、一般に
	 * another.fill(ret).equals(this) が成立します。
	 * 以下、参考情報<br><br>
	 * fill の制約として
	 * 要素への JsonValue(null) の設定ができない(JSON の null を指定すると、
	 * Java Object としての null 設定と解釈される)ことは同様です。
	 * (JsonValue を設定しうる extra の要素に対し、この制約が影響する可能性が
	 * あります。通常フィールドは JsonValue を認めていないため、影響はありま
	 * せん)
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
		merged.addAll(b.keySet());
		for (String field : merged) {
			JsonType ja = a.get(field);
			JsonType jb = b.get(field);
			
			if (ja == null) {
				if (jb != null) result.put(field, jb);
				continue;
			}
			if (ja.equals(jb)) continue;
			
			if (jb == null) result.put(field, JsonType.NULL);
			else result.put(field, jb);
		}
		return result;
	}
	
}
