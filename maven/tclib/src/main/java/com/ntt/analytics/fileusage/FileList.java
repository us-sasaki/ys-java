package com.ntt.analytics.fileusage;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.function.Function;

import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;
import abdom.data.json.JsonType;
import abdom.util.CsvReader;

/**
 * 複数ショットのディレクトリ内ファイルサイズ情報を格納します。
 * また、FileList や List&lt;FileEntry&gt; に関する便利な操作を行うメソッドを提供
 * します。
 *
 * @version		October 21, 2017
 * @author		Yusuke Sasaki
 */
public class FileList {
	static SimpleDateFormat	sdf = new SimpleDateFormat("yyyyMMdd");
	
	public static final int MAX_DEPTH = 20;
	
	/**
	 * ファイルシステムで保持されているユーザ名を表示する名称に変換する
	 * Function です。初期値は無変換です。
	 */
	public Function<String, String> ownerTable = (n -> n);
	
	/** FileEntry のリスト */
	public List<FileEntry> list;
	
	/** list の各エントリに対応するファイルの日付情報のリスト */
	public List<Long>		dateList;
	
	/** list の長さ */
	int sizeListCount;
	
	/** increase を計算する基準のショット, list の index で指定 */
	int referencePoint;
	
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
	/**
	 * increase の計算基準を最新のショット番号に設定します。
	 */
	public void setReferencePoint() {
		referencePoint = dateList.size() - 1;
		calcIncrease();
	}
	/**
	 * increase の計算基準を指定されたショット番号に設定します。
	 *
	 * @param	position	ショット番号(0～list.size()-1)
	 */
	public void setReferencePoint(int position) {
		if (position < 0 || position >= dateList.size()) throw new IndexOutOfBoundsException("setReferencePoint(int) 現在、データファイルは"+dateList.size()+"個設定されています。この数未満の非負整数を設定してください");
		referencePoint = position;
		calcIncrease();
	}
	
	/**
	 * increase の計算基準となっているショット番号を返却します。
	 *
	 * @return	increase の計算基準となっているショット番号
	 */
	public int getReferencePoint() {
		return referencePoint;
	}
	
	/**
	 * listyyyyMMdd.csv 形式(FileLister で生成)のファイルの情報を読み込み、
	 * 蓄積します。
	 *
	 * @param	fname	ファイル情報を格納した csv ファイル名
	 * @throws	java.io.IOException IO例外
	 */
	public void addFile(String fname) throws IOException {
		if (list == null) list = new ArrayList<FileEntry>();
		
		// 遅いため、内部処理で Map を使う実装に変更
		Map<String, FileEntry> map = new TreeMap<String, FileEntry>();
		for (FileEntry fe : list) map.put(fe.path, fe);
		
		//
		for (String[] row : CsvReader.rows(fname)) {
			// まず path を生成する
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i <= MAX_DEPTH; i++) {
				if (row[i].equals("")) continue;
				if (i > 1) sb.append('\\');
				sb.append(row[i]);
			}
			String p = sb.toString();
			
			FileEntry entry = map.get(p);
			if (entry == null) {
				// 新しい path
				entry = new FileEntry();
				map.put(p, entry); // entry の参照だけ先に登録、中身は以降変更
				
				entry.level = Integer.parseInt(row[0]);
				entry.path = p;
				entry.isDirectory = false; // as default
				entry.sizeList = new ArrayList<Long>();
				for (int i = 0; i < sizeListCount; i++) {
					entry.sizeList.add(0L);
				}
			}
			entry.sizeList.add(Long.decode(row[MAX_DEPTH + 1]));
			
			// owner を上書きする
			if (row.length >= MAX_DEPTH + 3)
				entry.owner = row[MAX_DEPTH + 2];
			// lastModified を上書きする
			if (row.length >= MAX_DEPTH + 4)
				entry.lastModified = Long.parseLong(row[MAX_DEPTH + 3]);
		}
		
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
	 * 指定したファイル名から日付部分を取り出し、long値の dateList に追加
	 *
	 * @param	filename	ファイル名
	 */
	private void addDateList(String filename) {
		filename = filename(filename);
		if (dateList == null) dateList = new ArrayList<Long>();
		try {
			long date = sdf.parse(filename.substring(4,12)).getTime();
			dateList.add(date);
		} catch (java.text.ParseException pe) {
			throw new RuntimeException(pe.toString());
		}
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
		
		// sizeList の長さを sizeListCount にそろえる
		for (FileEntry e : list) {
			int l = e.sizeList.size();
			for (int i = 0; i < sizeListCount - l; i++) {
				e.sizeList.add(0L); // フィールドが存在しない場合 0 を設定
			}
			e.size = e.sizeList.get(e.sizeList.size() - 1);
		}
		
		// isDirectory を設定
		FileEntry f = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			FileEntry next = list.get(i);
			if (next.path.startsWith(f.path)) f.isDirectory = true;
			else f.isDirectory = false;
			f = next;
		}
		calcIncrease();
	}
	
	private void calcIncrease() {
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
	 *
	 * @return	保持している FileEntry のリスト
	 */
	public List<FileEntry> getList() {
		return list;
	}
	
	/**
	 * ファイルの深さ(階層、level)を指定して、該当する FileEntry からなる
	 * list を返却します。
	 * 返却された list の FileEntry 内容を変更した場合、このインスタンスの list が
	 * 変更されることに注意が必要です。
	 *
	 * @param	level		ディレクトリ階層の深さ
	 * @return	抽出された FileEntry からなるリスト
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
	 *
	 * @param	isFile		ファイルを抽出(true) / ディレクトリを抽出(false)
	 * @return	抽出後のリスト
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
	 * 任意のルール(Predicate&lt;FileEntry&gt;)に従ってファイルを抽出する
	 *
	 * @param	fes		ファイル抽出ルール
	 * @return	抽出された FileEntry の List (shallow copy)
	 */
	public List<FileEntry> selectAs(java.util.function.Predicate<FileEntry> fes) {
		if (list == null) throw new IllegalStateException("addFile によって値を格納してください");
		return selectAs(list, fes);
	}
	
	/**
	 * 任意のルールに従って、sublist を取得する。
	 *
	 * @param	src		FileEntry のリスト
	 * @param	p		抽出ルール(FileEntry から boolean へのマップ)
	 * @return	抽出後のリスト
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
	 * path 文字列からファイル名部分を取得します。
	 * ファイル名とディレクトリ名のセパレータは、\ または / です。
	 *
	 * @param	pathString	ディレクトリを含むファイル名
	 * @return	ファイル名部分
	 */
	public static String filename(String pathString) {
		int idx = pathString.lastIndexOf('\\');
		if (idx == -1) {
			idx = pathString.lastIndexOf('/');
			if (idx == -1) return pathString;
		}
		return pathString.substring(idx+1);
	}
	
	/**
	 * path 文字列から指定された階層数分の親ディレクトリ名を含む
	 * ファイル名を取得します。
	 * <pre>
	 * 例： filename("a/b/c/d/e.txt", 3) == "b/c/d/e.txt"
	 * </pre>
	 * ファイル名、ディレクトリ名のセパレータは \, / が使えますが、返却
	 * される文字列は一律 / に変換されます。
	 *
	 * @param		pathString	path 文字列
	 * @param		depth		含ませたい親ディレクトリの階層数
	 * @return		指定された階層数分の親ディレクトリ名を含むファイル名
	 */
	public static String filename(String pathString, int depth) {
		String result = null;
		int startIdx = pathString.length() - 1;
		for (int i = 0; i < depth; i++) {
			int idx = pathString.lastIndexOf('\\', startIdx);
			if (idx == -1) {
				idx = pathString.lastIndexOf('/', startIdx);
				if (idx == -1) {
					if (result == null)
						return pathString.substring(0, startIdx);
					return pathString.substring(0, startIdx) + "/" + result;
				}
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
	 *
	 * @param	dateList	日付リスト
	 * @param	target		Line Chart のデータを格納したリスト
	 * @param	depth		対象とするディレクトリ階層
	 * @param	filename	出力ファイル名
	 * @throws	java.io.IOException	IO例外
	 */
	public static void writeJsonFile(List<Long> dateList,
							List<FileEntry> target, int depth,
							String filename) throws IOException {
		JsonArray ja = new JsonArray();
		for (FileEntry fe : target) {
			JsonObject jo = JsonType.o("key", filename(fe.path, depth));
			JsonArray values = new JsonArray();
			int j = 0;
			for (Long date : dateList) {
				values.push(JsonType.o("x", date)
								.put("y", (fe.sizeList.get(j++)/1024/1024)));
			}
			jo.put("values", values);
			ja.push(jo);
		}
		writeJsonType(ja, filename);
	}
	
	/**
	 * NVD3 Pie Chart 用 JSON ファイル出力
	 *
	 * @param	target		Pie Chart のデータを格納したリスト
	 * @param	depth		対象とするディレクトリ階層
	 * @param	filename	出力ファイル名
	 * @throws	java.io.IOException	IO例外
	 */
	public static void writePieChartJsonFile(List<FileEntry> target, int depth,
							String filename) throws IOException {
		JsonArray ja = new JsonArray();
		for (FileEntry fe : target) {
			ja.push(JsonType.o("label", filename(fe.path, depth))
							.put("value", (fe.size/1024/1024)));
		}
		
		writeJsonType(ja, filename);
	}
	
	/**
	 * owner 文字列から名前をマッピングします。
	 * 事前に ownerTable を上書きすると、そのテーブルが利用されます。
	 *
	 * @param	owner	名前マッピング元の文字列
	 * @return	名前
	 */
	public String reveal(String owner) {
		int codeIndex = owner.lastIndexOf("\\");
		if (codeIndex >= 0) owner = owner.substring(codeIndex + 1);
		return ownerTable.apply(owner);
	}
	
	/**
	 * JsonObject をファイルに出力する
	 *
	 * @param	obj			ファイル出力対象オブジェクト
	 * @param	filename	出力ファイル名
	 * @throws	java.io.IOException	IO例外
	 */
	public static void writeJsonType(JsonType obj, String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bos.write(obj.toString("  ").getBytes("UTF-8"));
		bos.close();
		fos.close();
	}
	
	public static List<FileEntry> cut(List<FileEntry> src, int count) {
		List<FileEntry> result = new ArrayList<FileEntry>();
		for (int i = 0; i < count; i++) {
			if (i >= src.size()) continue;
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
	 *
	 * @param	path	ファイルパス
	 * @return	FileList
	 * @throws	java.io.IOException	IO例外
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
			a.addFile(path+f);
			System.out.println("reading.." + f);
		}
		
		return a;
	}
	
}
