package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.PlotUtils;

/**
 * 隣り合う複数の Plot の平均位置(重心)を新たな Plot とします。
 * Plot の数は、(group-1)個減少します。時間(time)も平均値とします。
 * accuracy が失われる(-1f が設定される)ことに注意して下さい。
 * 3個の平均化であれば、Accuracy を加味した ULMPlotsFilter が一般に精度が
 * 良くなります。
 * この Filter は新しい List, Plot を生成し、元の List, Plot を変更しません。
 *
 */
public class AveragingPlotsFilter implements PlotsFilter {
	protected int group;
	
/*-------------
 * constructor
 */
	/**
	 * 平均化に使用する点の個数をデフォルト値(3)として
	 * AveragingPlotsFilter を作成します。
	 */
	public AveragingPlotsFilter() {
		this.group = 3;
	}
	/**
	 * 平均化に使用する点の個数を指定して AveragingPlotsFilter を作成します
	 *
	 * @param	group	いくつをグルーピングするか
	 */
	public AveragingPlotsFilter(int group) {
		this.group = group;
	}

/*------------
 * implements
 */
	/**
	 *
	 * @param	plots	平均化する対象の plot
	 */
	@Override
	public List<Plot> apply(List<Plot> plots) {
		List<Plot> result = new ArrayList<Plot>();
		Plot[] groups = new Plot[group]; // memory省力化
		
		loop:
		for (int i = 0; i < plots.size(); i++) {
			int n = 0;
			for (int j = 0; j < plots.size(); j++) {
				if (i+j >= plots.size()) break loop;
				groups[n++] = plots.get(i+j); // copy
				if (n == group) break;
			}
			// 平均化処理
			long startTime = groups[0].time; // 桁あふれさせないための基準値
			double lat = 0d;
			double lng = 0d;
			long time = 0L;
			
			for (int j = 0; j < group; j++) {
				lat += groups[j].latitude;
				lng += groups[j].longitude;
				time += (groups[j].time - startTime);
			}
			Plot newone = new Plot(lat/group, lng/group, (time/group)+startTime, -1f);
			result.add(newone);
		}
		PlotUtils.calcVelocity(result);
		return result;
	}
	
}