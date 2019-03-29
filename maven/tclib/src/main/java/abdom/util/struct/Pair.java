package abdom.util.struct;

/**
 * 2 つの要素を保持する構造体クラス。
 * フィールド名として key, value としているが、特に要素への制約はない。
 * 単に 2 要素を持つクラスをまとめ、List などの要素として利用することを
 * 想定している。
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
	@Override
	public String toString() {
		return "["+key+","+value+"]";
	}
	
	@Override
	public int hashCode() {
		return key.hashCode() ^ value.hashCode();
	}
}
