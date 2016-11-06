import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static java.util.Map.Entry;

/**
 * �t�@�C�����p�󋵕\���p�f�[�^�𐶐����郁�C���v���O����
 *
 * @author	Yusuke Sasaki
 * @version	June 19th, 2016
 */
public class MakeFileUsage {
	/** FileList �I�u�W�F�N�g */
	FileList		a;
	/** ���t�@�C���̂�(!isDirectory)�� List */
	List<FileEntry> list;
	/** �t�@�C�����Ɏg�p������t������(yyMMdd) */
	String			date;

/*------------------
 * instance methods
 */
	public void make() throws IOException {
		init();
		System.out.println("processing");
		System.out.println("calculating for chart");
		makeCharts();
		System.out.println("finding same files");
		findSameFiles();
		System.out.println("finding similar files");
		findSimilarFiles();
		System.out.println("listing big files");
		listBigFiles();
		System.out.println("listing file usage of users");
		listFileUsage();
		System.out.println("done!");
	}
	
	/**
	 * �t�@�C���Ǎ��A������
	 */
	private void init() throws IOException {
		// csv �t�@�C���Ǎ�
		a = FileList.readFiles(".");
		a.setReferencePoint(12); // 2016/6/17   9); // 2016/6/7
		list = a.selectFile(true); // �t�@�C���������o
		list.sort(new SizeOrder().reversed()); // �T�C�Y�~��
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		
		date = sdf.format(new Date());
	}
	
	private void makeCharts() throws IOException {
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
		l1 = FileList.cutFile(l1, true);
		l1.sort(new SizeOrder().reversed()); // size �~���Ń\�[�g
		
		FileList.writeJsonFile(a.dateList, l1, 1, "posL1Size"+date+".json");
		List<FileEntry> l1dir = FileList.cutFile(l1, true);
		FileList.writePieChartJsonFile(l1dir, 1, "pieL1Size"+date+".json");
		
		// ���x��2�ŏ��3�t�H���_���T�C�Y���ɕ\��
		int n = 3;
		if (n > l1dir.size()) n = l1dir.size();
		for (int i = 0; i < n; i++) {
			final String dir = l1dir.get(i).path;
			List<FileEntry> l2big = a.selectAs(
				 e -> { return (e.level == 2 && e.path.startsWith(dir));} );
				 // �ł���̂�(dir�w��)�A�ł���炵��(final�̏ꍇ(�ȗ��\))
			
			l2big.sort(new SizeOrder().reversed());
			FileList.writePieChartJsonFile(FileList.cut(l2big, 5), 1, "pieL2Size"+date+"_"+i+".json");
		}
		
		// ���x��2�ő����̑傫������10�\��
		List<FileEntry> l2 = a.selectLevel(2);
		l2.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l2, 10), 1, "lineL2Inc"+date+".json");
		
		// ���x��3�ő����̑傫������10�\��
		List<FileEntry> l3 = a.selectLevel(3);
		l3.sort(new IncreaseOrder().reversed());
		FileList.writeJsonFile(a.dateList, FileList.cut(l3, 10), 2, "lineL3Inc"+date+".json");
	}
	
	/**
	 * ����t�@�C���^�f��T��
	 */
	private void findSameFiles() throws IOException {
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
		
		JsonObject tableContainer = new JsonObject().add("key","top").add("label","����Ǝv����t�@�C���F�V���[�g�J�b�g���v����"); // values(Array) �͌�Ŏw��
		
		JsonArray top = new JsonArray(new JsonType[]{tableContainer});
		
		int c = 0;
		
		for (FileEntry f : list) {
			if (f.size/1024/1024 < 10) break;
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
	}
	
	/**
	 * ���Ă���t�@�C����T��
	 */
	private void findSimilarFiles() throws IOException {
		
		// ��Ɍ����i��Ƒ���
		List<FileEntry> largeFiles =
				FileList.selectAs(a.list,
						e ->
							( (e.size > 200000)&&(!e.isDirectory)&&
							 (e.path.endsWith("pptx") || e.path.endsWith("ppt") ||
							  e.path.endsWith("xlsx") || e.path.endsWith("xls") ||
							  e.path.endsWith("docx") || e.path.endsWith("doc") ||
							  e.path.endsWith("txt") )
							)
				);
		
		List<SimilarFilePicker.FileDistance> fdl = new SimilarFilePicker(largeFiles).getDistanceList();
		
		TreeMap<String, Integer> appearPaths = new TreeMap<String, Integer>();
		for (SimilarFilePicker.FileDistance fd : fdl) {
			if (fd.dist > 5000) break; // dist 5000�����͎��Ă��Ȃ��t�@�C��
			// fd.a �� path ��o�^
			String path = fd.a.path;
			int idx = path.lastIndexOf('\\');
			if (idx >= 0) {
				path = path.substring(0, idx); // path string
				Integer count = appearPaths.get(path);
				if (count == null) appearPaths.put(path, 1);
				else appearPaths.put(path, count+1);
			}
			// fd.b �� path ��o�^
			path = fd.b.path;
			idx = path.lastIndexOf('\\');
			if (idx >= 0) {
				path = path.substring(0, idx); // path string
				Integer count = appearPaths.get(path);
				if (count == null) appearPaths.put(path, 1);
				else appearPaths.put(path, count+1);
			}
		}
		// List �ɕϊ����ă\�[�g����(�~��)
		Set<Entry<String, Integer>> viewSet = appearPaths.entrySet();
		
		List<Entry<String, Integer>> view = new ArrayList<Entry<String, Integer>>();
		for (Entry<String, Integer> ent : viewSet) {
			view.add(ent);
		}
		
		view.sort(new Comparator<Entry<String, Integer>>() {
					public int compare(Entry<String, Integer> a, Entry<String, Integer> b) {
						return a.getValue() - b.getValue();
					}
		}.reversed() );
		
		JsonObject folder = new JsonObject();
		folder.add("key","file");
		folder.add("label", "�폜��₪�����Ɛ��肳���10�t�H���_(������)");
		
		int count = 0;
		for (Entry<String, Integer> e : view) {
			JsonObject jt = new JsonObject();
			jt.add("label", e.getKey());
			jt.add("value", e.getValue());
			
			folder.add("_values", jt); // ����Array���Ɋ���
			count++;
			if (count >= 10) break;
		}
		//
		// JSON �t�@�C���Ƃ��ďo��
		//
		FileList.writeJsonType(new JsonArray(new JsonType[] {folder}),
			"similarFile"+date+".json");
	}
	
	/**
	 * �T�C�Y�̑傫�ȃt�@�C�����o��
	 */
	private void listBigFiles() throws IOException {
		JsonObject jo = new JsonObject();
		jo.add("key","file");
		jo.add("label", "(�Q�l)�T�C�Y�̑傫�ȃt�@�C��");

		jo.add("_values", jsonObjectArray(FileList.cut(list, 20),
			// lambda expression
			(target, src) -> {
				target.add("label", src.path)
						.add("value", src.size/1024/1024)
						.add("owner", FileList.reveal(src.owner));
			}
		));
		//
		// JSON �t�@�C���Ƃ��ďo��
		//
		FileList.writeJsonType(new JsonArray(new JsonType[]{jo} ),
			"bigFile"+date+".json");
	}
	
	/**
	 * �l���̃t�@�C���g�p�ʂ�����
	 */
	private void listFileUsage() throws IOException {
		Map<String, Long> usage = new TreeMap<String, Long>();
		Map<String, Long> lastUsage = new TreeMap<String, Long>();
		Map<String, Long> actSize = new TreeMap<String, Long>();
		
		// referencePoint(2016/6/7) ����̌����ʂ��擾
		// name         : �t�@�C���̏��L�Җ�
		// usage        : ���̎g�p�҂̃t�@�C���T�C�Y���v
		// lastUsage    : referencePoint���̃t�@�C���T�C�Y���v
		// reductionRate: �O�񂩂�ǂꂾ���T�C�Y������������
		// actSize      : �O�񂩂�t�@�C���폜��ړ����ǂꂾ��������
		int referencePoint = a.getReferencePoint();
		for (FileEntry f : list) {
			String owner = FileList.reveal(f.owner);
			// usage ���擾
			Long size = usage.get(owner);
			if (size == null) usage.put(owner, f.size);
			else usage.put(owner, size + f.size);
			
			// lastUsage ���擾
			Long lastSize = lastUsage.get(owner);
			if (size == null) lastUsage.put(owner, f.sizeList.get(referencePoint));
			else lastUsage.put(owner, lastSize + f.sizeList.get(referencePoint));
			
			// actSize ���擾(���������̓J�E���g���Ȃ�)
			boolean exists  = false;
			long    maxSize = 0;
			for (int i = referencePoint; i < f.sizeList.size(); i++) {
				if (f.sizeList.get(i) > 0) { // �t�@�C�������݂���
					if (!exists) exists = true;
					if (maxSize < f.sizeList.get(i)) maxSize = f.sizeList.get(i);
				} else if (exists) {
					// ��x���݂���������� -> deleted
					Long s = actSize.get(owner);
					if (s == null) actSize.put(owner, 1L); //maxSize);
					else actSize.put(owner, s + 1L); //maxSize);
					break; // ��x�ڂ̓J�E���g���Ȃ�
				}
			}
		}
		
		// Json�`���ŕۑ�(����`��)
		JsonObject[] memberArray = new JsonObject[usage.keySet().size()];
		
		int idx = 0;
		for (String name : usage.keySet() ) {
			memberArray[idx] = new JsonObject().add("owner", name);
			Long u = usage.get(name);
			memberArray[idx].add("usage", u.toString());
			Long lu = lastUsage.get(name);
			memberArray[idx].add("lastUsage", lu.toString());
			int reductionRate = 0;
			if (0L != u) reductionRate = (int)((lu - u)*100L/lu);
			memberArray[idx].add("reductionRate", String.valueOf(reductionRate));
			Long ds = actSize.get(name);
			memberArray[idx].add("activitySize", String.valueOf(ds));
			idx++;
		}
		FileList.writeJsonType(new JsonArray(memberArray), "usage"+date+".json");
		
		// Json�`���ŕۑ�(scatterChart�`��)
		
		
		
		List<JsonObject> jl = new ArrayList<JsonObject>();
//		JsonObject[] jo = new JsonObject[usage.keySet().size()];
		idx = 0;
		for (String name : usage.keySet() ) {
			if (Math.abs(lastUsage.get(name) - usage.get(name)) < 1024.0) continue;
			if (actSize.get(name) == null) continue;
			
			JsonObject jo = new JsonObject().add("key", name);
			
			JsonObject j = new JsonObject();
			j.add("x", rescale(lastUsage.get(name) - usage.get(name)));
			Long act = actSize.get(name);
			if (act == null) act = new Long(0L);
			j.add("y", rescale(act.longValue()));
			j.add("size", rescale(usage.get(name).longValue()));
			j.add("shape", "circle");
			
			
			jo.add("values", new JsonArray(new JsonType[] { j } ));
			
			jl.add(jo);
//			idx++;
		}
		JsonArray data = new JsonArray(jl.toArray(new JsonObject[0]));
		
		FileList.writeJsonType(data, "activity"+date+".json");
	}
	
	private static double rescale(double x) {
		if (x < 0) return -Math.pow(-x, 0.25d);
		if (x == 0) return 0d;
		return Math.pow(x, 0.25d);
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
	private static JsonArray jsonObjectArray(List<FileEntry> l,
							java.util.function.BiConsumer<JsonObject, FileEntry> p) {
		JsonType[] result = new JsonType[l.size()];
		for (int i = 0; i < l.size(); i++) {
			JsonObject jo = new JsonObject();
			p.accept(jo, l.get(i));
			result[i] = jo;
		}
		return new JsonArray(result);
	}

/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		new MakeFileUsage().make();
	}
}
