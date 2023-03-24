import java.util.function.BinaryOperator;

/**
 * 実利用向けセグメントツリー class<br>
 * 型は E としているが、半群の積や e は実装が必要。
 * Exception 処理は省略。
 *
 * @version		15 August, 2022
 * @author		Yusuke Sasaki
 */
public class SegmentTreeLight<E> {
	
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
	 */
	public SegmentTree(int size) {
		init(size);
		// 葉の値を設定
		for (int i = 0; i < 2*m-1; i++)
			st[i] = identity; // identity 相当の値を入れる
	}
	
	/**
	 * 指定されたサイズの要素を保持する SegmentTree を生成します。
	 * 単位元に関する簡易チェックが行われます。すわなち、
	 * aggregator.apply(identity, identity) が identity に等しくない場合、
	 * IllegalArgumentException がスローされます。
	 * 単位元は ∀A, agg(A, E) = agg(E, A) = A を満たす E であり、この
	 * チェックは必要条件に過ぎないことに注意してください。
	 *
	 * @param		value		与える初期値を含む配列
	 */
	public SegmentTree(E[] value) {
		init(value.length);
		construct(value);
	}
	
	@SuppressWarnings("unchecked")
	private void init(int size) {
		this.size = size;
		m = (size == 1)? 1 : Integer.highestOneBit(size - 1) << 1;
		st = (E[])new Object[2*m-1];
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
		// 葉の値を設定
		int n = elements.length;
		for (int i = 0; i < n; i++)
			st[m-1+i] = elements[i];
		for (int i = m-1+n; i < 2*m-1; i++)
			st[i] = identity;
		// 各親の値を更新
		for (int i = m-2; i >= 0; i--)
			st[i] = aggregator.apply(st[i*2+1], st[i*2+2]);
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
	 * 与えられた区間に対する aggregator 演算結果を高速に取得します。
	 * 計算時間のオーダーは、O(log n) です。
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
