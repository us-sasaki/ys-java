package abdom.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 日本語の文字エンコードを検出し、変換するプログラム。
 * 実行したディレクトリ以下のすべての .java ファイルのエンコードを
 * UTF-8N に変換する。
 * バックアップファイルとして .orig ファイルを生成する。
 */
public class Encodings {
	
	private static boolean useBackup = true;
	
	/**
	 * 調査する文字エンコーディングリスト。
	 * 先に現れたものが優先される。テストして、この順序になっているため
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
	 * JIS の場合の特例処理 0x4A(Terapad) → 0x42(Java) の違いを
	 * 吸収することでましになった。(「あい」の２文字のファイルで、
	 * JIS/UTF-8/EUC/ShiftJIS を正しく判定した)
	 * Terapad は 0x4A になるが、sakura では 0x42 だったので、
	 * Terapad の変換がイレギュラーなのかもしれない。
	 *
	 * @param	text	文字列を表すバイナリ
	 * @return	エンコーディング判定結果(判定できなかった場合 null)
	 */
	public static String detect(byte[] text) {
		for (String encoding : ENCODINGS) {
			if (checkEncoding(text, encoding))
				return encoding;
		}
		return null;
	}
	
	/**
	 * 指定されたバイナリ列の日本語エンコーディングを自動判定し、Charset
	 * として返却します。
	 *
	 * @param	text	文字列を表すバイナリ
	 * @return	エンコーディング判定結果(判定できなかった場合 null)
	 */
	public static Charset charset(byte[] text) {
		String encoding = detect(text);
		if (encoding == null) return null;
		return Charset.forName(encoding);
	}
	
	/**
	 * 指定されたバイナリ列の日本語エンコーディングを自動判定し、Charset
	 * として返却します。
	 *
	 * @param	filename	ファイル名
	 * @return	エンコーディング判定結果(判定できなかった場合 null)
	 * @exception	RuntimeException	IOException が発生した
	 */
	public static Charset charset(String filename) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(filename);
			byte[] buff = new byte[1024];
			while (true) {
				int s = fis.read(buff);
				if (s == -1) break;
				baos.write(buff, 0, s);
			}
			fis.close();
			baos.close();
			return charset(baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * JIS の場合、バイトコードが異なることがあるが、それを許容する
	 * equals
	 *
	 * @param	a		比較元
	 * @param	b		比較先
	 */
	private static boolean equalsJIS(byte[] a, byte[] b) {
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
	 * 
	 * @param	text		対象文字列
	 * @param	encoding	エンコード文字列(ISO-2022-JP/UTF-8/EUC-JP/
	 *						Shift_JIS/Windows-31J/UTF-16/UTF-3)
	 * @return	text が encoding のものである場合、true
	 */
	private static boolean checkEncoding(byte[] text, String encoding) {
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
	 *
	 * @param	dir			対象ディレクトリ
	 * @param	ext			拡張子
	 * @param	encoding	エンコード文字列(ISO-2022-JP/UTF-8/EUC-JP/
	 *						Shift_JIS/Windows-31J/UTF-16/UTF-3)
	 * @throws	java.io.IOException	IO例外
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
	 *
	 * @param	f	対象 File
	 * @param	encoding	エンコード文字列(ISO-2022-JP/UTF-8/EUC-JP/
	 *						Shift_JIS/Windows-31J/UTF-16/UTF-3)
	 * @throws	java.io.IOException	IO例外
	 */
	public static void convertFile(File f, String encoding) throws IOException {
		System.out.println("Convert : " + f.getAbsolutePath());
		
		// ファイルを読み込み baos に格納
		byte[] fileimage = null;
		try (FileInputStream fis = new FileInputStream(f);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			for (;;) {
				int c = fis.read();
				if (c == -1) break;
				baos.write(c);
			}
			fileimage = baos.toByteArray();
		} catch (IOException ioe) {
			throw ioe;
		}
		
		String orgenc = detect(fileimage);
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
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("usage: java EncodingConverter [target directory] [use backup]");
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
			Encodings.useBackup = usebackup;
			String root = new File(".").getCanonicalPath();
			File f = new File(args[0]);
			if (!f.getCanonicalPath().startsWith(root)) {
				System.out.println("This tool is danger, so it must be applied to subdirectory of " + root + " :"+f.getCanonicalPath());
				System.exit(-1);
			}
			convert(args[0], ".java", encoding);
			convert(args[0], ".html", encoding);
		}
	}
}
