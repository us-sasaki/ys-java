import java.io.*;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.*;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.FileTime;

import com.ntt.analytics.fileusage.*;

/**
 * 指定されたディレクトリ以下のファイルパス、ファイルサイズ(ディレクトリの場合は配下の
 * ファイルサイズの総和)を csv 形式でファイルに出力する。
 * ファイル名は listyyyyMMdd.csv とする。
 *
 * @version 2016/3/14 修正(,や'がファイル名にある場合、カット)
 *          2016/5/20 オーナーを追加
 *          2016/6/03 lastModified (ファイル最終更新時)を追加
 *			2017/10/22 複数ファイル指定への対応、abdom.data.json の利用
 * @author	Yusuke Sasaki
 */
public class FileLister implements Closeable {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	PrintWriter out;
	String filename;
	
	public final static int MAX_DEPTH = FileList.MAX_DEPTH;
	
	public FileLister() throws IOException {
		filename = "csv/list"+sdf.format(new Date()) + ".csv";
		FileWriter fw = new FileWriter(filename);
		out = new PrintWriter(new BufferedWriter(fw));
	}
	
	public void list(File f) throws IOException {
		list(f, 0);
	}
	
	private long list(File f, int depth) throws IOException {
		if (f.isDirectory()) {
			// ディレクトリの場合
			File[] file = f.listFiles();
			if (file == null) return 0L;
			
			long size = 0;
			for (int i = 0; i < file.length; i++) {
				if (file[i] != null) {
					size += list(file[i], depth + 1);
				}
			}
			printFile(f, size, depth);
			return size;
			
		} else {
			// 通常のファイルの場合
			long size = f.length();
			printFile(f, size, depth);
			return size;
		}
	}
	
	/**
	 * 
	 */
	protected void printFile(File f, long size, int depth) throws IOException {
		out.print(String.valueOf(depth)+",");
		printPath(f.getPath());
		out.print(size);
		out.print(',');
		out.print(Files.getOwner(f.toPath()).getName());
		out.print(',');
		out.print(Files.getLastModifiedTime(f.toPath()).toMillis());
		out.println();
	}
	
	protected void printPath(String path) {
		StringTokenizer st = new StringTokenizer(path, "\\");
		for (int i = 0; i < MAX_DEPTH; i++) {
			try {
				out.print(cutIllegalChar(st.nextToken()));
			} catch (NoSuchElementException nsee) {
			}
			out.print(",");
		}
	}
	
	/**
	 * ファイルの文字列から特定の文字列 ' , をreplaceする
	 */
	private String cutIllegalChar(String target) {
		return target.replace("\'","<Q>").replace(",","<c>");
	}
	
	@Override
	public void close() throws IOException {
		out.close();
	}
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		try (FileLister f = new FileLister();) {
			for (String fname : args) {
				f.list(new File(fname));
			}
		}
	}
	
}


