import java.io.*;
import java.util.Arrays;

import abdom.data.ByteArray;

/**
 * ���{��̕����G���R�[�h�����o���A�ϊ�����v���O�����B
 * ���s�����f�B���N�g���ȉ��̂��ׂĂ� .java �t�@�C���̃G���R�[�h��
 * UTF-8N �ɕϊ�����B
 * �o�b�N�A�b�v�t�@�C���Ƃ��� .orig �t�@�C���𐶐�����B
 */
public class ConvEncoding {

	public static boolean useBackup = true;
	
	/**
	 * �������镶���G���R�[�f�B���O���X�g�B
	 * ��Ɍ��ꂽ���̂��D�悳���B�e�X�g���āA���̌��ʂɂȂ��Ă��邽��
	 * ������Ȃ����ƁB
	 * ���ɁAJIS �t�@�C���� UTF-8 �Ɍ�F�������B(�t�͂Ȃ�Ȃ�)
	 */
	private static final String[] ENCODINGS;
	static {
		ENCODINGS = new String[] {
						"ISO-2022-JP",
						"UTF-8",
						"EUC-JP",
						"Shift_JIS",
						"Windows-31J",
						"UTF-16",
						"UTF-32"
						};
	}
	
	/**
	 * JIS ���`�F�b�N�ł��Ȃ��BENCODINGS �̏�������ւ���
	 * JIS �̏ꍇ�̓��Ꮘ�� 0x4A(Terapad) -> 0x42(Java) �̈Ⴂ��
	 * �z�����邱�Ƃł܂��ɂȂ����B(�u�����v�̂Q�����̃t�@�C���ŁA
	 * JIS/UTF-8/EUC/ShiftJIS �𐳂������肵��)
	 * Terapad �� 0x4A �ɂȂ邪�Asakura �ł� 0x42 �������̂ŁA
	 * Terapad �̕ϊ����C���M�����[�Ȃ̂�������Ȃ��B
	 */
	public static String detectEncoding(byte[] text) {
		for (String encoding : ENCODINGS) {
			if (checkEncoding(text, encoding))
				return encoding;
		}
		return null;
	}
	
	/**
	 * JIS �̏ꍇ�A�o�C�g�R�[�h���قȂ邱�Ƃ����邪�A��������e����
	 * equals
	 */
	public static boolean equalsJIS(byte[] a, byte[] b) {
		if (a.length != b.length) return false;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == b[i]) continue;
			if (a[i] == (byte)0x4A && b[i] == (byte)0x42) continue;
			return false;
		}
		return true;
	}
	
	/**
	 * �w�肳�ꂽ�G���R�[�f�B���O�ŕ����񉻁AgetBytes() ���ĕω�
	 * ���Ȃ����̂������B�ω�������̂́@���������@�ƌ��Ȃ��B
	 */
	public static boolean checkEncoding(byte[] text, String encoding) {
		try {
			String str = new String(text, encoding);
			//System.out.println(str);
			byte[] enc = str.getBytes(encoding);
			//System.out.println("text.length = " + text.length + "  enc.length = " + enc.length);
			//System.out.println(ByteArray.toDumpList(text));
			//System.out.println(ByteArray.toDumpList(enc));
			if (encoding.equals("ISO-2022-JP")) return equalsJIS(text, enc);
			return Arrays.equals(text, enc);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException(uee.toString());
		}
	}
	
	/**
	 * �w�肵���f�B���N�g���ȍ~�̎w�肵���g���q�̃t�@�C���� encoding ��
	 * �w�肳��镶���G���R�[�h�ɕϊ�����B
	 */
	public static void convert(String dir, String ext, String encoding) throws IOException {
		File d = new File(dir);
		if (!ext.startsWith(".")) throw new IllegalArgumentException(ext + " �ɂ� . �ł͂��܂�g���q���w�肵�܂�");
		if (!d.isDirectory()) throw new IllegalArgumentException(dir + " is not directory.");
		String[] list = d.list();
		for (String fname : list) {
			File f = new File(d, fname);
			if (f.isDirectory()) convert(f.getAbsolutePath(), ext, encoding);
			else if (!fname.matches(".+(?i)"+ext.substring(1)+"$"))
				continue;
			else {
				convertFile(f, encoding);
			}
		}
	}
	
	/**
	 * �w�肳�ꂽ�P�t�@�C����ϊ�����B
	 * �ϊ��̕K�v���Ȃ�(���Ƃ��Ǝw�肳�ꂽ�G���R�[�f�B���O)�ꍇ�A�������Ȃ��B
	 */
	private static void convertFile(File f, String encoding) throws IOException {
		System.out.println("Convert : " + f.getAbsolutePath());
		
		// �t�@�C����ǂݍ��� baos �Ɋi�[
		FileInputStream fis = new FileInputStream(f);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (;;) {
			int c = fis.read();
			if (c == -1) break;
			baos.write(c);
		}
		fis.close();
		baos.close();
		
		byte[] fileimage = baos.toByteArray();
		
		String orgenc = detectEncoding(fileimage);
		System.out.println("File " + f.getName() + " encoding : " + orgenc);
		if (orgenc.equals(encoding)) return; // �ϊ��s�v
		
		if (useBackup) {
			// .orig �Ƃ��ăo�b�N�A�b�v
			File out = new File(f.getAbsolutePath() + ".orig");
			FileOutputStream fos = new FileOutputStream(out);
			fos.write(fileimage);
			fos.close();
		}
		
		// �G���R�[�f�B���O�ϊ����ď㏑��
		FileOutputStream fos2 = new FileOutputStream(f);
		fos2.write(new String(fileimage, orgenc).getBytes(encoding));
		fos2.close();
	}
	
	private static void deleteClass(File f) {
		if (f.isDirectory()) {
			String[] list = f.list();
			for (String fname : list) {
				deleteClass(new File(f, fname));
			}
		} else {
			if (f.getName().endsWith(".class")) f.delete();
		}
	}
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("usage: java ConvEncoding [target directory] [use backup]");
			System.out.println("[use backup] : true(default)..make backupfile(.orig)");
			System.out.println("               false        ..rewrite files");
			System.out.println("[encoding]   : name which encodes to.");
			System.out.println("                e.g. UTF-8");
			System.out.println("                     Shift_JIS");
			System.out.println("                     EUC-JP");
			
			System.exit(1);
		} else {
			boolean usebackup = true;
			if (args.length > 1) {
				if (args[1].equals("false")) usebackup = false;
			}
			String encoding = "UTF-8";
			if (args.length > 2) {
				encoding = args[2];
			}
			ConvEncoding.useBackup = usebackup;
			String root = new File(".").getCanonicalPath();
			File f = new File(args[0]);
			if (!f.getCanonicalPath().startsWith(root)) {
				System.out.println("This tool is danger, so it must be applied to subdirectory of " + root + " :"+f.getCanonicalPath());
				System.exit(-1);
			}
			convert(args[0], ".java", encoding);
			convert(args[0], ".html", encoding);
			deleteClass(f);
		}
	}
}
