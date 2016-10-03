package abdom.location;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import abdom.data.json.*;
import abdom.math.stats.Stats;


public class PlotUtils {
	/**
	 * 指定されたGPSLogファイルを読み込み、List<Plot> として返却する
	 */
	public static List<Plot> readGPSLog(String gpslogDir, String fname) throws IOException {
		// 読む
		Plot[] plots = File2Plot.read(gpslogDir + fname);
		
		// Arrays.asList(Plot<>) でできる List は Arrays$ArrayList クラスで
		// remove ができない(UnsupportedOperationException)ので手動作成
		List<Plot> result = new ArrayList<Plot>();
		for (Plot p : plots) result.add(p);
		
		return calcVelocity(result);
	}
	
	/**
	 * 速度、距離を(再)計算する。
	 * ある plot の速度は、そのひとつ前の plot との距離、経過時間によって計算される。
	 * 距離の単位は m、時間の単位は msec だが、格納する速度は m/s とする。
	 */
	public static List<Plot> calcVelocity(List<Plot> plots) {
		// 見つからない場合、次の行で ArrayIndexOutOfBounds がスローされる
		plots.get(0).velocity = 0d;
		Plot last = plots.get(0);
		for (int i = 1; i < plots.size(); i++) {
			Plot p = plots.get(i);
			double dist = Coord.calcDistHubeny(last, p);
			double time = (double)(p.time - last.time);
			if (time == 0d) p.velocity = 0d;
			else p.velocity = dist/time * 1000d;
			p.distance = dist;
			last = p;
		}
		
		return plots;
	}
	
	/**
	 * 指定された plots の総行程(m)を計算する
	 */
	public static double calcTotalDistance(List<Plot> plots) {
		double sum = 0d;
		for (Plot p : plots) sum += p.distance;
		return sum;
	}
	
	/**
	 * 指定された Plot[] の基本統計量を表示する
	 * 基本統計量は plot.velocity に関して計算する。
	 *
	 * @param	plots	基本統計量を計算する対象となる Plot[]
	 */
	public static void printStats(List<Plot> plots) {
		Stats<Plot> vStats = new Stats<Plot>();
		vStats.apply(plots, (plot -> plot.velocity) );
		
		System.out.println(vStats);
	}
	
	/**
	 * 指定された Plot[] を、指定されたファイル名(拡張子 .txt -> .json)で保存します。
	 * 文字コードは UTF-8 を指定します。
	 *
	 * @param	fname	保存する Json ファイル名(ただし拡張子 .txt 固定)
	 * @param	plots	保存する Plot データ
	 */
	public static void writeJson(String jsonDir, String fname, List<Plot> plots) throws IOException {
		// outlier のマークが付けられたものを除いて Json 変換
		JsonType[] jt = Plot.toJson(plots.toArray(new Plot[0]));
		
		// ファイル名を生成
		int idx = fname.indexOf(".txt");
		String jsonfname = fname.substring(0, idx);
		
		// 書く
		PrintWriter p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsonDir+jsonfname+".json"), "UTF-8"));
		p.println(new JsonArray(jt));
		p.close();
	}
	
	/**
	 * 指定された Plot[] を、指定されたファイル名(拡張子 .txt -> .csv)で保存する
	 * 時間と速度の関係を csv で保存する。
	 *
	 * @param	fname	保存する csv ファイル名
	 * @param	plots	保存する Plot データ
	 */
	public static void writeVelocity(String fname, List<Plot> plots) throws IOException {
		
		// ファイル名を生成
		int idx = fname.indexOf(".txt");
		String csvfname = fname.substring(0, idx);
		
		// 書く
		PrintWriter p = new PrintWriter(new FileWriter(csvfname+".csv"));
		for (Plot plot : plots) {
			p.println(plot.time + "," + (plot.time-1463797343309L)/1000 + "," + plot.velocity);
		}
		
		p.close();
	}
	
	/**
	 * 指定された Plot[] の重心を求める
	 * lat, lng, time は重心とする
	 */
	public static Plot calcCentroid(List<Plot> plots) {
		double slat = 0d;
		double slng = 0d;
		long stime = 0L;
		float acc = Float.MAX_VALUE;
		
		for (Plot p: plots) {
			slat += p.latitude;
			slng += p.longitude;
			stime += p.time;
			if (acc > p.accuracy) acc = p.accuracy;
		}
		int n = plots.size();
		Plot result = new Plot(slat/n, slng/n, stime/n, acc);
		
		return result;
	}
	
	
	
}
