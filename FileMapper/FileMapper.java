import java.io.*;
import java.util.*;
//import com.ntt.util.CsvReader;

/**
 * Csv�`���ŋL�ڂ��ꂽ�����̃t�@�C����ǂݏo���A�p�X��������L�[�Ƃ���
 * �t�@�C���T�C�Y�̔z���l�Ƃ��� Map �𐶐����܂��B
 */
public class FileMapper {
	Map<String, List<Long>> map;
	int files;
	
	public static final int MAX_DEPTH = FileLister2.MAX_DEPTH;
	
/*-------------
 * Constructor
 */
	public FileMapper() {
		map = new HashMap<String, List<Long>>();
		files = 0;
	}
	
/*------------------
 * instance methods
 */
	public Map<String, List<Long>> addFile(String fname) throws IOException {
	
		BufferedReader br = new BufferedReader(new FileReader(fname));
		//CsvReader cr = new CsvReader(fr);
		
		files++;
		
		int rows = 0;
		while (true) {
//			String[] row = cr.readRow();
			String line = br.readLine();
			rows++;
//			if (row == null || row.length == 0) break;
			if (line == null || line.equals("")) break;
			String[] row = line.split(","); // assume comma separated
			if (row.length != MAX_DEPTH + 3)
				throw new IndexOutOfBoundsException(fname + "�̃t�H�[�}�b�g�ُ�: " +
						row+"�s�ڂ̃J��������"+row.length+
						"�ɂȂ��Ă��܂�(��������"+(MAX_DEPTH+3)+")");
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < MAX_DEPTH + 1; i++) {
				sb.append(row[i]);
				sb.append(',');
			}
			sb.append(row[MAX_DEPTH + 1]);
			String key = sb.toString();
			
			// row �� MAX_DEPTH + 2 �Ԗڂ̗v�f��size
			Long size = Long.parseLong(row[MAX_DEPTH + 2]);
			
			// sb �� size �̑g��}��
			List<Long> v = map.get(key);
			
			if (v == null) v = new ArrayList<Long>();
			int l = v.size();
			
			for (int i = l; i < files - 1; i++) {
				v.add(new Long(0));
			}
			v.add(size);
			map.put(key, v);
		}
		return map;
	}
	
	/**
	 * map �� csv�`���t�@�C���ɏ����o���܂�
	 */
	public void toFile(String fname) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(fname));
		
		for (String key : map.keySet()) {
			List<Long> list = map.get(key);
			pr.print(key);
			for (Long size : list) {
				pr.print(",");
				pr.print(size);
			}
			pr.println();
		}
		
		pr.close();
	}
	
/*
 * main
 */
	public static void main(String[] args) throws IOException {
		FileMapper fm = new FileMapper();
		String fname = "";
		for (int i = 0; i < args.length; i++) {
			fm.addFile(args[i]);
			fname = fname + args[i];
		}
		fm.toFile(fname);
	}
	
}

