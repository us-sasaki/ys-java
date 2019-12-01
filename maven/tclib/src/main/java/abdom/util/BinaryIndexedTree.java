package abdom.util;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * 汎用の Binary Indexed Tree (BIT, Fenwick Tree) class です。<br>
 *
 * ある型の要素に対する二項演算が結合的であり、単位元をもつ(単位的半群)場合、
 * 直列した要素の始点からの区間に対する演算結果のクエリを高速に実行可能です。
 * 一般に、要素数 n に対し、始点からの区間に対して O(log n) の計算量で結果を
 * 取得可能です。また、BITの要素の変更は O(log n) の計算量です。
 * 構築のための計算量 O(n) のメソッドを公開しています。
 * Segment Tree との差異は、保持する要素数が n でメモリ効率がよい点です。
 * E に対して逆元が定義される場合、任意区間に拡張されます。
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
 * E[i]∈Ｎ, agg(a, b) = max(a, b) とすると、区間に含まれる自然数 E[i]
 * の最大値を取得することができる。
 * 
 * 例2)
 * E[i]∈ＮxＮ, agg(a, b) = (v1,v2) v1, v2 はE[i]の大きい順に2つ
 * とすると、区間に含まれる自然数の組の要素のうち、大きいもの２つを
 * 取得することができる。
 * </pre>
 *
 * @param	<E>	保持する要素の型
 * @version		1 December, 2019
 * @author		Yusuke Sasaki
 */
public class BinaryIndexedTree<E> {
	
	/** BIT の有効なサイズ */
	private int size;
	
	/** 要素を保持する配列。サイズは size */
	private E[] st;
	
	/** E x E → E となる演算 */
	private BinaryOperator<E> aggregator;
	
	/** E x I = E となる I */
	private E identity;
	
	/**
	 * E → E で、逆元を生成する演算。
	 * null でもよいが任意区間での演算時に必要。
	 */
	private UnaryOperator<E> inverse;
	
/*-------------
 * constructor
 */
	/**
	 * 指定されたサイズの要素を保持する BIT を生成します。
	 * 単位元に関する簡易チェックが行われます。すわなち、
	 * aggregator.apply(identity, identity) が identity に等しくない場合、
	 * IllegalArgumentException がスローされます。
	 * 単位元は ∀A, agg(A, E) = agg(E, A) = A を満たす E であり、この
	 * チェックは部分的であることに注意してください。
	 *
	 * @param		size		BITの大きさ
	 * @param		aggregator	2 つの E の要素から E の要素への演算
	 * @param		identity	演算 aggregator における単位元
	 */
	public BinaryIndexedTree(int size,
								BinaryOperator<E> aggregator, E identity) {
		this(size, aggregator, identity, null);
	}
	
	/**
	 * 指定されたサイズの要素を保持する BIT を生成します。
	 * 単位元に関する簡易チェックが行われます。すわなち、
	 * aggregator.apply(identity, identity) が identity に等しくない場合、
	 * IllegalArgumentException がスローされます。
	 * 単位元は ∀A, agg(A, E) = agg(E, A) = A を満たす E であり、この
	 * チェックは部分的であることに注意してください。
	 *
	 * @param		size		BITの大きさ
	 * @param		aggregator	2 つの E の要素から E の要素への演算
	 * @param		identity	演算 aggregator における単位元
	 * @param		inverse		aggregator における逆元生成(null の場合もある)
	 * @see			#calculate(int, int)
	 */
	@SuppressWarnings("unchecked")
	public BinaryIndexedTree(int size,
								BinaryOperator<E> aggregator,
								E identity,
								UnaryOperator<E> inverse) {
		if (size <= 1 || size > 0x40000000)
			throw new IllegalArgumentException("bad size : "+size);
		if (!aggregator.apply(identity, identity).equals(identity))
			throw new IllegalArgumentException("bad identity");
		if (inverse != null &&
				!inverse.apply(identity).equals(identity))
			throw new IllegalArgumentException("bad inverse");
		st = (E[])new Object[size];
		this.aggregator = aggregator;
		this.identity = identity;
		this.size = size;
		this.inverse = inverse;
		for (int i = 0; i < size; i++) st[i] = identity;
	}

/*------------------
 * instance methods
 */
	/**
	 * 配列でこのセグメント木を初期化します。
	 * SegmentTree との互換性のため設定されたメソッドで、
	 * 処理内容は update を繰り返し適用します。
	 *
	 * @param		elements		このセグメント木に設定する値
	 */
	public void construct(E[] elements) {
		construct(elements, 0, elements.length);
	}
	
	/**
	 * 配列でこのセグメント木を初期化します。
	 * SegmentTree との互換性のため設定されたメソッドで、
	 * 処理内容は update を繰り返し適用します。
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
		
		for (int i = 0; i < size; i++)
			update(i, elements[i+begin]);
	}
	
	/**
	 * 指定された index の要素を指定されたものに更新し、木の値を再計算します。
	 *
	 * @param		index		追加するインデックス(0 以上 size 未満)
	 * @param		element		更新する要素
	 */
	public void update(int index, E element) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index is required that 0 <= index < "+size+", but was "+index);
		while (index < size) {
			st[index] = aggregator.apply(st[index], element);
			index += ((index+1) & (-index-1));
		}
	}
	
	/**
	 * 始点から与えられた終点までの区間に対する aggregator 演算結果を高速に
	 * 取得します。計算時間のオーダーは、O(log size) です。
	 *
	 * @param		e		区間の終点(含まない)
	 * @return		区間における aggregator の結果
	 */
	public E calculate(int e) {
		if (e < 0 || e >= size)
			throw new IndexOutOfBoundsException("wrong index");
		E s = identity;
		e--;
		while (e >= 0) {
			s = aggregator.apply(s, st[e]);
			e -= ((e+1) & (-e-1));
		}
		return s;
	}
	
	/**
	 * 与えられた区間に対する aggregator 演算結果を高速に取得します。
	 * 計算時間のオーダーは、O(log size) です。
	 * このメソッドを利用するには、コンストラクタで inverse を設定する
	 * 必要があります。設定されていない場合、
	 * UnsupportedOperationException がスローされます。
	 *
	 * @param		s		区間の開始(含む)
	 * @param		e		区間の終点(含まない)
	 * @return		区間における aggregator の結果
	 * @throws		java.lang.UnsupportedOperationException	inverseが未定義
	 * @see			#BinaryIndexedTree(int, BinaryOperator, Object, UnaryOperator)
	 */
	public E calculate(int s, int e) {
		if (inverse == null)
			throw new UnsupportedOperationException("inverse is not defined");
		if (s >= e)
			throw new IllegalArgumentException("s must be smaller than e");
		if (s == 0) return calculate(e);

		E a = calculate(e);
		E b = calculate(s);
		return aggregator.apply(a, inverse.apply(b));
	}
	
}
