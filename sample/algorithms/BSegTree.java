import java.util.function.ToIntFunction;

/**
 * 任意個数の Node を保持でき、Node 全体に対する演算値(max/min/加算など、半群演算)を
 * 高速に計算できる構造です。
 * Segment Tree を可変容量とし、オブジェクト指定の delete/replace を高速化した
 * 反面、演算は全体に対してのみとなる制約があります。
 * この構造は、binary tree で実装され、次の特徴があります。
 * - 個数に応じたサイズ (Linked list 的な構造)
 * - add/delete/replace の計算量は O(log N)
 * - 演算結果の取得は O(1)
 * なお、delete は末端(leaf) のみを削除し、Branch 構造は変更しません。
 * (再追加時の Branch 生成を省略できるが、一度深くなった木構造は変わらないため、
 * 処理/メモリ効率は犠牲)
 * 
 * add では、weight のバランスが取れるように、root に近い方に追加されます。
 * これにより、木構造の最大ネストが大きくならないようにします。
 *
 * value として、Node とすることで、値更新可能な PriorityQueue として
 * 利用することができます。(Dijkstra 法で利用可能)
 *
 * 利用法
 * <pre>
 * Branch root = new Branch();
 * // add/replace/delete 計算
 * Node node = new Node();
 * node.value = ...;
 * root.add[node];
 * :
 * Node replacement = new Node();
 * replacement.value = ...;
 * root.replace(replacement);
 * :
 * root.delete(someSpecificNode);
 * :
 * int result = root.value;
 * </pre>
 */

/**
 * Leaf または Branch を表す Node です。
 * 演算値を value として保持しており、木構造における位置を id として保持します。
 * id は root を 1 として、次のように付与されています。
 * 
 *                                 root(1)
 *                 b(10)                             b(11)
 *         b(100)        l(101)             b(110)           l(111)
 *     l(1000) l(1001)                   -      b(1101)
 *                                         l(11010) l(11011)
 * b...branch l...leaf () 内はid(2進数)
 * id は int 型のため、Node は 2^31 個以下である必要があります。
 * depth を保持してトップビットの制約をなくし、2^32 個まで、微妙に高速化
 * できる可能性があります。
 *
 * @version		4th July, 2021
 * @author		Yusuke Sasaki
 */
class Node {
	/** 木構造における位置 */
	int id;
	/** 計算値(半群計算の結果) */
	int value;
	// ぶら下がっている Node(Leaf/Branch) の数(葉の場合、0)
	int weight;

	public String toString() {
		return "[id="+id+"#="+(hashCode() & 0xFF)+",v="+value+"]";
	}
}

class Branch extends Node {
	/**
	 * updater の例(実行上不要)
	 */
	static final ToIntFunction<Branch> MAX = b -> Math.max( (b.l == null)?-1:b.l.value, (b.r == null)?-1:b.r.value );
	
	/**
	 * value に対する半群演算 (value, value) |-> value
	 * Branch の l.value, r.value に対して作用することに注意
	 */
	ToIntFunction<Branch> updater;
	Node l, r;
	
	Branch(ToIntFunction<Branch> updater) {
		id = 1;
		this.updater = updater;
	}
	
	/**
	 * この Branch 配下に Node (leaf に限ります) を追加します。
	 * 追加後、各 Branch の value, weight が更新されます。
	 * 計算量は O(log N) です。
	 * 
	 * @param e 追加する leaf。id が更新されます。
	 */
	final void add(Node e) {
		if (l == null) {
			l = e;
			e.id = id << 1;
		} else if (r == null) {
			r = e;
			e.id = (id << 1) + 1;
		} else {
			// l, r ともに埋まっている
			Node toAdd = (l.weight > r.weight)?r:l;
			if (toAdd instanceof Branch) {
				((Branch)toAdd).add(e);
			} else {
				// toAdd は Node(leaf)。追加できないため、Branch 挿入
				Branch b = new Branch(updater);
				b.id = toAdd.id;
				b.add(toAdd);
				b.add(e); // value, weight, e.id 設定
				if (l.weight > r.weight) r = b;
				else l = b;
			}
		}
		update();
	}
	
	/**
	 * この Branch 配下にある Node を高速に削除します。
	 * 位置の特定のために、Node の id を位置情報として利用します。
	 * Branch の構造は変わりません。(Branch は delete されず、leaf のみが
	 * 削除されます)
	 * 計算量は O(log N) です。
	 *
	 * @param	e	削除する leaf。設定されている id が位置特定に用いられます。
	 */
	final void delete(Node e) {
		// e.id を元に位置を探索
		int m = 1 << (Integer.numberOfLeadingZeros(id) - Integer.numberOfLeadingZeros(e.id) - 1);

		if ( (m & e.id) == 0) {
			// l の方にある
			if (l instanceof Branch) ((Branch)l).delete(e);
			else l = null;
		} else {
			// r の方にある
			if (r instanceof Branch) ((Branch)r).delete(e);
			else r = null;
		}
		update();
	}
	
	/**
	 * この Branch 配下にある Node を指定したものに置き換えます。
	 * 通常の使い方は、該当 Node の value を更新し、このメソッドをコール
	 * することです。これにより、全体が再計算されます。
	 * 同一の Node オブジェクトである必要はありませんが、
	 * id, weight(=0) は元の Node と同一値に設定されている必要があります。
	 * 計算量は O(log N) です。
	 *
	 * @param	e	差し替える leaf。設定されている id が位置特定に用いられます。
	 */
	final void replace(Node e) {
		// e.id を元に位置を探索
		int m = 1 << (Integer.numberOfLeadingZeros(id) - Integer.numberOfLeadingZeros(e.id) - 1);

		if ( (m & e.id) == 0) {
			// l の方にある
			if (l instanceof Branch) ((Branch)l).replace(e);
			else l = e;
		} else {
			// r の方にある
			if (r instanceof Branch) ((Branch)r).replace(e);
			else r = e;
		}
		update();
	}
	
	private void update() {
		/** 半群演算(この実装は (int, int) -> int の例 */
		value = updater.applyAsInt(this);
		weight = ((l == null)?0:(l.weight+1)) + ((r == null)?0:(r.weight+1));
	}
	
	public String toString() {
		return "(id="+id+",v="+value+",w="+weight+",l:"+((l==null)?"-":l.toString())+",r:"+((r==null)?"-":r.toString())+")";
	}
}
