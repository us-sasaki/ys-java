package abdom.location.interval;

import java.util.List;
import java.util.ArrayList;

import abdom.location.Plot;
import abdom.location.Coord;

/**
 * 区間、List<Plot> を与え、label が move となっている区間を
 * 速度によって分割する。分割の仕方は、分散が最小となるようにする。
 * (決定木による回帰での分割法。おそらく二乗和の代わりにエントロ
 *  ピーを使う方法もある)
 *
 */
public class IntervalDivider {
	
	protected List<Plot> plots;
	
	
/*-------------
 * Constructor
 */
	public IntervalDivider(List<Plot> plots) {
		this.plots = plots;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 行程全体を速度分布によって分割する
	 */
	public List<Interval> divideByVelocity(List<Interval> total) {
		// move を抽出する
		List<Interval> moves = new ArrayList<Interval>();
		List<Interval> rests = new ArrayList<Interval>();
		for (Interval i : total) {
			if (i.label.equals("move")) moves.add(i);
			else rests.add(i);
		}
		
		List<Interval> divided = moves;
		for (int i = 0; i < 2; i++) { // 100 はやり過ぎ？ -> 2 にしている
			divided = divideMoves(divided);
		}
		
		divided.addAll(rests);
		
		divided.sort( (a,b) -> (a.sind - b.sind) ); // sind の昇順
		return divided;
	}
	
	/**
	 *
	 */
	private List<Interval> divideMoves(List<Interval> moves) {
		int divi = searchDivide(moves);
		List<Interval> result = new ArrayList<Interval>();
		for (Interval i : moves) {
			if ( (i.sind < divi)&&(divi <= i.eind) ) {
				Interval i1 = new Interval(i.sind, divi-1, plots.get(divi-1).time - plots.get(i.sind).time, "move");
				Interval i2 = new Interval(divi, i.eind, plots.get(i.eind).time - plots.get(divi).time, "move");
				result.add(i1);
				result.add(i2);
			} else {
				result.add(i);
			}
		}
		return result;
	}
	
	/**
	 * 最も分散が小さくなる index を検索する
	 */
	private int searchDivide(List<Interval> target) {
		double minv = Double.MAX_VALUE;
		int mini = -1;
		for (Interval i : target) {
			for (int j = i.sind; j < i.eind+1; j++) {
				double v = calcVariance(target, j);
				if (minv > v) {
					minv = v;
					mini = j;
				}
			}
		}
//System.out.println("最小の分散," + minv);
		return mini;
	}
	
	/**
	 * index で分割した場合の variance を返却する
	 * 分割は、 ～(index-1) , (index)～ の位置で分ける。
	 * Interval は連続していなくてよく、昇順でなくてもよい。
	 * disjoint(never intersects) であることは必要。
	 *
	 * @param	target	分割対象の Interval の List
	 * @param	index	分割箇所
	 * @return	分割した場合の(速度値の)分散
	 */
	private double calcVariance(List<Interval> target, int index) {
		double squErr = 0d;
		int n = 0;
		for (Interval i : target) {
			if ( (i.sind < index)&&(index <= i.eind) ) {
				Interval i1 = new Interval(i.sind, index-1, plots.get(index-1).time - plots.get(i.sind).time, "move");
				Interval i2 = new Interval(index, i.eind, plots.get(i.eind).time - plots.get(index).time, "move");
				squErr += getSquErr(i1) + getSquErr(i2);
			} else {
				squErr += getSquErr(i);
			}
			n += i.eind - i.sind + 1;
		}
		return squErr / n; // Plot の要素数で割る
	}
	
	/**
	 * Interval の List に対し、二乗和を計算する
	 */
	private double getSquErr(List<Interval> target) {
		double squErr = 0d;
		for (Interval i : target) {
			squErr += getSquErr(i);
		}
		return squErr;
	}
	
	/**
	 * Interval に対し、二乗和を計算する
	 */
	private double getSquErr(Interval interval) {
		double sum = 0d;
		for (int i = interval.sind; i <= interval.eind; i++) {
			sum += plots.get(i).velocity;
		}
		double mean = sum / (interval.eind - interval.sind + 1);
		
		double squErr = 0d;
		for (int i = interval.sind; i <= interval.eind; i++) {
			double v = (plots.get(i).velocity - mean);
			squErr += v * v;
		}
		return squErr;
	}
}
