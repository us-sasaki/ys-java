import java.util.function.BinaryOperator;

/**
 * �ėp�̃Z�O�����g�c���[ class<br>
 *
 * ����^�̗v�f�ɑ΂���񍀉��Z�������I�ł���A�P�ʌ�������(�P�ʓI���Q)�ꍇ�A
 * ���񂵂��v�f�̋�Ԃɑ΂��鉉�Z���ʂ̃N�G���������Ɏ��s�\�ł��B
 * ��ʂɁA�v�f�� n �ɑ΂��A�C�ӂ̋�Ԃɑ΂��� O(log n) �̌v�Z�ʂŌ��ʂ�
 * �擾�\�ł��B�܂��A�Z�O�����g�؂̗v�f�̕ύX�� O(log n) �̌v�Z�ʂł��B
 * �\�z�̂��߂̌v�Z�� O(n) �̃��\�b�h��L���Ă��܂��B
 * �Ȃ��Asize �� 2 �ȏ�ł���K�v������܂��B
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
 * E[i]���m, agg(a, b) = max(a, b) �Ƃ���ƁA�C�ӂ̋�ԂɊ܂܂�鎩�R�� E[i]
 * �̍ő�l���擾���邱�Ƃ��ł���B
 * 
 * ��2)
 * E[i]���mx�m, agg(a, b) = (v1,v2) v1, v2 ��E[i]�̑傫������2��
 * �Ƃ���ƁA�C�ӂ̋�ԂɊ܂܂�鎩�R���̑g�̗v�f�̂����A�傫�����̂Q��
 * �擾���邱�Ƃ��ł���B
 * </pre>
 *
 * @param	<E>	�ێ�����v�f�̌^
 * @version		26 November, 2019
 * @author		Yusuke Sasaki
 */
public class SegmentTree<E> {
	
	/** �Z�O�����g�c���[�̗L���ȃT�C�Y */
	private int size;
	
	/**
	 * �Z�O�����g�c���[�̑S�̃T�C�Y�Ɋ֘A���鐔�B
	 * �t�� index �� m-1 ����J�n����B
	 * size �ȏ�� 2 �ׂ̂��̌`�̍ŏ��̐��B
	 */
	private int m;
	
	/** �Z�O�����g�c���[�̗v�f��ێ�����z��B�T�C�Y�� 2m-1 */
	private E[] st;
	
	/** E x E �� E �ƂȂ鉉�Z */
	private BinaryOperator<E> aggregator;
	
	/** E x I = E �ƂȂ� I */
	private E identity;
	
/*-------------
 * constructor
 */
	/**
	 * �w�肳�ꂽ�T�C�Y�̗v�f��ێ����� SegmentTree �𐶐����܂��B
	 * �P�ʌ��Ɋւ���ȈՃ`�F�b�N���s���܂��B����Ȃ��A
	 * aggregator.apply(identity, identity) �� identity �ɓ������Ȃ��ꍇ�A
	 * IllegalArgumentException ���X���[����܂��B
	 * �P�ʌ��� ��A, agg(A, E) = agg(E, A) = A �𖞂��� E �ł���A����
	 * �`�F�b�N�͕K�v�����ɉ߂��Ȃ����Ƃɒ��ӂ��Ă��������B
	 * �����l�� identity �ƂȂ�܂��B
	 *
	 * @param		size		�Z�O�����g�؂̑傫��
	 * @param		aggregator	2 �� E �̗v�f���� E �̗v�f�ւ̉��Z
	 * @param		identity	���Z aggregator �ɂ�����P�ʌ�
	 */
	public SegmentTree(int size, BinaryOperator<E> aggregator, E identity) {
		init(size, aggregator, identity);
		// �t�̒l��ݒ�
		for (int i = 0; i < 2*m-1; i++)
			st[i] = identity;
	}
	
	/**
	 * �w�肳�ꂽ�v�f��ێ����� SegmentTree �𐶐����܂��B
	 * �P�ʌ��Ɋւ���ȈՃ`�F�b�N���s���܂��B����Ȃ��A
	 * aggregator.apply(identity, identity) �� identity �ɓ������Ȃ��ꍇ�A
	 * IllegalArgumentException ���X���[����܂��B
	 * �P�ʌ��� ��A, agg(A, E) = agg(E, A) = A �𖞂��� E �ł���A����
	 * �`�F�b�N�͕K�v�����ɉ߂��Ȃ����Ƃɒ��ӂ��Ă��������B
	 *
	 * @param		aggregator	2 �� E �̗v�f���� E �̗v�f�ւ̉��Z
	 * @param		identity	���Z aggregator �ɂ�����P�ʌ�
	 * @param		value		�����l��^����z��
	 */
	public SegmentTree(BinaryOperator<E> aggregator, E identity,
						E[] value) {
		this(value.length, aggregator, identity, value, 0, value.length);
	}
	
	/**
	 * �w�肳�ꂽ�T�C�Y�̗v�f��ێ����� SegmentTree �𐶐����܂��B
	 * �P�ʌ��Ɋւ���ȈՃ`�F�b�N���s���܂��B����Ȃ��A
	 * aggregator.apply(identity, identity) �� identity �ɓ������Ȃ��ꍇ�A
	 * IllegalArgumentException ���X���[����܂��B
	 * �P�ʌ��� ��A, agg(A, E) = agg(E, A) = A �𖞂��� E �ł���A����
	 * �`�F�b�N�͕K�v�����ɉ߂��Ȃ����Ƃɒ��ӂ��Ă��������B
	 *
	 * @param		size		�Z�O�����g�؂̑傫��(2 �ȏ�AMAX_VALUE/2 �ȉ�)
	 * @param		aggregator	2 �� E �̗v�f���� E �̗v�f�ւ̉��Z
	 * @param		identity	���Z aggregator �ɂ�����P�ʌ�
	 * @param		value		�^���鏉���l���܂ޔz��
	 * @param		begin		�����l�Ƃ��ė��p����n�_(�܂݂܂�)
	 * @param		endExclusive	�����l�Ƃ��ė��p����I�_(�܂݂܂���)
	 */
	public SegmentTree(BinaryOperator<E> aggregator, E identity,
						E[] value, int begin, int endExclusive) {
		init(endExclusive - begin, aggregator, identity);
		construct(value, begin, endExclusive);
	}
	
	@SuppressWarnings("unchecked")
	private void init(int size, BinaryOperator<E> aggregator, E identity) {
		this.size = size;
		if (size <= 0 || size > 0x40000000)
			throw new IllegalArgumentException("bad size : "+size);
		if (!aggregator.apply(identity, identity).equals(identity))
			throw new IllegalArgumentException("bad identity");
		m = (size == 1)? 1 : Integer.highestOneBit(size - 1) << 1;
		st = (E[])new Object[2*m-1];
		this.aggregator = aggregator;
		this.identity = identity;
		this.size = size;
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
		construct(elements, 0, elements.length);
	}
	
	/**
	 * �z��ł��̃Z�O�����g�؂����������܂��B
	 * update ���J��Ԃ��ĂԂ�荂���ŁA�v�Z�ʂ� O(n) �ł��B
	 *
	 * @param		elements		���̃Z�O�����g�؂ɐݒ肷��l
	 * @param		begin			elements �ɂ�����J�n�l(�܂݂܂�)
	 * @param		endExclusive	elements �ɂ�����I���l(�܂݂܂���)
	 */
	public void construct(E[] elements, int begin, int endExclusive) {
		if (endExclusive - begin != size)
			throw new IllegalArgumentException("size mismatch");
		// �t�̒l��ݒ�
		int n = elements.length;
		for (int i = 0; i < n; i++)
			st[m-1+i] = elements[begin+i];
		for (int i = m-1+ n; i < 2*m-1; i++)
			st[i] = identity;
		// �e�e�̒l���X�V
		for (int i = m-2; i >= 0; i--)
			st[i] = aggregator.apply(st[i*2+1], st[i*2+2]);
	}
	
	/**
	 * �w�肳�ꂽ index �̗v�f���w�肳�ꂽ���̂ɍX�V���A�؂̒l���Čv�Z���܂��B
	 * �v�Z���Ԃ̃I�[�_�[�́AO(log n) �ł��B
	 *
	 * @param		index		�ǉ�����C���f�b�N�X(0 �ȏ� size ����)
	 * @param		element		�X�V����v�f
	 */
	public void update(int index, E element) {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException("index is required that 0 <= index < "+size+", but was "+index);
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
	 * �^����ꂽ��Ԃɑ΂��� aggregator ���Z���ʂ������Ɏ擾���܂��B
	 * �v�Z���Ԃ̃I�[�_�[�́AO(log n) �ł��B
	 *
	 * @param		s		��Ԃ̊J�n(�܂�)
	 * @param		eExclusive		��Ԃ̏I��(�܂܂Ȃ�)
	 * @return		��Ԃɂ����� aggregator �̌���
	 */
	public E calculate(int s, int eExclusive) {
		if (s < 0 || eExclusive > size)
			throw new IndexOutOfBoundsException("wrong index");
		if (s >= eExclusive)
			throw new IllegalArgumentException("s must be smaller than eExclusive");
		return calcImpl(s, eExclusive, 0, 0, m);
	}
	
	/**
	 * �^����ꂽ��Ԃł̉��Z���ʂ�ԋp����B
	 *
	 * @param		s		��Ԃ̊J�n(�܂�)
	 * @param		eExclusive		��Ԃ̏I��(�܂܂Ȃ�)
	 * @param		n		�Z�O�����g�̃C���f�b�N�X
	 * @param		l		�Z�O�����g�̊J�n�ԍ�(�܂�)
	 * @param		r		�Z�O�����g�̏I���ԍ�(�܂܂Ȃ�)
	 */
	private E calcImpl(int s, int eExclusive, int n, int l, int r) {
		// ���ʕ������Ȃ��ꍇ
		if (r <= s || eExclusive <= l) return identity;
		// ���S�Ɋ܂�ł���ꍇ
		if (s <= l && r <= eExclusive) return st[n];
		// �ꕔ���ʂ��Ă���ꍇ
		int i = (l>>>1)+(r>>>1);
		E cl = calcImpl(s, eExclusive, 2*n + 1, l, i);
		E cr = calcImpl(s, eExclusive, 2*n + 2, i, r);
		return aggregator.apply(cl, cr);
	}
}
