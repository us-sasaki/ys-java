package abdom.location.interval;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.Coord;

/**
 * 停止している時間を抽出
 *
 * 停止の定義は以下の通り：
 * 　時間(s～t)において、
 * 　①任意の２点の距離が R(m) 以下
 * 　　(このとき、半径 2*R/sqrt(3) の円内にいる。逆は不成立)
 * 　②t-s が T msec 以上
 */
public class StopPicker {
	private static final double R = 50.0 * Math.sqrt(3) / 2; // 半径 50m の円内
	private static final long	T = 3 * 60 * 1000; // 3 min.
	
	protected List<Plot> plots;
	
/*-------------
 * Constructor
 */
	public StopPicker(List<Plot> plots) {
		this.plots = plots;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 行程全体を stop を含む区間を含むように分割する
	 */
	public List<Interval> divideByStop() {
		List<Interval> ss = pickStopList();
		List<Interval> total = new ArrayList<Interval>();
		int s = 0;
		for (Interval interval : ss) {
			if (interval.sind > s) { // interval 間に gap あり
				total.add(new Interval(s, interval.sind-1, plots.get(interval.sind-1).time - plots.get(s).time, "move"));
			}
			total.add(interval);
			s = interval.eind+1;
		}
		if (s < plots.size() - 1) { // 最後に gap あり
			total.add(new Interval(s, plots.size()-1, plots.get(plots.size()-1).time - plots.get(s).time, "move"));
		}
		return total;
	}
	/**
	 * 候補をすべて抽出し、よいもの(時間の長いもの)を選択する
	 */
	public List<Interval> pickStopList() {
		// 候補を格納するリスト
		List<Interval> r = new ArrayList<Interval>();
		
		for (int i = 0; i < plots.size(); i++) {
			Interval found = searchStandstill(i);
			if (found == null) break; // i 以降は該当がないため break
			r.add(found);
			i = found.sind;
		}
		
		// 並べ替えを行う(自然順序 : time 降順)
		r.sort(null);
		
		for (int i = 0; i < r.size(); i++) {
			// intersect する要素を削除
			for (int j = i+1; j < r.size(); j++) {
				if (r.get(i).intersects(r.get(j))) {
					r.remove(j);
					j--;
				}
			}
		}
		
		r.sort( (a,b) -> (a.sind - b.sind) ); // start index の昇順
		
		return r;
	}
	
	/**
	 * 指定された index 以降に条件を満たす区間があるかどうかを検索する
	 * ある場合、index が最も若いものの index を sind, tind に格納する。
	 * 最も時間の長くなる区間を見つけた方が良いと考えられるが、とりあえず
	 * 最も若いものを抽出する。
	 * 候補をすべて抽出し、よいものを選択する処理を入れることで実現できる。
	 *
	 * @return		見つかった(Interval) / 見つからなかった(null)
	 */
	private Interval searchStandstill(int startIndex) {
		for (int i = startIndex; i < plots.size() - 1; i++) {
			// ei - 1 までは i との距離が R 以内に収まっており、時間も T 以内
			int ei = searchFirstExceedIndex(i);
			if (ei == -1) continue; // 該当がなかった
			
			// 該当があった場合、任意の２点について距離が R 以内となるものが
			// あるかを確認
			for (int j = ei; j > i; j--) {
				long t = plots.get(j).time - plots.get(i).time;
				if (t < T) break;
				if (checkArbitraryPairIsClose(i, j)) {
					Interval result = new Interval(i, j, t, "stop");
					
					return result;
				}
			}
		}
		return null;
	}
	/**
	 * 最初に R を超える index を検索(高速化のための簡易判定)
	 */
	private int searchFirstExceedIndex(int i) {
		if (i == plots.size()-1) return -1;
		Plot p = plots.get(i);
		int j = i + 1;
		for (; j < plots.size(); j++) {
			if (Coord.calcDistHubeny(p, plots.get(j)) > R) break;
		}
		if (j == plots.size()) return j -1;
		// 最初に R を超える index までの時間が T 未満の短時間なら -1 を返却
		if (plots.get(j).time - p.time < T) return -1;
		
		// jまでのすべてのindexについて、i との距離が R 以内、かつ T 以上の時間
		return j;
	}
	
	/**
	 * s <= i <= e となる任意の２点a,bについて d(a,b)<=R となるかをテストする
	 * 時間が一定以上であることはチェックしない
	 */
	private boolean checkArbitraryPairIsClose(int s, int e) {
		for (int i = s; i < e; i++) {
			for (int j = i+1; j < e+1; j++) {
				if (Coord.calcDistHubeny(plots.get(i), plots.get(j)) > R) return false;
			}
		}
		return true;
	}
}
