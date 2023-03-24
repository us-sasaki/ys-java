import java.util.function.BinaryOperator;
import java.util.function.BiFunction;
import java.util.Arrays;

/**
 * 実利用向け遅延評価セグメントツリー class<br>
 * 区間に対する演算を高速に行えることに加え、
 * 区間に対する(加算や代入のような)演算を O(log n)で実行可能。
 * 通常のセグメント木より定数倍遅い。
 * 型は E としているが、半群の積(aggregator: E x E -> E)や IDENTITY は指定が必要。
 * 添え字は 0-indexed。
 * Exception 処理は省略。
 *
 * @version		23 October, 2022
 * @author		Yusuke Sasaki
 */
public class LazySegtreeLight<E> {
	E IDENTITY = null; // dummy identity (wont work)
	BinaryOperator<E> aggregator = (a, b) -> a; // dummy operation (select left)
	BiFunction<Integer, E, E> multiplier = (n, b) -> {
		E acc = IDENTITY;
		for (int i = 0; i < n; i++) acc = aggregator.apply(acc, b);
		return acc;
	}; // slow operation
	
	
	/** セグメントツリーの有効なサイズ */
	private int size;
	
	/**
	 * size 以上の 2 のべきの形の最小の数。
	 * セグメントツリーの全体サイズは 2m-1 。
	 * 葉の index は m-1 から開始する。
	 */
	private int m;
	
	/** セグメントツリーの要素を保持する配列および遅延評価内容。サイズは 2m-1 */
	private E[] st, lazy;
	
/*-------------
 * constructor
 */
	/**
	 * 指定されたサイズの要素を保持する遅延評価セグメント木 LazySegtree を生成します。
	 *
	 * @param		size		セグメント木の大きさ
	 */
	public LazySegtreeLight(int size) {
		init(size);
		// 葉の値を設定
		for (int i = 0; i < 2*m-1; i++)
			st[i] = IDENTITY; // identity 相当の値を入れる
	}
	
	/**
	 * 指定されたサイズの要素を保持する遅延評価セグメント木 LazySegtree を生成します。
	 *
	 * @param		value		与える初期値を含む配列
	 */
	public LazySegtreeLight(E[] value) {
		init(value.length);
		construct(value);
	}
	
	@SuppressWarnings("unchecked")
	private void init(int size) {
		this.size = size;
		m = (size == 1)? 1 : Integer.highestOneBit(size - 1) << 1;
		st = (E[])new Object[2*m-1];
		lazy = (E[])new Object[2*m-1];
		Arrays.fill(lazy, IDENTITY);
	}

/*------------------
 * instance methods
 */
	/**
	 * 配列でこのセグメント木を初期化します。
	 * update を繰り返し呼ぶより高速で、計算量は O(n) です。
	 *
	 * @param		elements		このセグメント木に設定する値
	 */
	public void construct(E[] elements) {
		int n = elements.length;
		// 葉の値を設定
		for (int i = 0; i < n; i++)
			st[m-1+i] = elements[i];
		for (int i = m-1+n; i < 2*m-1; i++)
			st[i] = IDENTITY;
		// 各親の値を更新
		for (int i = m-2; i >= 0; i--)
			st[i] = aggregator.apply(st[i*2+1], st[i*2+2]);
	}

	/**
	 * 指定セグメントに対し、遅延オペレーションが存在したらそれを値化(適用)し、
	 * 直接の子に遅延オペレーションを伝播する。
	 * @param n
	 * @param l
	 * @param r
	 */
	private void eval(int n, int l, int r) {
    if(lazy[n].equals(IDENTITY)) return;
		st[n] = aggregator.apply(st[n], multiplier.apply(r-l, lazy[n]));

		if(n < m-1) {
			// 子を持つ
			lazy[2*n+1] = lazy[n];
			lazy[2*n+2] = lazy[n];
		}
		lazy[n] = IDENTITY;
	}	
	/**
	 * 指定された index の要素を指定されたものに更新し、木の値を再計算します。
	 * 計算時間のオーダーは、O(log n) です。
	 *
	 * @param		index		追加するインデックス(0 以上 size 未満)
	 * @param		element		更新する要素
	 */
	public void update(int index, E element) {
		// M-1 が末端セグメントの開始番号
		int i = m-1+index;
		st[i] = element;
		while (i > 0) {
			// 親ノードに移行
			i = (i-1) >>> 1;
			st[i] = aggregator.apply(st[i*2 + 1], st[i*2 + 2]);
		}
	}

	/**
	 * 区間に対する作用 ( aggregator( _, element) ) を高速に(O(log n))施します。
	 * @param	s		区間の開始(含む)
	 * @param	eExclusive		区間の終了(含まない)
	 * @param	element 作用 aggregator( _, element) のパラメータ。
	 */
	public void operateSegment(int s, int eExclusive, E element) {
		operateSegmentImpl(s, eExclusive, element, 0, 0, m);
	}

	private void operateSegmentImpl(int s, int eExclusive, E element, int n, int l, int r) {
		eval(n, l, r);
    if (s <= l && r <= eExclusive) {
			// 完全に内側の時
			lazy[n] = element;
			eval(n, l, r);
    } else if (s < r && l < eExclusive) {
			// 一部区間が被る時
			int i = (l>>>1)+(r>>>1);
			operateSegmentImpl(s, eExclusive, element, 2*n + 1, l, i);
			operateSegmentImpl(s, eExclusive, element, 2*n + 2, i, r);
			st[n] = aggregator.apply(st[2*n + 1], st[2*n + 2]);
		}
	}
	
	/**
	 * 与えられた区間に対する aggregator 演算結果を高速( O(log n) )に取得します。
	 *
	 * @param		s		区間の開始(含む)
	 * @param		eExclusive		区間の終了(含まない)
	 * @return		区間における aggregator の結果
	 */
	public E calculate(int s, int eExclusive) {
		return calcImpl(s, eExclusive, 0, 0, m);
	}
	
	/**
	 * 与えられた区間での演算結果を返却する。
	 *
	 * @param		s		演算対象区間の開始(含む)
	 * @param		eExclusive		演算対象区間の終了(含まない)
	 * @param		n		セグメントのインデックス
	 * @param		l		セグメントの開始番号(含む)
	 * @param		r		セグメントの終了番号(含まない)
	 */
	private E calcImpl(int s, int eExclusive, int n, int l, int r) {
		eval(n, l, r);
		// 共通部分がない場合
		if (r <= s || eExclusive <= l) return IDENTITY;
		// 完全に含んでいる場合
		if (s <= l && r <= eExclusive) return st[n];
		// 一部共通している場合
		int i = (l>>>1)+(r>>>1);
		E cl = calcImpl(s, eExclusive, 2*n + 1, l, i);
		E cr = calcImpl(s, eExclusive, 2*n + 2, i, r);
		return aggregator.apply(cl, cr);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = m-1; i < m-1+size; i++) {
			sb.append(st[i]);
			sb.append(' ');
		}
		return sb.toString();
	}

/*---------------
 * main for test
 */
	public static void main(String[] args) throws Exception {
		test1();
		test2();
	}

	// 9 要素、1,2,3,4,5,6,7,8,9 の部分和を計算
	static void test1() {
		// 整数、加算
		int N = 9;
		LazySegtreeLight<Integer> lst = new LazySegtreeLight<>(N);
		lst.IDENTITY = 0; Arrays.fill(lst.st, 0);
		lst.aggregator = (a, b) -> a+b; Arrays.fill(lst.lazy, 0);

		for (int i = 0; i < N; i++)	lst.update(i, i+1);
		for (int i = 0; i < N; i++)
			for (int j = i+1; j < N+1; j++)
				if (lst.calculate(i, j) != ((i+1)+j)*(j-i)/2 )
					System.out.printf("error ! sum of [%d,%d)=%d\n", i+1, j+1, lst.calculate(i,j));
	}

	// 10 要素、1,2,3,4,5,6,7,8,9,10 を operateSegment で update して部分和計算
	static void test2() {
		// 整数、最大値
		int N = 10;
		LazySegtreeLight<Integer> lst = new LazySegtreeLight<>(N);
		// 加算
		// lst.IDENTITY = 0; Arrays.fill(lst.st, 0);
		// lst.aggregator = (a, b) -> a+b; Arrays.fill(lst.lazy, 0);
		// lst.multiplier = (n, b) -> n*b;

		// 負でない整数、最大
		lst.IDENTITY = -1; Arrays.fill(lst.st, -1);
		lst.aggregator = (a, b) -> Math.max(a, b); Arrays.fill(lst.lazy, -1);
		lst.multiplier = (n, b) -> b;

		for (int i = 0; i < N; i++) {
			lst.operateSegment(i, N, i);
			// for (int j = i; j < N; j++) lst.update(j, lst.calculate(j, j+1) + 1);
			if (i%2 != 0) continue;
			for (int j = 0; j < N; j++) System.out.print(lst.calculate(0, j+1) + "/");
			System.out.println();
			System.out.println(Arrays.toString(lst.lazy));
			System.out.println(Arrays.toString(lst.st));
			System.out.println(lst);
		}

	}
}
