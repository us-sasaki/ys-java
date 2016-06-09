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
 * @version	June 2nd, 2016
 */
public class MakeFileUsage {
	

/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		
		// csv ファイル読込
		FileList a = FileList.readFiles(".");
		a.setReferencePoint(3);
		
		System.out.println("processing");
		
		String date = sdf.format(new Date());
		
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
		l1.sort(new SizeOrder().reversed()); // size 降順でソート
		
		FileList.writePosJsonFile(a.dateList, l1, "posL1Size"+date+".json");
		List<FileEntry> l1dir = FileList.cutFile(l1, true);
		FileList.writePieChartJsonFile(l1dir, "pieL1Size"+date+".json");
		
		// レベル2で上位3フォルダをサイズ順に表示
		int n = 3;
		if (n > l1dir.size()) n = l1dir.size();
		for (int i = 0; i < n; i++) {
			final String dir = l1dir.get(i).path;
			List<FileEntry> l2big = a.selectAs(
				 e -> { return (e.level == 2 && e.path.startsWith(dir));} );
				 // できるのか(dir指定)、できるらしい(finalの場合(省略可能))
			
			l2big.sort(new SizeOrder().reversed());
			FileList.writePieChartJsonFile(FileList.cut(l2big, 5), "pieL2Size"+date+"_"+i+".json");
		}
		
		// レベル2で増分の大きい順に10個表示
		List<FileEntry> l2 = a.selectLevel(2);
		l2.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l2, 10), "lineL2Inc"+date+".json");
		
		// レベル3で増分の大きい順に10個表示
		List<FileEntry> l3 = a.selectLevel(3);
		l3.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l3, 10), "lineL3Inc"+date+".json");
		//
		// 同一ファイル疑惑を探す
		//
		List<FileEntry> list = a.selectFile(true);
		list.sort(new SizeOrder().reversed()); // サイズ降順
		
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
		
		//
		// 似ているファイルを探す
		//
		
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
					public boolean equals(Entry<String, Integer> a, Entry<String, Integer> b) {
						return a.getValue().equals(b.getValue());
					}
		}.reversed() );
		
		tableContainer = new JsonObject().add("key","top").add("label","チェックフォルダ");
		top = new JsonArray(new JsonType[]{tableContainer});
		
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
		tableContainer.add("values", new JsonArray(new JsonType[] {folder}));
		//
		// JSON ファイルとして出力
		//
		FileList.writeJsonType(top, "similarFile"+date+".json");
		
		//
		// サイズの大きなファイルを出力
		//
		tableContainer = new JsonObject().add("key","top").add("label","現在保存されているサイズの大きいファイル");
		top = new JsonArray(new JsonType[]{tableContainer});
		
		JsonObject jo = new JsonObject();
		jo.add("key","file");
		jo.add("label", "(参考)サイズの大きなファイル");

		jo.add("_values", jsonArrayPut(FileList.cut(list, 20),
			// lambda expression
			(target, src) -> {
				target.add("label", src.path);
				target.add("value", src.size/1024/1024);
				target.add("owner", FileList.reveal(src.owner));
			}
		));
		tableContainer.add("values", new JsonArray(new JsonType[] {jo}));
		
		//
		// JSON ファイルとして出力
		//
		FileList.writeJsonType(top, "bigFile"+date+".json");
		
		
		System.out.println("done!");
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
	private static JsonArray jsonArrayPut(List<FileEntry> l,
							java.util.function.BiConsumer<JsonObject, FileEntry> p) {
		JsonType[] result = new JsonType[l.size()];
		for (int i = 0; i < l.size(); i++) {
			JsonObject jo = new JsonObject();
			p.accept(jo, l.get(i));
			result[i] = jo;
		}
		return new JsonArray(result);
	}
}
