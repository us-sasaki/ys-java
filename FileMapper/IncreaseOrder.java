import java.util.Comparator;

/**
 * ‘•ª, [‚³, ƒpƒX–¼ ‚Ì‡‚É•À‚×‚é Comparator
 */
class IncreaseOrder implements Comparator<FileEntry> {
	@Override
	public int compare(FileEntry a, FileEntry b) {
		if (a.increase > b.increase) return 1;
		if (a.increase < b.increase) return -1;
		if (a.level != b.level) return a.level - b.level;
		return a.path.compareTo(b.path);
	}
}
