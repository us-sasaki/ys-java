import java.util.*;

/*------------------------------------------------------------
 *     B F S
 */

/**
 * グラフ BFS(queue版) サンプル実装。
 * 最短距離、経路がわかる。ループがわかる。連結成分がわかる。
 */
class BFSqueue {
	/** グラフ構造 各頂点番号から行先頂点番号の List での表現 */
	List<Integer>[] to;
	/** 始点からの距離。あらかじめ -1 を入れると処理後非連結成分は -1 となる */
	int[] d;
	/** 最短経路のための道しるべ */
	int[] from;
	
	void bfs(int start) {
		d = new int[N];
		Arrays.fill(d, -1);
		// bfs
		Deque<Integer> deq = new ArrayDeque<>();
		// 始点を追加
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
		// d に各点の始点からの最短距離が入る
		// 任意の点からの from を辿ると始点までの最短経路がわかる
	}
}

