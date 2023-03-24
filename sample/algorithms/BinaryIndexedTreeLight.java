/**
 * BIT を long 最小値計算に制限した実装。逆演算なし。
 * 0-indexed
 *
 * @version		5 February, 2022
 * @author		Yusuke Sasaki
 */
public class BinaryIndexedTreeLight {
	
	/** BIT の有効なサイズ */
	private int size;
	
	/** 要素を保持する配列。サイズは size */
	private long[] st;
	
	/** E x I = E となる I */
	private static final long ID = Long.MAX_VALUE / 2;
	
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
	 * @param		inverse		aggregator における逆元生成(null の場合もある)
	 * @see			#calculate(int, int)
	 */
	public BinaryIndexedTreeLight(int size) {
		st = new long[size];
		this.size = size;
		Arrays.fill(st, ID);
	}

/*------------------
 * instance methods
 */
	/**
	 * 指定された index の要素を指定されたものに更新し、木の値を再計算します。
	 *
	 * @param		index		追加するインデックス(0 以上 size 未満)
	 * @param		element		更新する要素
	 */
	public void update(int index, long element) {
		index++;
		while (index <= size) {
			// ★apply
			st[index-1] = Math.min(st[index-1], element);
			index += index & -index;
		}
	}
	
	/**
	 * 始点から与えられた終点までの区間に対する aggregator 演算結果を高速に
	 * 取得します。計算時間のオーダーは、O(log n) です。
	 *
	 * @param		e		区間の終点(含む)
	 * @return		区間における aggregator の結果
	 */
	public long calculate(int e) {
		if (e < 0) return ID;
		if (e >= size) e = size - 1;
		long s = ID;
		e++;
		while (e > 0) {
			// ★apply
			s = Math.min(s, st[e-1]);
			e -= e & -e;
		}
		return s;
	}
	
}

/**
 * 和演算、初期値０に特化した BIT です。
 * 1-index であることに注意してください。
 */
class BIT {
	private int size;
	private int[] st;
	
	BIT(int size) {
		st = new int[size];
		this.size = size;
	}

	void clear() {
		Arrays.fill(st, 0);
	}

	void update(int index, int value) {
		while (index <= size) {
			st[index-1] += value;
			index += index & -index;
		}
	}
	
	/**
	 * 始点から与えられた終点までの区間に対する和を高速に
	 * 取得します。計算時間のオーダーは、O(log n) です。
	 *
	 * @param		e		区間の終点(含む)
	 * @return		区間における和
	 */
	long calculate(int e) {
		if (e < 1) return 0;
		if (e > size) e = size;
		long s = 0;
		while (e > 0) {
			s += st[e-1];
			e -= e & -e;
		}
		return s;
	}
	
	/**
	 * 与えられた区間に対する和を高速に取得します。
	 * 計算時間のオーダーは、O(log n) です。
	 * このメソッドを利用するには、コンストラクタで inverse を設定する
	 * 必要があります。設定されていない場合、
	 * UnsupportedOperationException がスローされます。
	 *
	 * @param		s		区間の開始(含む)
	 * @param		e		区間の終点(含む)
	 * @return		区間における和
	 */
	long calculate(int s, int e) {
		return calculate(e) - calculate(s-1);
	}
}
