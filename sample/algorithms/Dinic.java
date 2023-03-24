import java.util.*;

/**
 * Dinic 法
 * https://ikatakos.com/pot/programming_algorithm/graph_theory/maximum_flow
 */
class Dinic {
	private static class Edge {
		/** capacity, destination, index to revert */
		int cap, to, rev;
		Edge(int cap, int to, int rev) {
			this.cap = cap; this.to = to; this.rev = rev;
		}
	}
	int n;
	List<Edge>[] links;
	int[] depth, progress;
	
	@SuppressWarnings("unchecked")
	Dinic(int n) {
		this.n = n;
		links = new List[n];
		for (int i = 0; i < n; i++)
			links[i] = new ArrayList<>();
		progress = new int[n];
	}
	
	void addLink(int from, int to, int cap) {
		links[from].add(new Edge(cap, to, links[to].size()));
		links[to].add(new Edge(0, from, links[from].size() - 1));
	}
	
	/**
	 * 1. s から BFS を行い、各頂点の s からの距離 depth(v) を記憶しておく（距離＝たどるリンク数）
	 * @param s 頂点番号
	 */
	private void bfs(int s) {
		int[] depth = new int[n];
		Arrays.fill(depth, -1);
		
		depth[s] = 0;
		Deque<Integer> q = new ArrayDeque<>();
		q.addFirst(s);
		while (!q.isEmpty()) {
			int v = q.removeFirst();
			for (Edge e: links[v]) {
				if (e.cap > 0 && depth[e.to] < 0) {
					// cap が 0 でなく、まだ到達していないとき、進む
					depth[e.to] = depth[v] + 1;
					q.addLast(e.to);
				}
			}
		}
		this.depth = depth;
	}
	
	/**
	 * 2. s からDFSを行い、t までの経路を1つ見つける
	 * - BFSで求めた depth(v) が増加する方向にのみ移動する
	 * - 各ノードにつき、「探索した結果、t にたどり着けなかった辺」を覚えておく
	 * - 経路が見つかったら、G を更新する
	 * 
 	 * 3. 改めて s からDFSを行う
	 * - やり方は同様
	 * - 前のDFSで、t にたどり着けないとわかった辺は使わない
	 * - 見つかったら、G を更新し、3.に戻る
	 * - 見つからなくなったら、探索した辺をリセットし、1.に戻る
	 * @param v 開始点
	 * @param t ゴール
	 * @param flow 流れの大きさ
	 * @return
	 */
	private int dfs(int v, int t, int flow) {
		if (v == t) return flow;
		List<Edge> linksV = links[v];
		for (int i = progress[v]; i < linksV.size(); i++) {
			progress[v] = i;
			Edge e = linksV.get(i);
			if (e.cap == 0 || depth[v] >= depth[e.to]) continue;
			int d = dfs(e.to, t, Math.min(flow, e.cap));
			if (d == 0) continue;
			e.cap -= d;
			links[e.to].get(e.rev).cap += d;
			return d;
		}
		return 0;
	}
	
	/**
	 * 1. s からBFSを行い、各頂点の s からの距離 depth(v) を記憶しておく（距離＝たどるリンク数）
	 * 2. s からDFSを行い、t までの経路を1つ見つける
 	 * 3. 改めて s からDFSを行う
	 */
	long maxFlow(int s, int t) {
		long flow = 0;
		while (true) {
			bfs(s);
			if (depth[t] < 0) return flow;
			Arrays.fill(progress, 0);
			int currentFlow = dfs(s, t, Integer.MAX_VALUE);
			while (currentFlow > 0) {
				flow += currentFlow;
				currentFlow = dfs(s, t, Integer.MAX_VALUE);
			}
		}
	}

	public static void main(String[] args) {
		// 使い方
		Dinic mf = new Dinic(6);
		mf.addLink(0, 1, 10);
		mf.addLink(0, 3, 4);
		mf.addLink(1, 2, 9);
		mf.addLink(1, 4, 6);
		mf.addLink(2, 5, 8);
		mf.addLink(3, 4, 3);
		mf.addLink(4, 5, 4);
		System.out.println(mf.maxFlow(0, 5)); // ans 12
	}
}
