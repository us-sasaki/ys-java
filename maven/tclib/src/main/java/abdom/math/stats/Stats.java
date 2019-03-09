package abdom.math.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.BiFunction;

import java.util.function.Supplier;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;

import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics;


/**
 * 基本的な統計量を保持するクラスです。
 * リストなどからオブジェクトの値を設定するメソッドを提供します。
 * このクラスでは double 値の列を入力とし、その基本統計量を算出、保持します。
 * double 値の入力にソースとなるクラスを指定できますが、null を返却するものは
 * 対象外とします。
 */
public class Stats {
	public boolean applied = false;
	
	/** 総和 */
	public double sum;
	
	/** 平均 */
	public double mean;
	
	/** 要素数 */
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
	 * @param	data	double 値の配列
	 * @return	計算された統計量を持つ Stats オブジェクト
	 */
	public static Stats value(double[] data) {
		Stats stats = new Stats();
		stats.apply(data);
		return stats;
	}
	
	/**
	 * 配列と、double値抽出関数を指定し、統計量を設定します。
	 * 
	 * @param	<T>		double 値を出力できるクラス
	 * @param	data	double 値を出力できるクラスの配列
	 * @param	f		Double 値の出力方法。null の場合その値は除外される。
	 * @return	計算された統計量を持つ Stats オブジェクト
	 */
	public static <T> Stats value(T[] data, Function<? super T, Double> f) {
		Stats stats = new Stats();
		stats.apply(data, f);
		return stats;
	}
	
	/**
	 * 配列と、double値抽出関数を指定し、統計量を設定します。
	 * 
	 * @param	<T>		double 値を出力できるクラス
	 * @param	data	double 値を出力できるクラスの配列
	 * @param	f		Double 値の出力方法。null の場合その値は除外される。
	 */
	public <T> void apply(T[] data, Function<? super T, Double> f) {
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
				if (d < min) min = d;
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
	 * @param	<T>		double 値を出力できるクラス
	 * @param	data	double 値を出力できるクラスのリスト
	 * @param	f		Double 値の出力方法。null の場合、その値は除外される。
	 * @return	計算された統計量を持つ Stats オブジェクト
	 */
	public static <T> Stats value(Iterable<T> data, Function<? super T, Double> f) {
		Stats stats = new Stats();
		stats.apply(data, f);
		return stats;
	}
	
	/**
	 * double配列を指定し、統計量を算出します。
	 *
	 * @param	data	double 値を出力できるクラスのリスト
	 */
	public void apply(double[] data) {
		n = 0;
		sum = 0d;
		variance = 0d;
		max = -Double.MAX_VALUE;
		min = Double.MAX_VALUE;
		
		for (double d : data) {
			if (!Double.isNaN(d)) { // d == NaN は除外する
				n++;
				sum += d;
				if (d > max) max = d;
				if (d < min) min = d;
			}
		}
		mean = sum / n;
		
		for (double d : data) {
			if (!Double.isNaN(d)) {
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
	 * @param	<T>		double 値を出力できるクラス
	 * @param	data	double 値を出力できるクラスのリスト
	 * @param	f		Double 値の出力方法。null の場合、その値は除外される。
	 */
	public <T> void apply(Iterable<T> data, Function<? super T, Double> f) {
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
				if (d < min) min = d;
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
	 *     BiFunction = (pList, i) -&gt;
	 *                    ( pList.get(i).x - pList.get( (i==0)?0:i-1 ).x )
	 *     (1つ前の点との x 軸の差) に関する統計量を得る
	 *</pre>
	 *
	 * @param	<T>		double 値を出力できるクラス
	 * @param	data	double 値を出力できるクラスの Iterable
	 * @param	f		Iterable から生成されるリストと添え字から Double を
	 *					出力する関数
	 * @return	計算された Stats オブジェクト
	 */
	public static <T> Stats value(Iterable<T> data, BiFunction<List<? super T>, Integer, Double> f) {
		Stats stats = new Stats();
		stats.apply(data, f);
		return stats;
	}
	
	/**
	 * List と、List の要素から添え字情報を用いて double を出力する場合の統計量。
	 *<pre>
	 * 例) T = java.awt.Point
	 *     BiFunction = (pList, i) -&gt;
	 *                    ( pList.get(i).x - pList.get( (i==0)?0:i-1 ).x )
	 *     (1つ前の点との x 軸の差) に関する統計量を得る
	 *</pre>
	 *
	 * @param	<T>		double 値を出力できるクラス
	 * @param	data	double 値を出力できるクラスの Iterable
	 * @param	f		Iterable から生成されるリストと添え字から Double を
	 *					出力する関数
	 */
	public <T> void apply(Iterable<T> data, BiFunction<List<? super T>, Integer, Double> f) {
		n = 0;
		sum = 0d;
		variance = 0d;
		max = -Double.MAX_VALUE;
		min = Double.MAX_VALUE;
		
        List<T> list = null;
        if (data instanceof List) list = (List<T>)data;
        else {
            list = new ArrayList<T>();
            data.iterator().forEachRemaining(list::add);
        }
		
		int i = 0;
		for (T datum : data) {
			Double d = f.apply(list, i++);
			if (d != null) { // d == null は除外する
				n++;
				sum += d;
				if (d > max) max = d;
				if (d < min) min = d;
			}
		}
		mean = sum / n;
		
		i = 0;
		for (T datum : data) {
			Double d = f.apply(list, i++);
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
	 * 正規分布(1次元)の確率密度関数の微分
	 *
	 * @param	x		求めたい点
	 * @param	mean	平均値(軸)
	 * @param	dev		標準偏差
	 * @return	正規分布の確率密度関数の微分値
	 */
	public static double dgaussian(double x, double mean, double dev) {
		return -(x-mean)/dev/dev*gaussian(x, mean, dev);
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
	
	/**
	 * 正規分布(1次元)の確率密度関数の微分
	 *
	 * @param	x		求めたい点
	 * @return	正規分布の確率密度関数の微分値
	 */
	public double dgaussian(double x) {
		return -(x-mean)/variance*gaussian(x);
	}
	
	private static final Set<Characteristics> CH_NOID = Collections.emptySet();
	
	/**
	 * ストリームの終端操作で利用できる collector を返却します。
	 *
	 * @param		<T>		double 値を出力できる型
	 * @param		f		T から double を取得する関数
	 * @return		Stats への collector
	 */
	@SuppressWarnings("unchecked")
	public static <T> Collector<T, ?, Stats>
						collector(Function<? super T, Double> f) {
	
		return new CollectorImpl<T, List<T>, Stats>(
						(Supplier<List<T>>) ArrayList::new,
						List::add,
						(l, r) -> {	l.addAll(r); return l;	},
						(d) -> Stats.value(d, f),
						CH_NOID);
	}
	
/*--------------------
 * inner static class
 */
	private static class CollectorImpl<T, A, R> implements Collector {
		private final Supplier<A> supplier;
		private final BiConsumer<A, T> accumulator;
		private final BinaryOperator<A> combiner;
		private final Function<A, R> finisher;
		private final Set<Characteristics> characteristics;

		CollectorImpl(Supplier<A> supplier,
					  BiConsumer<A, T> accumulator,
					  BinaryOperator<A> combiner,
					  Function<A,R> finisher,
					  Set<Characteristics> characteristics) {
			this.supplier = supplier;
			this.accumulator = accumulator;
			this.combiner = combiner;
			this.finisher = finisher;
			this.characteristics = characteristics;
		}
		
		@Override
		public BiConsumer<A, T> accumulator() {
			return accumulator;
		}

		@Override
		public Supplier<A> supplier() {
			return supplier;
		}

		@Override
		public BinaryOperator<A> combiner() {
			return combiner;
		}

		@Override
		public Function<A, R> finisher() {
			return finisher;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(n="+n+";");
		sb.append("mean="+mean+";");
		sb.append("var.="+variance+";");
		sb.append("dev.="+deviation+";");
		sb.append("max="+max+";");
		sb.append("min="+min+")");
		
		return sb.toString();
	}
}
