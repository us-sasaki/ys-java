import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * 複数回のディレクトリ内ファイルサイズ情報を格納する。
 * また、FileList や List<FileEntry> に関する便利な操作を行うメソッドを提供する。
 */
public class FileList {
	public static int MAX_DEPTH = FileLister.MAX_DEPTH;
	
	static SimpleDateFormat	sdf = new SimpleDateFormat("yyyyMMdd");
	
	List<Long>		dateList;
	List<FileEntry> list;
	int sizeListCount;
	int referencePoint; // increase を計算する基準, list の index で指定
	
/*-------------
 * constructor
 */
	/**
	 * 空の FileList を作ります。
	 */
	public FileList() {
		list = null;
		dateList = null;
		sizeListCount = 0;
		referencePoint = 0; // 初期値：最初のサイズと比較して increase を計算
	}
	
/*------------------
 * instance methods
 */
	public void setReferencePoint() {
		referencePoint = dateList.size() - 1;
	}
	public void setReferencePoint(int position) {
		if (position < 0 || position >= dateList.size()) throw new IndexOutOfBoundsException("setReferencePoint(int) 現在、データファイルは"+dateList.size()+"個設定されています。この数未満の非負整数を設定してください");
		referencePoint = position;
	}
	public int getReferencePoint() {
		return referencePoint;
	}
	
	/**
	 * readFile した場合、dateList が構築されないため、手動構築するための
	 * メソッド
	 *
	 *
	 */
	public void addDateList(String filename) {
		if (dateList == null) dateList = new ArrayList<Long>();
		try {
			long date = sdf.parse(filename.substring(4,12)).getTime();
			dateList.add(date);
		} catch (java.text.ParseException pe) {
			throw new RuntimeException(pe.toString());
		}
	}
	
	/**
	 * listyyyyMMdd.csv 形式(FileLister2 で生成)のファイルの情報を読み込みます。
	 */
	public void addFile(String fname) throws IOException {
		if (list == null) list = new ArrayList<FileEntry>();
		
		// 遅いため、内部処理で Map を使う実装に変更
		Map<String, FileEntry> map = new TreeMap<String, FileEntry>();
		for (FileEntry fe : list) map.put(fe.path, fe);
		
		FileReader		fr	= new FileReader(fname);
		BufferedReader	br	= new BufferedReader(fr);
		
		while (true) {
			String line = br.readLine();
			if ( (line == null)||(line.equals("")) ) break;
			
			String[] token = line.split(",");
			
			// まず path を生成する
			String p = "";
			for (int i = 1; i <= MAX_DEPTH; i++) {
				if (token[i].equals("")) continue;
				if (i > 1) p = p + "\\";
				p = p + token[i];
			}
			
			FileEntry entry = map.get(p);
			if (entry == null) {
				// 新しい path
				entry = new FileEntry();
				map.put(p, entry); // entry の参照だけ先に登録、中身は以降変更
				
				entry.level = Integer.parseInt(token[0]);
				entry.path = p;
				entry.isDirectory = false; // ちゃんと処理してない
				entry.sizeList = new ArrayList<Long>();
				for (int i = 0; i < sizeListCount; i++) {
					entry.sizeList.add(0L);
				}
			}
			entry.sizeList.add(Long.decode(token[MAX_DEPTH + 1]));
			
			// owner を上書きする
			if (token.length >= MAX_DEPTH + 3)
				entry.owner = token[MAX_DEPTH + 2];
			// lastModified を上書きする
			if (token.length >= MAX_DEPTH + 4)
				entry.lastModified = Long.parseLong(token[MAX_DEPTH + 3]);
		}
		
		sizeListCount++;
		
		fr.close();
		br.close();
		
		// map を list に再設定
		list.clear();
		for (FileEntry entry : map.values()) {
			list.add(entry);
		}
		
		// sizeList の長さをそろえ、isDirectory を設定
		makeup();
		
		// ファイル名から、date を取得
		addDateList(fname);
	}
	
	/**
	 * list を path の辞書式順序に整列し、
	 * sizeList の長さを sizeListCount (addFile した回数) に揃え、
	 * 各 FileEntry の isDirectory フラグを設定する。このフラグ設定は、
	 * path の辞書式順に並べたとき、自分以降に自分を含む path が存在
	 * しない場合にファイル(isDirectory = false)としている。<BR>
	 * 例)												<br>
	 * path1 = Y:\hoge\tarou.hoe						<br>
	 * path2 = Y:\hoge\tarou.hoe\bar					<br>
	 * path3 = Y:\hoge\tarou.hoe\foo					<br>
	 * path4 =(Y:\hoge\tarou.hoe\foo で始まらないもの)	<br>
	 *
	 * のようになっていた場合、path1 のみ directory, path2/3 は file と判定する
	 * つまり、空のディレクトリはファイルと判定される弊害があるが、path のみから
	 * directory を判定する手段はなく、このファイル(ディレクトリ)は常にサイズが0
	 * なので、後続処理に影響しないため、問題ない。
	 */
	private void makeup() {
		// path の辞書式順序でソート
		list.sort(new PathOrder());
		
		for (FileEntry e : list) {
			int l = e.sizeList.size();
			for (int i = 0; i < sizeListCount - l; i++) {
				e.sizeList.add(new Long(0));
			}
			e.size = e.sizeList.get(e.sizeList.size() - 1);
		}
		FileEntry f = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			FileEntry next = list.get(i);
			if (next.path.startsWith(f.path)) f.isDirectory = true;
			else f.isDirectory = false;
			f = next;
		}
		// increase を計算
		for (FileEntry e : list) {
			List<Long> l = e.sizeList;
			e.size	= l.get(sizeListCount - 1); // 最後(最新)のサイズ
			e.increase = e.size - l.get(referencePoint);
		}
	}
	
	/**
	 * このインスタンスが保持している list への参照を返却します。
	 * 返却された list の FileEntry 内容を変更した場合、このインスタンスの list が
	 * 変更されることに注意が必要です。
	 */
	public List<FileEntry> getList() {
		return list;
	}
	
	/**
	 * ファイルの深さ(階層、level)を指定して、該当する FileEntry からなる
	 * list を返却します。
	 * 返却された list の FileEntry 内容を変更した場合、このインスタンスの list が
	 * 変更されることに注意が必要です。
	 */
	public List<FileEntry> selectLevel(int level) {
		if (list == null) throw new IllegalStateException("addFile によって値を格納してください");
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : list) {
			if (f.level == level) result.add(f);
		}
		return result;
	}
	
	/**
	 * ファイルの種類(ファイル/ディレクトリ)を指定して、該当する FileEntry からなる
	 * list を返却します。
	 * 返却された list の FileEntry 内容を変更した場合、このインスタンスの list が
	 * 変更されることに注意が必要です。
	 */
	public List<FileEntry> selectFile(boolean isFile) {
		if (list == null) throw new IllegalStateException("addFile によって値を格納してください");
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : list) {
			if (f.isDirectory != isFile) result.add(f);
		}
		return result;
	}
	
	/**
	 * 任意のルール(Predicate<FileEntry>)に従ってファイルを抽出する
	 *
	 * @param	fes		ファイル抽出ルール
	 * @return	抽出された FileEntry の List (shallow copy)
	 */
	public List<FileEntry> selectAs(java.util.function.Predicate<FileEntry> fes) {
		if (list == null) throw new IllegalStateException("addFile によって値を格納してください");
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : list) {
			if (fes.test(f)) result.add(f);
		}
		return result;
	}
	
	/**
	 * 任意のルールに従って、sublist を取得する。
	 */
	public static List<FileEntry> selectAs(List<FileEntry> src, java.util.function.Predicate<FileEntry> p) {
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : src) {
			if (p.test(f)) result.add(f);
		}
		return result;
	}
	
/*---------------
 * class methods
 *
	/**
	 * path 文字列からファイル名を取得
	 */
	public static String filename(String pathString) {
		return new File(pathString).getName();
	}
	
	public static String filename(String pathString, int depth) {
		String result = null;
		int startIdx = pathString.length() - 1;
		for (int i = 0; i < depth; i++) {
			int idx = pathString.lastIndexOf('\\', startIdx);
			if (idx == -1) {
				if (result == null) return pathString.substring(0, startIdx);
				return pathString.substring(0, startIdx) + "/" + result;
			}
			if (result != null) {
				result = pathString.substring(idx+1, startIdx + 1) + "/" + result;
			} else {
				result = pathString.substring(idx+1, startIdx + 1);
			}
			startIdx = idx - 1;
		}
		return result;
	}
	
	/**
	 * NVD3 line chart 用 JSON ファイル出力
	 */
	public static void writeJsonFile(List<Long> dateList,
							List<FileEntry> target, int depth,
							String filename) throws IOException {
		JsonObject[] data = new JsonObject[target.size()];
		for (int i = 0; i < data.length; i++) {
			FileEntry fe = target.get(i);
			data[i] = new JsonObject().add("key", filename(fe.path, depth));
			JsonType[] values = new JsonType[dateList.size()];
			for (int j = 0; j < values.length; j++) {
				values[j] = new JsonObject()
								.add("x", dateList.get(j))
								.add("y", (fe.sizeList.get(j)/1024/1024) );
			}
			data[i].add("values", values);
		}
		JsonArray top = new JsonArray(data);
		
		writeJsonType(top, filename);
	}
	
	/**
	 * NVD3 stacked area chart / cumulative line chart 用 JSON ファイル出力
	 */
	public static void writePosJsonFile(List<Long> dateList,
							List<FileEntry> target,
							String filename) throws IOException {
		JsonObject[] data = new JsonObject[target.size()];
		for (int i = 0; i < data.length; i++) {
			FileEntry fe = target.get(i);
			data[i] = new JsonObject().add("key", filename(fe.path));
			JsonArray[] values = new JsonArray[dateList.size()];
			for (int j = 0; j < values.length; j++) {
				values[j] = new JsonArray(new long[] { dateList.get(j), (fe.sizeList.get(j)/1024/1024) } );
			}
			data[i].add("values", values);
		}
		JsonArray top = new JsonArray(data);
		
		writeJsonType(top, filename);
	}
	/**
	 * NVD3 Pie Chart 用 JSON ファイル出力
	 */
	public static void writePieChartJsonFile(List<FileEntry> target, int depth,
							String filename) throws IOException {
		JsonType[] data = new JsonType[target.size()];
		for (int i = 0; i < data.length; i++) {
			FileEntry fe = target.get(i);
			data[i] = new JsonObject().add("label", filename(fe.path, depth))
										.add("value", (fe.size/1024/1024) );
		}
		JsonArray top = new JsonArray(data);
		
		writeJsonType(top, filename);
	}
	
	/**
	 * owner 文字列から名前をマッピングする
	 */
	public static String reveal(String owner) {
		int codeIndex = owner.lastIndexOf("\\");
		if (codeIndex >= 0) owner = owner.substring(codeIndex + 1);
		return OwnerTable.convert(owner);
	}
	
	/**
	 * JsonObject をファイルに出力する
	 */
	public static void writeJsonType(JsonType obj, String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		//bos.write("[\n".getBytes("UTF-8"));
		bos.write(obj.toString().getBytes("UTF-8"));
		//bos.write("\n]".getBytes("UTF-8"));
		bos.close();
		fos.close();
	}
	
	public static List<FileEntry> cut(List<FileEntry> src, int count) {
		List<FileEntry> result = new ArrayList<FileEntry>();
		for (int i = 0; i < count; i++) {
			result.add(src.get(i));
		}
		return result;
	}
	
	public static List<FileEntry> cutFile(List<FileEntry> src, boolean cutFile) {
		List<FileEntry> result = new ArrayList<FileEntry>();
		for (FileEntry fe : src) {
			if (fe.isDirectory == cutFile) result.add(fe);
		}
		return result;
	}
	
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
	
}
