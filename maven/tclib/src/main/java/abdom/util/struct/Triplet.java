package abdom.util.struct;

/**
 * 3 つの要素を保持する構造体クラス。
 * フィールド名として key, value1, value2 としているが、特に要素への制約はない。
 * 単に 3 要素を持つクラスをまとめ、List などの要素として利用することを
 * 想定しています。
 * <pre>
 * public class Profile extends Triplet&lt;String, Integer, Double&gt; {
 *     public Profile() { }
 *     public Profile(String name, Integer age, Double weight) {
 *         super(name, age, weight);
 *     }
 * }
 * </pre>
 * のように定義を簡略化することができます。もちろん、
 * <pre>
 * Triplet&lt;String, Integer, Double&gt; profile = new Triplet&lt;&gt;("name", 29, 63.2);
 * </pre>
 * のように新しいクラスを定義せず使うこともできます。
 * Singlet, Doublet, Triplett, Quartet, Quintet, Sextet, Septet, Octet, Nonet
 *
 * @version		March 2, 2019
 * @author		Yusuke Sasaki
 *
 * @param	<K>		key の型
 * @param	<V1>	value1 の型
 * @param	<V2>	value2 の型
 */
public class Triplet<K, V1, V2> {
	public K key;
	public V1 value1;
	public V2 value2;
	
/*-------------
 * constructor
 */
	public Triplet() {
	}
	public Triplet(K key, V1 value1, V2 value2) {
		this.key = key;
		this.value1 = value1;
		this.value2 = value2;
	}

/*------------------
 * instance methods
 */
	/**
	 * [{key},{value1},{value2}] の形式で文字列化します。
	 *
	 * @return		この Pair の文字列化
	 */
	@Override
	public String toString() {
		return "["+key+","+value1+","+value2+"]";
	}
	
	/**
	 * hash code を求めます。
	 * この実装では、key.hashCode() ^ value1.hashCode() ^ value2.hashCode()
	 * により hash 値を計算しています。
	 *
	 * @return		このオブジェクトのハッシュコード
	 */
	@Override
	public int hashCode() {
		return key.hashCode() ^ value1.hashCode() ^ value2.hashCode();
	}
	
	/**
	 * 指定されたオブジェクトと等しいかチェックします。
	 * 等しいとは、対象が Pair であり、key, value1, value2 いずれも等しい
	 * ことを意味します。
	 * key や value1, value2 が null の場合、対象の該当フィールドも null
	 * となっている場合等しくなります。
	 *
	 * @param		obj		比較対象
	 * @return		等しい場合 true
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Triplet)) return false;
		Triplet<?,?,?> t = (Triplet<?,?,?>)obj;
		
		return ( (key == null && t.key == null)
				||(key != null && key.equals(t.key)) )
			&& ( (value1 == null && t.value1 == null)
				||(value1 != null && value1.equals(t.value1)) )
			&& ( (value2 == null && t.value2 == null)
				||(value2 != null && value2.equals(t.value2)) );
	}
}
