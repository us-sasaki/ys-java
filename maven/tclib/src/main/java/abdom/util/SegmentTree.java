package abdom.util;

import java.util.function.BinaryOperator;

/**
 * 汎用のセグメントツリー class<br>
 *
 * ある型の要素に対する二項演算が結合的であり、単位元をもつ(単位的半群)場合、
 * 直列した要素の区間に対する演算結果のクエリを高速に実行可能です。
 * 一般に、要素数 n に対し、任意の区間に対して O(log n) の計算量で結果を
 * 取得可能です。また、セグメント木の要素の変更は O(log n) の計算量です。
 * 構築のための計算量 O(n) のメソッドを有しています。
 *
 * <pre>
 * 演算 agg : E x E → E が結合的である、とは、
 *
 *    agg(agg(a, b), c) = agg(a, agg(b, c))
 *
 * となること。
 * また、区間 [a,b) = { a, a+1, a+2, ... , b-2, b-1 } に対する agg の
 * 演算結果 R とは、
 *
 *    R([a,b)) = agg(E[a], agg(E[a+1], agg(E[a+2], ... agg(E[b-2], E[b-1]))..))
 *
 * と定義する。
 * 
 * 例1)
 * E[i]∈Ｎ, agg(a, b) = max(a, b) とすると、任意の区間に含まれる自然数 E[i]
 * の最大値を取得することができる。
 * 
 * 例2)
 * E[i]∈ＮxＮ, agg(a, b) = (v1,v2) v1, v2 はE[i]の大きい順に2つ
 * とすると、任意の区間に含まれる自然数の組の要素のうち、大きいもの２つを
 * 取得することができる。
 * </pre>
 *
 * @param	<E>	保持する要素の型
 * @version		26 November, 2019
 * @author		Yusuke Sasaki
 */
public class SegmentTree<E> {
	
	/** セグメントツリーの有効なサイズ */
	private int size;
	
	/**
	 * セグメントツリーの全体サイズに関連する数。
	 * 葉の index は m-1 から開始する。
	 * size 以上の 2 のべきの形の最小の数。
	 */
	private int m;
	
	/** セグメントツリーの要素を保持する配列。サイズは 2m-1 */
	private E[] st;
	
	/** E x E → E となる演算 */
	private BinaryOperator<E> aggregator;
	
	/** E x I = E となる I */
	private E identity;
	
/*-------------
 * constructor
 */
	/**
	 * 指定されたサイズの要素を保持する SegmentTree を生成します。
	 * 単位元に関する簡易チェックが行われます。すわなち、
	 * aggregator.apply(identity, identity) が identity に等しくない場合、
	 * IllegalArgumentException がスローされます。
	 * 単位元は ∀A, agg(A, E) = agg(E, A) = A を満たす E であり、この
	 * チェックは必要条件に過ぎないことに注意してください。
	 * 初期値は identity となります。
	 *
	 * @param		size		セグメント木の大きさ
	 * @param		aggregator	2 つの E の要素から E の要素への演算
	 * @param		identity	演算 aggregator における単位元
	 */
	public SegmentTree(int size, BinaryOperator<E> aggregator, E identity) {
		init(size, aggregator, identity);
		// 葉の値を設定
		for (int i = 0; i < 2*m-1; i++)
			st[i] = identity;
	}
	
	/**
	 * 指定されたサイズの要素を保持する SegmentTree を生成します。
	 * 単位元に関する簡易チェックが行われます。すわなち、
	 * aggregator.apply(identity, identity) が identity に等しくない場合、
	 * IllegalArgumentException がスローされます。
	 * 単位元は ∀A, agg(A, E) = agg(E, A) = A を満たす E であり、この
	 * チェックは必要条件に過ぎないことに注意してください。
	 *
	 * @param		size		セグメント木の大きさ
	 * @param		aggregator	2 つの E の要素から E の要素への演算
	 * @param		identity	演算 aggregator における単位元
	 * @param		value		初期値を与える配列
	 */
	public SegmentTree(int size, BinaryOperator<E> aggregator, E identity,
						E[] value) {
		this(size, aggregator, identity, value, 0, value.length);
	}
	
	/**
	 * 指定されたサイズの要素を保持する SegmentTree を生成します。
	 * 単位元に関する簡易チェックが行われます。すわなち、
	 * aggregator.apply(identity, identity) が identity に等しくない場合、
	 * IllegalArgumentException がスローされます。
	 * 単位元は ∀A, agg(A, E) = agg(E, A) = A を満たす E であり、この
	 * チェックは必要条件に過ぎないことに注意してください。
	 *
	 * @param		size		セグメント木の大きさ
	 * @param		aggregator	2 つの E の要素から E の要素への演算
	 * @param		identity	演算 aggregator における単位元
	 * @param		value		与える初期値を含む配列
	 * @param		begin		初期値として利用する始点(含みます)
	 * @param		endExclusive	初期値として利用する終点(含みません)
	 */
	public SegmentTree(int size, BinaryOperator<E> aggregator, E identity,
						E[] value, int begin, int endExclusive) {
		init(size, aggregator, identity);
		construct(value, begin, endExclusive);
	}
	
	@SuppressWarnings("unchecked")
	private void init(int size, BinaryOperator<E> aggregator, E identity) {
		if (size <= 1 || size > 0x40000000)
			throw new IllegalArgumentException("bad size : "+size);
		if (!aggregator.apply(identity, identity).equals(identity))
			throw new IllegalArgumentException("bad identity");
		m = pow2form(size);
		st = (E[])new Object[2*m-1];
		this.aggregator = aggregator;
		this.identity = identity;
		this.size = size;
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
		construct(elements, 0, elements.length);
	}
	
	/**
	 * 配列でこのセグメント木を初期化します。
	 * update を繰り返し呼ぶより高速で、計算量は O(n) です。
	 *
	 * @param		elements		このセグメント木に設定する値
	 * @param		begin			elements における開始値(含みます)
	 * @param		endExclusive	elements における終了値(含みません)
	 */
	public void construct(E[] elements, int begin, int endExclusive) {
		int n = endExclusive - begin;
		if (n != size)
			throw new IllegalArgumentException("size mismatch");
		// 葉の値を設定
		for (int i = 0; i < n; i++)
			st[m-1+i] = elements[begin+i];
		for (int i = m-1+n; i < 2*m-1; i++)
			st[i] = identity;
		// 各親の値を更新
		for (int i = m-2; i >= 0; i--)
			st[i] = aggregator.apply(st[i*2+1], st[i*2+2]);
	}
	
	/**
	 * 与えられた正の int 値 n に対し、n 以上かつ 2 のべきの形の
	 * 最小の int 値を返却します。
	 *
	 * @param		n		対象となる int 値
	 * @return		2 のべきの数。n 以上で最小。
	 */
	private int pow2form(int n) {
		int m = n-1;
		m = m | (m >>> 1);
		m = m | (m >>> 2);
		m = m | (m >>> 4);
		m = m | (m >>> 8);
		m = m | (m >>> 16);
		return m+1;
	}
	
	/**
	 * 指定された index の要素を指定されたものに更新し、木の値を再計算します。
	 * 計算時間のオーダーは、O(log n) です。
	 *
	 * @param		index		追加するインデックス(0 以上 size 未満)
	 * @param		element		更新する要素
	 */
	public void update(int index, E element) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index is required that 0 <= index < "+size+", but was "+index);
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
	 * 与えられた区間に対する aggregator 演算結果を高速に取得します。
	 * 計算時間のオーダーは、O(log n) です。
	 *
	 * @param		s		区間の開始(含む)
	 * @param		eExclusive		区間の終了(含まない)
	 * @return		区間における aggregator の結果
	 */
	public E calculate(int s, int eExclusive) {
		if (s < 0 || eExclusive >= size)
			throw new IndexOutOfBoundsException("wrong index");
		if (s >= eExclusive)
			throw new IllegalArgumentException("s must be smaller than eExclusive");
		return calcImpl(s, eExclusive, 0, 0, m);
	}
	
	/**
	 * 与えられた区間での演算結果を返却する。
	 *
	 * @param		s		区間の開始(含む)
	 * @param		eExclusive		区間の終了(含まない)
	 * @param		n		セグメントのインデックス
	 * @param		l		セグメントの開始番号(含む)
	 * @param		r		セグメントの終了番号(含まない)
	 */
	private E calcImpl(int s, int eExclusive, int n, int l, int r) {
		// 共通部分がない場合
		if (r <= s || eExclusive <= l) return identity;
		// 完全に含んでいる場合
		if (s <= l && r <= eExclusive) return st[n];
		// 一部共通している場合
		int i = (l>>>1)+(r>>>1);
		E cl = calcImpl(s, eExclusive, 2*n + 1, l, i);
		E cr = calcImpl(s, eExclusive, 2*n + 2, i, r);
		return aggregator.apply(cl, cr);
	}
}
