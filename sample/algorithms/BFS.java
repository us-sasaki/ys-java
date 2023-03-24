import java.util.*;

/*------------------------------------------------------------
 *     B F S
 */

/**
 * �O���t BFS(queue��) �T���v�������B
 * �ŒZ�����A�o�H���킩��B���[�v���킩��B�A���������킩��B
 */
class BFSqueue {
	/** �O���t�\�� �e���_�ԍ�����s�撸�_�ԍ��� List �ł̕\�� */
	List<Integer>[] to;
	/** �n�_����̋����B���炩���� -1 ������Ə������A�������� -1 �ƂȂ� */
	int[] d;
	/** �ŒZ�o�H�̂��߂̓������ */
	int[] from;
	
	void bfs(int start) {
		d = new int[N];
		Arrays.fill(d, -1);
		// bfs
		Deque<Integer> deq = new ArrayDeque<>();
		// �n�_��ǉ�
		deq.add(start);
		
		while (deq.size() > 0) {
			int parent = deq.poll();
			for (int n : to[parent]) {
				if (d[n] > -1) continue;
				d[n] = d[parent] + 1;
				from[n] = parent;
				deq.add(n);
			}
		}
		// d �Ɋe�_�̎n�_����̍ŒZ����������
		// �C�ӂ̓_����� from ��H��Ǝn�_�܂ł̍ŒZ�o�H���킩��
	}
}

