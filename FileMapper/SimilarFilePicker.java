import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class SimilarFilePicker {
	// (FileEntry, FileEntry) -> distance
	private List<FileDistance> dist;
	static class FileDistance {
		FileEntry a;
		FileEntry b;
		int dist;
		FileDistance(FileEntry a, FileEntry b, Integer dist) {
			this.a = a;
			this.b = b;
			this.dist = dist;
		}
	}
	
/*-------------
 * Constructor
 */
	public SimilarFilePicker(List<FileEntry> list) {
		//
		dist = new ArrayList<FileDistance>();
		
		System.out.println("SimilarFilePicker: calculating distances");
		int size = list.size();
		
		int loopCount = (size - 1) * size / 2;
		int count = 0;
		int percentage = 0;
		
		// かなり遅い。CPU 25%しか使ってないので、マルチスレッドにした方がよいかも
		for (int i = 0; i < size - 1; i++) {
			for (int j = i+1; j < size; j++) {
				FileEntry a = list.get(i);
				FileEntry b = list.get(j);
				
				dist.add(new FileDistance(a, b, d(a,b) ));
				
				count++;
				if (count >= loopCount/100*percentage) {
					System.out.print("" + percentage +"%..");
					System.out.flush();
					percentage += 5;
				}
			}
		}
		
		System.out.println("SimilarFilePicker: sorting by distance");
		// Integer でソート(昇順)
		dist.sort(new Comparator<FileDistance>() {
				public int compare(FileDistance a, FileDistance b) {
					if (a.dist != b.dist) return (a.dist - b.dist);
					if (a.a.path.compareTo(b.a.path) != 0) return (a.a.path.compareTo(b.a.path));
					return (a.b.path.compareTo(b.b.path));
				}
				public boolean equals(FileDistance a, FileDistance b) {
					return (a.dist == b.dist)&&(a.a.path.equals(b.a.path))&&(a.b.path.equals(b.b.path));
				}
			} );
		
	}
	
/*------------------
 * instance methods
 */
	public List<FileDistance> getDistanceList() {
		return dist;
	}
	
/*----------------
 * static methods
 */
	private static String longestCommonSubsequence(String a, String b) {
		if (a.length() > b.length() ) {
			// a <= b となるようにする
			String c = a; a = b; b = c;
		}
		
		int maxLen = 0;
		int maxLenStartIdxA = -1;
		int alen = a.length();
		int blen = b.length();
		
		for (int i = 0; i < alen; i++) {
			//if (alen - i < maxLen) break; // 後過ぎて記録更新できない
			// ↑(逆に遅くなりそうなのでcut)
			char c = a.charAt(i);
			int bidx = b.indexOf(c);
			if (bidx == -1) continue;
			if (blen - bidx < maxLen) break; // 後過ぎて記録更新できない
			
			// 1文字合った
			int len = 1;
			bidx++;
			for (int j = i+1; j < alen; j++) { // どこまで合いつづけるか
				if (bidx >= blen) break;
				if (a.charAt(j) != b.charAt(bidx)) break;
				len++;
				bidx++;
			}
			if (len > maxLen) { // 記録更新
				maxLen = len;
				maxLenStartIdxA = i;
			}
		}
		if (maxLen == 0) return "";
		return a.substring(maxLenStartIdxA, maxLenStartIdxA+maxLen);
		
//		この実装は遅すぎるので没
//		int length = a.length();
//		String subs = null;
//		
//		loop:
//		for (int len = length; len > 0; len--) {
//			for (int i = 0; i <= length-len; i++) {
//				subs = a.substring(i, i+len);
//				if (b.indexOf(subs) >= 0) return subs;
//			}
//		}
//		return "";
	}
	
	/**
	 * ２つのFileEntryの距離を測る。
	 * ただし、 !FileEntry.isDirectory である必要がある。
	 */
	private static int d(FileEntry a, FileEntry b) {
		//
		// 前処理
		//
		
		// ファイル名を取得
		String na = new File(a.path).getName();
		String nb = new File(b.path).getName();
		
		// extension と名前本体を取得
		int ind = na.lastIndexOf('.');
		String pa = na;
		String ea = "";
		if (ind > -1) { pa = na.substring(0,ind); ea = na.substring(ind+1); }
		ind = nb.lastIndexOf('.');
		String pb = nb;
		String eb = "";
		if (ind > -1) { pb = nb.substring(0,ind); eb = nb.substring(ind+1); }
		
		//
		// 距離算出
		//
		int d = 0;
		
		// extension が違うとかなり違うファイル
		// (データがあれば定数を学習させた方がよい？)
		if (!ea.equals(eb)) d += 10000;
		
		// 最大共通部分を探す
		String sub = longestCommonSubsequence(pa, pb);
		int ia = pa.indexOf(sub);
		int ib = pb.indexOf(sub);
		// 差分文字列
		String diff = pa.substring(0, ia) + pa.substring(ia+sub.length())
						+ pb.substring(0, ib) + pb.substring(ib+sub.length());
		// 差分文字列の種類によって距離が異なる
		// 教師データがあれば文字種と距離は学習させる手もあるかも知れない
		for (int i = 0; i < diff.length(); i++) {
			char c = diff.charAt(i);
			if (c >= '0' && c <= '9') d += 2;
			else if (c == '_')  d += 2;
			else if (c == '-')  d += 2;
			else if (c == ' ')  d += 2;
			else if (c == '　') d += 2;
			else if (c == '(')  d += 2;
			else if (c == ')')  d += 2;
			else if (c == '（') d += 2;
			else if (c == '）') d += 2;
			else if (c == '[')  d += 2;
			else if (c == ']')  d += 2;
			else if (c == '「') d += 2;
			else if (c == '」') d += 2;
			else if (c == '<')  d += 2;
			else if (c == '>')  d += 2;
			else if (c == '＜') d += 2;
			else if (c == '＞') d += 2;
			else d += 20;
		}
		
		// 一致している部分が小さいと距離は長い(式は適当)
		d = d * 1000 / (sub.length() + 5); // 学習の余地あり？
		
		// サイズによる判定も加味する
		long sizeGap = a.size - b.size;
		long minSize = b.size;
		if (sizeGap < 0) {
			sizeGap = -sizeGap;
			minSize = a.size;
		}
		if (sizeGap > 0) d += (int)(Math.log(sizeGap) * 200); // 学習の余地あり？
		
		// 更新日時による判定も追加
		long dateGap = a.lastModified - b.lastModified;
		if (dateGap < 0) dateGap = -dateGap;
		
		d += (int)(Math.log(dateGap) * 10); // 学習の余地あり？
		
		return d;
	}
	
	public static void main(String[] args) throws Exception {
		String s = longestCommonSubsequence("適当な文字列35", "ちょっと違う適当な文字列");
		System.out.println(s);
		System.out.println("長さ="+s.length());
		
		FileList l = MakeFileUsage.readFiles(".");
		List<FileDistance> fdl = new SimilarFilePicker(l.list).getDistanceList();
		for (int i = 0; i < 1000; i++) {
			FileDistance f = fdl.get(i);
			System.out.println("---------------------------------------");
			System.out.println("           " + f.dist + "             ");
			System.out.println("---------------------------------------");
			System.out.println(f.a);
			System.out.println(f.b);
		}
		
	}
}
