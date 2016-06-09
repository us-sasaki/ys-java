import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

/**
 * ������̃f�B���N�g�����t�@�C���T�C�Y�������ɑ�R�e�ʂ�����Ă���
 * �t�@�C���𒊏o����B
 */
public class FileList {
	public static int MAX_DEPTH = FileLister.MAX_DEPTH;
	
	
	static SimpleDateFormat	sdf = new SimpleDateFormat("yyyyMMdd");
	
	List<Long>		dateList;
	List<FileEntry> list;
	int sizeListCount;
	int referencePoint; // increase ���v�Z����, list �� index �Ŏw��
	
/*-------------
 * constructor
 */
	/**
	 * ��� FileList �����܂��B
	 */
	public FileList() {
		list = null;
		dateList = null;
		sizeListCount = 0;
		referencePoint = 0; // �ŏ��̃T�C�Y�Ɣ�r���� increase ���v�Z
	}
	
/*------------------
 * instance methods
 */
	public void setReferencePoint() {
		referencePoint = dateList.size() - 1;
	}
	public void setReferencePoint(int position) {
		if (position < 0 || position >= dateList.size()) throw new IndexOutOfBoundsException("setReferencePoint(int) ���݁A�f�[�^�t�@�C����"+dateList.size()+"�ݒ肳��Ă��܂��B���̐������̒l��ݒ肵�Ă�������");
		referencePoint = position;
	}
	
	/**
	 * listyyyyMMdd.csv �`��(FileLister �Ő���)�̃t�@�C���̏���ǂݍ��݂܂��B
	 */
	public void addFile(String fname) throws IOException {
		if (list == null) list = new ArrayList<FileEntry>();
		
		// �x�����߁A���������� Map ���g�������ɕύX
		Map<String, FileEntry> map = new TreeMap<String, FileEntry>();
		for (FileEntry fe : list) map.put(fe.path, fe);
		
		FileReader		fr	= new FileReader(fname);
		BufferedReader	br	= new BufferedReader(fr);
		
		while (true) {
			String line = br.readLine();
			if ( (line == null)||(line.equals("")) ) break;
			
			String[] token = line.split(",");
			
			// �܂� path �𐶐�����
			String p = "";
			for (int i = 1; i <= MAX_DEPTH; i++) {
				if (token[i].equals("")) continue;
				if (i > 1) p = p + "\\";
				p = p + token[i];
			}
			
			FileEntry entry = map.get(p);
			if (entry == null) {
				// �V���� path
				entry = new FileEntry();
				map.put(p, entry); // entry �̎Q�Ƃ�����ɓo�^�A���g�͈ȍ~�ύX
				
				entry.level = Integer.parseInt(token[0]);
				entry.path = p;
				entry.isDirectory = false; // �����Ə������ĂȂ�
				entry.sizeList = new ArrayList<Long>();
				for (int i = 0; i < sizeListCount; i++) {
					entry.sizeList.add(0L);
				}
			}
			entry.sizeList.add(Long.decode(token[MAX_DEPTH + 1]));
			
			// owner ���㏑������
			if (token.length >= MAX_DEPTH + 3)
				entry.owner = token[MAX_DEPTH + 2];
			// lastModified ���㏑������
			if (token.length >= MAX_DEPTH + 4)
				entry.lastModified = Long.parseLong(token[MAX_DEPTH + 3]);
		}
		
		sizeListCount++;
		
		fr.close();
		br.close();
		
		// map �� list �ɍĐݒ�
		list.clear();
		for (FileEntry entry : map.values()) {
			list.add(entry);
		}
		
		// sizeList �̒��������낦�AisDirectory ��ݒ�
		makeup();
		
		// �t�@�C��������Adate ���擾
		try {
			long date = sdf.parse(fname.substring(4,12)).getTime();
			dateList.add(date);
		} catch (java.text.ParseException pe) {
			throw new RuntimeException(pe.toString());
		}
	}
	
	/**
	 * list �� path �̎����������ɐ��񂵁A
	 * sizeList �̒����� sizeListCount (addFile ������) �ɑ����A
	 * �e FileEntry �� isDirectory �t���O��ݒ肷��B���̃t���O�ݒ�́A
	 * path �̎��������ɕ��ׂ��Ƃ��A�����ȍ~�Ɏ������܂� path ������
	 * ���Ȃ��ꍇ�Ƀt�@�C��(isDirectory = false)�Ƃ��Ă���B<BR>
	 * ��)												<br>
	 * path1 = Y:\hoge\tarou.hoe						<br>
	 * path2 = Y:\hoge\tarou.hoe\bar					<br>
	 * path3 = Y:\hoge\tarou.hoe\foo					<br>
	 * path4 =(Y:\hoge\tarou.hoe\foo �Ŏn�܂�Ȃ�����)	<br>
	 *
	 * �̂悤�ɂȂ��Ă����ꍇ�Apath1 �̂� directory, path2/3 �� file �Ɣ��肷��
	 * �܂�A��̃f�B���N�g���̓t�@�C���Ɣ��肳��镾�Q�����邪�Apath �݂̂���
	 * directory �𔻒肷���i�͂Ȃ��A���̃t�@�C��(�f�B���N�g��)�͏�ɃT�C�Y��0
	 * �Ȃ̂ŁA�㑱�����ɉe�����Ȃ����߁A���Ȃ��B
	 */
	private void makeup() {
		// path �̎����������Ń\�[�g
		list.sort(new PathOrder());
		
		for (FileEntry e : list) {
			int l = e.sizeList.size();
			for (int i = 0; i < sizeListCount - l; i++) {
				e.sizeList.add(new Long(0));
			}
			e.size = e.sizeList.get(e.sizeList.size() - 1);
		}
		FileEntry f = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			FileEntry next = list.get(i);
			if (next.path.startsWith(f.path)) f.isDirectory = true;
			else f.isDirectory = false;
			f = next;
		}
		// increase ���v�Z
		for (FileEntry e : list) {
			List<Long> l = e.sizeList;
			e.size	= l.get(sizeListCount - 1); // �Ō�(�ŐV)�̃T�C�Y
			e.increase = e.size - l.get(referencePoint);
		}
	}
	
	/**
	 * ���̃C���X�^���X���ێ����Ă��� list �ւ̎Q�Ƃ�ԋp���܂��B
	 * �ԋp���ꂽ list �� FileEntry ���e��ύX�����ꍇ�A���̃C���X�^���X�� list ��
	 * �ύX����邱�Ƃɒ��ӂ��K�v�ł��B
	 */
	public List<FileEntry> getList() {
		return list;
	}
	
	/**
	 * �t�@�C���̐[��(�K�w�Alevel)���w�肵�āA�Y������ FileEntry ����Ȃ�
	 * list ��ԋp���܂��B
	 * �ԋp���ꂽ list �� FileEntry ���e��ύX�����ꍇ�A���̃C���X�^���X�� list ��
	 * �ύX����邱�Ƃɒ��ӂ��K�v�ł��B
	 */
	public List<FileEntry> selectLevel(int level) {
		if (list == null) throw new IllegalStateException("addFile �ɂ���Ēl���i�[���Ă�������");
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : list) {
			if (f.level == level) result.add(f);
		}
		return result;
	}
	
	/**
	 * �t�@�C���̎��(�t�@�C��/�f�B���N�g��)���w�肵�āA�Y������ FileEntry ����Ȃ�
	 * list ��ԋp���܂��B
	 * �ԋp���ꂽ list �� FileEntry ���e��ύX�����ꍇ�A���̃C���X�^���X�� list ��
	 * �ύX����邱�Ƃɒ��ӂ��K�v�ł��B
	 */
	public List<FileEntry> selectFile(boolean isFile) {
		if (list == null) throw new IllegalStateException("addFile �ɂ���Ēl���i�[���Ă�������");
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : list) {
			if (f.isDirectory != isFile) result.add(f);
		}
		return result;
	}
	
	/**
	 * �C�ӂ̃��[��(Predicate<FileEntry>)�ɏ]���ăt�@�C���𒊏o����
	 *
	 * @param	fes		�t�@�C�����o���[��
	 * @return	���o���ꂽ FileEntry �� List (shallow copy)
	 */
	public List<FileEntry> selectAs(java.util.function.Predicate<FileEntry> fes) {
		if (list == null) throw new IllegalStateException("addFile �ɂ���Ēl���i�[���Ă�������");
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : list) {
			if (fes.test(f)) result.add(f);
		}
		return result;
	}
	
	/**
	 * �C�ӂ̃��[���ɏ]���āAsublist ���擾����B
	 */
	public static List<FileEntry> selectAs(List<FileEntry> src, java.util.function.Predicate<FileEntry> p) {
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : src) {
			if (p.test(f)) result.add(f);
		}
		return result;
	}
	
/*---------------
 * class methods
 *
	/**
	 * path �����񂩂�t�@�C�������擾
	 */
	public static String filename(String pathString) {
		return new File(pathString).getName();
	}
	
	/**
	 * NVD3 line chart �p JSON �t�@�C���o��
	 */
	public static void writeJsonFile(List<Long> dateList,
							List<FileEntry> target,
							String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter p = new PrintWriter(fw);
		
		p.println("["); // start mark
		boolean first = true;
		for (FileEntry fe : target) {
			if (!fe.isDirectory) continue;
			if (!first) {
				p.println(",");
			} else {
				first = false;
			}
			p.println("  {");
			String path = fe.path;
			path = path.replace("\\", "\\\\");
			p.println("    \"key\": \"" + filename(path) + "\",");
			p.print("    \"values\": [ ");
			int i = 0;
			for (Long date : dateList) {
				if (i > 0) p.print(" , ");
				p.print("{\"x\":" + date + " , \"y\":" + (fe.sizeList.get(i++)/1024/1024) + "}");
			}
			p.println("]");
			p.print("  }");
		}
		p.println();
		p.println("]");
		
		p.close();
		fw.close();
		fos.close();
	}
	
	/**
	 * NVD3 stacked area chart / cumulative line chart �p JSON �t�@�C���o��
	 */
	public static void writePosJsonFile(List<Long> dateList,
							List<FileEntry> target,
							String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter p = new PrintWriter(fw);
		
		p.println("["); // start mark
		boolean first = true;
		for (FileEntry fe : target) {
			if (!fe.isDirectory) continue;
			if (!first) {
				p.println(",");
			} else {
				first = false;
			}
			p.println("  {");
			String path = fe.path;
			path = path.replace("\\", "\\\\");
			p.println("    \"key\": \"" + path + "\",");
			p.print("    \"values\": [ ");
			int i = 0;
			for (Long date : dateList) {
				if (i > 0) p.print(", ");
				p.print("[" + date + ", " + (fe.sizeList.get(i++)/1024/1024) + "]");
			}
			p.println("]");
			p.print("  }");
		}
		p.println();
		p.println("]");
		
		p.close();
		fw.close();
		fos.close();
	}
	/**
	 * NVD3 Pie Chart �p JSON �t�@�C���o��
	 */
	public static void writePieChartJsonFile(List<FileEntry> target,
							String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter p = new PrintWriter(fw);
		
		p.println("["); // start mark
		boolean first = true;
		for (FileEntry fe : target) {
			//if (!fe.isDirectory) continue;
			if (!first) {
				p.println(",");
			} else {
				first = false;
				p.println();
			}
			p.print("  {");
			p.print(" \"label\": \"" + filename(fe.path) + "\",");
			p.print(" \"value\": \"" +(fe.size/1024/1024)+"\"" );
			p.print("  }");
		}
		p.println();
		p.println("]");
		
		p.close();
		fw.close();
		fos.close();
	}
	/**
	 * NVD3 Indented Table �p JSON �t�@�C���o��
	 */
	public static void writeTableChartJsonFile(List<FileEntry> target,
							String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter p = new PrintWriter(fw);
		
		p.println("["); // start mark
		p.println("  { \"key\": \"file\", \"label\": \"�T�C�Y�̑傫�ȃt�@�C��\", \"values\": [");
		boolean first = true;
		for (FileEntry fe : target) {
			if (!first) {
				p.println(",");
			} else {
				first = false;
				p.println();
			}
			p.print("  {");
			p.print(" \"label\": \"" + fe.path.replace("\\", "\\\\") + "\",");
			p.print(" \"value\": \"" +(fe.size/1024/1024)+"\"," );
			p.print(" \"owner\": \"" +reveal(fe.owner)+"\"" );
			p.print("  }");
		}
		p.println("]}");
		p.println("]");
		
		p.close();
		fw.close();
		fos.close();
	}
	
	/**
	 * owner �����񂩂疼�O���}�b�s���O����
	 */
	public static String reveal(String owner) {
		int codeIndex = owner.lastIndexOf("\\");
		if (codeIndex >= 0) owner = owner.substring(codeIndex + 1);
		return OwnerTable.convert(owner);
	}
	
	/**
	 * JsonObject ���t�@�C���ɏo�͂���
	 */
	public static void writeJsonType(JsonType obj, String filename) throws Exception {
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		//bos.write("[\n".getBytes("UTF-8"));
		bos.write(obj.toString().getBytes("UTF-8"));
		//bos.write("\n]".getBytes("UTF-8"));
		bos.close();
		fos.close();
	}
	
	/**
	 * CSV�`���t�@�C���o��
	 */
	public static void writeFile(String filename, List<FileEntry> target, List<Long> dateList, int maxCount)
				throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		Writer fw = new OutputStreamWriter(fos, "Shift_JIS"); // for EXCEL
		PrintWriter p = new PrintWriter(fw);
		int count = 0;
		
		p.print("�t�H���_");
		for (Long date : dateList) {
			p.print(",");
			p.print(sdf.format(new Date(date)));
		}
		p.println();
		for (FileEntry fe : target) {
			p.print(fe.path);
			for (Long size : fe.sizeList) {
				p.print(",");
				p.print(size/1024/1024); // Mbyte
			}
			p.println();
			count++;
			if (count >= maxCount) break;
		}
		
		p.close();
		fw.close();
		fos.close();
	}
	
	public static List<FileEntry> cut(List<FileEntry> src, int count) {
		List<FileEntry> result = new ArrayList<FileEntry>();
		for (int i = 0; i < count; i++) {
			result.add(src.get(i));
		}
		return result;
	}
	
	public static List<FileEntry> cutFile(List<FileEntry> src, boolean cutFile) {
		List<FileEntry> result = new ArrayList<FileEntry>();
		for (FileEntry fe : src) {
			if (fe.isDirectory == cutFile) result.add(fe);
		}
		return result;
	}
	
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
	
}
