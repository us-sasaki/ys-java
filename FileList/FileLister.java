import java.io.*;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.*;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.FileTime;

/**
 * �w�肳�ꂽ�f�B���N�g���ȉ��̃t�@�C���p�X�A�t�@�C���T�C�Y(�f�B���N�g���̏ꍇ�͔z����
 * �t�@�C���T�C�Y�̑��a)�� csv �`���Ńt�@�C���ɏo�͂���B
 * �t�@�C������ listyyyyMMdd.csv �Ƃ���B
 *
 * @version 2016/3/14 �C��(,��'���t�@�C�����ɂ���ꍇ�A�J�b�g)
 *          2016/5/20 �I�[�i�[��ǉ�
 *          2016/6/03 lastModified (�t�@�C���ŏI�X�V��)��ǉ�
 */
public class FileLister {
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	PrintWriter out;
	
	public final static int MAX_DEPTH = 20;
	

	public void list(File f) throws IOException {
		FileWriter fw = new FileWriter("list"+sdf.format(new Date()) + ".csv");
		out = new PrintWriter(new BufferedWriter(fw));
		list(f, 0);
		out.close();
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
	
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		FileLister f = new FileLister();
		f.list(new File(args[0]));
	}
	
}


