package abdom.util;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * 汎用の(ただし各次元が同じ型)二次元 Binary Indexed Tree (BIT, Fenwick Tree)
 * class です。<br>
 *
 * そのまま適用も可能ですが、各問題に適用するためのリファレンス実装として
 * つくったものです。<br>
 * ある型の要素に対する二項演算が結合的であり、単位元をもつ(単位的半群)場合、
 * 直列した要素の始点(二次元のため矩形領域)からの区間に対する演算結果の
 * クエリを高速に実行可能です。
 * 一般に、要素数 n に対し、始点からの区間(矩形領域)に対して O((log n)^2) の
 * 計算量で結果を取得可能です。
 * また、BITの要素の変更は O((log n)^2) の計算量です。
 * 構築のための計算量 O(n^2) のメソッドを公開しています。
 * Segment Tree との差異は、区間が始点からという制約があること、および保持する
 * 要素数が n でメモリ効率がよい点です。
 * なお、E に対して逆元が定義される場合、始点開始制約はなくなり、任意区間に
 * 拡張されます。
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
public class BinaryIndexedTree2D<E> {
	
	/** BIT の有効なサイズ */
	private int size1, size2;
	
	/** 要素を保持する配列。サイズは size1, size2 */
	private E[][] st;
	
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
	 * @param		size1		BITの大きさ
	 * @param		size2		BITの大きさ
	 * @param		aggregator	2 つの E の要素から E の要素への演算
	 * @param		identity	演算 aggregator における単位元
	 */
	public BinaryIndexedTree2D(int size1, int size2,
								BinaryOperator<E> aggregator, E identity) {
		this(size1, size2, aggregator, identity, null);
	}
	
	/**
	 * 指定されたサイズの要素を保持する BIT を生成します。
	 * 単位元に関する簡易チェックが行われます。すわなち、
	 * aggregator.apply(identity, identity) が identity に等しくない場合、
	 * IllegalArgumentException がスローされます。
	 * 単位元は ∀A, agg(A, E) = agg(E, A) = A を満たす E であり、この
	 * チェックは部分的であることに注意してください。
	 *
	 * @param		size1		BITの大きさ1
	 * @param		size2		BITの大きさ2
	 * @param		aggregator	2 つの E の要素から E の要素への演算
	 * @param		identity	演算 aggregator における単位元
	 * @param		inverse		aggregator における逆元生成(null の場合もある)
	 * @see			#calculate(int, int)
	 */
	@SuppressWarnings("unchecked")
	public BinaryIndexedTree2D(int size1, int size2,
								BinaryOperator<E> aggregator,
								E identity,
								UnaryOperator<E> inverse) {
		if (size1 <= 1 || size1 > 0x40000000)
			throw new IllegalArgumentException("bad size1 : "+size1);
		if (size2 <= 1 || size2 > 0x40000000)
			throw new IllegalArgumentException("bad size2 : "+size2);
		if (!aggregator.apply(identity, identity).equals(identity))
			throw new IllegalArgumentException("bad identity");
		if (inverse != null &&
				!inverse.apply(identity).equals(identity))
			throw new IllegalArgumentException("bad inverse");
		st = (E[][])new Object[size1][size2];
		this.aggregator = aggregator;
		this.identity = identity;
		this.size1 = size1;
		this.size2 = size2;
		this.inverse = inverse;
		for (int i = 0; i < size1; i++)
			for (int j = 0; j < size2; j++) st[i][j] = identity;
	}

/*------------------
 * instance methods
 */
	/**
	 * 配列でこの BIT を初期化します。
	 * SegmentTree との互換性のため設定されたメソッドで、
	 * 処理内容は update を繰り返し適用します。
	 *
	 * @param		elements		この BIT に設定する値
	 */
	public void construct(E[][] elements) {
		construct(elements, 0, elements.length, 0, elements[0].length);
	}
	
	/**
	 * 配列でこの BIT を初期化します。
	 * SegmentTree との互換性のため設定されたメソッドで、
	 * 処理内容は update を繰り返し適用します。
	 *
	 * @param		elements		この BIT に設定する値
	 * @param		begin1			elements における開始値1(含みます)
	 * @param		end1Exclusive	elements における終了値1(含みません)
	 * @param		begin2			elements における開始値2(含みます)
	 * @param		end2Exclusive	elements における終了値2(含みません)
	 */
	public void construct(E[][] elements, int begin1, int end1Exclusive
									, int begin2, int end2Exclusive) {
		int n1 = end1Exclusive - begin1;
		if (n1 != size1)
			throw new IllegalArgumentException("size1 mismatch");
		int n2 = end2Exclusive - begin2;
		if (n2 != size2)
			throw new IllegalArgumentException("size2 mismatch");
		// 葉の値を設定
		
		for (int i = 0; i < size1; i++)
			for (int j = 0; j < size2; j++)
				update(i, j, elements[i+begin1][j+begin2]);
	}
	
	/**
	 * 指定された index の要素を指定されたものに更新し、木の値を再計算します。
	 *
	 * @param		index1		追加するインデックス1(0 以上 size 未満)
	 * @param		index2		追加するインデックス2(0 以上 size 未満)
	 * @param		element		更新する要素
	 */
	public void update(int index1, int index2, E element) {
		if (index1 < 0 || index1 >= size1)
			throw new IndexOutOfBoundsException("index1 must be 0 <= index1 < "
							+size1+", but was "+index1);
		if (index2 < 0 || index2 >= size2)
			throw new IndexOutOfBoundsException("index2 must be 0 <= index2 < "
							+size2+", but was "+index2);
		int i2 = index2;
		while (index1 < size1) {
			index2 = i2;
			while (index2 < size2) {
				st[index1][index2] = aggregator.apply(
										st[index1][index2], element);
				index2 += ((index2+1) & (-index2-1));
			}
			index1 += ((index1+1) & (-index1-1));
		}
	}
	
	/**
	 * 始点から与えられた終点までの区間に対する aggregator 演算結果を高速に
	 * 取得します。計算時間のオーダーは、O((log n)^2) です。
	 *
	 * @param		e1		区間の終点1(含まない)
	 * @param		e2		区間の終点2(含まない)
	 * @return		区間における aggregator の結果
	 */
	public E calculate(int e1, int e2) {
		if (e1 < 0 || e1 >= size1)
			throw new IndexOutOfBoundsException("wrong index1");
		if (e2 < 0 || e2 >= size2)
			throw new IndexOutOfBoundsException("wrong index2");
		E s = identity;
		e1--;
		int e2i = e2-1;
		while (e1 >= 0) {
			e2 = e2i;
			while (e2 >= 0) {
				s = aggregator.apply(s, st[e1][e2]);
				e2 -= ((e2+1) & (-e2-1));
			}
			e1 -= ((e1+1) & (-e1-1));
		}
		return s;
	}
	
	/**
	 * 与えられた区間に対する aggregator 演算結果を高速に取得します。
	 * 計算時間のオーダーは、O((log n)^2) です。
	 * このメソッドを利用するには、コンストラクタで inverse を設定する
	 * 必要があります。設定されていない場合、
	 * UnsupportedOperationException がスローされます。
	 *
	 * @param		s1		区間の開始1(含む)
	 * @param		e1		区間の終点1(含まない)
	 * @param		s2		区間の開始2(含む)
	 * @param		e2		区間の終点2(含まない)
	 * @return		区間における aggregator の結果
	 * @throws		java.lang.UnsupportedOperationException	inverseが未定義
	 * @see			#BinaryIndexedTree2D(int, int, BinaryOperator, Object,
	 *					UnaryOperator)
	 */
	public E calculate(int s1, int e1, int s2, int e2) {
		if (inverse == null)
			throw new UnsupportedOperationException("inverse is not defined");
		if (s1 >= e1)
			throw new IllegalArgumentException("s1 must be smaller than e1");
		if (s2 >= e2)
			throw new IllegalArgumentException("s2 must be smaller than e2");
		
		//  a | b  D = a+b+c+d
		// ---+--- C = a+c
		//  c | d  B = a+b
		//         A = a とする(A～Dは calculate(int,int) で計算できる領域)
		//
		// d = D-C-B+A
		E D = calculate(e1, e2);
		E C = calculate(s1, e2);
		E B = calculate(e1, s2);
		E A = calculate(s1, s2);
				
		return aggregator.apply(
					aggregator.apply(
						aggregator.apply(D, inverse.apply(C)),
						inverse.apply(B)
					),
					A
				);
	}
	
}
