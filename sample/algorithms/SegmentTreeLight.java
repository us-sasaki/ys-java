import java.util.function.BinaryOperator;

/**
 * �����p�����Z�O�����g�c���[ class<br>
 * �^�� E �Ƃ��Ă��邪�A���Q�̐ς� e �͎������K�v�B
 * Exception �����͏ȗ��B
 *
 * @version		15 August, 2022
 * @author		Yusuke Sasaki
 */
public class SegmentTreeLight<E> {
	
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
	 */
	public SegmentTree(int size) {
		init(size);
		// �t�̒l��ݒ�
		for (int i = 0; i < 2*m-1; i++)
			st[i] = identity; // identity �����̒l������
	}
	
	/**
	 * �w�肳�ꂽ�T�C�Y�̗v�f��ێ����� SegmentTree �𐶐����܂��B
	 * �P�ʌ��Ɋւ���ȈՃ`�F�b�N���s���܂��B����Ȃ��A
	 * aggregator.apply(identity, identity) �� identity �ɓ������Ȃ��ꍇ�A
	 * IllegalArgumentException ���X���[����܂��B
	 * �P�ʌ��� ��A, agg(A, E) = agg(E, A) = A �𖞂��� E �ł���A����
	 * �`�F�b�N�͕K�v�����ɉ߂��Ȃ����Ƃɒ��ӂ��Ă��������B
	 *
	 * @param		value		�^���鏉���l���܂ޔz��
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
	 * �z��ł��̃Z�O�����g�؂����������܂��B
	 * update ���J��Ԃ��ĂԂ�荂���ŁA�v�Z�ʂ� O(n) �ł��B
	 *
	 * @param		elements		���̃Z�O�����g�؂ɐݒ肷��l
	 */
	public void construct(E[] elements) {
		// �t�̒l��ݒ�
		int n = elements.length;
		for (int i = 0; i < n; i++)
			st[m-1+i] = elements[i];
		for (int i = m-1+n; i < 2*m-1; i++)
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
