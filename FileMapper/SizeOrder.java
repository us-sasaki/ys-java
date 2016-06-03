import java.util.Comparator;

/**
 * �T�C�Y, �[��, �p�X�� �̏��ɕ��ׂ� Comparator
 */
class SizeOrder implements Comparator<FileEntry> {
	public int compare(FileEntry a, FileEntry b) {
		if (a.size > b.size) return 1;
		if (a.size < b.size) return -1;
		if (a.level != b.level) return a.level - b.level;
		return a.path.compareTo(b.path);
	}
	
	public boolean equals(FileEntry a, FileEntry b) {
		return ((a.size == b.size)&& // �����ĕ���\������ size �ł܂���r
					(a.level == b.level)&&
					(a.path.equals(b.path)) ); // �x���͍̂Ō�
	}
}
