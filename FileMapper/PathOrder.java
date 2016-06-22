import java.util.Comparator;

/**
 * �p�X�� �̎����������ŕ��ׂ� Comparator
 */
class PathOrder implements Comparator<FileEntry> {
	@Override
	public int compare(FileEntry a, FileEntry b) {
		return a.path.compareTo(b.path);
	}
}
