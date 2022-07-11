package abdom.util;

/**
 * 二項ヒープ
 * 昇順
 */
class BinomialHeapNode {
	int value;
  /** 次数。root でのみ意味がある */
  int degree;
  /** 親ノード(root では null) */
	BinomialHeapNode parent;
	/** 隣のノード, degree が必ず大きくなる */
	BinomialHeapNode sibling;
  /** 子ノード、値は必ずこのノードより大きい */
	BinomialHeapNode child;
  /** sibling を含まないサイズ(子, 子のsiblingを含む) */
  int size;

/*-------------
 * constructor
 */

 /**
	 * 指定された値を持つ BinomialHeapNode を作成します。
	 * @param value 値
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
	 * 最初の(値が最小の)ノードを取得します。計算量は O(log n) です。
	 * @return 最初の(値が最小の) BinomialHeapNode
	 */
	BinomialHeapNode findFirstNode() {
    assert parent == null : "findFirstNode: root ではありません";
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
	 * sibling チェーンを逆順にし、チェーン末尾に指定された BinomialHeapNode を追加します。
	 * @param sibl 追加対象の sibling
	 * @return this からはじまる sibiling チェーンを逆順にし、
	 *         チェーン末尾に sibl を追加した Node (逆順とされた sibling チェーンの root Node)
	 */
	BinomialHeapNode reverse(BinomialHeapNode sibl) {
    parent = null; // 設定しなくても問題ない(assertion 回避目的のみ)
		BinomialHeapNode ret;
		if (sibling != null) ret = sibling.reverse(this);
		else ret = this;
		sibling = sibl;
		return ret;
	}

  /**
   * 指定された value の BinomialHeapNode を検索します。
   * 計算量は O(n) です。
   * @param value 検索対象の Node の値
   * @return 指定 value の BinomialHeapNode, 見つからなかった場合 null
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
   * この node (root) の左の子(第一子)として同次数の tree を追加します。
   * 結果、degree は +1 されます。
   * 
   * @param heap 追加対象の heap。degree は this と等しい必要がある。
   */
  private void add(BinomialHeapNode tree) {
    assert tree != null : "tree が null になっています";
    assert degree == tree.degree : "次数が異なっています";
    assert tree.parent == null : "root になっていません";
    BinomialHeapNode tmp = child;
    child = tree;
    tree.parent = this;
    tree.sibling = tmp;

    degree++;
    size += tree.size;
  }

  /**
   * 同じ次数の heap node (root) をマージします。
   * merge 後の node の sibling は this または tree の sibling のまま変更されません
   * merge された方は、適宜変更されます。
   * @param heap merge する heap の root node
   * @return merge 後の root node
   */
  private BinomialHeapNode mergeTree(BinomialHeapNode tree) {
    assert degree == tree.degree : "次数が異なっています";
    assert tree.parent == null : "root になっていません";
    if (value < tree.value) {
      this.add(tree);
      return this;
    } else {
      tree.add(this);
      return tree;
    }
  }

  /**
   * heap をマージします。
   * この heap、また指定された heap は左の sibling を持たない
   * BinomialHeap 全体の root である必要があります。
   * @param heap マージする heap 全体の root node (null 許容)
   * @return マージ後の heap 全体の root node
   */
  BinomialHeapNode merge(BinomialHeapNode heap) {
    if (heap == null) return this;
    assert parent == null;
    assert heap.parent == null;

    // result 結果(左上の node)
    // last (result の最右の sibling) deg と同じか低い次数 (non null)
    // a merge 対象の木の root (sibling にずれていく)
    // b merge 対象の木の root (sibling にずれていく)
    BinomialHeapNode result = null, last = null, lastLeft = null, a = this, b = heap;
    // 次数が低い順に同次数のものを merge していく
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
      // last に c を merge
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
      // この辺に終了条件を書いて break する形が簡潔と思われる
      // 下のコードは不要になる
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
   * Heap の最初の値を取得します。構造は変化しません。
   * @return 最初の値
   */
  public int peek() {
    return root.findFirstNode().value;
  }

  /**
   * Heap の最初の値を取得し、削除します。
   * @return 最初の値
   */
  public int poll() {
    assert root.parent == null : "deleteFirstNode: root ではありません";
    // 最小 Node を探し、最小値の左の Node (leftSibling) も取得する。
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
    // x を木ごと root から delete する
    if (firstNode == root) {
      root = firstNode.sibling;
    } else {
      assert leftSibling != null : "leftSibling が null になっています";
      leftSibling.sibling = firstNode.sibling;
    }

    // 木 x の root を削除し、merge
    if (firstNode.child != null) root = firstNode.child.reverse(null).merge(root);

    return first;
  }

  void merge(BinomialHeap heap) {
    root = root.merge(heap.root);
  }

  int size() {
    // root の siblings の size の和
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

  // キー値減算
  // 削除
}
