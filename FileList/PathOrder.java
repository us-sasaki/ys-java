import java.util.Comparator;

/**
 * パス名 の辞書式順序で並べる Comparator
 */
class PathOrder implements Comparator<FileEntry> {
	@Override
	public int compare(FileEntry a, FileEntry b) {
		return a.path.compareTo(b.path);
	}
}
