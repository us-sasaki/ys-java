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
	
	/**
	 * �����p�ɍ�������́B�����listyyyyMMdd.csv �`���̂��̂��ĂԂ悤�ɕύX
	 * �t�@�C��������A���̏�񂩂��擾����K�v�����邽�߁B
	 * FileMapper ���s�v�ɂȂ肻��
	 *
	 * @deprecated
	 */
	public void readFile(String fname) throws IOException {
		if (list != null) throw new IllegalStateException("���łɒl��ێ����Ă��܂�");
		list = new ArrayList<FileEntry>();
		
		FileReader fr = new FileReader(fname);
		BufferedReader br = new BufferedReader(fr);
		
		int maxTokens = 0;
		while (true) {
			String line = br.readLine();
			if ( (line == null)||(line.equals("")) ) break;
			
			String[] token = line.split(",");
			
			// �t�@�C���T�C�Y���r���Ő؂�邱�Ƃ����邽�߁A�ő�l���擾
			if (token.length > maxTokens) maxTokens = token.length;
			
			// FileEntry �ɕϊ�����
			FileEntry entry = new FileEntry();
			
			entry.level = Integer.parseInt(token[0]);
			String p = "";
			for (int i = 1; i <= MAX_DEPTH; i++) {
				if (token[i].equals("")) continue;
				if (i > 1) p = p + "\\";
				p = p + token[i];
			}
			entry.path = p;
			entry.isDirectory = false; // �����Ə������ĂȂ�
			entry.sizeList = new ArrayList<Long>();
			
			boolean hasSize  = false;
			boolean hasOwner = false;
			
			for (int i = MAX_DEPTH+1; i < token.length; i++) {
				if (token[i].equals("")) token[i] = "0";
				try {
					entry.sizeList.add(Long.decode(token[i]));
					if (i == token.length - 1) hasSize = true;
				} catch (NumberFormatException e) {
					if (i == token.length - 1) {
						entry.owner = token[i];
						hasOwner = true;
					}
					else throw new NumberFormatException(e.getMessage());
				}
			}
			// �Ō�̐����������̏ꍇ�A������̏ꍇ�̗������܂ނƃG���[
			if (hasSize&&hasOwner) throw new NumberFormatException("�t�H�[�}�b�g�G���[");
			
			List<Long> l = entry.sizeList;
			entry.size	= l.get(l.size() - 1);
			if (l.size() == 1) {
				entry.increase = entry.size;
			} else {
				entry.increase = entry.size - l.get(referencePoint);
			}
			
			list.add(entry);
		}
		
		sizeListCount = maxTokens - MAX_DEPTH - 1;
		
		fr.close();
		br.close();
		// isDirectory �̐ݒ�
		// �A���S���Y��
		//   �����path��������܂ޑ��� Entry ������� directory
		//   �܂� path �ɂ��Ď������ɂȂ�ׂ�Ƃ����킩�肻��
		makeup(); // sizeList �̒��������낦�AisDirectory��ݒ�
	}
	
	public void addDateList(String filename) {
		if (dateList == null) dateList = new ArrayList<Long>();
		try {
			long date = sdf.parse(filename.substring(4,12)).getTime();
			dateList.add(date);
		} catch (java.text.ParseException pe) {
			throw new RuntimeException(pe.toString());
		}
	}
	
	/**
	 * listyyyyMMdd.csv �`��(FileLister2 �Ő���)�̃t�@�C���̏���ǂݍ��݂܂��B
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
		addDateList(fname);
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
		if (list == null) throw new IllegalStateException("set �܂��� readFile �ɂ���Ēl���i�[���Ă�������");
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
		if (list == null) throw new IllegalStateException("set �܂��� readFile �ɂ���Ēl���i�[���Ă�������");
		ArrayList<FileEntry> result = new ArrayList<FileEntry>();
		
		for (FileEntry f : list) {
			if (f.isDirectory != isFile) result.add(f);
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
			String owner = fe.owner;
			int codeIndex = owner.lastIndexOf("\\");
			if (codeIndex >= 0) owner = owner.substring(codeIndex + 1);
			p.print(" \"owner\": \"" +(OwnerTable.convert(owner))+"\"" );
			p.print("  }");
		}
		p.println("]}");
		p.println("]");
		
		p.close();
		fw.close();
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
}
