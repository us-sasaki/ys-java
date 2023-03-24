import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;

/*------------------------------------------------------------
 *     D F S
 */

/**
 * �� DFS(stack��) �T���v�������BBFS �� queue �ɂ��邾���B
 * ���������A�߂�̏����������Ȃ�
 */
class DFSstack {
	/** �O���t�B�e���_�ԍ�����s�撸�_�ԍ��� List �ł̕\�� */
	static List<Integer>[] to;
	/** ���_���ƂɖK�₵���� */
	static int[] seen;
	
	/**
	 * @param		start		�J�n���_�ԍ�
	 */
	static void dfs(int start) {
		Deque<Integer> stack = new ArrayDeque<>();
		// �ŏ��̓_��ǉ�
		stack.push(start);
		while (!stack.isEmpty()) {
			int from = stack.pop();
			// �����ɍs��������������
			seen[from] = 1;
			for (int v : to[from]) {
				if (seen[v] > 0) continue;
				stack.push(v);
			}
		}
	}
}

/**
 * �� DFS(stack��) �T���v�������BBFS �� queue �ɂ��邾���B
 * �߂�̏����������ꍇ�B
 */
class DFSstack2 {
	/** �O���t�B�e���_�ԍ�����s�撸�_�ԍ��� List �ł̕\�� */
	static List<Integer>[] to;
	/** ���_���ƂɊ��������q�̐� */
	static int[] seen;
	
	/**
	 * @param		start		�J�n���_�ԍ�
	 */
	static void dfs(int start) {
		Deque<Integer> stack = new ArrayDeque<>();
		int[] order = new int[to.length]; // ���_�̐�
		int c = 0;
		
		// �ŏ��̓_��ǉ�
		stack.push(start);
		while (!stack.isEmpty()) {
			int from = stack.pop();
			order[c++] = from;
			seen[from] = 1;
			// �����ɍs��������������
			for (int v : to[from]) {
				if (seen[v] == 1) continue;
				stack.push(v);
			}
		}
		// �߂菈��
		for (int i = c-1; i >= 0; i--) {
			// ���_ order[i] �ɑ΂��Ė߂菈����������
			// ���_ order[i] �ȍ~�̂��ׂĂ̒��_�͖߂菈����
			// �����ɂ͂��΂��Ύq�m�[�h�ɑ΂���J��Ԃ������������
		}
	}
}

/**
 * �� DFS(�ċA��) �T���v�������B
 */
class DFSrecursion {
	/** �O���t�B�e���_�ԍ�����s�撸�_�ԍ��� List �ł̕\�� */
	static List<Integer>[] to;
	/** ���_���ƂɖK�₵���� */
	static int[] seen;
	
	/**
	 * @param		start		�J�n���_�ԍ�
	 */
	static void dfs(int start) {
		// �����ɍs��������������
		seen[start] = 1;
		for (int v : to[start]) {
			if (seen[v] > 0) continue;
			dfs(v);
		}
		// �����ɖ߂菈����������
	}
}

/**
 * �� DFS(seen �Ȃ��ċA��) �T���v�������B
 */
class DFSrecursion2 {
	/** �O���t�B�e���_�ԍ�����s�撸�_�ԍ��� List �ł̕\�� */
	static List<Integer>[] to;
	
	/**
	 * @param		parent		�e���_�ԍ�
	 * @param		start		�J�n���_�ԍ�
	 */
	static void dfs(int parent, int start) {
		// �����ɍs��������������
		for (int v : to[start]) {
			if (v == parent) continue;
			dfs(start, v);
		}
		// �����ɖ߂菈����������
	}
}
