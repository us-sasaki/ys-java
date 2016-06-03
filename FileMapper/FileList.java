import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * 複数回のディレクトリ内ファイルサイズ情報を元に沢山容量を消費している
 * ファイルを抽出する。
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
		referencePoint = 0; // 最初のサイズと比較して increase を計算
	}
	
/*------------------
 * instance methods
 */
	public void setReferencePoint() {
		referencePoint = dateList.size() - 1;
	}
	
	/**
	 * 試験用に作ったもの。今後はlistyyyyMMdd.csv 形式のものを呼ぶように変更
	 * ファイル名から、いつの情報かを取得する必要があるため。
	 * FileMapper も不要になりそう
	 *
	 * @deprecated
	 */
	public void readFile(String fname) throws IOException {
		if (list != null) throw new IllegalStateException("すでに値を保持しています");
		list = new ArrayList<FileEntry>();
		
		FileReader fr = new FileReader(fname);
		BufferedReader br = new BufferedReader(fr);
		
		int maxTokens = 0;
		while (true) {
			String line = br.readLine();
			if ( (line == null)||(line.equals("")) ) break;
			
			String[] token = line.split(",");
			
			// ファイルサイズが途中で切れることがあるため、最大値を取得
			if (token.length > maxTokens) maxTokens = token.length;
			
			// FileEntry に変換する
			FileEntry entry = new FileEntry();
			
			entry.level = Integer.parseInt(token[0]);
			String p = "";
			for (int i = 1; i <= MAX_DEPTH; i++) {
				if (token[i].equals("")) continue;
				if (i > 1) p = p + "\\";
				p = p + token[i];
			}
			entry.path = p;
			entry.isDirectory = false; // ちゃんと処理してない
			entry.sizeList = new ArrayList<Long>();
			
			boolean hasSize  = false;
			boolean hasOwner = false;
			
			for (int i = MAX_DEPTH+1; i < token.length; i++) {
				if (token[i].equals("")) token[i] = "0";
				try {
					entry.sizeList.add(Long.decode(token[i]));
					if (i == token.length - 1) hasSize = true;
				} catch (NumberFormatException e) {
					if (i == token.length - 1) {
						entry.owner = token[i];
						hasOwner = true;
					}
					else throw new NumberFormatException(e.getMessage());
				}
			}
			// 最後の成分が長さの場合、文字列の場合の両方を含むとエラー
			if (hasSize&&hasOwner) throw new NumberFormatException("フォーマットエラー");
			
			List<Long> l = entry.sizeList;
			entry.size	= l.get(l.size() - 1);
			if (l.size() == 1) {
				entry.increase = entry.size;
			} else {
				entry.increase = entry.size - l.get(referencePoint);
			}
			
			list.add(entry);
		}
		
		sizeListCount = maxTokens - MAX_DEPTH - 1;
		
		fr.close();
		br.close();
		// isDirectory の設定
		// アルゴリズム
		//   同一のpath文字列を含む他の Entry があれば directory
		//   まず path について辞書式にならべるとすぐわかりそう
		makeup(); // sizeList の長さをそろえ、isDirectoryを設定
	}
	
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
		if (list == null) throw new IllegalStateException("set または readFile によって値を格納してください");
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
		if (list == null) throw new IllegalStateException("set または readFile によって値を格納してください");
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : list) {
			if (f.isDirectory != isFile) result.add(f);
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
	
	/**
	 * NVD3 line chart 用 JSON ファイル出力
	 */
	public static void writeJsonFile(List<Long> dateList,
							List<FileEntry> target,
							String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter p = new PrintWriter(fw);
		
		p.println("["); // start mark
		boolean first = true;
		for (FileEntry fe : target) {
			if (!fe.isDirectory) continue;
			if (!first) {
				p.println(",");
			} else {
				first = false;
			}
			p.println("  {");
			String path = fe.path;
			path = path.replace("\\", "\\\\");
			p.println("    \"key\": \"" + filename(path) + "\",");
			p.print("    \"values\": [ ");
			int i = 0;
			for (Long date : dateList) {
				if (i > 0) p.print(" , ");
				p.print("{\"x\":" + date + " , \"y\":" + (fe.sizeList.get(i++)/1024/1024) + "}");
			}
			p.println("]");
			p.print("  }");
		}
		p.println();
		p.println("]");
		
		p.close();
		fw.close();
		fos.close();
	}
	
	/**
	 * NVD3 stacked area chart / cumulative line chart 用 JSON ファイル出力
	 */
	public static void writePosJsonFile(List<Long> dateList,
							List<FileEntry> target,
							String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter p = new PrintWriter(fw);
		
		p.println("["); // start mark
		boolean first = true;
		for (FileEntry fe : target) {
			if (!fe.isDirectory) continue;
			if (!first) {
				p.println(",");
			} else {
				first = false;
			}
			p.println("  {");
			String path = fe.path;
			path = path.replace("\\", "\\\\");
			p.println("    \"key\": \"" + path + "\",");
			p.print("    \"values\": [ ");
			int i = 0;
			for (Long date : dateList) {
				if (i > 0) p.print(", ");
				p.print("[" + date + ", " + (fe.sizeList.get(i++)/1024/1024) + "]");
			}
			p.println("]");
			p.print("  }");
		}
		p.println();
		p.println("]");
		
		p.close();
		fw.close();
		fos.close();
	}
	/**
	 * NVD3 Pie Chart 用 JSON ファイル出力
	 */
	public static void writePieChartJsonFile(List<FileEntry> target,
							String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter p = new PrintWriter(fw);
		
		p.println("["); // start mark
		boolean first = true;
		for (FileEntry fe : target) {
			//if (!fe.isDirectory) continue;
			if (!first) {
				p.println(",");
			} else {
				first = false;
				p.println();
			}
			p.print("  {");
			p.print(" \"label\": \"" + filename(fe.path) + "\",");
			p.print(" \"value\": \"" +(fe.size/1024/1024)+"\"" );
			p.print("  }");
		}
		p.println();
		p.println("]");
		
		p.close();
		fw.close();
		fos.close();
	}
	/**
	 * NVD3 Indented Table 用 JSON ファイル出力
	 */
	public static void writeTableChartJsonFile(List<FileEntry> target,
							String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter p = new PrintWriter(fw);
		
		p.println("["); // start mark
		p.println("  { \"key\": \"file\", \"label\": \"サイズの大きなファイル\", \"values\": [");
		boolean first = true;
		for (FileEntry fe : target) {
			if (!first) {
				p.println(",");
			} else {
				first = false;
				p.println();
			}
			p.print("  {");
			p.print(" \"label\": \"" + fe.path.replace("\\", "\\\\") + "\",");
			p.print(" \"value\": \"" +(fe.size/1024/1024)+"\"," );
			String owner = fe.owner;
			int codeIndex = owner.lastIndexOf("\\");
			if (codeIndex >= 0) owner = owner.substring(codeIndex + 1);
			p.print(" \"owner\": \"" +(OwnerTable.convert(owner))+"\"" );
			p.print("  }");
		}
		p.println("]}");
		p.println("]");
		
		p.close();
		fw.close();
		fos.close();
	}

	/**
	 * CSV形式ファイル出力
	 */
	public static void writeFile(String filename, List<FileEntry> target, List<Long> dateList, int maxCount)
				throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "Shift_JIS"); // for EXCEL
		PrintWriter p = new PrintWriter(fw);
		int count = 0;
		
		p.print("フォルダ");
		for (Long date : dateList) {
			p.print(",");
			p.print(sdf.format(new Date(date)));
		}
		p.println();
		for (FileEntry fe : target) {
			p.print(fe.path);
			for (Long size : fe.sizeList) {
				p.print(",");
				p.print(size/1024/1024); // Mbyte
			}
			p.println();
			count++;
			if (count >= maxCount) break;
		}
		
		p.close();
		fw.close();
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
}
