package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import abdom.location.Plot;

/**
 * 同じ点に複数回行っている場所に photoFileName をセットします。
 * Google Maps では、旗が表示されます。
 * この Filter は、List, Plot(photoFileName以外) を変更しません。
 */
public class MarkSamePlotsFilter implements PlotsFilter {
	/**
	 * 1. 全く同じ点に複数回行っているところをマーク(photoFileNameセット)
	 * 　　マークするのみで削除しない
	 *
	 * @param	target	マークする対象の List<Plot>
	 * @return	マーク済 List<Plot>
	 */
	public List<Plot> apply(List<Plot> target) {
		Map<Double, Plot> map = new TreeMap<Double, Plot>();
		for (Plot p : target) {
			Plot recent = map.put(p.latitude, p); // latしか見てない
			if (recent != null) {
				recent.photoFileName = recent.toString();
				p.photoFileName = p.toString();
			}
		}
		return target;
	}
}
