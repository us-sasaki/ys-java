import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

/**
 * ファイル利用状況表示用データを生成するメインプログラム
 *
 * @author	Yusuke Sasaki
 * @version	June 2nd, 2016
 */
public class MakeFileUsage {
	
	/**
	 * 指定されたディレクトリのファイルを読み込む
	 * list20yyMMdd.csv のような形のファイルをすべて読み込む
	 */
	public static FileList readFiles(String path) throws IOException {
		File dir = new File(path);
		if (!dir.isDirectory()) return null;
		
		List<String> filelist = new ArrayList<String>();
		for (String f : dir.list() ) {
			if (f.matches("list20[0-9]{6}\\.csv")) {
				filelist.add(f);
			}
		}
		filelist.sort(null);

		FileList a = new FileList();
		for (String f : filelist) {
			a.addFile(f);
			System.out.println("reading.." + f);
		}
		
		return a;
	}

/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		
		// csv ファイル読込
		FileList a = readFiles(".");
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
		
		// レベル1で増分の大きい順に表示
		List<FileEntry> l1 = a.selectLevel(1);
		l1.sort(new SizeOrder().reversed()); // size 降順でソート
		
		FileList.writePosJsonFile(a.dateList, l1, "posL1Size"+date+".json");
		FileList.writePieChartJsonFile(FileList.cutFile(l1, true), "pieL1Size"+date+".json");
		
		// レベル2で増分の大きい順に10個表示
		List<FileEntry> l2 = a.selectLevel(2);
		
		l2.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l2, 10), "lineL2Inc"+date+".json");
		
		// レベル3で増分の大きい順に20個表示
		List<FileEntry> l3 = a.selectLevel(3);
		l3.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l3, 20), "lineL3Inc"+date+".json");
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
		
		JsonObject tableContainer = new JsonObject().add("key","top").add("label","ファイル使用量削減に向けたヒント"); // values(Array) は後で指定
		
		JsonArray top = new JsonArray(new JsonType[]{tableContainer});
		
		int c = 0;
		
		for (FileEntry f : list) {
			String p = f.path;
			try {
				String filename = FileList.filename(p);
				if (lastFile.equals(filename)) {
					JsonObject same = new JsonObject();
					same.add("key", "same");
					same.add("label", "同一ファイルかも知れません(No."+(c+1)+", "+FileList.filename(f.path)+")。ショートカット化できないか検討して下さい。");
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
		// サイズの大きなファイルを出力
		//
		JsonObject jo = new JsonObject();
		jo.add("key","file");
		jo.add("label", "(参考)サイズの大きなファイル");

		jo.add("_values", jsonArrayPut(FileList.cut(list, 20),
			// lambda expression ( new Putter(JsonObject target, FileEntry src) {..
			(target, src) -> {
				target.add("label", src.path);
				target.add("value", src.size/1024/1024);
				target.add("owner", FileList.reveal(src.owner));
			}
		));
		tableContainer.add("values", jo);
		
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
	private static JsonArray jsonArrayPut(List<FileEntry> l, Putter p) {
		JsonType[] result = new JsonType[l.size()];
		for (int i = 0; i < l.size(); i++) {
			JsonObject jo = new JsonObject();
			p.put(jo, l.get(i));
			result[i] = jo;
		}
		return new JsonArray(result);
	}
	
/*--------------
 * static class
 */
	private static interface Putter {
		void put(JsonObject target, FileEntry srcData);
	}
}
