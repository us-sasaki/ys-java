import java.util.List;

/** �e�폈���p�ɕ֗��Ȓl��ǉ�. key��path */
public class FileEntry {
	String		path;
	int			level;		// �[��
	boolean		isDirectory; // �f�B���N�g�����H
	List<Long>	sizeList;	// �ߋ��̃T�C�Y����
	long		size;
	long		increase;	// ���߂̑���
	
	// �ȉ��A�g���t�B�[���h�Ő̂� csv �ɂ͊܂܂�Ă��Ȃ�
	String		owner	= "unknown";	// �t�@�C�����L�� since 16/05/27
	long		lastModified	= 0;	// �ŏI�X�V�� since 16/06/03
}
