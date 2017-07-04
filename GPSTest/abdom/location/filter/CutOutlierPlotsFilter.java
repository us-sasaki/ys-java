package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.Coord;
import abdom.location.PlotUtils;
import abdom.math.stats.Stats;
import abdom.math.stats.Tdist;

/**
 * 速度に関し、Smirnov-Grubbs 検定を行う Filter です。
 * デフォルトの棄却率、最大カット割合はともに５％です。
 * 区間ごとに速度が異なる場合、もっとも多い速度からはずれた速度が逐次
 * カットされていきます。
 * したがって誤差が正規分布に従うような場合にこの filter は最も有効です。
 * そうでない場合、maxCutRate を調整することで改善することがあります。
 * もっと良いのは、混合ガウスモデル等の分類を用いて各区間の平均速度が
 * あまり変わらないように分割した後で区間単位にこのfilter を使うことと
 * 思われます。
 * この Filter は元の List を変更し、Plot は速度、距離再計算以外の変更を
 * しません。
 *
 * @author	Yusuke Sasaki
 * @version	2016/9/24
 */
public class CutOutlierPlotsFilter implements PlotsFilter {

	/** 5%検定 */
	private double rejection = 0.05d;
	
	private double maxCutRate = 0.05d; // 5%
	
/*-------------
 * Constructor
 */
	public CutOutlierPlotsFilter() {
	}
	
	/**
	 * 検定率(棄却率)を指定して Smirnov-Grubbs検定を行う filter を作成します。
	 *
	 * @param	rejection	棄却率(0.05 = 両側 5% をはずれと見なす)
	 */
	public CutOutlierPlotsFilter(double rejection) {
		this.rejection = rejection;
	}
	
	/**
	 * 検定率(棄却率)、最大カット割合を指定して Smirnov-Grubbs検定を行う
	 * filter を作成します。
	 * 
	 * @param	rejection	棄却率(0.05 = 両側 5% をはずれと見なす)
	 * @param	maxCutRate	最大カット割合(0.05 = 全体の 5% カットしたら終了)
	 */
	public CutOutlierPlotsFilter(double rejection, double maxCutRate) {
		this.rejection = rejection;
		this.maxCutRate = maxCutRate;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 4. 速度について、外れ値を除外する(Smirnov-Grubbs検定)
	 * 指定された plots の velocity は最初に再計算されます。
	 *
	 * @param	plots	外れ値除去を行う対象
	 * @return	外れ値除去を行った結果
	 */
	public List<Plot> apply(List<Plot> plots) {
		plots = PlotUtils.calcVelocity(plots); // non outlier を対象に再計算
		int count = 0;
		int maxCount = (int)(plots.size() * maxCutRate);
		while (true) {
			int oi = chooseOneOutlier(plots);
			if (oi == -1) break; // 棄却するものはない
			count++;
			if (count >= maxCount) break;
		}
		return plots;
	}
	
	
	//
	// outlier フラグをたて、必要なやつだけ velo 再計算する
	//
	private int chooseOneOutlier(List<Plot> plots) {
		// 速度に関する基本統計量を求める
		Stats<Plot> vs = new Stats<Plot>();
		vs.apply(plots, (plot -> plot.velocity ));
		
		// スミルノフ・グラブスのγを求める
		// t : 自由度 n-2 の t分布の (α/ｎ×100) パーセンタイル
		double t = Tdist.dist(vs.n - 2, rejection/vs.n*100); // 定数(95パーセンタイル)
		double gamma = ((vs.n-1) * t)/Math.sqrt(vs.n * (vs.n-2) + vs.n * t * t);
		
		// 検定する
		int index = -1;
		double maxv = gamma; // γを超えるものは外れ値
		
		for (int i = 0; i < plots.size(); i++) {
			// 両側検定しているが、実質片側
			double v = Math.abs(plots.get(i).velocity - vs.mean) / vs.deviation;
			if (v > maxv) { // 外れ値発見
				index = i;
				maxv = v;
			}
		}
		
		if (index == -1) return index; // 外れ値がなかった
		plots.remove(index); // 削除
		
		// 次の non outlier の velocity の修正が必要
		if (index == 0) { // 最初の plot がマークされた
			plots.get(0).velocity = 0d; // デフォルト値
		} else if (index <= plots.size() - 1) {
			double dist = Coord.calcDistHubeny(plots.get(index-1), plots.get(index));
			double time = (double)(plots.get(index).time - plots.get(index-1).time);
			if (time == 0d) plots.get(index).velocity = 0d;
			else plots.get(index).velocity = dist/time * 1000d;
			plots.get(index).distance = dist;
		}
		return index;
	}
}
