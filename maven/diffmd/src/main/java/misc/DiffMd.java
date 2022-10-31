package misc;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.yaml.snakeyaml.Yaml;

/**
 * オリジナル原文ディレクトリ、更新後原文ディレクトリ、翻訳文ディレクトリ、
 * 新翻訳文ディレクトリを指定し、
 * ファイル対応チェックののち、新翻訳文ディレクトリに差分抽出ファイルを
 * 格納します。
 *
 * 原則として、オリジナル原文と翻訳文は１：１対応しており、内容も整合して
 * いることを前提としています。
 *
 * Step 1. ファイル対応チェック
 * 　オリジナル原文と翻訳文が対応することをチェック。
 * 　翻訳文が足りない場合、ログに警告を出して処理継続、全文更新要の扱い
 * 　翻訳文が多い場合、独自文書とみなし、差分抽出範囲外とする
 *
 * 　オリジナル原文、更新後原文の差分をチェック
 *   オリジナル原文が多い　 → 翻訳ファイル削除の警告を出す
 *   オリジナル原文が少ない → 翻訳ファイル追加の警告を出す
 *
 * (Step 2.) ファイル内容チェック(大幅に改変する可能性があるため当面スキップ)
 * 　オリジナル原文と翻訳文の書式情報が一致することをチェック。
 * 　一致しない場合、ログに警告を出して処理継続。
 *
 * (Step 3.) 原文ファイル対応チェック
 * 　（linewise diff で一致度があまりに低い場合、エラー終了することを
 * 　　考えるが、閾値を決めるのが面倒なので何もしない)
 *
 * ログファイル出力
 * (1) 原文ファイル対応
 *     ファイル単位の増加減少、変更をサマリ(数)、リストとして出力
 *
 * (2) オリジナルファイル対応
 * 　　ファイル単位の増加現象、書式の差異をサマリ(数)、リストとして出力
 *
 * (3) 結果ファイル、ディレクトリ出力情報
 * 　　結果ファイル(差分抽出ファイル)の生成状況を出力
 * 　　新規生成
 *
 * 画面(System.out)にはサマリのみ出力
 */
public class DiffMd {
	
	private File oldOriginalDir;
	private File newOriginalDir;
	private File oldTranslatedDir;
	private File newTranslatedDir;
	
	private Map<String, Triplet> triplets;
	
	public PrintWriter report;
	private String reportFilename;
	
	private String[] except;
	
/*--------------------
 * inner static class
 */
	/**
	 * 対応するファイルを格納する。
	 * ファイルは相対ディレクトリとファイル名が一致するものを組とする。
	 * 存在しないファイルは、null とする。
	 */
	private static class Triplet {
		private File[] f;
		
		private Triplet() {
			f = new File[3];
		}
		
		private boolean isFilled() {
			return (f[0] != null && f[1] != null && f[2] != null);
		}
	}
	
/*-------------
 * constructor
 */
	public DiffMd(	String oldOriginalDir,
					String newOriginalDir,
					String oldTranslatedDir,
					String newTranslatedDir,
					String reportFilename) {
		// 指定されたパラメータがディレクトリであることを確認する
		this.oldOriginalDir = new File(oldOriginalDir);
		if (!this.oldOriginalDir.isDirectory())
			throw new IllegalArgumentException("oldOriginalDir にはディレクトリを指定して下さい：" + oldOriginalDir);
		this.newOriginalDir = new File(newOriginalDir);
		if (!this.newOriginalDir.isDirectory())
			throw new IllegalArgumentException("newOriginalDir にはディレクトリを指定してください：" + newOriginalDir);
		this.oldTranslatedDir = new File(oldTranslatedDir);
		if (!this.oldTranslatedDir.isDirectory())
			throw new IllegalArgumentException("oldTranslatedDir にはディレクトリを指定してください：" + oldTranslatedDir);
		this.newTranslatedDir = new File(newTranslatedDir);
		if (!this.newTranslatedDir.exists())
			this.newTranslatedDir.mkdir();
		if (!this.newTranslatedDir.isDirectory())
			throw new IllegalArgumentException("newTranslatedDir にはディレクトリを指定してください：" + newTranslatedDir);
		
		this.reportFilename = reportFilename;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 対象外ディレクトリを設定
	 */
	public void setExcept(String[] except) {
		this.except = except;
	}
	
	public void exec() throws IOException {
		report = new PrintWriter(new FileWriter(reportFilename));
		report.println("DiffMd レポート (" + new Date() + ")");
		report.println();
		report.println("■対象ディレクトリ");
		report.println();
		report.println("　更新前　原文ディレクトリ：" + this.oldOriginalDir.getCanonicalPath());
		report.println("　更新後　原文ディレクトリ：" + this.newOriginalDir.getCanonicalPath());
		report.println("　更新前　訳文ディレクトリ：" + this.oldTranslatedDir.getCanonicalPath());
		report.println("　更新後　訳文ディレクトリ：" + this.newTranslatedDir.getCanonicalPath());
		report.println();
		if (except != null) {
			report.println("■除外ディレクトリ/ファイル");
			report.println();
			for (String e : except) {
				report.println(e);
			}
			report.println();
		}
		
		// Triplets を格納する
		listDirectories();
		
		// ファイルレベルの有無に関する警告を出す
		warnUnmatch();
		
		// 各 diff ファイルを生成する
		makeDiffFiles();
		
		report.close();
	}
	
	/**
	 * 指定されたディレクトリから、Triplet を抽出する
	 * 同時に読み込んだファイル、除外するディレクトリ/ファイルをレポートに
	 * 出力する。
	 */
	private void listDirectories() throws IOException {
		report.println("■ファイル読み込み");
		report.println();
		report.println("　*.md ファイル以外のファイル、除外ディレクトリ/ファイルはスキップされます");
		report.println();
		triplets = new TreeMap<String, Triplet>();
		
		listDirectory(oldOriginalDir, 0);
		listDirectory(newOriginalDir, 1);
		listDirectory(oldTranslatedDir, 2);
		
		report.println();
	}
	
	/**
	 * 指定されたディレクトリ以下にあるディレクトリ/mdファイルを Triplet
	 * に格納する。
	 * Triplet は、en-old en-new ja-old の 3 ファイルの組で、過不足チェックに
	 * 使用される。
	 * ただし、除外対象に含まれるディレクトリ、ファイルは除外する。
	 *
	 * @param		dir			Triplet 作成対象ディレクトリ
	 * @param		index		en-old(0), en-new(1), ja-old(2)
	 */
	private void listDirectory(File dir, int index) throws IOException {
		listDirectoryImpl(dir, "", index);
	}
	
	/**
	 * 指定された root ディレクトリ以下の path のディレクトリ/ファイルを
	 * Triplet に格納する。
	 *
	 * @param		root		ルートディレクトリ(変わらない)
	 * @param		path		ルート以下のファイルパス
	 * @param		index		en-old(0), en-new(1), ja-old(2)
	 */
	private void listDirectoryImpl(File root, String path, int index)
						throws IOException {
		File f = new File(root, path);
		String rootPathStr = root.getCanonicalPath();
		// root の一つ上のディレクトリを取得
		int rootPathStrLen = rootPathStr.lastIndexOf(
								System.getProperty("file.separator"));
		
		// 除外ディレクトリ/ファイルになっているか確認
		if (except != null) {
			String canpath = f.getCanonicalPath();
			for (String e : except) {
				File g = new File(root, e);
				if (canpath.startsWith(g.getCanonicalPath())) {
					report.println("　対象外　：" + f.getCanonicalPath().substring(rootPathStrLen));
					return;
				}
			}
		}
		
		if (f.isDirectory()) {
			// ディレクトリもリストに登録
			putTriplets(root, path + "/", index);
			
			String[] files = f.list();
			for (String file : files) {
				listDirectoryImpl(root, path + "/" + file, index);
			}
		} else {
			if (path.endsWith(".md")) {
				// リストに登録
				putTriplets(root, path, index);
			} else {
				// スキップ
				report.println("　スキップ：" + f.getCanonicalPath().substring(rootPathStrLen));
			}
		}
	}
	
	/**
	 * Triplet への格納処理
	 */
	private void putTriplets(File root, String path, int index) {
		Triplet t = triplets.get(path);
		if (t == null) t = new Triplet();
		if (t.f[index] != null) throw new InternalError();
		t.f[index] = new File(root, path);
		triplets.put(path, t);
	}
	
	/**
	 * ファイルレベルの過不足に関し、レポートに出力し、リストから
	 * 削除します。
	 * ついでにディレクトリを生成します。(triplets が TreeMap のため、
	 * ファイルよりもディレクトリが先に来ます)
	 */
	private void warnUnmatch() throws IOException {
		report.println("■ファイル過不足チェック");
		report.println();
		
		int comOriginal = 0;
		int shouldBeTranslated = 0;
		int conflict = 0;
		int removed = 0;
		int needToRemove = 0;
		
		for (String key : triplets.keySet()) {
			Path newPath = new File(newTranslatedDir, key).toPath();
			if (key.endsWith("/")) {
				// ディレクトリは生成しておく
				newPath.toFile().mkdir();
				//triplets.remove(key);
				continue;
			}
			Triplet t = triplets.get(key);
			if (t.isFilled()) continue;
			
			//triplets.remove(key);
			if (t.f[0] == null && t.f[1] == null && t.f[2] != null) {
				// 独自文書と判定。単にコピー
				report.println("　独自文書　　　：" + t.f[2]);
				Files.copy(t.f[2].toPath(), newPath);
				comOriginal++;
			}
			else if (t.f[0] == null && t.f[1] != null && t.f[2] == null) {
				// 新規原文が追加された。新規原文をコピー
				report.println("　新規訳文が必要：" + t.f[1]);
				Files.copy(t.f[1].toPath(), newPath);
				shouldBeTranslated++;
			}
			else if (t.f[0] == null && t.f[1] != null && t.f[2] != null) {
				// 新規原文とコンフリクトするファイル。訳文をコピー。
				report.println("　ファイルコンフリクト！！(新原文："+t.f[1]+"　訳文：" + t.f[2]+")");
				Files.copy(t.f[2].toPath(), newPath);
				conflict++;
			}
			else if (t.f[0] != null && t.f[1] == null && t.f[2] == null) {
				// 旧原文のみにあるファイル。結果オーライ
				report.println("　削除済み　　　：" + t.f[0]);
				removed++;
			}
			else if (t.f[0] != null && t.f[1] == null && t.f[2] != null) {
				// 新規原文では消えている。確認後削除を促す
				report.println("　削除が必要　　：" + t.f[2]);
				Files.copy(t.f[2].toPath(), newPath);
				needToRemove++;
			}
			else if (t.f[0] != null && t.f[1] != null && t.f[2] == null) {
				// 新規原文が追加された。新規原文をコピー
				report.println("　新規訳文が必要：" + t.f[1]);
				Files.copy(t.f[1].toPath(), newPath);
				shouldBeTranslated++;
			}
		}
		report.println();
		report.println("　サマリ");
		report.println("　　独自文書　　　：" + comOriginal);
		report.println("　　新規訳文が必要：" + shouldBeTranslated);
		report.println("　　コンフリクト　：" + conflict);
		report.println("　　削除済み　　　：" + removed);
		report.println("　　削除が必要　　：" + needToRemove);
		report.println("　　　　　　　合計：" + (comOriginal+shouldBeTranslated+conflict+removed+needToRemove));
		report.println();
	}
	
	/**
	 * Triplet に登録されていて、不足していないファイルについて、
	 * 翻訳用 md ファイルを生成します。
	 *
	 * @exception		java.io.IOException		IO異常
	 */
	private void makeDiffFiles() throws IOException {
		report.println("■更新ファイル作成(カッコ内の数値は変更された行の割合(%))");
		report.println();
		
		// カウントする
		int notChanged = 0;
		int muchChanged = 0;
		int changed = 0;
		
		for (String key : triplets.keySet()) {
			Triplet t = triplets.get(key);
			// ディレクトリや不足のあるものは除外
			if (key.endsWith("/") || !t.isFilled()) continue;
			
			Path p0 = t.f[0].toPath();
			Path p1 = t.f[1].toPath();
			Path p2 = t.f[2].toPath();
			Path newPath = new File(newTranslatedDir, key).toPath(); //格納先
			
	        List<String> o = readLines(p0);
    	    List<String> n = readLines(p1);
        	List<String> oj = readLines(p2);
			
			// md の差分抽出を行い、翻訳 suggestion データを生成、
			// テキストにフォーマットする instance
			DiffMdInTranslate dmit = new DiffMdInTranslate2(o, n, oj);
			List<String> text = dmit.toText();
			
			Files.write(newPath, text, StandardCharsets.UTF_8 );
			
			// 差分を dmit.getDiffRate() から取得し、レポート出力
			double rate = dmit.getDiffRate();
			int r = (int)(rate * 100d);
			if (rate > 0 && r == 0) r++;
			if (r == 0) {
				report.println("　変更なし：" + key);
				notChanged++;
			} else if (r >= 20) {
				report.println("　差大("+r+"%)：" + key);
				muchChanged++;
			} else {
				report.println("　修正("+r+"%)：" + key);
				changed++;
			}
		}
		report.println();
		report.println("　サマリ");
		report.println("　　変更なし　　　：" + notChanged);
		report.println("　　差大　　　　　：" + muchChanged);
		report.println("　　修正　　　　　：" + changed);
		report.println("　　　　　　　合計：" + (notChanged+muchChanged+changed));
	}
	
	private List<String> readLines(Path p) throws IOException {
		try {
			return Files.readAllLines(p, StandardCharsets.UTF_8);
		} catch (java.nio.charset.MalformedInputException e) {
			List<String> result = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(p.toFile()));
			while (true) {
				String line = br.readLine();
				if (line == null) break;
				result.add(line);
			}
			br.close();
			
			return result;
		}
	}
	
	public static void main(String[] args) throws Exception {
		Yaml yaml = new Yaml();
		DiffMdProps props = yaml.loadAs(new FileReader("diffmd.yaml"), DiffMdProps.class);
		
		String target = props.getTarget();
		
		String oldOrg = target + "0_en-old";
		String newOrg = target + "1_en-new";
		String oldJa  = target + "2_ja-old";
		String newJa  = target + "3_ja-new";
		String report = target + "report.txt";
		DiffMd d = new DiffMd(oldOrg, newOrg, oldJa, newJa, report);
		d.setExcept(props.getExcept());
		
		try {
			d.exec();
		} catch (Exception e) {
			d.report.close();
			throw e;
		}
	}
}
