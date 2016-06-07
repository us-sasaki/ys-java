import java.util.Comparator;

/**
 * 増分, 深さ, パス名 の順に並べる Comparator
 */
class IncreaseOrder implements Comparator<FileEntry> {
	public int compare(FileEntry a, FileEntry b) {
		if (a.increase > b.increase) return 1;
		if (a.increase < b.increase) return -1;
		if (a.level != b.level) return a.level - b.level;
		return a.path.compareTo(b.path);
	}
	
	public boolean equals(FileEntry a, FileEntry b) {
		return ((a.increase == b.increase)&& // 早くて分解能が高い increase でまず比較
					(a.level == b.level)&&
					(a.path.equals(b.path)) ); // 遅いのは最後
	}
}
