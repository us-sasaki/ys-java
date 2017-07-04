package abdom.math.stats;

import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.List;

/**
 * 基本的な統計量を求める。
 * T は統計量を求めたいdouble値を出力できるクラス。
 * 実際の値の出力は apply の第２引数(Function<T, Double>)で指定する。
 */
public class Stats<T> {
	public boolean applied = false;
	
	/** 総和 */
	public double sum;
	
	/** 平均 */
	public double mean;
	
	/** 要素数(applyの第２引数がnullを返すものは含まない) */
	public int n;
	
	/** 分散 */
	public double variance;
	
	/** 標準偏差 */
	public double deviation;
	
	/** 最大値 */
	public double max;
	
	/** 最小値 */
	public double min;
	
/*------------------
 * instance methods
 */
	/**
	 * 配列と、double値抽出関数を指定し、統計量を設定します。
	 * 
	 * @param	data	double 値を出力できるクラスの配列
	 * @param	f		Double 値の出力方法。null の場合その値は除外される。
	 * @return	計算された統計量を持つ Stats オブジェクト
	 */
	public static <T> Stats<T> value(T[] data, Function<T, Double> f) {
		Stats<T> stats = new Stats<T>();
		stats.apply(data, f);
		return stats;
	}
	
	/**
	 * 配列と、double値抽出関数を指定し、統計量を設定します。
	 * 
	 * @param	data	double 値を出力できるクラスの配列
	 * @param	f		Double 値の出力方法。null の場合その値は除外される。
	 */
	public void apply(T[] data, Function<T, Double> f) {
		n = 0;
		sum = 0d;
		variance = 0d;
		max = -Double.MAX_VALUE;
		min = Double.MAX_VALUE;
		
		for (int i = 0; i < data.length; i++) {
			Double d = f.apply(data[i]);
			if (d != null) { // d == null は除外する
				n++;
				sum += d;
				if (d > max) max = d;
				else if (d < min) min = d;
			}
		}
		mean = sum / n;
		
		for (int i = 0; i < data.length; i++) {
			Double d = f.apply(data[i]);
			if (d != null) {
				double a = d - mean;
				variance += a*a;
			}
		}
		variance = variance / n;
		deviation = Math.sqrt(variance);
		
		applied = true;
	}
	
	/**
	 * List と、double値抽出関数を指定し、統計量を算出します。
	 *
	 * @param	data	double 値を出力できるクラスのリスト
	 * @param	f		Double 値の出力方法。null の場合、その値は除外される。
	 * @return	計算された統計量を持つ Stats オブジェクト
	 */
	public static <T> Stats<T> value(List<T> data, Function<T, Double> f) {
		Stats<T> stats = new Stats<T>();
		stats.apply(data, f);
		return stats;
	}
	
	/**
	 * List と、double値抽出関数を指定し、統計量を算出します。
	 *
	 * @param	data	double 値を出力できるクラスのリスト
	 * @param	f		Double 値の出力方法。null の場合、その値は除外される。
	 */
	public void apply(List<T> data, Function<T, Double> f) {
		n = 0;
		sum = 0d;
		variance = 0d;
		max = -Double.MAX_VALUE;
		min = Double.MAX_VALUE;
		
		for (T datum: data) {
			Double d = f.apply(datum);
			if (d != null) { // d == null は除外する
				n++;
				sum += d;
				if (d > max) max = d;
				else if (d < min) min = d;
			}
		}
		mean = sum / n;
		
		for (T datum : data) {
			Double d = f.apply(datum);
			if (d != null) {
				double a = d - mean;
				variance += a*a;
			}
		}
		variance = variance / n;
		deviation = Math.sqrt(variance);
		
		applied = true;
	}
	
	/**
	 * List と、List の要素から添え字情報を用いて double を出力する場合の統計量。
	 *<pre>
	 * 例) T = java.awt.Point
	 *     BiFunction = (pList, i) ->
	 *                    ( pList.get(i).x - pList.get( (i==0)?0:i-1 ).x )
	 *     (1つ前の点との x 軸の差) に関する統計量を得る
	 *</pre>
	 *
	 * @param	data	double 値を出力できるクラスのリスト
	 * @param	f		要素を含むリストと添え字から Double を出力する関数
	 * @return	計算された Stats オブジェクト
	 */
	public static <T> Stats<T> value(List<T> data, BiFunction<List<T>, Integer, Double> f) {
		Stats<T> stats = new Stats<T>();
		stats.apply(data, f);
		return stats;
	}
	
	/**
	 * List と、List の要素から添え字情報を用いて double を出力する場合の統計量。
	 *<pre>
	 * 例) T = java.awt.Point
	 *     BiFunction = (pList, i) ->
	 *                    ( pList.get(i).x - pList.get( (i==0)?0:i-1 ).x )
	 *     (1つ前の点との x 軸の差) に関する統計量を得る
	 *</pre>
	 *
	 * @param	data	double 値を出力できるクラスのリスト
	 * @param	f		要素を含むリストと添え字から Double を出力する関数
	 */
	public void apply(List<T> data, BiFunction<List<T>, Integer, Double> f) {
		n = 0;
		sum = 0d;
		variance = 0d;
		max = -Double.MAX_VALUE;
		min = Double.MAX_VALUE;
		
		for (int i = 0 ; i < data.size(); i++) {
			Double d = f.apply(data, i);
			if (d != null) { // d == null は除外する
				n++;
				sum += d;
				if (d > max) max = d;
				else if (d < min) min = d;
			}
		}
		mean = sum / n;
		
		for (int i = 0; i < data.size(); i++) {
			Double d = f.apply(data, i);
			if (d != null) {
				double a = d - mean;
				variance += a*a;
			}
		}
		variance = variance / n;
		deviation = Math.sqrt(variance);
		
		applied = true;
	}
	
	/**
	 * 正規分布(1次元)の確率密度関数
	 *
	 * @param	x		求めたい点
	 * @param	mean	平均値(軸)
	 * @param	dev		標準偏差
	 * @return	正規分布の確率密度関数の値
	 */
	public static double gaussian(double x, double mean, double dev) {
		double var = dev * dev; // 分散
		double t = 1d / Math.sqrt(2d * Math.PI * var); // 係数
		double t2 = (x - mean) * (x - mean) / 2d / var;
		return t * Math.exp(-t2);
	}
	
	/**
	 * このオブジェクトが持つ平均、分散(標準偏差)を用いた正規分布の確率密度関数
	 * 
	 * @param	x		求めたい点
	 * @return	正規分布の確率密度関数の値
	 */
	public double gaussian(double x) {
		double t = 1d / Math.sqrt(2d * Math.PI * variance); // 係数
		double t2 = (x - mean) * (x - mean) / 2d / variance;
		return t * Math.exp(-t2);
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("n     = "+n+"\n");
		sb.append("mean  = "+mean+"\n");
		sb.append("var.  = "+variance+"\n");
		sb.append("dev.  = "+deviation+"\n");
		sb.append("max   = "+max+"\n");
		sb.append("min   = "+min+"\n");
		
		return sb.toString();
	}
}
