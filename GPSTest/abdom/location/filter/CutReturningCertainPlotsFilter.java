package abdom.location.filter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import abdom.location.Plot;
import abdom.location.PlotUtils;

/**
 * 連続せず同じ点に戻った場合、戻った点と元の点をともに削除します。
 * 戻った点の直後に関しては、連続する同一点を削除します。
 * CutSamePlotsFilter を実行すると該当する点は削除されてしまうため、
 * 実行順序に注意してください。
 * 同じ点であるかどうかは、latitude の double 値が == かどうかで判定
 * しています。longitude は見ていません。
 * この Filter は元の List を変更しますが、Plot は変更しません。
 * 削除した後 velocity 再計算をします。
 */
public class CutReturningCertainPlotsFilter implements PlotsFilter {
	/**
	 * 3. 連続せずに同じ点に戻る挙動があった場合、その点を outlier とする
	 *    注意： 2.をやると該当する点はなくなってしまう
	 *
	 *    ※　結構効果があるようだ
	 *
	 * ★★★ List化して結果が変わっている ★★★
	 */
	public List<Plot> apply(List<Plot> plots) {
		Map<Double, Plot> map = new TreeMap<Double, Plot>();
		Plot lastPlot = plots.get(0);
		map.put(lastPlot.latitude, lastPlot);
		for (int i = 1; i < plots.size(); i++) {
			Plot p = plots.get(i);
			Plot previous = map.put(p.latitude, p); // latしか見てない
			if ( (previous != null)&&(lastPlot != previous) ) {
				// 同一の点に戻っており、かつ直前でない
				if (plots.remove(previous)) i--;
				if (plots.remove(p)) i--;
				// p が削除点になったときは、すぐ続く同一点も削除するため、
				// lastPlot を更新しない
			} else {
				// 削除されなかった最後の点
				lastPlot = p; 
			}
		}
		PlotUtils.calcVelocity(plots);
		return plots;
	}
	

}
