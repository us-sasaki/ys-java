package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import abdom.location.Plot;

/**
 * Google Maps で、各点に旗を表示させます。
 * この Filter は、List, Plot(photoFileName以外) を変更しません。
 */
public class MarkerPlotsFilter implements PlotsFilter {
	protected int step;
	
	public MarkerPlotsFilter() {
		this(30);
	}
	
	public MarkerPlotsFilter(int step) {
		this.step = step;
	}
	
	/**
	 * マーク(photoFileNameセット)
	 * 　　マークするのみで削除しない
	 *
	 * @param	target	マークする対象の List<Plot>
	 * @return	マーク済 List<Plot>
	 */
	public List<Plot> apply(List<Plot> plots) {
		int count = 0;
		long t0 = plots.get(0).time;
		for (int i = 0; i < plots.size(); i++) {
			Plot p = plots.get(i);
			if (((i % step) == 0)&&(p.photoFileName == null)) {
				float v = (float)((int)(p.velocity * 10))/10f;
				float t = (float)((p.time - t0)/100L)/10f;
				p.photoFileName = String.valueOf(count++) + ":v="+v+" t="+t; //p.toString();
			}
		}
		return plots;
	}
}
