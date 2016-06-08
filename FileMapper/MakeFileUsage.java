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
	

/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		
		// csv �t�@�C���Ǎ�
		FileList a = FileList.readFiles(".");
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
		
		// ���x��1�ŃT�C�Y���ɕ\��
		List<FileEntry> l1 = a.selectLevel(1);
		l1.sort(new SizeOrder().reversed()); // size �~���Ń\�[�g
		
		FileList.writePosJsonFile(a.dateList, l1, "posL1Size"+date+".json");
		List<FileEntry> l1dir = FileList.cutFile(l1, true);
		FileList.writePieChartJsonFile(l1dir, "pieL1Size"+date+".json");
		
		// ���x��2�ŏ��3�t�H���_���T�C�Y���ɕ\��
		int n = 3;
		if (n > l1dir.size()) n = l1dir.size();
		for (int i = 0; i < n; i++) {
			final String dir = l1dir.get(i).path;
			List<FileEntry> l2big = a.selectAs(
				 e -> { return (e.level == 2 && e.path.startsWith(dir));} );
				 // �ł���̂�(dir�w��)�A�ł���炵��(final�̏ꍇ(�ȗ��\))
			
			l2big.sort(new SizeOrder().reversed());
			FileList.writePieChartJsonFile(FileList.cut(l2big, 5), "pieL2Size"+date+"_"+i+".json");
		}
		
		// ���x��2�ő����̑傫������10�\��
		List<FileEntry> l2 = a.selectLevel(2);
		l2.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l2, 10), "lineL2Inc"+date+".json");
		
		// ���x��3�ő����̑傫������10�\��
		List<FileEntry> l3 = a.selectLevel(3);
		l3.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l3, 10), "lineL3Inc"+date+".json");
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
		
		JsonObject tableContainer = new JsonObject().add("key","top").add("label","����t�@�C�������m��܂���B�V���[�g�J�b�g���ł��Ȃ����������ĉ������B"); // values(Array) �͌�Ŏw��
		
		JsonArray top = new JsonArray(new JsonType[]{tableContainer});
		
		int c = 0;
		
		for (FileEntry f : list) {
			String p = f.path;
			try {
				String filename = FileList.filename(p);
				if (lastFile.equals(filename)) {
					JsonObject same = new JsonObject();
					same.add("key", "same");
					same.add("label", "No."+(c+1)+", "+FileList.filename(f.path));
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
		// JSON �t�@�C���Ƃ��ďo��
		//
		FileList.writeJsonType(top, "sameFile"+date+".json");
		
		//
		// �T�C�Y�̑傫�ȃt�@�C�����o��
		//
		tableContainer = new JsonObject().add("key","top").add("label","���ݕۑ�����Ă���T�C�Y�̑傫���t�@�C��");
		top = new JsonArray(new JsonType[]{tableContainer});
		
		JsonObject jo = new JsonObject();
		jo.add("key","file");
		jo.add("label", "(�Q�l)�T�C�Y�̑傫�ȃt�@�C��");

		jo.add("_values", jsonArrayPut(FileList.cut(list, 20),
			// lambda expression
			(target, src) -> {
				target.add("label", src.path);
				target.add("value", src.size/1024/1024);
				target.add("owner", FileList.reveal(src.owner));
			}
		));
		tableContainer.add("values", new JsonArray(new JsonType[] {jo}));
		
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
	private static JsonArray jsonArrayPut(List<FileEntry> l,
							java.util.function.BiConsumer<JsonObject, FileEntry> p) {
		JsonType[] result = new JsonType[l.size()];
		for (int i = 0; i < l.size(); i++) {
			JsonObject jo = new JsonObject();
			p.accept(jo, l.get(i));
			result[i] = jo;
		}
		return new JsonArray(result);
	}
}
