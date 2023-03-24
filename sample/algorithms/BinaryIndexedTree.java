import java.util.*;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * �ėp�� Binary Indexed Tree (BIT, Fenwick Tree) class �ł��B<br>
 *
 * ����^�̗v�f�ɑ΂���񍀉��Z�������I�ł���A�P�ʌ�������(�P�ʓI���Q)�ꍇ�A
 * ���񂵂��v�f�̎n�_����̋�Ԃɑ΂��鉉�Z���ʂ̃N�G���������Ɏ��s�\�ł��B
 * ��ʂɁA�v�f�� n �ɑ΂��A�n�_����̋�Ԃɑ΂��� O(log n) �̌v�Z�ʂŌ��ʂ�
 * �擾�\�ł��B�܂��ABIT�̗v�f�̕ύX�� O(log n) �̌v�Z�ʂł��B
 * �\�z�̂��߂̌v�Z�� O(n) �̃��\�b�h�����J���Ă��܂��B
 * Segment Tree �Ƃ̍��ق́A��Ԃ��n�_����Ƃ������񂪂��邱�ƁA����ѕێ�����
 * �v�f���� n �Ń������������悢�_�ł��B
 * E �ɑ΂��ċt������`�����ꍇ�A�n�_�J�n����͂Ȃ��Ȃ�A�C�Ӌ�Ԃ�
 * �g������܂��B
 *
 * <pre>
 * ���Z agg : E x E �� E �������I�ł���A�Ƃ́A
 *
 *    agg(agg(a, b), c) = agg(a, agg(b, c))
 *
 * �ƂȂ邱�ƁB
 * �܂��A��� [a,b) = { a, a+1, a+2, ... , b-2, b-1 } �ɑ΂��� agg ��
 * ���Z���� R �Ƃ́A
 *
 *    R([a,b)) = agg(E[a], agg(E[a+1], agg(E[a+2], ... agg(E[b-2], E[b-1]))..))
 *
 * �ƒ�`����B
 * 
 * ��1)
 * E[i]���m, agg(a, b) = max(a, b) �Ƃ���ƁA��ԂɊ܂܂�鎩�R�� E[i]
 * �̍ő�l���擾���邱�Ƃ��ł���B
 * 
 * ��2)
 * E[i]���mx�m, agg(a, b) = (v1,v2) v1, v2 ��E[i]�̑傫������2��
 * �Ƃ���ƁA��ԂɊ܂܂�鎩�R���̑g�̗v�f�̂����A�傫�����̂Q��
 * �擾���邱�Ƃ��ł���B
 * </pre>
 *
 * @param	<E>	�ێ�����v�f�̌^
 * @version		1 December, 2019
 * @author		Yusuke Sasaki
 */
public class BinaryIndexedTree<E> {
	
	/** BIT �̗L���ȃT�C�Y */
	private int size;
	
	/** �v�f��ێ�����z��B�T�C�Y�� size */
	private E[] st;
	
	/** E x E �� E �ƂȂ鉉�Z */
	private BinaryOperator<E> aggregator;
	
	/** E x I = E �ƂȂ� I */
	private E identity;
	
	/**
	 * E �� E �ŁA�t���𐶐����鉉�Z�B
	 * null �ł��悢���C�Ӌ�Ԃł̉��Z���ɕK�v�B
	 */
	private UnaryOperator<E> inverse;
	
/*-------------
 * constructor
 */
	/**
	 * �w�肳�ꂽ�T�C�Y�̗v�f��ێ����� BIT �𐶐����܂��B
	 * �P�ʌ��Ɋւ���ȈՃ`�F�b�N���s���܂��B����Ȃ��A
	 * aggregator.apply(identity, identity) �� identity �ɓ������Ȃ��ꍇ�A
	 * IllegalArgumentException ���X���[����܂��B
	 * �P�ʌ��� ��A, agg(A, E) = agg(E, A) = A �𖞂��� E �ł���A����
	 * �`�F�b�N�͕����I�ł��邱�Ƃɒ��ӂ��Ă��������B
	 *
	 * @param		size		BIT�̑傫��
	 * @param		aggregator	2 �� E �̗v�f���� E �̗v�f�ւ̉��Z
	 * @param		identity	���Z aggregator �ɂ�����P�ʌ�
	 */
	public BinaryIndexedTree(int size,
								BinaryOperator<E> aggregator, E identity) {
		this(size, aggregator, identity, null);
	}
	
	/**
	 * �w�肳�ꂽ�T�C�Y�̗v�f��ێ����� BIT �𐶐����܂��B
	 * �P�ʌ��Ɋւ���ȈՃ`�F�b�N���s���܂��B����Ȃ��A
	 * aggregator.apply(identity, identity) �� identity �ɓ������Ȃ��ꍇ�A
	 * IllegalArgumentException ���X���[����܂��B
	 * �P�ʌ��� ��A, agg(A, E) = agg(E, A) = A �𖞂��� E �ł���A����
	 * �`�F�b�N�͕����I�ł��邱�Ƃɒ��ӂ��Ă��������B
	 *
	 * @param		size		BIT�̑傫��
	 * @param		aggregator	2 �� E �̗v�f���� E �̗v�f�ւ̉��Z
	 * @param		identity	���Z aggregator �ɂ�����P�ʌ�
	 * @param		inverse		aggregator �ɂ�����t������(null �̏ꍇ������)
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
	 * �z��ł��̃Z�O�����g�؂����������܂��B
	 * SegmentTree �Ƃ̌݊����̂��ߐݒ肳�ꂽ���\�b�h�ŁA
	 * �������e�� update ���J��Ԃ��K�p���܂��B
	 *
	 * @param		elements		���̃Z�O�����g�؂ɐݒ肷��l
	 */
	public void construct(E[] elements) {
		construct(elements, 0, elements.length);
	}
	
	/**
	 * �z��ł��̃Z�O�����g�؂����������܂��B
	 * SegmentTree �Ƃ̌݊����̂��ߐݒ肳�ꂽ���\�b�h�ŁA
	 * �������e�� update ���J��Ԃ��K�p���܂��B
	 *
	 * @param		elements		���̃Z�O�����g�؂ɐݒ肷��l
	 * @param		begin			elements �ɂ�����J�n�l(�܂݂܂�)
	 * @param		endExclusive	elements �ɂ�����I���l(�܂݂܂���)
	 */
	public void construct(E[] elements, int begin, int endExclusive) {
		int n = endExclusive - begin;
		if (n != size)
			throw new IllegalArgumentException("size mismatch");
		// �t�̒l��ݒ�
		
		for (int i = 0; i < size; i++)
			update(i, elements[i+begin]);
	}
	
	/**
	 * �w�肳�ꂽ index �̗v�f���w�肳�ꂽ���̂ɍX�V���A�؂̒l���Čv�Z���܂��B
	 *
	 * @param		index		�ǉ�����C���f�b�N�X(0 �ȏ� size ����)
	 * @param		element		�X�V����v�f
	 */
	public void update(int index, E element) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index is required that 0 <= index < "+size+", but was "+index);
		index++;
		while (index <= size) {
			st[index-1] = aggregator.apply(st[index-1], element);
			index += index & -index;
		}
	}
	
	/**
	 * �n�_����^����ꂽ�I�_�܂ł̋�Ԃɑ΂��� aggregator ���Z���ʂ�������
	 * �擾���܂��B�v�Z���Ԃ̃I�[�_�[�́AO(log n) �ł��B
	 *
	 * @param		e		��Ԃ̏I�_(�܂�)
	 * @return		��Ԃɂ����� aggregator �̌���
	 */
	public E calculate(int e) {
		if (e < 0) return identity;
		if (e >= size) e = size - 1;
		E s = identity;
		e++;
		while (e > 0) {
			s = aggregator.apply(s, st[e-1]);
			e -= e & -e;
		}
		return s;
	}
	
	/**
	 * �^����ꂽ��Ԃɑ΂��� aggregator ���Z���ʂ������Ɏ擾���܂��B
	 * �v�Z���Ԃ̃I�[�_�[�́AO(log n) �ł��B
	 * ���̃��\�b�h�𗘗p����ɂ́A�R���X�g���N�^�� inverse ��ݒ肷��
	 * �K�v������܂��B�ݒ肳��Ă��Ȃ��ꍇ�A
	 * UnsupportedOperationException ���X���[����܂��B
	 *
	 * @param		s		��Ԃ̊J�n(�܂�)
	 * @param		e		��Ԃ̏I�_(�܂܂Ȃ�)
	 * @return		��Ԃɂ����� aggregator �̌���
	 * @throws		java.lang.UnsupportedOperationException	inverse������`
	 * @see			#BinaryIndexedTree(int, BinaryOperator, Object, UnaryOperator)
	 */
	public E calculate(int s, int e) {
		if (inverse == null)
			throw new UnsupportedOperationException("inverse is not defined");
		if (s >= e)
			throw new IllegalArgumentException("s must be smaller than e");
		if (s < 0) return calculate(e);

		E a = calculate(e);
		E b = calculate(s);
		return aggregator.apply(a, inverse.apply(b));
	}
	
}

/**
 * �a���Z�A�����l�O�ɓ������� BIT �ł��B
 * index �� 1�` �ł��邱�Ƃɒ��ӂ��Ă��������B
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
	 * �n�_����^����ꂽ�I�_�܂ł̋�Ԃɑ΂���a��������
	 * �擾���܂��B�v�Z���Ԃ̃I�[�_�[�́AO(log n) �ł��B
	 *
	 * @param		e		��Ԃ̏I�_(�܂�)
	 * @return		��Ԃɂ�����a
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
	 * �^����ꂽ��Ԃɑ΂���a�������Ɏ擾���܂��B
	 * �v�Z���Ԃ̃I�[�_�[�́AO(log n) �ł��B
	 * ���̃��\�b�h�𗘗p����ɂ́A�R���X�g���N�^�� inverse ��ݒ肷��
	 * �K�v������܂��B�ݒ肳��Ă��Ȃ��ꍇ�A
	 * UnsupportedOperationException ���X���[����܂��B
	 *
	 * @param		s		��Ԃ̊J�n(�܂�)
	 * @param		e		��Ԃ̏I�_(�܂�)
	 * @return		��Ԃɂ�����a
	 */
	long calculate(int s, int e) {
		return calculate(e) - calculate(s-1);
	}
	
	public static void main(String[] args) {
		// test1();
		test2();
	}
	
	static void test1() {
		int N = 1;
		List<Integer> arr = new ArrayList<Integer>();
		for (int i = 0; i < 300000; i++) arr.add(i);
		for (int i = 0; i < N; i++) {
			Collections.shuffle(arr, new Random(i));
			BIT bit = new BIT(arr.size());
			for (int j = 0; j < 1000; j++) {
				bit.update(arr.get(j), 1);
				int ans = bit.calculate(j);
				
				int ans2 = 0;
				for (int k = 0; k <= j; k++) {
					if (arr.get(k) < j) ans2++;
				}
				System.out.print("j="+j+" ans="+ans+" ans2="+ans2);
				if (ans != ans2) System.out.println("error");
				else System.out.println();
			}
		}
	}
	
	static void test2() {
		BIT bit = new BIT(300000);
		bit.update(0, 1);
		bit.update(10, 1);
		bit.update(11, 1);
		
		for (int i = 0; i < 12; i++) {
			System.out.printf("calc(%d)=%d\n", i, bit.calculate(i));
		}
	}
	
}
