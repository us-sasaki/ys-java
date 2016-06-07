import java.util.Comparator;

/**
 * ƒpƒX–¼ ‚Ì«‘®‡˜‚Å•À‚×‚é Comparator
 */
class PathOrder implements Comparator<FileEntry> {
	public int compare(FileEntry a, FileEntry b) {
		return a.path.compareTo(b.path);
	}
	
	public boolean equals(FileEntry a, FileEntry b) {
		return a.path.equals(b.path);
	}
}
