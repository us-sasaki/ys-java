import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

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
	
		FileList a = new FileList();
		a.addFile("list20160212.csv");
		a.addFile("list20160308.csv");
		a.addFile("list20160418.csv");
		a.addFile("list20160419.csv"); a.setReferencePoint();
		a.addFile("list20160510.csv");
		a.addFile("list20160516.csv");
		a.addFile("list20160527.csv");
		a.addFile("list20160601.csv");
		
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
		
		// 同一ファイル疑惑を探す
		System.out.println("■以下のファイルは同一ファイルと思われます。ショートカット化できないか検討してください。");
		List<FileEntry> list = a.selectFile(true);
		list.sort(new SizeOrder().reversed()); // サイズ降順
		
		long lastSize = -1;
		String lastPath = "";
		String lastFile = "";
		int c = 0;
		for (FileEntry f : list) {
			String p = f.path;
			try {
				String filename = FileList.filename(p);
				if (lastFile.equals(filename)) {
					System.out.println("-------------------------------------------");
					System.out.println(lastPath);
					System.out.println(f.path);
				}
				lastSize = f.size;
				lastPath = f.path;
				lastFile = filename;
				c++;
				if (c > 20) break;
			} catch (Exception e) {
				System.out.println(e);
				System.out.println(p);
				break;
			}
		}
		// 大きいファイルを20個
		FileList.writeTableChartJsonFile(FileList.cut(list, 20), "bigFile"+date+".json");

	}
	
}
