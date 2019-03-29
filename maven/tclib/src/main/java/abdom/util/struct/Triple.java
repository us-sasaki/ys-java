package abdom.util.struct;

/**
 * 3 つの要素を保持する構造体クラス。
 * フィールド名として key, value としているが、特に要素への制約はない。
 * 単に 3 要素を持つクラスをまとめ、List などの要素として利用することを
 * 想定している。
 *
 * @version		March 2, 2019
 * @author		Yusuke Sasaki
 *
 * @param	<K>		key の型
 * @param	<V1>	value1 の型
 * @param	<V2>	value2 の型
 */
public class Triple<K, V1, V2> {
	public K key;
	public V1 value1;
	public V2 value2;
	
/*-------------
 * constructor
 */
	public Triple() {
	}
	public Triple(K key, V1 value1, V2 value2) {
		this.key = key;
		this.value1 = value1;
		this.value2 = value2;
	}

/*------------------
 * instance methods
 */
	@Override
	public String toString() {
		return "["+key+","+value1+","+value2+"]";
	}
	
	@Override
	public int hashCode() {
		return key.hashCode() ^ value1.hashCode() ^ value2.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Triple)) return false;
		Triple<?,?,?> t = (Triple<?,?,?>)obj;
		
		return ( (key == null && t.key == null)
				||(key != null && key.equals(t.key)) )
			&& ( (value1 == null && t.value1 == null)
				||(value1 != null && value1.equals(t.value1)) )
			&& ( (value2 == null && t.value2 == null)
				||(value2 != null && value2.equals(t.value2)) );
	}
}
