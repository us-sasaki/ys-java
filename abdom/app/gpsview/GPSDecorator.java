package abdom.app.gpsview;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.text.SimpleDateFormat;

import java.util.Date;

import abdom.data.json.*;
import abdom.location.*;
import abdom.location.filter.CutReturningCertainPlotsFilter;
import abdom.location.filter.CutOutlierPlotsFilter;
import abdom.location.filter.ULMPlotsFilter;
import abdom.location.interval.Interval;
import abdom.location.interval.StopPicker;
import abdom.location.interval.IntervalDivider;
import abdom.math.stats.Stats;
import abdom.image.exif.MyExifUtils;

/**
 * GPSRefiner を入力として、photoFileName など、Google Maps に渡す情報を生成します。
 * また、Webにアップロードするための最終的なファイルを生成し、アップロードします。
 */
public class GPSDecorator {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");
	private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd(EEE) HH:mm");
	
//	protected String jsonDir	= "json/";
	
//	protected String photoUrl	= "img/";
	
	protected SortedMap<Long, String> photoMap;
	
	protected boolean manual = false;
	
	protected List<String> photoList;
	
/*-------------
 * constructor
 */
	public GPSDecorator() {
		photoMap = new TreeMap<Long, String>();
	}
	
/*------------------
 * instance methods
 */
	public void addPhotoDirectory(String photoDir) {
		if ( (!photoDir.endsWith("\\"))&&(!photoDir.endsWith("/")) )
			throw new IllegalArgumentException("directoryは \\ または / で終わる必要があります");
		// photo map (ファイル名と時間)を取得しておく
		File dir = new File(photoDir);
		String[] p = dir.list();
		if (p == null) return;
		for (String pname : p) {
			// 最後が .jpg で終わる(jpg は大文字、小文字区別しない (?i) )
			if (pname.matches(".+(?i)jpg$")) {
				Date date = null;
				try {
					date = MyExifUtils.getJpegDate(photoDir + pname);
				} catch (IOException e) {
					System.out.println(e);
				}
				if (date == null) {
					System.out.println(pname + " ファイルから日時を取得できません");
					continue;
				}
				photoMap.put(date.getTime(), photoDir + pname);
			}
		}
	
	}
	/**
	 * GPSRefiner の plots の写真情報 (photoFileName) フィールドを設定します。
	 * 写真の示す時間の plot が新規に挿入されます。
	 * setStops() では plots が縮退(削減)され、通例写真はある時間止まって撮影
	 * されることから削減されてしまうことがあります。
	 * これを防ぐため、このメソッドは setStops() の後に呼ぶようにして下さい。
	 *
	 * @param	g	plots を保持する GPSRefiner
	 * @see		GPSRefiner
	 */
	public void setPhotoFileName(GPSRefiner g) {
		manual = true;
		
		photoList = new ArrayList<String>();
		
		// photoMap の情報を map に挿入(適切な位置に Plot として挿入する)
		List<Plot> plots = g.getPlots();
		
		long startTime	= plots.get(0).time;
		long endTime	= plots.get(plots.size()-1).time;
		
		for (Long pmapTime : photoMap.keySet()) {
			long time = pmapTime;
			if ( (startTime <= time)&&(time <= endTime) ) {
				// 写真の日時が指定されたファイルの時間に含まれている
//System.out.println(photoMap.get(pmapTime) + " を " + g.filename + " に挿入します");
				
				// 近い日時(前後)を取得
				int i = g.getIndexOf(time);
				int prev = i - 1;
				if (prev < 0) prev = 0;
				
				Plot pre = plots.get(prev);
				Plot post = plots.get(i);
				
				// geodesic でなく、直線で計算(手抜き)
				double rate;
				if (post.time == pre.time) rate = 0d;
				else rate = ((double)(time - pre.time))/((double)(post.time - pre.time));
				
				double lat = (1.0d - rate) * pre.latitude + rate * post.latitude;
				double lng = (1.0d - rate) * pre.longitude + rate * post.longitude;
				
				// Plot オブジェクト生成
				Plot photoPlot = new Plot(lat, lng, time, (pre.accuracy + post.accuracy)/2f);
				photoPlot.photoFileName = "photo:" + getFileName(photoMap.get(pmapTime));
				
				// plots, interval の整合を取りつつ挿入
				g.addPlot(i, photoPlot);
				
				// photoList に追加
				photoList.add(photoMap.get(pmapTime));
			}
		}
	}
	
	/**
	 * path 文字列のうち、最後の部分(ファイル名)を抽出し、返却する
	 * path 区切り文字は \ または / とする。
	 */
	private String getFileName(String path) {
		int sl = path.lastIndexOf('/');
		int bl = path.lastIndexOf('\\');
		if (sl == -1 && bl == -1) return path;
		return path.substring( (sl > bl)? sl+1 : bl+1);
	}
	
	/**
	 * 指定された GPSRefiner のもつ stop interval を検出し、
	 * この interval を intervalの始点、重心、終点　の３点に集約します。
	 * i.e. 設定した GPSRefiner の plots, interval が変更されます。
	 * また、重心の位置の photoFileName に stop: タグをつけ、Google Maps
	 * 上にコーヒーアイコンを表示するようにします。
	 * interval を集約するため、setPhotoFileName の後に呼ぶと
	 * もともと stop 区間中にあった photoFileName が削除されてしまうため、
	 * 注意してください。
	 *
	 * @param	g	plots, interval を保持する GPSRefiner。
	 */
	public void setStops(GPSRefiner g) {
		manual = true;
		
		List<Interval> interval = g.getInterval();
		for (Interval i : interval) {
			if (i.label.equals("stop")) {
				List<Plot> stops = g.getPlotsOfInterval(i);
				List<Plot> replacement = new ArrayList<Plot>();
				
				Plot sp = stops.get(0);
				replacement.add(sp); // 最初の点
				
				Plot centroid = PlotUtils.calcCentroid(stops);
				replacement.add(centroid); // 重心
				
				Plot ep = stops.get(stops.size()-1);
				replacement.add(ep); // 最後の点
				
				centroid.photoFileName = "stop:"+((ep.time-sp.time)/60000L) + "min.";
				
				g.replaceInterval(i, replacement);
			}
		}
	}
	
	/**
	 * GPSRefiner の持つ情報の総括情報を JsonObject で返却する
	 *<pre>
	 *      log      : plotログファイル名
	 *     title     : タイトル
	 * totalDistance : 総行程(m)
	 *     stops     : 滞留回数
	 *  totalTime    : 移動時間(s)(総時間から滞留時間を除いたもの)
	 * averageSpeed  : 平均速度(km/h) (滞留は除く)
	 *     plots     : plotログファイルに含まれる点の数
	 *   maxSpeed    : 最大速度(km/h) 誤差により大きくなる傾向がある
	 * meanPlotInterval : plot平均間隔(s) 滞留時間を含むので大きいことがある
	 *  startDate    : 開始日時
	 *  finishDate   : 終了日時
	 *</pre>
	 */
	public JsonObject getMetaInfoAsJson(GPSRefiner g) {
		JsonObject jo = new JsonObject();
		// plots Jsonファイル名
		String fname = g.getFileName();
		int i1 = fname.lastIndexOf('\\');
		int i2 = fname.lastIndexOf('/');
		if (i1 > i2) fname = fname.substring(i1+1);
		else if (i1 < i2) fname = fname.substring(i2+1);
		
		jo.add("log", fname.substring(0, fname.length()-3)+"json");
		
		// タイトル
		i1 = fname.lastIndexOf('.');
		if (i1 == -1) jo.add("title", fname.substring(6));
		else jo.add("title", fname.substring(6, i1));
		
		// 統計情報
		List<Plot> plots = g.getPlots();
		// 総行程(m)
		double dist = PlotUtils.calcTotalDistance(plots);
		jo.add("totalDistance", ((int)(dist/100d))/10f);
		// 滞留回数とトータル時間
		int sc = 0;
		long time = 0;
		for (Interval i : g.getInterval()) {
			if (i.label.equals("stop")) sc++;
			else {
				time += plots.get(i.eind).time - plots.get(i.sind).time;
			}
		}
		jo.add("stops", sc);
		// トータル時間(sec)、ただしとまってる間は除く
		time = time/1000L;
		jo.add("totalTime", time);
		// 平均速度(km/h)
		jo.add("averageSpeed", ((int)(dist/time*3600/1000*10))/10f);
		
		// 速度情報
		Stats<Plot> vstats = new Stats<Plot>();
		vstats.apply(plots, p -> p.velocity);
		
		jo.add("plots", vstats.n);
		jo.add("maxSpeed", ((int)(vstats.max*3600/1000*10))/10f);
		
		// plot 情報
		Stats<Plot> tstats = new Stats<Plot>();
		tstats.apply(plots, (pl, i) -> (double)(pl.get(i).time - pl.get( (i==0)?0:i-1).time));
		jo.add("meanPlotInterval", (long)(tstats.mean/1000));
		
		// photo 情報
		int photos = 0;
		for (Plot p : plots) {
			if ((p.photoFileName != null)
					&&(p.photoFileName.startsWith("photo:"))) photos++;
		}
		jo.add("photos", photos);
		
		jo.add("startDate", sdf2.format(new Date(plots.get(0).time)));
		jo.add("finishDate", sdf2.format(new Date(plots.get(plots.size()-1).time)));
		
		return jo;
	}
	
	/**
	 * GPSRefiner を作成し、GPSDecorator による修飾を行い、ファイルとして格納
	 * します。Json ファイルの文字コードは UTF-8 を指定します。
	 * GPSRefiner の各種メソッド、setStops(), setPhotoFileName() は内部で呼ばれる
	 * ため、外部から呼んではいけません。IllegalStateException がスローされます。
	 */
	public void processAllLogs(String gpsLogDir, String jsonDir) throws IOException {
		if (manual) throw new IllegalStateException("setStops(), setPhotoFileName() を呼んだ場合、このメソッドは使用できません");
		
		File dir = new File(gpsLogDir);
		List<String> flist = new ArrayList<String>();
		String[] f = dir.list();
		for (String fname : f) {
			// . 一文字 + 任意個の直前の文字 + txt 終わり($)
			if (fname.matches("GPSLog.+txt$")) flist.add(fname);
		}
		flist.sort(null);
		
		List<JsonObject> joList = new ArrayList<JsonObject>();
		for (String fname : flist) {
System.out.println("processing.. " + fname);
			GPSRefiner g = new GPSRefiner();
			g.setGPSLogDirectory(gpsLogDir);
			g.setStandardAlgorithm();
			g.readGPSLog(fname);
			
			setStops(g);
			setPhotoFileName(g);
			
			if (g.getPlots().size() < 30) {
				System.out.println("    30plots に満たないため、スキップします");
				continue;
			}
			
			joList.add(getMetaInfoAsJson(g));
			
			// ファイル書き込み
			g.setJsonDirectory(jsonDir);
			g.writeJson(fname);
		}
		// meta data (GPSMetaData.json) 書き込み
		JsonArray ja = new JsonArray(joList.toArray(new JsonObject[0]));
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsonDir+"GPSMetaData.json"), "UTF-8"));
		pw.println(ja);
		pw.close();
	}
}
