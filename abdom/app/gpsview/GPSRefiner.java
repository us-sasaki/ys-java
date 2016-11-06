package abdom.app.gpsview;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.text.SimpleDateFormat;

import java.util.Iterator;
import java.util.Date;

import abdom.data.json.*;
import abdom.location.*;
import abdom.location.filter.PlotsFilter;
import abdom.location.filter.CutReturningCertainPlotsFilter;
import abdom.location.filter.CutOutlierPlotsFilter;
import abdom.location.filter.CutSamePlotsFilter;
import abdom.location.filter.ULMPlotsFilter;
import abdom.location.filter.VelocityPlotsFilter;
import abdom.location.interval.Interval;
import abdom.location.interval.StopPicker;
import abdom.location.interval.StopPicker2;
import abdom.location.interval.IntervalDivider;

/**
 * ファイルを読み、List<Plot>, List<Interval> など行程に関する情報を補正、
 * 保持するクラスです。
 *
 * 写真情報との結合など、経路情報の補正以外の付加価値を付けるのは別のクラス
 * (GPSDecorator)です。Interval 分解は、補正の役に立つことを想定してこちらの
 * クラスに組み込んでいます。
 *
 * アルゴリズムを add し readGPSLog() で修正する、というのが基本の使い方です。
 * このクラスの保持する plots, interval は必ず添え字が対応することを保証しま
 * す。このため、一部の get メソッドで返り値に制約がある場合があります。
 *
 * @see		abdom.app.gpsview.GPSDecorator
 */
public class GPSRefiner {
	protected String gpslogDir	= "gpslog/";
//	protected String jsonDir	= "json/";
	
	protected String filename;
	protected List<Plot> plots;
	protected List<Interval> interval;
	
	protected List<PlotsFilter> filter;
	
/*-------------
 * constructor
 */
	public GPSRefiner() {
		filter = new ArrayList<PlotsFilter>();
	}
	
/*------------------
 * instance methods
 */
	/**
	 * お薦めのアルゴリズムセットを設定します。
	 */
	public void setStandardAlgorithm() {
		// 全く同一点にマーカーを立てる
		// 同一点は、
		// GPS(API)の仕様で衛星情報が取れない場合に、
		// 　①NW(Wifi)による位置推定
		// 　②前回と同一結果を返す
		// という挙動のため起こる、と想定している。
		// 同一点となる確率はほぼ０であり、前回同一の場合情報量もないため削除可能
		
		// 速度 42m/s 以上はカット
		filter.add(new VelocityPlotsFilter(42d));
		
		// 上の①をカット
		filter.add(new CutReturningCertainPlotsFilter());
		
		// 上の②をカット
		filter.add(new CutSamePlotsFilter());
		
		// Smirnov-Grubbs検定で、棄却されなくなるまで外れ値を除去
		filter.add(new CutOutlierPlotsFilter(0.05d));
		
		// 局所的に等速直線運動を仮定してなめらかにする
//		filter.add(new ULMPlotsFilter(10d)); // 10秒以上離れたら補正しない
	}
	
	/**
	 * 任意のアルゴリズムを登録します。
	 */
	public void addAlgorithm(PlotsFilter pf) {
		filter.add(pf);
	}
	
	public void readGPSLog(String filename) throws IOException {
		this.filename = filename;
		plots = PlotUtils.readGPSLog(gpslogDir, filename);
		
		//
		// 点単位の補正を行う
		//
		for (PlotsFilter f : filter) {
			plots = f.apply(plots);
		}
		
		//
		// 区間分析を行う
		//
		
		// 滞留区間を検出する
		StopPicker2 t = new StopPicker2(plots);
		interval = t.divideByStop();
		
		// 速度に関し、二乗誤差(分散)が小さくなるように区間を分割する
		// 速度傾向が変わるところで分割されるはず
		//interval = new IntervalDivider(plots).divideByVelocity(interval);
		
	}
	
	/**
	 * 参照を返すが、List を変更してはならない(intervalとの整合が崩れる)。
	 * 要素に対する変更は可。
	 */
	public List<Plot> getPlots() {	return plots;	}
	
	/**
	 * 参照を返すが、List を変更してはならない(plotsとの整合が崩れる)。
	 * 要素に対する変更は可。
	 */
	public List<Interval> getInterval() {	return interval;	}
	
//	public void setJsonDirectory(String d) {	jsonDir = d;	}
	
	public void setGPSLogDirectory(String d) {	gpslogDir = d;	}
	
	/**
	 * 指定されたファイル名で既定のディレクトリに json ファイルを書き出します。
	 * 文字コードは UTF-8 が使用されます。
	 */
//	public void writeJson(String fname) throws IOException {
//		PlotUtils.writeJson(jsonDir, fname, plots);
//	}
	public void writeVelocity(String fname) throws IOException {
		PlotUtils.writeVelocity(fname, plots);
	}
	public JsonArray getPlotsAsJson() {
		return new JsonArray(Plot.toJson(plots.toArray(new Plot[0])));
	}
	public String getFileName() { return filename; }
	
	/**
	 * plots, interval の整合を保ちつつ、指定された plot を挿入する。
	 *
	 * @param	index	挿入位置(この位置以降の plot が後(右)にずれる)
	 * @param	p		挿入する plot
	 */
	public void addPlot(int index, Plot p) {
		if (index < 0 || index >= plots.size()) throw new IndexOutOfBoundsException("index は 0以上、"+plots.size()+"未満でなければなりません。指定値:"+index);
		plots.add(index, p);
		// interval の index もずらす
		// 挿入された Plot はもともと指定された index を含む interval に
		// 含まれるように interval の長さを拡張する
		boolean needsShift = false;
		for (Interval i : interval) {
			if (i.sind <= index && index <= i.eind) {
				i.eind++;
				needsShift = true;
			} else if (needsShift) {
				i.sind++;
				i.eind++;
			}
		}
	}
	
	/**
	 * plots, interval の整合を保ちつつ、指定された位置の plot を削除する。
	 * 以降の plot は前(左)にずれる。
	 *
	 * @param	index	削除する plot の位置
	 * @return	削除された plot
	 */
	public Plot removePlot(int index) {
		if (index < 0 || index >= plots.size()) throw new IndexOutOfBoundsException("index は 0以上、"+plots.size()+"未満でなければなりません。指定値:"+index);
		Plot p = plots.remove(index);
		boolean needsShift = false;
		Interval disappear = null;
		for (Interval i : interval) {
			if (i.sind <= index && index <= i.eind) {
				i.eind--;
				if (i.eind < i.sind) {
					// interval 消失
					disappear = i; // 後で削除
				}
				needsShift = true;
			} else if (needsShift) {
				i.sind--;
				i.eind--;
			}
		}
		// assert 使ってみる
		assert disappear != null : "disappear is null";
		
		if (!interval.remove(disappear)) throw new InternalError("interval doesn\'t contain disappear.");
		return p;
	}
	
	/**
	 * plots で、plots[index-1].time <= time < plots[index].time
	 * を満たす index を返却します。
	 * 二分探索により検索します。
	 */
	public int getIndexOf(long time) {
		if (time >= plots.get(plots.size()-1).time) return plots.size();
		// 2分探索する
		int l = 0;
		int r = plots.size() -1;
		while (true) {
			int i = (l+r)/2;
			long t = plots.get(i).time;
			if (t == time) return i+1;
			if (t > time) {
				if (r == i) return r;
				r = i;
			} else if (t < time) {
				if (l == i) return r;
				l = i;
			}
		}
	}
	
	/**
	 * 指定された interval 番号の interval の内容を、指定された List<Plot> に
	 * 置き換えます。interval の他の属性は変更しません。
	 */
	public void replaceInterval(int index, List<Plot> p) {
		if (index < 0 || index >= interval.size() )
			throw new IndexOutOfBoundsException("index は 0以上、"+interval.size()+"未満でなければなりません。指定値:"+index);
		//List<Interval> left = interval.subList(0, index);
		List<Interval> right = interval.subList(index+1, interval.size());
		Interval replaced = interval.get(index);
		
		//
		// plots の操作
		//
		
		// eind - sind + 1 個 Plot を削除
		for (int j = 0; j < replaced.eind - replaced.sind + 1; j++) {
			plots.remove(replaced.sind); // 同じ位置を消せばよい
		}
		plots.addAll(replaced.sind, p); // 挿入
		
		//
		// interval の操作
		//
		int newEind = replaced.sind + p.size() - 1;
		int move = newEind - replaced.eind;
		replaced.eind = newEind;
		// 後の interval の index をずらす
		for (Interval i : right) {
			i.sind += move;
			i.eind += move;
		}
	}
	
	/**
	 * 指定された interval の内容を、指定された List<Plot> に置き換えます。
	 * interval の他の属性は変更しません。
	 * 指定された interval が含まれない場合、RuntimeException をスローします。
	 */
	public void replaceInterval(Interval i, List<Plot> p) {
		int index = interval.indexOf(i);
		if (index == -1) throw new RuntimeException("Intervalが含まれません");
		replaceInterval(index, p);
	}
	
	/**
	 * 指定された interval 番号に対応する List<Plot> を返却します。
	 * 結果は、List.subList() オペレーションにより生成されるため、
	 * getPlots() 同様、List を変更してはなりません。
	 */
	public List<Plot> getPlotsOfInterval(int index) {
		Interval i = interval.get(index);
		
		return plots.subList(i.sind, i.eind + 1);
	}
	
	/**
	 * 指定された interval に対応する List<Plot> を返却します。
	 * 結果は、List.subList() オペレーションにより生成されるため、
	 * getPlots() 同様、List を変更してはなりません。
	 */
	public List<Plot> getPlotsOfInterval(Interval i) {
		return plots.subList(i.sind, i.eind + 1);
	}
	
	
}
