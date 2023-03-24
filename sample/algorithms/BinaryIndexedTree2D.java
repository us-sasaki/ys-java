import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * �ėp��(�������e�����������^)�񎟌� Binary Indexed Tree (BIT, Fenwick Tree)
 * class �ł��B<br>
 *
 * ���̂܂ܓK�p���\�ł����A�e���ɓK�p���邽�߂̃��t�@�����X�����Ƃ���
 * ���������̂ł��B<br>
 * ����^�̗v�f�ɑ΂���񍀉��Z�������I�ł���A�P�ʌ�������(�P�ʓI���Q)�ꍇ�A
 * ���񂵂��v�f�̎n�_(�񎟌��̂��ߋ�`�̈�)����̋�Ԃɑ΂��鉉�Z���ʂ�
 * �N�G���������Ɏ��s�\�ł��B
 * ��ʂɁA�v�f�� n �ɑ΂��A�n�_����̋��(��`�̈�)�ɑ΂��� O((log n)^2) ��
 * �v�Z�ʂŌ��ʂ��擾�\�ł��B
 * �܂��ABIT�̗v�f�̕ύX�� O((log n)^2) �̌v�Z�ʂł��B
 * �\�z�̂��߂̌v�Z�� O(n^2) �̃��\�b�h�����J���Ă��܂��B
 * Segment Tree �Ƃ̍��ق́A��Ԃ��n�_����Ƃ������񂪂��邱�ƁA����ѕێ�����
 * �v�f���� n �Ń������������悢�_�ł��B
 * �Ȃ��AE �ɑ΂��ċt������`�����ꍇ�A�n�_�J�n����͂Ȃ��Ȃ�A�C�Ӌ�Ԃ�
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
public class BinaryIndexedTree2D<E> {
	
	/** BIT �̗L���ȃT�C�Y */
	private int size1, size2;
	
	/** �v�f��ێ�����z��B�T�C�Y�� size1, size2 */
	private E[][] st;
	
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
	 * @param		size1		BIT�̑傫��
	 * @param		size2		BIT�̑傫��
	 * @param		aggregator	2 �� E �̗v�f���� E �̗v�f�ւ̉��Z
	 * @param		identity	���Z aggregator �ɂ�����P�ʌ�
	 */
	public BinaryIndexedTree2D(int size1, int size2,
								BinaryOperator<E> aggregator, E identity) {
		this(size1, size2, aggregator, identity, null);
	}
	
	/**
	 * �w�肳�ꂽ�T�C�Y�̗v�f��ێ����� BIT �𐶐����܂��B
	 * �P�ʌ��Ɋւ���ȈՃ`�F�b�N���s���܂��B����Ȃ��A
	 * aggregator.apply(identity, identity) �� identity �ɓ������Ȃ��ꍇ�A
	 * IllegalArgumentException ���X���[����܂��B
	 * �P�ʌ��� ��A, agg(A, E) = agg(E, A) = A �𖞂��� E �ł���A����
	 * �`�F�b�N�͕����I�ł��邱�Ƃɒ��ӂ��Ă��������B
	 *
	 * @param		size1		BIT�̑傫��1
	 * @param		size2		BIT�̑傫��2
	 * @param		aggregator	2 �� E �̗v�f���� E �̗v�f�ւ̉��Z
	 * @param		identity	���Z aggregator �ɂ�����P�ʌ�
	 * @param		inverse		aggregator �ɂ�����t������(null �̏ꍇ������)
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
	 * �z��ł��� BIT �����������܂��B
	 * SegmentTree �Ƃ̌݊����̂��ߐݒ肳�ꂽ���\�b�h�ŁA
	 * �������e�� update ���J��Ԃ��K�p���܂��B
	 *
	 * @param		elements		���� BIT �ɐݒ肷��l
	 */
	public void construct(E[][] elements) {
		construct(elements, 0, elements.length, 0, elements[0].length);
	}
	
	/**
	 * �z��ł��� BIT �����������܂��B
	 * SegmentTree �Ƃ̌݊����̂��ߐݒ肳�ꂽ���\�b�h�ŁA
	 * �������e�� update ���J��Ԃ��K�p���܂��B
	 *
	 * @param		elements		���� BIT �ɐݒ肷��l
	 * @param		begin1			elements �ɂ�����J�n�l1(�܂݂܂�)
	 * @param		end1Exclusive	elements �ɂ�����I���l1(�܂݂܂���)
	 * @param		begin2			elements �ɂ�����J�n�l2(�܂݂܂�)
	 * @param		end2Exclusive	elements �ɂ�����I���l2(�܂݂܂���)
	 */
	public void construct(E[][] elements, int begin1, int end1Exclusive
									, int begin2, int end2Exclusive) {
		int n1 = end1Exclusive - begin1;
		if (n1 != size1)
			throw new IllegalArgumentException("size1 mismatch");
		int n2 = end2Exclusive - begin2;
		if (n2 != size2)
			throw new IllegalArgumentException("size2 mismatch");
		// �t�̒l��ݒ�
		
		for (int i = 0; i < size1; i++)
			for (int j = 0; j < size2; j++)
				update(i, j, elements[i+begin1][j+begin2]);
	}
	
	/**
	 * �w�肳�ꂽ index �̗v�f���w�肳�ꂽ���̂ɍX�V���A�؂̒l���Čv�Z���܂��B
	 *
	 * @param		index1		�ǉ�����C���f�b�N�X1(0 �ȏ� size ����)
	 * @param		index2		�ǉ�����C���f�b�N�X2(0 �ȏ� size ����)
	 * @param		element		�X�V����v�f
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
	 * �n�_����^����ꂽ�I�_�܂ł̋�Ԃɑ΂��� aggregator ���Z���ʂ�������
	 * �擾���܂��B�v�Z���Ԃ̃I�[�_�[�́AO((log n)^2) �ł��B
	 *
	 * @param		e1		��Ԃ̏I�_1(�܂܂Ȃ�)
	 * @param		e2		��Ԃ̏I�_2(�܂܂Ȃ�)
	 * @return		��Ԃɂ����� aggregator �̌���
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
	 * �^����ꂽ��Ԃɑ΂��� aggregator ���Z���ʂ������Ɏ擾���܂��B
	 * �v�Z���Ԃ̃I�[�_�[�́AO((log n)^2) �ł��B
	 * ���̃��\�b�h�𗘗p����ɂ́A�R���X�g���N�^�� inverse ��ݒ肷��
	 * �K�v������܂��B�ݒ肳��Ă��Ȃ��ꍇ�A
	 * UnsupportedOperationException ���X���[����܂��B
	 *
	 * @param		s1		��Ԃ̊J�n1(�܂�)
	 * @param		e1		��Ԃ̏I�_1(�܂܂Ȃ�)
	 * @param		s2		��Ԃ̊J�n2(�܂�)
	 * @param		e2		��Ԃ̏I�_2(�܂܂Ȃ�)
	 * @return		��Ԃɂ����� aggregator �̌���
	 * @throws		java.lang.UnsupportedOperationException	inverse������`
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
		//         A = a �Ƃ���(A�`D�� calculate(int,int) �Ōv�Z�ł���̈�)
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
