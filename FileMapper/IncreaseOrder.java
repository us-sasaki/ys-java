import java.util.Comparator;

/**
 * ����, �[��, �p�X�� �̏��ɕ��ׂ� Comparator
 */
class IncreaseOrder implements Comparator<FileEntry> {
	public int compare(FileEntry a, FileEntry b) {
		if (a.increase > b.increase) return 1;
		if (a.increase < b.increase) return -1;
		if (a.level != b.level) return a.level - b.level;
		return a.path.compareTo(b.path);
	}
	
	public boolean equals(FileEntry a, FileEntry b) {
		return ((a.increase == b.increase)&& // �����ĕ���\������ increase �ł܂���r
					(a.level == b.level)&&
					(a.path.equals(b.path)) ); // �x���͍̂Ō�
	}
}