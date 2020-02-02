package abdom.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import abdom.util.Encodings;

/**
 * ファイル処理に便利なクラスメソッドを提供します。
 * 特定のディレクトリ配下にあるファイルすべてのリストやストリームの抽出、
 * テキストファイルの全行に対する変換などのメソッドを含みます。
 *
 * @version		January 25, 2019
 * @author		Yusuke Sasaki
 */
public final class FileUtils {
	
	private FileUtils() { // to disable instantiation
	}
	
/*---------------
 * class methods
 */
	/**
	 * 指定された Path で示されるテキストファイルの各行に指定された
	 * 作用(UnaryOperator)を施し、上書きします。
	 * Charset は自動判定されます。バイナリファイルの場合など、Charset が
	 * 判定できない場合、UncheckedIOException がスローされます。
	 *
	 * @param		p		テキストファイルの Path
	 * @param		converter	各行の変換規則
	 * @exception	java.io.UncheckedIOException	エンコード異常、
	 * 												ファイル読み書き異常
	 */
	public static void convert(Path p, UnaryOperator<String> converter) {
		try {
			// Encoding 判定
			Charset cs = Encodings.charset(p.toString());
			if (cs == null) throw new CharacterCodingException();
			
			// ファイルの全行を List として読み込み
			List<String> lines = Files.readAllLines(p, cs);
			
			// 変換後の全行を List に格納
			List<String> newLines = lines.stream()
										.map( line -> converter.apply(line) )
										.collect(Collectors.toList());
			// 変換後の全行をファイルに書き出し
			Files.write(p, newLines, cs);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	/**
	 * 指定された Path で示されるテキストファイルの文字列を置換し、上書きします。
	 * Charset は自動判定されます。
	 *
	 * @param		p		テキストファイルの Path
	 * @param		regex	置換箇所を示す正規表現
	 * @param		replacement	置換後の文字列
	 * @exception	java.io.UncheckedIOException	エンコード異常、
	 * 												ファイル読み書き異常
	 */
	public static void replace(Path p, String regex, String replacement) {
		// regex をコンパイルしておく
		final Pattern pat = Pattern.compile(regex);
		
		// 置換を作用として convert を呼ぶ
		convert(p, line -> pat.matcher(line).replaceAll(replacement) );
	}
	
	/**
	 * 指定されたディレクトリ配下にあるファイルのリストを作成します。
	 *
	 * @param		path		リストを作成するルートディレクトリ
	 * @return		Path の List
	 */
	public static List<Path> listOfFiles(String path) {
		ArrayList<Path> list = new ArrayList<>();
		
		File f = new File(path);
		addList(list, f);
		
		return list;
	}
	
	/**
	 * 指定されたディレクトリ配下にあるファイルのリストを作成します。
	 * path 文字列に対し、指定されたフィルターで File の List を作成します。
	 *
	 * @param		path		リストを作成するルートディレクトリ
	 * @param		regex		抽出条件(Path の文字列表現に対し、
	 *							この正規表現にマッチするものを加える)
	 * @return		Path の List
	 */
	public static List<Path> listOfFiles(String path, String regex) {
		final Pattern pat = Pattern.compile(regex);
		return listOfFiles(path, (p) -> pat.matcher(p.toString()).matches());
	}
	
	/**
	 * 指定されたディレクトリ配下にあるファイルのリストを作成します。
	 * path 文字列に対し、指定されたフィルターで File の List を作成します。
	 *
	 * @param		path		リストを作成するルートディレクトリ
	 * @param		filter		抽出条件(Path に対し、trueのものを
	 *							加える)
	 * @return		Path の List
	 */
	public static List<Path> listOfFiles(String path, Predicate<Path> filter) {
		ArrayList<Path> list = new ArrayList<>();
		
		File f = new File(path);
		addList(list, filter, f);
		
		return list;
	}
	
	/**
	 * 指定されたディレクトリ配下にあるファイルの stream を作成します。
	 *
	 * @param		path		stream を作成するルートディレクトリ
	 * @return		Path の Stream
	 */
	public static Stream<Path> streamOfFiles(String path) {
		return listOfFiles(path).stream();
	}
	
	/**
	 * 指定されたディレクトリ配下にあるファイルの stream を作成します。
	 * path 文字列に対し、指定されたフィルターで File の stream を作成します。
	 *
	 * @param		path		stream を作成するルートディレクトリ
	 * @param		regex		抽出条件(Path の文字列表現に対し、
	 *							この正規表現にマッチするものを加える)
	 * @return		Path の Stream
	 */
	public static Stream<Path> streamOfFiles(String path, String regex) {
		final Pattern pat = Pattern.compile(regex);
		return streamOfFiles(path, (p) -> pat.matcher(p.toString()).matches());
	}
	
	/**
	 * path 文字列に対し、指定されたフィルターで File の Stream を作成します。
	 *
	 * @param		path		stream を作成するルートディレクトリ
	 * @param		filter		抽出条件(Path に対し、trueのものを
	 *							加える)
	 * @return		Path の Stream
	 */
	public static Stream<Path> streamOfFiles(String path, Predicate<Path> filter) {
		return listOfFiles(path, filter).stream();
	}
	
	/**
	 * 指定されたファイル名のテキストファイルを読み込み、String として
	 * 返却します。
	 *
	 * @param		p	テキストファイルの Path
	 * @return		テキストファイル
	 */
	public static String readFileAsString(Path p) {
		try {
			// byte[] として読み込み
			byte[] fimage = Files.readAllBytes(p);
			// Encoding 判定
			Charset cs = Encodings.charset(fimage);
			if (cs == null) throw new CharacterCodingException();
			
			return new String(fimage, cs.toString());
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
	
	/**
	 * 文字列を改行で区切り、List 化して返却します。
	 *
	 * @param	s	処理対象のテキスト文字列
	 * @return	行ごとに区切られた List
	 */
	public static List<String> splitIntoLines(String s) {
		List<String> result = new ArrayList<String>();
		try (StringReader sr = new StringReader(s);
				BufferedReader br = new BufferedReader(sr);) {
			while (true) {
				String line = br.readLine();
				if (line == null) break;
				result.add(line);
			}
			return result;
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
	
/*------------------
 * private methods
 */
	private static void addList(List<Path> list, File f) {
		if (f.isDirectory()) {
			for (String fname : f.list()) {
				addList(list, new File(f, fname));
			}
		} else {
			list.add(f.toPath());
		}
	}
	
	private static void addList(List<Path> list, Predicate<Path> filter, File f) {
		if (f.isDirectory()) {
			for (String fname : f.list()) {
				addList(list, filter, new File(f, fname));
			}
		} else {
			Path p = f.toPath();
			if (filter == null || filter.test(p)) list.add(p);
		}
	}
	
}
