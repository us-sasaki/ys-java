import java.util.function.ToIntFunction;

/**
 * �C�ӌ��� Node ��ێ��ł��ANode �S�̂ɑ΂��鉉�Z�l(max/min/���Z�ȂǁA���Q���Z)��
 * �����Ɍv�Z�ł���\���ł��B
 * Segment Tree ���ϗe�ʂƂ��A�I�u�W�F�N�g�w��� delete/replace ������������
 * ���ʁA���Z�͑S�̂ɑ΂��Ă݂̂ƂȂ鐧�񂪂���܂��B
 * ���̍\���́Abinary tree �Ŏ�������A���̓���������܂��B
 * - ���ɉ������T�C�Y (Linked list �I�ȍ\��)
 * - add/delete/replace �̌v�Z�ʂ� O(log N)
 * - ���Z���ʂ̎擾�� O(1)
 * �Ȃ��Adelete �͖��[(leaf) �݂̂��폜���ABranch �\���͕ύX���܂���B
 * (�Ēǉ����� Branch �������ȗ��ł��邪�A��x�[���Ȃ����؍\���͕ς��Ȃ����߁A
 * ����/�����������͋]��)
 * 
 * add �ł́Aweight �̃o�����X������悤�ɁAroot �ɋ߂����ɒǉ�����܂��B
 * ����ɂ��A�؍\���̍ő�l�X�g���傫���Ȃ�Ȃ��悤�ɂ��܂��B
 *
 * value �Ƃ��āANode �Ƃ��邱�ƂŁA�l�X�V�\�� PriorityQueue �Ƃ���
 * ���p���邱�Ƃ��ł��܂��B(Dijkstra �@�ŗ��p�\)
 *
 * ���p�@
 * <pre>
 * Branch root = new Branch();
 * // add/replace/delete �v�Z
 * Node node = new Node();
 * node.value = ...;
 * root.add[node];
 * :
 * Node replacement = new Node();
 * replacement.value = ...;
 * root.replace(replacement);
 * :
 * root.delete(someSpecificNode);
 * :
 * int result = root.value;
 * </pre>
 */

/**
 * Leaf �܂��� Branch ��\�� Node �ł��B
 * ���Z�l�� value �Ƃ��ĕێ����Ă���A�؍\���ɂ�����ʒu�� id �Ƃ��ĕێ����܂��B
 * id �� root �� 1 �Ƃ��āA���̂悤�ɕt�^����Ă��܂��B
 * 
 *                                 root(1)
 *                 b(10)                             b(11)
 *         b(100)        l(101)             b(110)           l(111)
 *     l(1000) l(1001)                   -      b(1101)
 *                                         l(11010) l(11011)
 * b...branch l...leaf () ����id(2�i��)
 * id �� int �^�̂��߁ANode �� 2^31 �ȉ��ł���K�v������܂��B
 * depth ��ێ����ăg�b�v�r�b�g�̐�����Ȃ����A2^32 �܂ŁA�����ɍ�����
 * �ł���\��������܂��B
 *
 * @version		4th July, 2021
 * @author		Yusuke Sasaki
 */
class Node {
	/** �؍\���ɂ�����ʒu */
	int id;
	/** �v�Z�l(���Q�v�Z�̌���) */
	int value;
	// �Ԃ牺�����Ă��� Node(Leaf/Branch) �̐�(�t�̏ꍇ�A0)
	int weight;

	public String toString() {
		return "[id="+id+"#="+(hashCode() & 0xFF)+",v="+value+"]";
	}
}

class Branch extends Node {
	/**
	 * updater �̗�(���s��s�v)
	 */
	static final ToIntFunction<Branch> MAX = b -> Math.max( (b.l == null)?-1:b.l.value, (b.r == null)?-1:b.r.value );
	
	/**
	 * value �ɑ΂��锼�Q���Z (value, value) |-> value
	 * Branch �� l.value, r.value �ɑ΂��č�p���邱�Ƃɒ���
	 */
	ToIntFunction<Branch> updater;
	Node l, r;
	
	Branch(ToIntFunction<Branch> updater) {
		id = 1;
		this.updater = updater;
	}
	
	/**
	 * ���� Branch �z���� Node (leaf �Ɍ���܂�) ��ǉ����܂��B
	 * �ǉ���A�e Branch �� value, weight ���X�V����܂��B
	 * �v�Z�ʂ� O(log N) �ł��B
	 * 
	 * @param e �ǉ����� leaf�Bid ���X�V����܂��B
	 */
	final void add(Node e) {
		if (l == null) {
			l = e;
			e.id = id << 1;
		} else if (r == null) {
			r = e;
			e.id = (id << 1) + 1;
		} else {
			// l, r �Ƃ��ɖ��܂��Ă���
			Node toAdd = (l.weight > r.weight)?r:l;
			if (toAdd instanceof Branch) {
				((Branch)toAdd).add(e);
			} else {
				// toAdd �� Node(leaf)�B�ǉ��ł��Ȃ����߁ABranch �}��
				Branch b = new Branch(updater);
				b.id = toAdd.id;
				b.add(toAdd);
				b.add(e); // value, weight, e.id �ݒ�
				if (l.weight > r.weight) r = b;
				else l = b;
			}
		}
		update();
	}
	
	/**
	 * ���� Branch �z���ɂ��� Node �������ɍ폜���܂��B
	 * �ʒu�̓���̂��߂ɁANode �� id ���ʒu���Ƃ��ė��p���܂��B
	 * Branch �̍\���͕ς��܂���B(Branch �� delete ���ꂸ�Aleaf �݂̂�
	 * �폜����܂�)
	 * �v�Z�ʂ� O(log N) �ł��B
	 *
	 * @param	e	�폜���� leaf�B�ݒ肳��Ă��� id ���ʒu����ɗp�����܂��B
	 */
	final void delete(Node e) {
		// e.id �����Ɉʒu��T��
		int m = 1 << (Integer.numberOfLeadingZeros(id) - Integer.numberOfLeadingZeros(e.id) - 1);

		if ( (m & e.id) == 0) {
			// l �̕��ɂ���
			if (l instanceof Branch) ((Branch)l).delete(e);
			else l = null;
		} else {
			// r �̕��ɂ���
			if (r instanceof Branch) ((Branch)r).delete(e);
			else r = null;
		}
		update();
	}
	
	/**
	 * ���� Branch �z���ɂ��� Node ���w�肵�����̂ɒu�������܂��B
	 * �ʏ�̎g�����́A�Y�� Node �� value ���X�V���A���̃��\�b�h���R�[��
	 * ���邱�Ƃł��B����ɂ��A�S�̂��Čv�Z����܂��B
	 * ����� Node �I�u�W�F�N�g�ł���K�v�͂���܂��񂪁A
	 * id, weight(=0) �͌��� Node �Ɠ���l�ɐݒ肳��Ă���K�v������܂��B
	 * �v�Z�ʂ� O(log N) �ł��B
	 *
	 * @param	e	�����ւ��� leaf�B�ݒ肳��Ă��� id ���ʒu����ɗp�����܂��B
	 */
	final void replace(Node e) {
		// e.id �����Ɉʒu��T��
		int m = 1 << (Integer.numberOfLeadingZeros(id) - Integer.numberOfLeadingZeros(e.id) - 1);

		if ( (m & e.id) == 0) {
			// l �̕��ɂ���
			if (l instanceof Branch) ((Branch)l).replace(e);
			else l = e;
		} else {
			// r �̕��ɂ���
			if (r instanceof Branch) ((Branch)r).replace(e);
			else r = e;
		}
		update();
	}
	
	private void update() {
		/** ���Q���Z(���̎����� (int, int) -> int �̗� */
		value = updater.applyAsInt(this);
		weight = ((l == null)?0:(l.weight+1)) + ((r == null)?0:(r.weight+1));
	}
	
	public String toString() {
		return "(id="+id+",v="+value+",w="+weight+",l:"+((l==null)?"-":l.toString())+",r:"+((r==null)?"-":r.toString())+")";
	}
}
