import java.util.function.BinaryOperator;
import java.util.function.BiFunction;
import java.util.Arrays;

/**
 * �����p�����x���]���Z�O�����g�c���[ class<br>
 * ��Ԃɑ΂��鉉�Z�������ɍs���邱�Ƃɉ����A
 * ��Ԃɑ΂���(���Z�����̂悤��)���Z�� O(log n)�Ŏ��s�\�B
 * �ʏ�̃Z�O�����g�؂��萔�{�x���B
 * �^�� E �Ƃ��Ă��邪�A���Q�̐�(aggregator: E x E -> E)�� IDENTITY �͎w�肪�K�v�B
 * �Y������ 0-indexed�B
 * Exception �����͏ȗ��B
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
	
	
	/** �Z�O�����g�c���[�̗L���ȃT�C�Y */
	private int size;
	
	/**
	 * size �ȏ�� 2 �ׂ̂��̌`�̍ŏ��̐��B
	 * �Z�O�����g�c���[�̑S�̃T�C�Y�� 2m-1 �B
	 * �t�� index �� m-1 ����J�n����B
	 */
	private int m;
	
	/** �Z�O�����g�c���[�̗v�f��ێ�����z�񂨂�ђx���]�����e�B�T�C�Y�� 2m-1 */
	private E[] st, lazy;
	
/*-------------
 * constructor
 */
	/**
	 * �w�肳�ꂽ�T�C�Y�̗v�f��ێ�����x���]���Z�O�����g�� LazySegtree �𐶐����܂��B
	 *
	 * @param		size		�Z�O�����g�؂̑傫��
	 */
	public LazySegtreeLight(int size) {
		init(size);
		// �t�̒l��ݒ�
		for (int i = 0; i < 2*m-1; i++)
			st[i] = IDENTITY; // identity �����̒l������
	}
	
	/**
	 * �w�肳�ꂽ�T�C�Y�̗v�f��ێ�����x���]���Z�O�����g�� LazySegtree �𐶐����܂��B
	 *
	 * @param		value		�^���鏉���l���܂ޔz��
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
	 * �z��ł��̃Z�O�����g�؂����������܂��B
	 * update ���J��Ԃ��ĂԂ�荂���ŁA�v�Z�ʂ� O(n) �ł��B
	 *
	 * @param		elements		���̃Z�O�����g�؂ɐݒ肷��l
	 */
	public void construct(E[] elements) {
		int n = elements.length;
		// �t�̒l��ݒ�
		for (int i = 0; i < n; i++)
			st[m-1+i] = elements[i];
		for (int i = m-1+n; i < 2*m-1; i++)
			st[i] = IDENTITY;
		// �e�e�̒l���X�V
		for (int i = m-2; i >= 0; i--)
			st[i] = aggregator.apply(st[i*2+1], st[i*2+2]);
	}

	/**
	 * �w��Z�O�����g�ɑ΂��A�x���I�y���[�V���������݂����炻���l��(�K�p)���A
	 * ���ڂ̎q�ɒx���I�y���[�V������`�d����B
	 * @param n
	 * @param l
	 * @param r
	 */
	private void eval(int n, int l, int r) {
    if(lazy[n].equals(IDENTITY)) return;
		st[n] = aggregator.apply(st[n], multiplier.apply(r-l, lazy[n]));

		if(n < m-1) {
			// �q������
			lazy[2*n+1] = lazy[n];
			lazy[2*n+2] = lazy[n];
		}
		lazy[n] = IDENTITY;
	}	
	/**
	 * �w�肳�ꂽ index �̗v�f���w�肳�ꂽ���̂ɍX�V���A�؂̒l���Čv�Z���܂��B
	 * �v�Z���Ԃ̃I�[�_�[�́AO(log n) �ł��B
	 *
	 * @param		index		�ǉ�����C���f�b�N�X(0 �ȏ� size ����)
	 * @param		element		�X�V����v�f
	 */
	public void update(int index, E element) {
		// M-1 �����[�Z�O�����g�̊J�n�ԍ�
		int i = m-1+index;
		st[i] = element;
		while (i > 0) {
			// �e�m�[�h�Ɉڍs
			i = (i-1) >>> 1;
			st[i] = aggregator.apply(st[i*2 + 1], st[i*2 + 2]);
		}
	}

	/**
	 * ��Ԃɑ΂����p ( aggregator( _, element) ) ��������(O(log n))�{���܂��B
	 * @param	s		��Ԃ̊J�n(�܂�)
	 * @param	eExclusive		��Ԃ̏I��(�܂܂Ȃ�)
	 * @param	element ��p aggregator( _, element) �̃p�����[�^�B
	 */
	public void operateSegment(int s, int eExclusive, E element) {
		operateSegmentImpl(s, eExclusive, element, 0, 0, m);
	}

	private void operateSegmentImpl(int s, int eExclusive, E element, int n, int l, int r) {
		eval(n, l, r);
    if (s <= l && r <= eExclusive) {
			// ���S�ɓ����̎�
			lazy[n] = element;
			eval(n, l, r);
    } else if (s < r && l < eExclusive) {
			// �ꕔ��Ԃ���鎞
			int i = (l>>>1)+(r>>>1);
			operateSegmentImpl(s, eExclusive, element, 2*n + 1, l, i);
			operateSegmentImpl(s, eExclusive, element, 2*n + 2, i, r);
			st[n] = aggregator.apply(st[2*n + 1], st[2*n + 2]);
		}
	}
	
	/**
	 * �^����ꂽ��Ԃɑ΂��� aggregator ���Z���ʂ�����( O(log n) )�Ɏ擾���܂��B
	 *
	 * @param		s		��Ԃ̊J�n(�܂�)
	 * @param		eExclusive		��Ԃ̏I��(�܂܂Ȃ�)
	 * @return		��Ԃɂ����� aggregator �̌���
	 */
	public E calculate(int s, int eExclusive) {
		return calcImpl(s, eExclusive, 0, 0, m);
	}
	
	/**
	 * �^����ꂽ��Ԃł̉��Z���ʂ�ԋp����B
	 *
	 * @param		s		���Z�Ώۋ�Ԃ̊J�n(�܂�)
	 * @param		eExclusive		���Z�Ώۋ�Ԃ̏I��(�܂܂Ȃ�)
	 * @param		n		�Z�O�����g�̃C���f�b�N�X
	 * @param		l		�Z�O�����g�̊J�n�ԍ�(�܂�)
	 * @param		r		�Z�O�����g�̏I���ԍ�(�܂܂Ȃ�)
	 */
	private E calcImpl(int s, int eExclusive, int n, int l, int r) {
		eval(n, l, r);
		// ���ʕ������Ȃ��ꍇ
		if (r <= s || eExclusive <= l) return IDENTITY;
		// ���S�Ɋ܂�ł���ꍇ
		if (s <= l && r <= eExclusive) return st[n];
		// �ꕔ���ʂ��Ă���ꍇ
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

	// 9 �v�f�A1,2,3,4,5,6,7,8,9 �̕����a���v�Z
	static void test1() {
		// �����A���Z
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

	// 10 �v�f�A1,2,3,4,5,6,7,8,9,10 �� operateSegment �� update ���ĕ����a�v�Z
	static void test2() {
		// �����A�ő�l
		int N = 10;
		LazySegtreeLight<Integer> lst = new LazySegtreeLight<>(N);
		// ���Z
		// lst.IDENTITY = 0; Arrays.fill(lst.st, 0);
		// lst.aggregator = (a, b) -> a+b; Arrays.fill(lst.lazy, 0);
		// lst.multiplier = (n, b) -> n*b;

		// ���łȂ������A�ő�
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
