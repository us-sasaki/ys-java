import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

/**
 * �t�@�C�����p�󋵕\���p�f�[�^�𐶐����郁�C���v���O����
 *
 * @author	Yusuke Sasaki
 * @version	June 2nd, 2016
 */
public class MakeFileUsage {
	
	/**
	 * �w�肳�ꂽ�f�B���N�g���̃t�@�C����ǂݍ���
	 * list20yyMMdd.csv �̂悤�Ȍ`�̃t�@�C�������ׂēǂݍ���
	 */
	public static FileList readFiles(String path) throws IOException {
		File dir = new File(path);
		if (!dir.isDirectory()) return null;
		
		List<String> filelist = new ArrayList<String>();
		for (String f : dir.list() ) {
			if (f.matches("list20[0-9]{6}\\.csv")) {
				filelist.add(f);
			}
		}
		filelist.sort(null);

		FileList a = new FileList();
		for (String f : filelist) {
			a.addFile(f);
			System.out.println(f);
		}
		
		return a;
	}

/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		
		// csv �t�@�C���Ǎ�
		FileList a = readFiles(".");
		a.setReferencePoint(3);
		
		String date = sdf.format(new Date());
		
		//
		// �t�@�C�������K��
		// [dataType][Level][SortTarget][yyMMdd].json
		//
		// [dataType] ... pos (for stacked or cumulative chart)
		//                pie (for pie chart)
		//                line (for line chart)
		// [Level]    ... Level at which data lies
		// [SortTarget].. Size / Inc(rease)
		//
		
		// ���x��1�ő����̑傫�����ɕ\��
		List<FileEntry> l1 = a.selectLevel(1);
		l1.sort(new SizeOrder().reversed()); // size �~���Ń\�[�g
		
		FileList.writePosJsonFile(a.dateList, l1, "posL1Size"+date+".json");
		FileList.writePieChartJsonFile(FileList.cutFile(l1, true), "pieL1Size"+date+".json");
		
		// ���x��2�ő����̑傫������10�\��
		List<FileEntry> l2 = a.selectLevel(2);
		
		l2.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l2, 10), "lineL2Inc"+date+".json");
		
		// ���x��3�ő����̑傫������20�\��
		List<FileEntry> l3 = a.selectLevel(3);
		l3.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l3, 20), "lineL3Inc"+date+".json");
		
		// ����t�@�C���^�f��T��
		System.out.println("���ȉ��̃t�@�C���͓���t�@�C���Ǝv���܂��B�V���[�g�J�b�g���ł��Ȃ����������Ă��������B");
		List<FileEntry> list = a.selectFile(true);
		list.sort(new SizeOrder().reversed()); // �T�C�Y�~��
		
		long lastSize = -1;
		String lastPath = "";
		String lastFile = "";
		int c = 0;
		for (FileEntry f : list) {
			String p = f.path;
			try {
				String filename = FileList.filename(p);
				if (lastFile.equals(filename)) {
					System.out.println("-------------------------------------------");
					System.out.println(lastPath);
					System.out.println(f.path);
				}
				lastSize = f.size;
				lastPath = f.path;
				lastFile = filename;
				c++;
				if (c > 20) break;
			} catch (Exception e) {
				System.out.println(e);
				System.out.println(p);
				break;
			}
		}
		// �傫���t�@�C����20��
		FileList.writeTableChartJsonFile(FileList.cut(list, 20), "bigFile"+date+".json");

	}
	
}
