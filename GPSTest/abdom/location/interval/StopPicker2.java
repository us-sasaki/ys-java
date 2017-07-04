package abdom.location.interval;

import java.util.List;

import abdom.location.Plot;
import abdom.location.filter.AveragingPlotsFilter;
import abdom.location.filter.MedianPlotsFilter;

/**
 * 停止している時間を抽出。Averaging の後に StopPicker の評価を行う。
 *
 * 停止の定義は以下の通り：
 * 　時間(s～t)において、
 * 　①任意の２点の距離が R(m) 以下
 * 　　(このとき、半径 2*R/sqrt(3) の円内にいる。逆は不成立)
 * 　②t-s が T msec 以上
 */
public class StopPicker2 {
	protected int groups = 13; // as default
	protected StopPicker s;
	protected List<Plot> plots;
	
/*-------------
 * constructor
 */
	public StopPicker2(List<Plot> plots) {
		this.plots = plots;
		s = new StopPicker(new MedianPlotsFilter(groups).apply(plots), 30d, 60);
	}
	
/*------------------
 * instance methods
 */
	public List<Interval> divideByStop() {
		List<Interval> sl = s.divideByStop();
		
		// Averaging により groups -1 個少なくなっているので、元に戻す
		int gap = (groups - 1)/2;
		for (Interval i : sl) {
			if (i.sind > 0) i.sind += gap; // 最初を含んでいたら最初から
			assert i.sind >= 0 : "sind < 0";
			if (i.eind == plots.size() - groups) {
				i.eind = plots.size() -1; // 最後を含んでいたら最後まで
			} else {
				i.eind += gap;
			}
			assert i.eind <= plots.size() - groups : "index exceeded";
			
		}
		return sl;
	}
}

