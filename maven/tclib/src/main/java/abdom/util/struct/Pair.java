package abdom.util.struct;

import java.util.Map;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

/**
 * 2 つの要素を保持する構造体クラス。
 * フィールド名として key, value としているが、特に要素への制約はありません。
 * 単に 2 要素を持つクラスをまとめ、List などの要素として利用することを
 * 想定しています。
 *
 * <pre>
 * public class Profile extends Pair&lt;String, Integer&gt; {
 *     public Profile() { }
 *     public Profile(String name, Integer age) {
 *         super(name, age);
 *     }
 * }
 * </pre>
 * のように定義を簡略化することができます。もちろん、
 * <pre>
 * Pair&lt;String, Integer&gt; profile = new Pair&lt;&gt;("name", 29);
 * </pre>
 * のように新しいクラスを定義せず使うこともできます。
 *
 * @version		March 2, 2019
 * @author		Yusuke Sasaki
 *
 * @param	<K>		key の型
 * @param	<V>		value の型
 */
public class Pair<K, V> {
	public K key;
	public V value;
	
/*-------------
 * constructor
 */
	public Pair() {
	}
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * Collection から Map を生成します。
	 * Map は LinkedHashMap のインスタンスが生成され、順序は Collection
	 * の iterator の順序になります。
	 * Collection に重複する key があった場合、IllegalStateException が
	 * スローされます。
	 *
	 * @param		<K>		key の型
	 * @param		<V>		value の型
	 * @param		collection	Collection
	 * @return		Pair の key, value に基づき生成された LinkedHashMap
	 * @throws		IllegalStateException	key の重複があった場合
	 */
	public static <K,V> LinkedHashMap<K, V> toMap(Collection<Pair<K,V>> collection) {
		LinkedHashMap<K,V> result = new LinkedHashMap<>();
		for (Pair<K,V> pair : collection) {
			V lastValue = result.put(pair.key, pair.value);
			if (lastValue != null)
				throw new IllegalStateException("key "+ pair.key
						+ " conflicts.");
		}
		return result;
	}
	
	/**
	 * Map.entrySet の Pair 版です。
	 * LinkedHashList のインスタンスが返却され、順序は map.keySet の
	 * iterator のものになります。
	 * 要素は shallow copy です。
	 *
	 * @param		<K>		key の型
	 * @param		<V>		value の型
	 * @param		map		データソースとなる Map
	 * @return		Map&lt;K, V&gt; の key, value の値を持つ Pair の
	 *				LinkedHashSet
	 */
	public static <K,V> LinkedHashSet<Pair<K, V>> fromMap(Map<K, V> map) {
		LinkedHashSet<Pair<K, V>> result = new LinkedHashSet<>();
		for (K key : map.keySet()) {
			V value = map.get(key);
			result.add(new Pair<K, V>(key, value));
		}
		return result;
	}
	
	/**
	 * [{key},{value}] の形式で文字列化します。
	 *
	 * @return		この Pair の文字列化
	 */
	@Override
	public String toString() {
		return "["+key+","+value+"]";
	}
	
	/**
	 * hash code を求めます。
	 * この実装では、key.hashCode() ^ value.hashCode() により hash 値を
	 * 計算しています。
	 *
	 * @return		このオブジェクトのハッシュコード
	 */
	@Override
	public int hashCode() {
		return key.hashCode() ^ value.hashCode();
	}
	
	/**
	 * 指定されたオブジェクトと等しいかチェックします。
	 * 等しいとは、対象が Pair であり、key, value いずれも等しいことを意味します。
	 * key や value が null の場合、対象の該当フィールドも null となっている場合
	 * 等しくなります。
	 *
	 * @param		obj		比較対象
	 * @return		等しい場合 true
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Pair)) return false;
		Pair<?,?> p = (Pair<?,?>)obj;
		
		return ( (key == null && p.key == null)
				||(key != null && key.equals(p.key)) )
			&& ( (value == null && p.value == null)
				||(value != null && value.equals(p.value)) );
	}
}
