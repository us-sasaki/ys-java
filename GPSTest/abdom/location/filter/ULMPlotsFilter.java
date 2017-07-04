package abdom.location.filter;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.Coord;
import abdom.location.PlotUtils;

/**
 * Uniform Linear Motion
 * 等速直線運動を仮定、Accuracyを加味して位置を補正する。
 *
 * 連続する３点(p(-1), p(0), p(1))について、等速直線運動となるような、
 * ３点のAccuracyを踏まえ確率(尤度)が最大となる軌跡を求める。<br>
 * x(-1), x(0), x(1)
 *    s.t. F(p(-1),x(-1)) * F(p(0),x(0)) * F(p(1),x(1)) が最大
 *         かつ x(-1), x(0), x(1) は等速直線運動
 * ここで F：p()において 標準偏差=accuracy を満たす確率密度関数(正規分布)とする。
 * 
 * 補正は、p(0) -> x(0) のように真ん中の点について行う。
 *
 * この Filter は、新しい List を返却し、元の List を速度、距離を除いて
 * 変更しません。実行後、速度、距離が変更されますが、新しい List に基い
 * た値が設定されているため、元の List としては不正な値となっていること
 * に注意して下さい。
 * 正しくするためには PlotUtils.calcVelocity を元の List に対して使用し
 * てください。
 */
public class ULMPlotsFilter implements PlotsFilter {

	// ２点間の時間が空くと誤差が大きくなる。補正をスキップする閾値
	private double maxTimeRange = 10d;
	
/*-------------
 * constructor
 */
	public ULMPlotsFilter() {
	}
	
	public ULMPlotsFilter(double maxTimeRange) {
		this.maxTimeRange = maxTimeRange;
	}
	
/*------------------
 * instance methods
 */
 	/**
	 * 局所的(10秒程度)に等速直線運動を仮定した補正を行う。
	 * 結果の List, Plot オブジェクトは新規に作成されます。
	 */
	public List<Plot> apply(List<Plot> plots) {
		List<Plot> result = new ArrayList<Plot>();
		
		for (int i = 0; i < plots.size(); i++) {
			// ３点をとる
			Plot a = null;
			if (i == 0) a = plots.get(i); else a = plots.get(i-1);
			Plot b = plots.get(i); // 基準点
			Plot c = null;
			if (i == plots.size()-1) c = plots.get(i); else c = plots.get(i+1);
			
			// 計算
			
			// 時間(sec)
			double s = (double)(b.time - a.time) / 1000d;
			double t = (double)(c.time - b.time) / 1000d;
			
			Plot x = new Plot();
			if ( (s > maxTimeRange)||(t > maxTimeRange) ) {
				// ３点の時間が離れすぎているため、補正しない方がよい
				// x = b としてもよいが、新しいオブジェクトとすることに統一
				x.latitude	= b.latitude;
				x.longitude	= b.longitude;
				x.time		= b.time;
				x.isOutlier	= b.isOutlier;
				x.date		= b.date;
				x.accuracy	= b.accuracy;
			} else {
				// ３点の時間が比較的近い(各点間10秒以内を目安)ので補正
				
				// 標準偏差(m)　の２乗(分散)の逆数
				double va = 1d / (double)a.accuracy / (double)a.accuracy;
				double vb = 1d / (double)b.accuracy / (double)b.accuracy;
				double vc = 1d / (double)c.accuracy / (double)c.accuracy;
				
				// 分母
				double m = va*vc*(t+s)*(t+s)+vb*(va*s*s+vc*t*t);
				
				// a,b,c の係数
				double ka = va*vc*t*(s+t)/m;
				double kb = vb*(va*s*s+vc*t*t)/m;
				double kc = va*vc*s*(s+t)/m;
				
				// x を求める
				
				// たぶん m に合わせなくてよい(局所的に平面と仮定)
				// ka, kb, kc はメートル単位の値だが、そのまま lat,lng
				// に適用している(lat, lng がメートルに比例=局所的に平面　、と仮定)
				x.latitude = ka*a.latitude + kb*b.latitude + kc*c.latitude;
				x.longitude = ka*a.longitude + kb*b.longitude + kc*c.longitude;
				
				// velocity は求めない(最後にまとめてやる)
				
				x.time = b.time;
				x.isOutlier = false;
				x.date = b.date;
				x.accuracy = b.accuracy; // accuracyは変えないでおく
				
				//
				// ちなみに v は
				// (分母) = m で同じ
				// ka = va*va*(-vb*vb*s-vc*vc*t-vc*vc*s) / m
				// kb = vb*vb*(va*va*s-vc*vc*t) / m
				// kc = vc*vc*(va*va*s-va*va*t-vb*vb*t) / m
				//
				// x - sv が1つ前の点
				// x + tv が1つ後の点
			}
			result.add(x);
		}
		result = PlotUtils.calcVelocity(result);
		
		return result;
	}
	
/*--------------
 * test 用 main
 */
	public static void main(String[] args) throws Exception {
		List<Plot> plots = new ArrayList<Plot>();
		
		Plot a = new Plot();
		a.latitude = -1;
		a.longitude = 1;
		a.time = 1000L;
		a.accuracy = 50;
		plots.add(a);
		
		Plot b = new Plot();
		b.latitude = 0;
		b.longitude = 0;
		b.time = 2000L;
		b.accuracy = 50;
		plots.add(b);
		
		Plot c = new Plot();
		c.latitude = 1;
		c.longitude = 1;
		c.time = 3000L;
		c.accuracy = 90;
		plots.add(c);
		
		plots = new ULMPlotsFilter().apply(plots);
		
		for (Plot p : plots) {
			System.out.println(p);
		}
		
	}

}
