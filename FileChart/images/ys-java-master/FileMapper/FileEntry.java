import java.util.List;

/** �e�폈���p�ɕ֗��Ȓl��ǉ�. key��path */
public class FileEntry {
	String		path;
	int			level;		// �[��
	boolean		isDirectory; // �f�B���N�g�����H
	List<Long>	sizeList;	// �ߋ��̃T�C�Y����
	long		size;
	long		increase;	// ���߂̑���
	String		owner	= "unknown";		// �t�@�C�����L��
}
