import java.util.Deque;
import java.util.ArrayDeque;

/**
 * Union Find Tree �̎�����
 * size �ɂ����̂ŁA�e�؂̃T�C�Y���킩��
 * �ق��ɁA����(����) rank �ɂ�����������B
 * https://algo-logic.info/union-find-tree/
 */
final class UnionFind {
	/** �m�[�h�̐��̔z�� */
	int[] size, parent;
	
	UnionFind(int n) {
		size = new int[n];
		parent = new int[n];
		for (int i = 0; i < n; i++) {
			parent[i] = i;
			size[i] = 1;
		}
	}
	
	boolean isConnected(int x, int y) {
		return getRoot(x) == getRoot(y);
	}

	boolean connect(int x, int y) {
		int ix = getRoot(x);
		int iy = getRoot(y);
		if (ix == iy) return false;
		if (size[ix] > size[iy]) {
			parent[iy] = ix;
			size[ix] += size[iy];
		} else {
			parent[ix] = iy;
			size[iy] += size[ix];
		}
		return true;
	}

	int getRoot(int x) {
		if (x != parent[x]) {
			parent[x] = getRoot(parent[x]);
		}
		return parent[x];
	}
	
	int size(int x) { return size[getRoot(x)]; }
	
	//------------------------------------
	
	/**
	 * �ċA��� stack ���g���������x��
	 */
	private Deque<Integer> stack = new ArrayDeque<>();
	int getRoot2(int x) {
		stack.clear();
		while (x != parent[x]) {
			stack.push(x);
			x = parent[x];
		}
		while (!stack.isEmpty()) {
			parent[stack.pop()] = x;
		}
		return x;
	}
	
	/**
	 * �ȒP�ȃe�X�g
	 */
	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();
		{
			UnionFind uf = new UnionFind(3);
			expect(!uf.isConnected(0, 1));
			expect(!uf.isConnected(1, 2));
			expect(!uf.isConnected(0, 2));
			
			uf.connect(0, 2);
			expect(!uf.isConnected(0, 1));
			expect(!uf.isConnected(1, 2));
			expect(uf.isConnected(0, 2));
		}
		
		{
			int n = 10000000;
			
			// �e�X�g�����ɍ����悤�� n ��ύX
			n = Math.max( (n/2)*2, 10000 );
			UnionFind uf = new UnionFind(n);
			for (int i = 0; i < n/2-1; i++) uf.connect(i, i+1);
			for (int i = n/2; i < n-1; i++) uf.connect(i, i+1);
			
			for (int i = 0; i < n/2; i++) {
				expect(uf.isConnected(i, (i*3+1)%(n/2)));
				expect(uf.isConnected(i+(n/2), (i*7+2)%(n/2)+(n/2)));
				expect(!uf.isConnected( i*11%(n/2), i*13%(n/2)+(n/2) ));
			}
			
			uf.connect(1234, (n/2) +678);
			for (int i = 0; i < (n/2); i++) {
				expect(uf.isConnected( i*11%(n/2), i*13%(n/2)+(n/2) ));
			}

		}
		System.out.println("test finished. elapsed time = "+ (System.currentTimeMillis() - t0));
	}
	
	static void expect(boolean t) {
		if (!t) throw new RuntimeException("error");
	}
}

