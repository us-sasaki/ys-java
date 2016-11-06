import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * 削除されたファイルは不要なファイルと判断された、とみなし、教師データにする。
 * たまにしか実行しないので、性能は求めない
 */
public class DeletedFilePicker {
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String date = sdf.format(new Date());
		
		FileList a = FileList.readFiles(".");
		List<FileEntry> list = new ArrayList<FileEntry>();
		
		for (FileEntry e : a.list) {
			if (e.isDirectory) continue;
			
			List<Long> sl = e.sizeList;
			
			// どこかで 0 になるもの
			boolean exist = false;
			boolean deleted = false;
			for (int i = 0; i < sl.size(); i++) {
				if (sl.get(i) > 0) exist = true;
				else if (exist && sl.get(i) == 0) deleted = true;
			}
			
			if (!deleted) continue;
			
			// かつ他のところにないものを探す
			boolean moved = false;
			for (FileEntry f : a.list) {
				if (e == f) continue; // 同じオブジェクトはスキップ
				String ename = FileList.filename(e.path);
				String fname = FileList.filename(f.path);
				if (ename.equals(fname) && e.size==f.size) {
					// ファイル名とサイズが一致していたら、移動したとみなす
					moved = true;
					break;
				}
			}
			// 消えていて移動したわけではない⇒削除された
			if (!moved) list.add(e);
		}
		
		// 表示、ファイル出力
		PrintWriter p = new PrintWriter(new FileWriter("deletedFiles"+date+".txt"));
		
		for (FileEntry e : list) {
//			System.out.println("--------------------");
//			System.out.println(e);
			p.print(e.path);
			p.print(",");
			String fname = FileList.filename(e.path);
			int extIdx = fname.lastIndexOf(".");
			if (extIdx > 0) {
				p.print(fname.substring(0, extIdx));
				p.print(",");
				p.print(fname.substring(extIdx+1));
				p.print(",");
			} else {
				p.print(fname + ",,");
			}
			
			long size = 0;
			List<Long> sl = e.sizeList;
			// どこかで 0 になるもの
			boolean exist = false;
			for (int i = 0; i < sl.size(); i++) {
				if (sl.get(i) > 0) {
					exist = true;
					size = sl.get(i);
				} else if (exist && sl.get(i) == 0) break;
			}
			
			p.print(size);
			p.print(",");
			p.println(e.lastModified);
		}
		
		p.close();
		
	}
}
