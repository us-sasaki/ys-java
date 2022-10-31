import java.io.*;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.*;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.FileTime;

import com.ntt.analytics.fileusage.*;

/**
 * �w�肳�ꂽ�f�B���N�g���ȉ��̃t�@�C���p�X�A�t�@�C���T�C�Y(�f�B���N�g���̏ꍇ�͔z����
 * �t�@�C���T�C�Y�̑��a)�� csv �`���Ńt�@�C���ɏo�͂���B
 * �t�@�C������ listyyyyMMdd.csv �Ƃ���B
 *
 * @version 2016/3/14 �C��(,��'���t�@�C�����ɂ���ꍇ�A�J�b�g)
 *          2016/5/20 �I�[�i�[��ǉ�
 *          2016/6/03 lastModified (�t�@�C���ŏI�X�V��)��ǉ�
 *			2017/10/22 �����t�@�C���w��ւ̑Ή��Aabdom.data.json �̗��p
 * @author	Yusuke Sasaki
 */
public class FileLister implements Closeable {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	PrintWriter out;
	String filename;
	
	public final static int MAX_DEPTH = FileList.MAX_DEPTH;
	
	public FileLister() throws IOException {
		filename = "csv/list"+sdf.format(new Date()) + ".csv";
		FileWriter fw = new FileWriter(filename);
		out = new PrintWriter(new BufferedWriter(fw));
	}
	
	public void list(File f) throws IOException {
		list(f, 0);
	}
	
	private long list(File f, int depth) throws IOException {
		if (f.isDirectory()) {
			// �f�B���N�g���̏ꍇ
			File[] file = f.listFiles();
			if (file == null) return 0L;
			
			long size = 0;
			for (int i = 0; i < file.length; i++) {
				if (file[i] != null) {
					size += list(file[i], depth + 1);
				}
			}
			printFile(f, size, depth);
			return size;
			
		} else {
			// �ʏ�̃t�@�C���̏ꍇ
			long size = f.length();
			printFile(f, size, depth);
			return size;
		}
	}
	
	/**
	 * 
	 */
	protected void printFile(File f, long size, int depth) throws IOException {
		out.print(String.valueOf(depth)+",");
		printPath(f.getPath());
		out.print(size);
		out.print(',');
		out.print(Files.getOwner(f.toPath()).getName());
		out.print(',');
		out.print(Files.getLastModifiedTime(f.toPath()).toMillis());
		out.println();
	}
	
	protected void printPath(String path) {
		StringTokenizer st = new StringTokenizer(path, "\\");
		for (int i = 0; i < MAX_DEPTH; i++) {
			try {
				out.print(cutIllegalChar(st.nextToken()));
			} catch (NoSuchElementException nsee) {
			}
			out.print(",");
		}
	}
	
	/**
	 * �t�@�C���̕����񂩂����̕����� ' , ��replace����
	 */
	private String cutIllegalChar(String target) {
		return target.replace("\'","<Q>").replace(",","<c>");
	}
	
	@Override
	public void close() throws IOException {
		out.close();
	}
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		try (FileLister f = new FileLister();) {
			for (String fname : args) {
				f.list(new File(fname));
			}
		}
	}
	
}


