package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import abdom.location.Plot;
import abdom.location.PlotUtils;

/**
 * 隣り合う複数の Plot のメジアン(中間値)を新たな Plot とします。
 * Plot の数は、(group-1)個減少します。時間(time)も中間値とします。
 * accuracy が失われる(-1fを設定)ことに注意してください。
 * この Filter は新しい List, Plot を生成し、元の List, Plot を変更しません。
 *
 * @author	Yusuke Sasaki
 * @version	2016/10/7
 */
public class MedianPlotsFilter implements PlotsFilter {
	protected int group;
	
/*-------------
 * constructor
 */
	/**
	 * 中央値抽出に使用する点の個数をデフォルト値(5)として
	 * MedianPlotsFilter を作成します。
	 */
	public MedianPlotsFilter() {
		this.group = 5;
	}
	/**
	 * 中央値検出に使用する点の個数を指定して MedianPlotsFilter を作成します
	 *
	 * @param	group	いくつをグルーピングするか
	 */
	public MedianPlotsFilter(int group) {
		this.group = group;
	}

/*------------
 * implements
 */
	/**
	 *
	 * @param	plots	中央値化する対象の plot
	 */
	@Override
	public List<Plot> apply(List<Plot> plots) {
		List<Plot> result = new ArrayList<Plot>();
		List<Double> xgroups = new ArrayList<Double>(); // memory省力化
		List<Double> ygroups = new ArrayList<Double>();
		List<Long> tgroups = new ArrayList<Long>();
		int mid = group / 2;
		
	loop:
		for (int i = 0; i < plots.size(); i++) {
			xgroups.clear();
			ygroups.clear();
			tgroups.clear();
			for (int j = 0; j < group; j++) {
				if (i+j >= plots.size()) break loop;
				Plot p = plots.get(i+j);
				xgroups.add(p.latitude);
				ygroups.add(p.longitude);
				tgroups.add(p.time);
			}
			// 中央値検出
			xgroups.sort(null);
			ygroups.sort(null);
			tgroups.sort(null);
			
			Plot newone = new Plot(xgroups.get(mid), ygroups.get(mid), tgroups.get(mid), -1f);
			result.add(newone);
		}
		PlotUtils.calcVelocity(result);
		return result;
	}
	
}