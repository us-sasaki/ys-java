package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Coord;
import abdom.location.Plot;
import abdom.location.PlotUtils;

/**
 * 一定以上の速度となっている点をカットする PlotsFilter。
 * この Filter は元の List を変更し、Plot は速度、距離再計算以外の変更を
 * しません。
 *
 * @author	Yusuke Sasaki
 * @version	2016/9/28
 */
public class VelocityPlotsFilter implements PlotsFilter {
	
	protected double threshold;
	
/*-------------
 * Constructor
 */
	/**
	 * デフォルトの threshold (42m/s = 150km/h) を上限とする
	 * VelocityPlotsFilter を作成します。
	 */
	public VelocityPlotsFilter() {
		this(42d); // 150km/h
	}
	
	/**
	 * 指定された threshold (m/s) を上限とする VelocityPlotsFilter を
	 * 作成します。
	 *
	 * @param	threshold	カット対象とならない速度の上限(m/s)
	 */
	public VelocityPlotsFilter(double threshold) {
		this.threshold = threshold;
	}

/*------------
 * implements
 */
	@Override
	public List<Plot> apply(List<Plot> plots) {
		PlotUtils.calcVelocity(plots); // CutReturning は速度を破壊する
		
		for (int i = 0; i < plots.size(); i++) {
			Plot p = plots.get(i);
			if (p.velocity > threshold) {
				double latxlng = p.latitude * p.longitude; // 同一点をカットする
				// カット
				remove(plots, latxlng);
				i = -1; // 最初から
			}
		}
		return plots;
	}
	
/*------------------
 * instance methods
 */
	private void remove(List<Plot> plots, double latxlng) {
		for (int i = 0; i < plots.size(); i++) {
			Plot p = plots.get(i);
			if (p.latitude * p.longitude == latxlng) {
				// カット
				plots.remove(i);
				if (i >= plots.size()-1) return; // 最後の点だったら終わり
				// 次の点の velocity 再計算
				Plot pre = null;
				if (i == 0) pre = plots.get(0);
				else pre = plots.get(i-1);
				p = plots.get(i); // 次のやつ
				double dist = Coord.calcDistHubeny(p, pre);
				double time = (double)(p.time - pre.time);
				if (time == 0d) p.velocity = 0d;
				else p.velocity = dist/time*1000d;
				p.distance = dist;
				i--;
			}
		}
	}
}
