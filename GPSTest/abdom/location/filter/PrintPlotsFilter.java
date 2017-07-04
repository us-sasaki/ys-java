package abdom.location.filter;

import java.util.List;

import abdom.location.Plot;
import abdom.math.stats.Stats;

/**
 * plots の統計情報を System.out に出力する Filter です。
 * デバッグや、精度改善などに使用することを想定しています。
 * この Filter は List, Plot を変更しません。
 */
public class PrintPlotsFilter implements PlotsFilter {
	protected String title;
	
/*-------------
 * constructor
 */
	/**
	 * デフォルトのタイトルで PrintPlotsFilter を作成します。
	 */
	public PrintPlotsFilter() {
		this("--- Plot\'s Velocity Stats. ---");
	}
	
	/**
	 * タイトルを指定して PrintPlotsFilter を作成します。
	 */
	public PrintPlotsFilter(String title) {
		this.title = title;
	}
	
/*------------
 * implements
 */
	@Override
	public List<Plot> apply(List<Plot> plots) {
		Stats<Plot> vstats = new Stats<Plot>();
		vstats.apply(plots, p -> p.velocity);
		System.out.println(title);
		System.out.println(vstats);
		return plots;
	}
}
