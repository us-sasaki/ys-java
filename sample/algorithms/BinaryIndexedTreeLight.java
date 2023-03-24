/**
 * BIT �� long �ŏ��l�v�Z�ɐ������������B�t���Z�Ȃ��B
 * 0-indexed
 *
 * @version		5 February, 2022
 * @author		Yusuke Sasaki
 */
public class BinaryIndexedTreeLight {
	
	/** BIT �̗L���ȃT�C�Y */
	private int size;
	
	/** �v�f��ێ�����z��B�T�C�Y�� size */
	private long[] st;
	
	/** E x I = E �ƂȂ� I */
	private static final long ID = Long.MAX_VALUE / 2;
	
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
	 * @param		inverse		aggregator �ɂ�����t������(null �̏ꍇ������)
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
	 * �w�肳�ꂽ index �̗v�f���w�肳�ꂽ���̂ɍX�V���A�؂̒l���Čv�Z���܂��B
	 *
	 * @param		index		�ǉ�����C���f�b�N�X(0 �ȏ� size ����)
	 * @param		element		�X�V����v�f
	 */
	public void update(int index, long element) {
		index++;
		while (index <= size) {
			// ��apply
			st[index-1] = Math.min(st[index-1], element);
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
	public long calculate(int e) {
		if (e < 0) return ID;
		if (e >= size) e = size - 1;
		long s = ID;
		e++;
		while (e > 0) {
			// ��apply
			s = Math.min(s, st[e-1]);
			e -= e & -e;
		}
		return s;
	}
	
}

/**
 * �a���Z�A�����l�O�ɓ������� BIT �ł��B
 * 1-index �ł��邱�Ƃɒ��ӂ��Ă��������B
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
}
