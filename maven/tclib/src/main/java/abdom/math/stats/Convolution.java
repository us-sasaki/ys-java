package abdom.math.stats;

import java.util.Collection;
import java.util.List;
//import java.util.Arrays;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

/**
 * 点の集まりから確率密度関数を得る、などの目的で利用する畳込み和演算を提供
 * します。
 * T は Double 値を出力できるオブジェクトで、通常 T[], List&lt;T&gt;
 * でオブジェクト列が指定されます。
 *
 *
 * @version		September 17th, 2017
 * @author		Yusuke Sasaki
 * @param	<T>	double 値を抽出できるクラス
 */
public class Convolution<T> {
	protected T[] array;
	public Stats stats;
	protected Function<T, Double> f;
//	protected double s;
	protected ToDoubleBiFunction<Double, Double> g;
	protected Double[] d;
	
/*-------------
 * constructor
 */
	/**
	 * 与えられたオブジェクトリスト、偏差、double取得関数を用いて、
	 * 正規分布関数での畳み込みを行った度数分布関数を返却します。
	 * リストから取得される double 値の分布を表す関数を作成します。
	 * 各点(double値)は、その値を平均値とする正規分布関数で重みづけされます。
	 *
	 * @param	list	double値を取得できるオブジェクトのリスト
	 * @param	s		畳み込み関数(正規分布関数)の偏差
	 * @param	f		list 要素から double 値を取得する関数
	 */
	@SuppressWarnings("unchecked")
	public Convolution(List<T> list, double s, Function<T, Double> f) {
		this((T[])list.toArray(), s, f);
	}
	
	/**
	 * 与えられたオブジェクトリスト、偏差、double取得関数を用いて、
	 * 正規分布関数での畳み込みを行った度数分布関数を返却します。
	 * リストから取得される double 値の分布を表す関数を作成します。
	 * 各点(double値)は、その値を平均値とする正規分布関数で重みづけされます。
	 *
	 * @param	array	double値を取得できるオブジェクトのリスト
	 * @param	s		畳み込み関数(正規分布関数)の偏差
	 * @param	f		array 要素から double 値を取得する関数
	 */
	public Convolution(T[] array, double s, Function<T, Double> f) {
		this(array, f, ((x, d) -> Stats.gaussian(x, d, s)) );
	}
	
	public Convolution(T[] array, Function<T, Double> f, ToDoubleBiFunction<Double, Double> g) {
		this.array = array;
		stats = Stats.value(array, f);
		this.f = f;
		this.g = g;
		
		d = new Double[array.length];
		int i = 0;
		for (T e : array) {
			d[i++] = f.apply(e);
		}
	}

/*------------------
 * instance method
 */
	public double v(double x) {
		int n = stats.n;
		double y = 0d;
		int i = 0;
		for (T e : array) {
			Double f = d[i++];
			if (f == null) continue; // skip
			y += g.applyAsDouble(x, f)/n; // (1 * g)/n の意味
		}
		return y;
	}
	
/*----------------
 * main for debug
 */
	private static class Elm {
		double e;
	}
	public static void main(String[] args) {
		Elm[] elm = new Elm[3];
		for (int i = 0; i < 3; i++) {
			elm[i] = new Elm();
			elm[i].e = i * i;
		}
		Stats stats = Stats.value(elm, (e -> e.e));
		System.out.println(stats);
		
		Convolution<Elm> c = new Convolution<Elm>(elm, stats.deviation, (e -> e.e));
		
		double sum = 0d;
		for (double x = -5; x < 10; x++) {
			System.out.println("x = " + x + ", c(x) = " + c.v(x));
			sum += c.v(x);
		}
		System.out.println("sum = " + sum);
	}

}
