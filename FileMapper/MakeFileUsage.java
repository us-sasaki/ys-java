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
			System.out.println("reading.." + f);
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
		
		System.out.println("processing");
		
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
		//
		// ����t�@�C���^�f��T��
		//
		List<FileEntry> list = a.selectFile(true);
		list.sort(new SizeOrder().reversed()); // �T�C�Y�~��
		
		String lastFile = "";
		FileEntry lastF = null;
		
		// TableChart �ɂ��o�^
		//
		// TableChart��JSON�`���͈ȉ�
		//
		// top = array[] { table }
		// table    = {	"key":"top",
		//				"label":"�t�@�C���g�p�ʍ팸�Ɍ������q���g"
		//				"values": [ group1, group2, group3, ... ] }
		//
		// group(n) = { "key":"key",
		//				"label":"�\�����郉�x����",
		//				"values": [ entry1, entry2, entry3, ... ] }
		//
		// entry(n) = { "label":"�\�����郉�x����",
		//				"value":"�t�@�C���T�C�Y",
		//				"owner":"���L�Җ�" }
		
		JsonObject tableContainer = new JsonObject().add("key","top").add("label","�t�@�C���g�p�ʍ팸�Ɍ������q���g"); // values(Array) �͌�Ŏw��
		
		JsonArray top = new JsonArray(new JsonType[]{tableContainer});
		
		int c = 0;
		
		for (FileEntry f : list) {
			String p = f.path;
			try {
				String filename = FileList.filename(p);
				if (lastFile.equals(filename)) {
					JsonObject same = new JsonObject();
					same.add("key", "same");
					same.add("label", "����t�@�C�������m��܂���(No."+(c+1)+", "+FileList.filename(f.path)+")�B�V���[�g�J�b�g���ł��Ȃ����������ĉ������B");
					JsonType[] jt = new JsonType[2];
					jt[0] = new JsonObject().add("label", lastF.path)
								.add("value", lastF.size/1024/1024)
								.add("owner", FileList.reveal(lastF.owner));
					jt[1] = new JsonObject().add("label", f.path)
								.add("value", f.size/1024/1024)
								.add("owner", FileList.reveal(f.owner));
					same.add("_values", jt); // �����̏ꍇ�A����Array��(1���Ƃ��܂������Ȃ��Ǝv����)
					// �ŏ��� JsonObject �Ƃ��ēo�^����邪�A2��ڂɓ��� key �œo�^
					// ����Ƃ��AJsonArray �ɕϊ������B
					// table chart �ł� JsonArray �ł��邱�Ƃ��K�v�B
					tableContainer.add("values", same); // _ means folded
					c++;
					if (c >= 10) break; // 10�܂�
					
				}
				lastFile	= filename;
				lastF		= f;
			} catch (Exception e) {
				System.out.println(e);
				System.out.println(p);
				break;
			}
		}
		
		//
		// �T�C�Y�̑傫�ȃt�@�C�����o��
		//
		JsonObject jo = new JsonObject();
		jo.add("key","file");
		jo.add("label", "(�Q�l)�T�C�Y�̑傫�ȃt�@�C��");

		jo.add("_values", jsonArrayPut(FileList.cut(list, 20),
			// lambda expression ( new Putter(JsonObject target, FileEntry src) {..
			(target, src) -> {
				target.add("label", src.path);
				target.add("value", src.size/1024/1024);
				target.add("owner", FileList.reveal(src.owner));
			}
		));
		tableContainer.add("values", jo);
		
		//
		// JSON �t�@�C���Ƃ��ďo��
		//
		FileList.writeJsonType(top, "bigFile"+date+".json");
		
		System.out.println("done!");
	}
	
	/**
	 * ���Ă���t�@�C���𒊏o���A��ƒ��t�@�C���^�f�𔭐M
	 *
	 * ���Ă���t�@�C��
	 * �@(1) �g���q�������ł��邱��
	 * �@(2) �t�@�C���������Ă��邱��(�T�^��F���t�����Ⴄ)
	 * �@(3) �T�C�Y�����Ă��邱��
	 *
	 * �@��L�A���Ă��� or ���Ă��Ȃ� �𔻒�B臒l�͓��v�I�Ɍv�Z����B
	 */
	
	
	/**
	 * �w�肳�ꂽ List ����w��̃��[���Ńf�[�^�𒊏o���AJsonArray �`���ŕԋp����B
	 *
	 * @param	l	FileEntry��List
	 * @param	p	List����f�[�^�𒊏o�AJsonObject������JsonArray�ɒǉ��������
	 * @return	�ł��������� JsonArray
	 */
	private static JsonArray jsonArrayPut(List<FileEntry> l, Putter p) {
		JsonType[] result = new JsonType[l.size()];
		for (int i = 0; i < l.size(); i++) {
			JsonObject jo = new JsonObject();
			p.put(jo, l.get(i));
			result[i] = jo;
		}
		return new JsonArray(result);
	}
	
/*--------------
 * static class
 */
	private static interface Putter {
		void put(JsonObject target, FileEntry srcData);
	}
}
