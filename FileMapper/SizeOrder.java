import java.util.Comparator;

/**
 * サイズ, 深さ, パス名 の順に並べる Comparator
 */
class SizeOrder implements Comparator<FileEntry> {
	public int compare(FileEntry a, FileEntry b) {
		if (a.size > b.size) return 1;
		if (a.size < b.size) return -1;
		if (a.level != b.level) return a.level - b.level;
		return a.path.compareTo(b.path);
	}
	
	public boolean equals(FileEntry a, FileEntry b) {
		return ((a.size == b.size)&& // 早くて分解能が高い size でまず比較
					(a.level == b.level)&&
					(a.path.equals(b.path)) ); // 遅いのは最後
	}
}
