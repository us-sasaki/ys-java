import java.util.Comparator;

/**
 * ‘•ª, [‚³, ƒpƒX–¼ ‚Ì‡‚É•À‚×‚é Comparator
 */
class IncreaseOrder implements Comparator<FileEntry> {
	public int compare(FileEntry a, FileEntry b) {
		if (a.increase > b.increase) return 1;
		if (a.increase < b.increase) return -1;
		if (a.level != b.level) return a.level - b.level;
		return a.path.compareTo(b.path);
	}
	
	public boolean equals(FileEntry a, FileEntry b) {
		return ((a.increase == b.increase)&& // ‘‚­‚Ä•ª‰ğ”\‚ª‚‚¢ increase ‚Å‚Ü‚¸”äŠr
					(a.level == b.level)&&
					(a.path.equals(b.path)) ); // ’x‚¢‚Ì‚ÍÅŒã
	}
}
