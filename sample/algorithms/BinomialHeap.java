import java.util.*;

/**
 * �񍀃q�[�v
 * ����
 */
class BinomialHeapNode {
	int value;
  /** �����Broot �ł݈̂Ӗ������� */
  int degree;
  /** �e�m�[�h(root �ł� null) */
	BinomialHeapNode parent;
	/** �ׂ̃m�[�h, degree ���K���傫���Ȃ� */
	BinomialHeapNode sibling;
  /** �q�m�[�h�A�l�͕K�����̃m�[�h���傫�� */
	BinomialHeapNode child;
  /** sibling ���܂܂Ȃ��T�C�Y(�q, �q��sibling���܂�) */
  int size;

/*-------------
 * constructor
 */

 /**
	 * �w�肳�ꂽ�l������ BinomialHeapNode ���쐬���܂��B
	 * @param value �l
	 */
	BinomialHeapNode(int value) {
		this.value = value;
		degree = 0;
    size = 1;
	}

/*------------------
 * instance methods
 */
	/**
	 * �ŏ���(�l���ŏ���)�m�[�h���擾���܂��B�v�Z�ʂ� O(log n) �ł��B
	 * @return �ŏ���(�l���ŏ���) BinomialHeapNode
	 */
	BinomialHeapNode findFirstNode() {
    assert parent == null : "findFirstNode: root �ł͂���܂���";
		BinomialHeapNode x = this.sibling, firstNode = this;
		int first = value;

		while (x != null) {
			if (x.value < first) {
				firstNode = x;
				first = x.value;
			}
			x = x.sibling;
		}
		return firstNode;
	}

	/**
	 * sibling �`�F�[�����t���ɂ��A�`�F�[�������Ɏw�肳�ꂽ BinomialHeapNode ��ǉ����܂��B
	 * @param sibl �ǉ��Ώۂ� sibling
	 * @return this ����͂��܂� sibiling �`�F�[�����t���ɂ��A
	 *         �`�F�[�������� sibl ��ǉ����� Node (�t���Ƃ��ꂽ sibling �`�F�[���� root Node)
	 */
	BinomialHeapNode reverse(BinomialHeapNode sibl) {
    parent = null; // �ݒ肵�Ȃ��Ă����Ȃ�(assertion ���ړI�̂�)
		BinomialHeapNode ret;
		if (sibling != null) ret = sibling.reverse(this);
		else ret = this;
		sibling = sibl;
		return ret;
	}

  /**
   * �w�肳�ꂽ value �� BinomialHeapNode ���������܂��B
   * �v�Z�ʂ� O(n) �ł��B
   * @param value �����Ώۂ� Node �̒l
   * @return �w�� value �� BinomialHeapNode, ������Ȃ������ꍇ null
   */
  BinomialHeapNode find(int value) {
    if (this.value == value) return this;
    if (child != null) {
      BinomialHeapNode result = child.find(value);
      if (result != null) return result;
    }
    if (sibling == null) return null;
    return sibling.find(value);
  }

  /**
   * ���� node (root) �̍��̎q(���q)�Ƃ��ē������� tree ��ǉ����܂��B
   * ���ʁAdegree �� +1 ����܂��B
   * 
   * @param heap �ǉ��Ώۂ� heap�Bdegree �� this �Ɠ������K�v������B
   */
  private void add(BinomialHeapNode tree) {
    assert tree != null : "tree �� null �ɂȂ��Ă��܂�";
    assert degree == tree.degree : "�������قȂ��Ă��܂�";
    assert tree.parent == null : "root �ɂȂ��Ă��܂���";
    BinomialHeapNode tmp = child;
    child = tree;
    tree.parent = this;
    tree.sibling = tmp;

    degree++;
    size += tree.size;
  }

  /**
   * ���������� heap node (root) ���}�[�W���܂��B
   * merge ��� node �� sibling �� this �܂��� tree �� sibling �̂܂ܕύX����܂���
   * merge ���ꂽ���́A�K�X�ύX����܂��B
   * @param heap merge ���� heap �� root node
   * @return merge ��� root node
   */
  private BinomialHeapNode mergeTree(BinomialHeapNode tree) {
    assert degree == tree.degree : "�������قȂ��Ă��܂�";
    assert tree.parent == null : "root �ɂȂ��Ă��܂���";
    if (value < tree.value) {
      this.add(tree);
      return this;
    } else {
      tree.add(this);
      return tree;
    }
  }

  /**
   * heap ���}�[�W���܂��B
   * ���� heap�A�܂��w�肳�ꂽ heap �͍��� sibling �������Ȃ�
   * BinomialHeap �S�̂� root �ł���K�v������܂��B
   * @param heap �}�[�W���� heap �S�̂� root node (null ���e)
   * @return �}�[�W��� heap �S�̂� root node
   */
  BinomialHeapNode merge(BinomialHeapNode heap) {
    if (heap == null) return this;
    assert parent == null;
    assert heap.parent == null;

    // result ����(����� node)
    // last (result �̍ŉE�� sibling) deg �Ɠ������Ⴂ���� (non null)
    // a merge �Ώۂ̖؂� root (sibling �ɂ���Ă���)
    // b merge �Ώۂ̖؂� root (sibling �ɂ���Ă���)
    BinomialHeapNode result = null, last = null, lastLeft = null, a = this, b = heap;
    // �������Ⴂ���ɓ������̂��̂� merge ���Ă���
    while (a != null && b != null) {
      int deg = Math.min(a.degree, b.degree);
      // for (int deg = 0; a != null || b != null; deg++) {
      if (deg > 63) throw new InternalError("overflow");
      // c = a.merge(b) (at the "deg")
      BinomialHeapNode c = null;
      if (a.degree == deg) {
        c = a;
        a = a.sibling;
      }
      if (b.degree == deg) {
        BinomialHeapNode bsib = b.sibling;
        if (c == null) c = b;
        else c = c.mergeTree(b);
        b = bsib;
      }
      // last �� c �� merge
      if (c != null) {
        if (result == null) {
          result = last = c;
        } else if (last.degree == c.degree) {
          last = last.mergeTree(c);
          if (lastLeft == null) {
            result = last;
          } else lastLeft.sibling = last;
        } else {
          // last.degree < c.degree
          last.sibling = c;
          lastLeft = last; last = last.sibling;
        }
        last.sibling = null;
      }
      // ���̕ӂɏI�������������� break ����`���Ȍ��Ǝv����
      // ���̃R�[�h�͕s�v�ɂȂ�
    }
    BinomialHeapNode c = (a != null)? a : b;
    if (c != null) {
      if (result == null) {
        result = last = c;
      } else if (last.degree == c.degree) {
        last = last.mergeTree(c);
        if (lastLeft == null) result = last;
        else lastLeft.sibling = last;
      } else {
        // last.degree < c.degree
        last.sibling = c;
        last = last.sibling;
      }
      last.sibling = null;
    }

    return result;
  }

  @Override
  public String toString() {
    String result = "";
    if (parent == null) result += "[d:"+degree+"]";
    result += value + " ";
    if (sibling != null) result += "(" + sibling.toString() + ")";
    if (child != null) result += child.toString();
    return result;
  }

}

public class BinomialHeap {
  BinomialHeapNode root;

  public void add(int value) {
    BinomialHeapNode node = new BinomialHeapNode(value);
    if (root == null) root = node;
    else root = root.merge(node);
  }

  /**
   * Heap �̍ŏ��̒l���擾���܂��B�\���͕ω����܂���B
   * @return �ŏ��̒l
   */
  public int peek() {
    return root.findFirstNode().value;
  }

  /**
   * Heap �̍ŏ��̒l���擾���A�폜���܂��B
   * @return �ŏ��̒l
   */
  public int poll() {
    assert root.parent == null : "deleteFirstNode: root �ł͂���܂���";
    // �ŏ� Node ��T���A�ŏ��l�̍��� Node (leftSibling) ���擾����B
		BinomialHeapNode x = root, firstNode = root, leftSibling = null, lastX = null;
		int first = x.value;

		while (x != null) {
			if (x.value < first) {
				firstNode = x;
				first = x.value;
        leftSibling = lastX;
			}
      lastX = x;
			x = x.sibling;
		}
    // x ��؂��� root ���� delete ����
    if (firstNode == root) {
      root = firstNode.sibling;
    } else {
      assert leftSibling != null : "leftSibling �� null �ɂȂ��Ă��܂�";
      leftSibling.sibling = firstNode.sibling;
    }

    // �� x �� root ���폜���Amerge
    if (firstNode.child != null) root = firstNode.child.reverse(null).merge(root);

    return first;
  }

  void merge(BinomialHeap heap) {
    root = root.merge(heap.root);
  }

  int size() {
    // root �� siblings �� size �̘a
    int size = root.size;
    BinomialHeapNode sib = root.sibling;
    while (sib != null) {
      size += sib.size;
      sib = sib.sibling;
    }
    return size;
  }

  boolean isEmpty() {
    return root == null;
  }

  @Override
  public String toString() {
    return root.toString();
  }

  // �L�[�l���Z
  // �폜
  
/*---------------
 * main for test
 */
  public static void main(String[] args) {
    BinomialHeapNode b1 = new BinomialHeapNode(1);
    System.out.println(b1);
    BinomialHeapNode b2 = new BinomialHeapNode(3);
    System.out.println(b2);
    BinomialHeapNode b3 = new BinomialHeapNode(2);

    BinomialHeapNode a1 = b1.merge(b2);
    System.out.println("merge: " + a1);
    a1 = a1.merge(b3);
    System.out.println("merge: " + a1);

    BinomialHeap b = new BinomialHeap();

    b.add(1);
    b.add(3);
    b.add(2);

    System.out.println(b);

    test0();
    test1();
  }
	static PriorityQueue<Integer> merge(PriorityQueue<Integer> a, PriorityQueue<Integer> b) {
		while (!b.isEmpty()) {
			a.add(b.poll());
		}
		return a;
	}

  static void add(BinomialHeap bh, int ...values) {
    for (int value: values) {
      bh.add(value);
      System.out.println("add " + value + ": result=" + bh);
    }
  }
  // test0
  // @SuppressWarnings("unchecked")
	static void test0() {
    System.out.println("--------------");
    BinomialHeap bh1 = new BinomialHeap();
    add(bh1, 26,1,43);
    add(bh1,21);
    add(bh1, 8);
    System.out.println("bh1="+bh1);
    BinomialHeap bh2 = new BinomialHeap();
    add(bh2, 8,6,2,4);
    System.out.println("bh2="+bh2);

    bh1.merge(bh2);
    System.out.println("merged="+bh1);
    System.out.println("merged.size="+bh1.size());
  }
	
  // test1
  @SuppressWarnings("unchecked")
	static void test1() {
		int M = 10000;
		Random r = new Random(12345L);
		
		int N = 801;
		BinomialHeap[] heaps = new BinomialHeap[N];
		for (int i = 0; i < N; i++) heaps[i] = new BinomialHeap();
		
		PriorityQueue<Integer>[] ques = new PriorityQueue[N];
		for (int i = 0; i < N; i++) ques[i] = new PriorityQueue<>();
		
		for (int i = 0; i < N; i++) {
			int rnd = r.nextInt(N) + 1;
			heaps[i].add(rnd);
      // System.out.println("heaps["+i+"]="+heaps[i]);
			ques[i].add(rnd);
		}
		
		for (int i = 0; i < M; i++) {
			int i1 = r.nextInt(N);
			// System.out.println("i1 = " +i1);
			while (heaps[i1] == null) i1 = (i1+1) %N;
			
			int i2 = r.nextInt(N);
			if (i1 == i2) i2 = (i2 + 1) %N;
			if (heaps[i2] == null) {
				int rnd = r.nextInt(N) + 1;
				// System.out.println("add " + rnd + " to ["+i1+"]");
				heaps[i1].add(rnd);
				ques[i1].add(rnd);
        // System.out.println("added heap =" + heaps[i1]);
			} else {
				// System.out.println("merge [" + i2 + "] to [" + i1 + "]");
				heaps[i1].merge(heaps[i2]); heaps[i2] = null;
				merge(ques[i1], ques[i2]); ques[i2] = null;
        // System.out.println("merged heap =" + heaps[i1]);
        if (heaps[i1].size() != ques[i1].size()) System.out.println("size mismatch");
			}
		}
		// check
		for (int i = 0; i < N; i++) {
			System.out.println("test No." + i);
			if (heaps[i] == null) {
				if (ques[i] != null) System.out.println("mismatch "+ i);
			} else {
				while (!heaps[i].isEmpty()) {
					int a = heaps[i].poll();
					int b = ques[i].poll();
					if (a != b) {
						System.out.println("mismatch2 " + i + " heap="+a+" que="+b);
					}
				}
			}
		}
	}

}
