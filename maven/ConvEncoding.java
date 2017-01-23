import java.io.*;
import java.util.Arrays;

import abdom.data.ByteArray;

/**
 * 日本語の文字エンコードを検出し、変換するプログラム。
 * 実行したディレクトリ以下のすべての .java ファイルのエンコードを
 * UTF-8N に変換する。
 * バックアップファイルとして .orig ファイルを生成する。
 */
public class ConvEncoding {

	public static boolean useBackup = true;
	
	/**
	 * 調査する文字エンコーディングリスト。
	 * 先に現れたものが優先される。テストして、この結果になっているため
	 * いじらないこと。
	 * 特に、JIS ファイルは UTF-8 に誤認識される。(逆はならない)
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
	 * JIS がチェックできない。ENCODINGS の順序入れ替えと
	 * JIS の場合の特例処理 0x4A(Terapad) -> 0x42(Java) の違いを
	 * 吸収することでましになった。(「あい」の２文字のファイルで、
	 * JIS/UTF-8/EUC/ShiftJIS を正しく判定した)
	 * Terapad は 0x4A になるが、sakura では 0x42 だったので、
	 * Terapad の変換がイレギュラーなのかもしれない。
	 */
	public static String detectEncoding(byte[] text) {
		for (String encoding : ENCODINGS) {
			if (checkEncoding(text, encoding))
				return encoding;
		}
		return null;
	}
	
	/**
	 * JIS の場合、バイトコードが異なることがあるが、それを許容する
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
	 * 指定されたエンコーディングで文字列化、getBytes() して変化
	 * しないものを検索。変化するものは　文字化け　と見なす。
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
	 * 指定したディレクトリ以降の指定した拡張子のファイルを encoding で
	 * 指定される文字エンコードに変換する。
	 */
	public static void convert(String dir, String ext, String encoding) throws IOException {
		File d = new File(dir);
		if (!ext.startsWith(".")) throw new IllegalArgumentException(ext + " には . ではじまる拡張子を指定します");
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
	 * 指定された１ファイルを変換する。
	 * 変換の必要がない(もともと指定されたエンコーディング)場合、何もしない。
	 */
	private static void convertFile(File f, String encoding) throws IOException {
		System.out.println("Convert : " + f.getAbsolutePath());
		
		// ファイルを読み込み baos に格納
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
		if (orgenc.equals(encoding)) return; // 変換不要
		
		if (useBackup) {
			// .orig としてバックアップ
			File out = new File(f.getAbsolutePath() + ".orig");
			FileOutputStream fos = new FileOutputStream(out);
			fos.write(fileimage);
			fos.close();
		}
		
		// エンコーディング変換して上書き
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
