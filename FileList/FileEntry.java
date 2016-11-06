import java.util.List;
import java.util.Date;

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
	long		lastModified	= 614201724000000L;	// �ŏI�X�V�� since 16/06/03
	
/*-----------
 * overrides
 */
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("    path   :"+path+"\n");
		s.append("   level   :"+level+"\n");
		s.append("isDirectory:"+isDirectory+"\n");
		s.append("  sizeList :{");
		for (Long l: sizeList) {
			s.append(l);
			s.append(" ");
		}
		s.append("}\n");
		s.append("   size    :"+size+"\n");
		s.append(" increase  :"+increase+"\n");
		s.append("   owner   :"+FileList.reveal(owner)+"\n");
		s.append("lastModified:"+new Date(lastModified)+"\n");
		
		return s.toString();
	}
}
