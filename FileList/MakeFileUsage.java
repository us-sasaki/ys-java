import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static java.util.Map.Entry;

/**
 * ファイル利用状況表示用データを生成するメインプログラム
 *
 * @author	Yusuke Sasaki
 * @version	June 19th, 2016
 */
public class MakeFileUsage {
	/** FileList オブジェクト */
	FileList		a;
	/** 実ファイルのみ(!isDirectory)の List */
	List<FileEntry> list;
	/** ファイル名に使用する日付文字列(yyMMdd) */
	String			date;

/*------------------
 * instance methods
 */
	public void make() throws IOException {
		init();
		System.out.println("processing");
		System.out.println("calculating for chart");
		makeCharts();
		System.out.println("finding same files");
		findSameFiles();
		System.out.println("finding similar files");
		findSimilarFiles();
		System.out.println("listing big files");
		listBigFiles();
		System.out.println("listing file usage of users");
		listFileUsage();
		System.out.println("done!");
	}
	
	/**
	 * ファイル読込、初期化
	 */
	private void init() throws IOException {
		// csv ファイル読込
		a = FileList.readFiles(".");
		a.setReferencePoint(12); // 2016/6/17   9); // 2016/6/7
		list = a.selectFile(true); // ファイルだけ抽出
		list.sort(new SizeOrder().reversed()); // サイズ降順
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		
		date = sdf.format(new Date());
	}
	
	private void makeCharts() throws IOException {
		//
		// ファイル命名規則
		// [dataType][Level][SortTarget][yyMMdd].json
		//
		// [dataType] ... pos (for stacked or cumulative chart)
		//                pie (for pie chart)
		//                line (for line chart)
		// [Level]    ... Level at which data lies
		// [SortTarget].. Size / Inc(rease)
		//
		
		// レベル1でサイズ順に表示
		List<FileEntry> l1 = a.selectLevel(1);
		l1 = FileList.cutFile(l1, true);
		l1.sort(new SizeOrder().reversed()); // size 降順でソート
		
		FileList.writeJsonFile(a.dateList, l1, 1, "posL1Size"+date+".json");
		List<FileEntry> l1dir = FileList.cutFile(l1, true);
		FileList.writePieChartJsonFile(l1dir, 1, "pieL1Size"+date+".json");
		
		// レベル2で上位3フォルダをサイズ順に表示
		int n = 3;
		if (n > l1dir.size()) n = l1dir.size();
		for (int i = 0; i < n; i++) {
			final String dir = l1dir.get(i).path;
			List<FileEntry> l2big = a.selectAs(
				 e -> { return (e.level == 2 && e.path.startsWith(dir));} );
				 // できるのか(dir指定)、できるらしい(finalの場合(省略可能))
			
			l2big.sort(new SizeOrder().reversed());
			FileList.writePieChartJsonFile(FileList.cut(l2big, 5), 1, "pieL2Size"+date+"_"+i+".json");
		}
		
		// レベル2で増分の大きい順に10個表示
		List<FileEntry> l2 = a.selectLevel(2);
		l2.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l2, 10), 1, "lineL2Inc"+date+".json");
		
		// レベル3で増分の大きい順に10個表示
		List<FileEntry> l3 = a.selectLevel(3);
		l3.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l3, 10), 2, "lineL3Inc"+date+".json");
	}
	
	/**
	 * 同一ファイル疑惑を探す
	 */
	private void findSameFiles() throws IOException {
		String lastFile = "";
		FileEntry lastF = null;
		
		// TableChart にも登録
		//
		// TableChartのJSON形式は以下
		//
		// top = array[] { table }
		// table    = {	"key":"top",
		//				"label":"ファイル使用量削減に向けたヒント"
		//				"values": [ group1, group2, group3, ... ] }
		//
		// group(n) = { "key":"key",
		//				"label":"表示するラベル名",
		//				"values": [ entry1, entry2, entry3, ... ] }
		//
		// entry(n) = { "label":"表示するラベル名",
		//				"value":"ファイルサイズ",
		//				"owner":"所有者名" }
		
		JsonObject tableContainer = new JsonObject().add("key","top").add("label","同一と思われるファイル：ショートカット化要検討"); // values(Array) は後で指定
		
		JsonArray top = new JsonArray(new JsonType[]{tableContainer});
		
		int c = 0;
		
		for (FileEntry f : list) {
			if (f.size/1024/1024 < 10) break;
			String p = f.path;
			try {
				String filename = FileList.filename(p);
				if (lastFile.equals(filename)) {
					JsonObject same = new JsonObject();
					same.add("key", "same");
					same.add("label", "No."+(c+1)+", "+FileList.filename(f.path));
					JsonType[] jt = new JsonType[2];
					jt[0] = new JsonObject().add("label", lastF.path)
								.add("value", lastF.size/1024/1024)
								.add("owner", FileList.reveal(lastF.owner));
					jt[1] = new JsonObject().add("label", f.path)
								.add("value", f.size/1024/1024)
								.add("owner", FileList.reveal(f.owner));
					same.add("_values", jt); // 複数の場合、自動Array化(1個だとうまくいかないと思われる)
					// 最初は JsonObject として登録されるが、2回目に同一 key で登録
					// するとき、JsonArray に変換される。
					// table chart では JsonArray であることが必要。
					tableContainer.add("values", same); // _ means folded
					c++;
					if (c >= 10) break; // 10個まで
					
				}
				lastFile	= filename;
				lastF		= f;
			} catch (Exception e) {
				System.out.println(e);
				System.out.println(p);
				break;
			}
		}
		//
		// JSON ファイルとして出力
		//
		FileList.writeJsonType(top, "sameFile"+date+".json");
	}
	
	/**
	 * 似ているファイルを探す
	 */
	private void findSimilarFiles() throws IOException {
		
		// 先に候補を絞ると早い
		List<FileEntry> largeFiles =
				FileList.selectAs(a.list,
						e ->
							( (e.size > 200000)&&(!e.isDirectory)&&
							 (e.path.endsWith("pptx") || e.path.endsWith("ppt") ||
							  e.path.endsWith("xlsx") || e.path.endsWith("xls") ||
							  e.path.endsWith("docx") || e.path.endsWith("doc") ||
							  e.path.endsWith("txt") )
							)
				);
		
		List<SimilarFilePicker.FileDistance> fdl = new SimilarFilePicker(largeFiles).getDistanceList();
		
		TreeMap<String, Integer> appearPaths = new TreeMap<String, Integer>();
		for (SimilarFilePicker.FileDistance fd : fdl) {
			if (fd.dist > 5000) break; // dist 5000超えは似ていないファイル
			// fd.a の path を登録
			String path = fd.a.path;
			int idx = path.lastIndexOf('\\');
			if (idx >= 0) {
				path = path.substring(0, idx); // path string
				Integer count = appearPaths.get(path);
				if (count == null) appearPaths.put(path, 1);
				else appearPaths.put(path, count+1);
			}
			// fd.b の path を登録
			path = fd.b.path;
			idx = path.lastIndexOf('\\');
			if (idx >= 0) {
				path = path.substring(0, idx); // path string
				Integer count = appearPaths.get(path);
				if (count == null) appearPaths.put(path, 1);
				else appearPaths.put(path, count+1);
			}
		}
		// List に変換してソートする(降順)
		Set<Entry<String, Integer>> viewSet = appearPaths.entrySet();
		
		List<Entry<String, Integer>> view = new ArrayList<Entry<String, Integer>>();
		for (Entry<String, Integer> ent : viewSet) {
			view.add(ent);
		}
		
		view.sort(new Comparator<Entry<String, Integer>>() {
					public int compare(Entry<String, Integer> a, Entry<String, Integer> b) {
						return a.getValue() - b.getValue();
					}
		}.reversed() );
		
		JsonObject folder = new JsonObject();
		folder.add("key","file");
		folder.add("label", "削除候補が多いと推定される10フォルダ(実験中)");
		
		int count = 0;
		for (Entry<String, Integer> e : view) {
			JsonObject jt = new JsonObject();
			jt.add("label", e.getKey());
			jt.add("value", e.getValue());
			
			folder.add("_values", jt); // 自動Array化に期待
			count++;
			if (count >= 10) break;
		}
		//
		// JSON ファイルとして出力
		//
		FileList.writeJsonType(new JsonArray(new JsonType[] {folder}),
			"similarFile"+date+".json");
	}
	
	/**
	 * サイズの大きなファイルを出力
	 */
	private void listBigFiles() throws IOException {
		JsonObject jo = new JsonObject();
		jo.add("key","file");
		jo.add("label", "(参考)サイズの大きなファイル");

		jo.add("_values", jsonObjectArray(FileList.cut(list, 20),
			// lambda expression
			(target, src) -> {
				target.add("label", src.path)
						.add("value", src.size/1024/1024)
						.add("owner", FileList.reveal(src.owner));
			}
		));
		//
		// JSON ファイルとして出力
		//
		FileList.writeJsonType(new JsonArray(new JsonType[]{jo} ),
			"bigFile"+date+".json");
	}
	
	/**
	 * 人毎のファイル使用量を見る
	 */
	private void listFileUsage() throws IOException {
		Map<String, Long> usage = new TreeMap<String, Long>();
		Map<String, Long> lastUsage = new TreeMap<String, Long>();
		Map<String, Long> actSize = new TreeMap<String, Long>();
		
		// referencePoint(2016/6/7) からの減少量を取得
		// name         : ファイルの所有者名
		// usage        : その使用者のファイルサイズ合計
		// lastUsage    : referencePoint時のファイルサイズ合計
		// reductionRate: 前回からどれだけサイズが減少したか
		// actSize      : 前回からファイル削除や移動をどれだけしたか
		int referencePoint = a.getReferencePoint();
		for (FileEntry f : list) {
			String owner = FileList.reveal(f.owner);
			// usage を取得
			Long size = usage.get(owner);
			if (size == null) usage.put(owner, f.size);
			else usage.put(owner, size + f.size);
			
			// lastUsage を取得
			Long lastSize = lastUsage.get(owner);
			if (size == null) lastUsage.put(owner, f.sizeList.get(referencePoint));
			else lastUsage.put(owner, lastSize + f.sizeList.get(referencePoint));
			
			// actSize を取得(増えた分はカウントしない)
			boolean exists  = false;
			long    maxSize = 0;
			for (int i = referencePoint; i < f.sizeList.size(); i++) {
				if (f.sizeList.get(i) > 0) { // ファイルが存在した
					if (!exists) exists = true;
					if (maxSize < f.sizeList.get(i)) maxSize = f.sizeList.get(i);
				} else if (exists) {
					// 一度存在した後消えた -> deleted
					Long s = actSize.get(owner);
					if (s == null) actSize.put(owner, 1L); //maxSize);
					else actSize.put(owner, s + 1L); //maxSize);
					break; // 二度目はカウントしない
				}
			}
		}
		
		// Json形式で保存(自作形式)
		JsonObject[] memberArray = new JsonObject[usage.keySet().size()];
		
		int idx = 0;
		for (String name : usage.keySet() ) {
			memberArray[idx] = new JsonObject().add("owner", name);
			Long u = usage.get(name);
			memberArray[idx].add("usage", u.toString());
			Long lu = lastUsage.get(name);
			memberArray[idx].add("lastUsage", lu.toString());
			int reductionRate = 0;
			if (0L != u) reductionRate = (int)((lu - u)*100L/lu);
			memberArray[idx].add("reductionRate", String.valueOf(reductionRate));
			Long ds = actSize.get(name);
			memberArray[idx].add("activitySize", String.valueOf(ds));
			idx++;
		}
		FileList.writeJsonType(new JsonArray(memberArray), "usage"+date+".json");
		
		// Json形式で保存(scatterChart形式)
		
		
		
		List<JsonObject> jl = new ArrayList<JsonObject>();
//		JsonObject[] jo = new JsonObject[usage.keySet().size()];
		idx = 0;
		for (String name : usage.keySet() ) {
			if (Math.abs(lastUsage.get(name) - usage.get(name)) < 1024.0) continue;
			if (actSize.get(name) == null) continue;
			
			JsonObject jo = new JsonObject().add("key", name);
			
			JsonObject j = new JsonObject();
			j.add("x", rescale(lastUsage.get(name) - usage.get(name)));
			Long act = actSize.get(name);
			if (act == null) act = new Long(0L);
			j.add("y", rescale(act.longValue()));
			j.add("size", rescale(usage.get(name).longValue()));
			j.add("shape", "circle");
			
			
			jo.add("values", new JsonArray(new JsonType[] { j } ));
			
			jl.add(jo);
//			idx++;
		}
		JsonArray data = new JsonArray(jl.toArray(new JsonObject[0]));
		
		FileList.writeJsonType(data, "activity"+date+".json");
	}
	
	private static double rescale(double x) {
		if (x < 0) return -Math.pow(-x, 0.25d);
		if (x == 0) return 0d;
		return Math.pow(x, 0.25d);
	}
	
	/**
	 * 似ているファイルを抽出し、作業中ファイル疑惑を発信
	 *
	 * 似ているファイル
	 * 　(1) 拡張子が同じであること
	 * 　(2) ファイル名が似ていること(典型例：日付だけ違う)
	 * 　(3) サイズが似ていること
	 *
	 * 　上記、似ている or 似ていない を判定。閾値は統計的に計算する。
	 */
	
	
	/**
	 * 指定された List から指定のルールでデータを抽出し、JsonArray 形式で返却する。
	 *
	 * @param	l	FileEntryのList
	 * @param	p	Listからデータを抽出、JsonObject化してJsonArrayに追加する実装
	 * @return	できあがった JsonArray
	 */
	private static JsonArray jsonObjectArray(List<FileEntry> l,
							java.util.function.BiConsumer<JsonObject, FileEntry> p) {
		JsonType[] result = new JsonType[l.size()];
		for (int i = 0; i < l.size(); i++) {
			JsonObject jo = new JsonObject();
			p.accept(jo, l.get(i));
			result[i] = jo;
		}
		return new JsonArray(result);
	}

/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		new MakeFileUsage().make();
	}
}
