import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;

/*------------------------------------------------------------
 *     D F S
 */

/**
 * 木 DFS(stack版) サンプル実装。BFS は queue にするだけ。
 * 高速だが、戻りの処理が書けない
 */
class DFSstack {
	/** グラフ。各頂点番号から行先頂点番号の List での表現 */
	static List<Integer>[] to;
	/** 頂点ごとに訪問したか */
	static int[] seen;
	
	/**
	 * @param		start		開始頂点番号
	 */
	static void dfs(int start) {
		Deque<Integer> stack = new ArrayDeque<>();
		// 最初の点を追加
		stack.push(start);
		while (!stack.isEmpty()) {
			int from = stack.pop();
			// ここに行き処理が書ける
			seen[from] = 1;
			for (int v : to[from]) {
				if (seen[v] > 0) continue;
				stack.push(v);
			}
		}
	}
}

/**
 * 木 DFS(stack版) サンプル実装。BFS は queue にするだけ。
 * 戻りの処理を書く場合。
 */
class DFSstack2 {
	/** グラフ。各頂点番号から行先頂点番号の List での表現 */
	static List<Integer>[] to;
	/** 頂点ごとに完了した子の数 */
	static int[] seen;
	
	/**
	 * @param		start		開始頂点番号
	 */
	static void dfs(int start) {
		Deque<Integer> stack = new ArrayDeque<>();
		int[] order = new int[to.length]; // 頂点の数
		int c = 0;
		
		// 最初の点を追加
		stack.push(start);
		while (!stack.isEmpty()) {
			int from = stack.pop();
			order[c++] = from;
			seen[from] = 1;
			// ここに行き処理が書ける
			for (int v : to[from]) {
				if (seen[v] == 1) continue;
				stack.push(v);
			}
		}
		// 戻り処理
		for (int i = c-1; i >= 0; i--) {
			// 頂点 order[i] に対して戻り処理がかける
			// 頂点 order[i] 以降のすべての頂点は戻り処理済
			// ここにはしばしば子ノードに対する繰り返し処理が現れる
		}
	}
}

/**
 * 木 DFS(再帰版) サンプル実装。
 */
class DFSrecursion {
	/** グラフ。各頂点番号から行先頂点番号の List での表現 */
	static List<Integer>[] to;
	/** 頂点ごとに訪問したか */
	static int[] seen;
	
	/**
	 * @param		start		開始頂点番号
	 */
	static void dfs(int start) {
		// ここに行き処理が書ける
		seen[start] = 1;
		for (int v : to[start]) {
			if (seen[v] > 0) continue;
			dfs(v);
		}
		// ここに戻り処理が書ける
	}
}

/**
 * 木 DFS(seen なし再帰版) サンプル実装。
 */
class DFSrecursion2 {
	/** グラフ。各頂点番号から行先頂点番号の List での表現 */
	static List<Integer>[] to;
	
	/**
	 * @param		parent		親頂点番号
	 * @param		start		開始頂点番号
	 */
	static void dfs(int parent, int start) {
		// ここに行き処理が書ける
		for (int v : to[start]) {
			if (v == parent) continue;
			dfs(start, v);
		}
		// ここに戻り処理が書ける
	}
}
