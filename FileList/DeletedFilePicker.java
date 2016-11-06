import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * �폜���ꂽ�t�@�C���͕s�v�ȃt�@�C���Ɣ��f���ꂽ�A�Ƃ݂Ȃ��A���t�f�[�^�ɂ���B
 * ���܂ɂ������s���Ȃ��̂ŁA���\�͋��߂Ȃ�
 */
public class DeletedFilePicker {
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		String date = sdf.format(new Date());
		
		FileList a = FileList.readFiles(".");
		List<FileEntry> list = new ArrayList<FileEntry>();
		
		for (FileEntry e : a.list) {
			if (e.isDirectory) continue;
			
			List<Long> sl = e.sizeList;
			
			// �ǂ����� 0 �ɂȂ����
			boolean exist = false;
			boolean deleted = false;
			for (int i = 0; i < sl.size(); i++) {
				if (sl.get(i) > 0) exist = true;
				else if (exist && sl.get(i) == 0) deleted = true;
			}
			
			if (!deleted) continue;
			
			// �����̂Ƃ���ɂȂ����̂�T��
			boolean moved = false;
			for (FileEntry f : a.list) {
				if (e == f) continue; // �����I�u�W�F�N�g�̓X�L�b�v
				String ename = FileList.filename(e.path);
				String fname = FileList.filename(f.path);
				if (ename.equals(fname) && e.size==f.size) {
					// �t�@�C�����ƃT�C�Y����v���Ă�����A�ړ������Ƃ݂Ȃ�
					moved = true;
					break;
				}
			}
			// �����Ă��Ĉړ������킯�ł͂Ȃ��ˍ폜���ꂽ
			if (!moved) list.add(e);
		}
		
		// �\���A�t�@�C���o��
		PrintWriter p = new PrintWriter(new FileWriter("deletedFiles"+date+".txt"));
		
		for (FileEntry e : list) {
//			System.out.println("--------------------");
//			System.out.println(e);
			p.print(e.path);
			p.print(",");
			String fname = FileList.filename(e.path);
			int extIdx = fname.lastIndexOf(".");
			if (extIdx > 0) {
				p.print(fname.substring(0, extIdx));
				p.print(",");
				p.print(fname.substring(extIdx+1));
				p.print(",");
			} else {
				p.print(fname + ",,");
			}
			
			long size = 0;
			List<Long> sl = e.sizeList;
			// �ǂ����� 0 �ɂȂ����
			boolean exist = false;
			for (int i = 0; i < sl.size(); i++) {
				if (sl.get(i) > 0) {
					exist = true;
					size = sl.get(i);
				} else if (exist && sl.get(i) == 0) break;
			}
			
			p.print(size);
			p.print(",");
			p.println(e.lastModified);
		}
		
		p.close();
		
	}
}
