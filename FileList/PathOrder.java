import java.util.Comparator;

/**
 * ƒpƒX–¼ ‚Ì«‘®‡˜‚Å•À‚×‚é Comparator
 */
class PathOrder implements Comparator<FileEntry> {
	@Override
	public int compare(FileEntry a, FileEntry b) {
		return a.path.compareTo(b.path);
	}
}
