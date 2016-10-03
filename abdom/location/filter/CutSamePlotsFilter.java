package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import abdom.location.Plot;
import abdom.location.Coord;

/**
 * 同じ点に戻る点を最初の１つを除いて削除します。
 * この Filter は元の List を変更し、Plot は変更しません。
 * 削除したの velocity 再計算をします。
 */
public class CutSamePlotsFilter implements PlotsFilter {

/*------------------
 * instance methods
 */
 	/**
	 * 2. 全く同じ点に複数行っている場合、最初の１つを除いてすべて outlier とする
	 */
	public List<Plot> apply(List<Plot> plots) {
		Map<Double, Plot> map = new TreeMap<Double, Plot>();
		Plot pre = plots.get(0);
		map.put(pre.latitude, pre);
		for (int i = 1; i < plots.size(); i++) {
			Plot p = plots.get(i);
			Plot previous = map.put(p.latitude, p); // latしか見てない
			if (previous != null) {
				plots.remove(i);
				// velocity 再計算
				if (i >= plots.size() -1) break;
				p = plots.get(i);
				double dist = Coord.calcDistHubeny(p, pre);
				double time = (double)( p.time - pre.time );
				if (time == 0d) p.velocity = 0d;
				else p.velocity = dist / time * 1000d;
				p.distance = dist;
				i--;
			} else {
				pre = p;
			}
		}
		return plots;
	}

}
