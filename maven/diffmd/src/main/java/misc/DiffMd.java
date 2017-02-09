package misc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Chunk;

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
 * 　翻訳文が多い場合、ディレクトリ指定間違いとみなし、エラー終了
 *
 * Step 2. ファイル内容チェック
 * 　オリジナル原文と翻訳文の書式情報が一致することをチェック。
 * 　一致しない場合、ログに警告を出して処理継続。
 *
 * Step 3. 原文ファイル対応チェック
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
	
	private List<String> filesToBeCreated;
	private List<String> filesToBeModified;
	private List<String> filesToBeDeleted;
	
	private Patch diffOriginalFiles;
	private Patch diffOldFiles;
	
/*-------------
 * constructor
 */
	public DiffMd(	String oldOriginalDir,
					String newOriginalDir,
					String oldTranslatedDir,
					String newTranslatedDir) {
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
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 各ルートディレクトリの中を走査し、
	 * filesToBeCreated
	 * filesToBeDeleted
	 * を格納。
	 */
	public void diffDirectories() throws IOException {
		List<String> oldOriginalFiles = listFiles(oldOriginalDir);
		List<String> newOriginalFiles = listFiles(newOriginalDir);
		List<String> oldTranslatedFiles = listFiles(oldTranslatedDir);
		
		
		diffOriginalFiles = DiffUtils.diff(oldOriginalFiles, newOriginalFiles);
		diffOldFiles	= DiffUtils.diff(oldOriginalFiles, oldTranslatedFiles);
		
		// 原文におけるファイルの差異を格納
		filesToBeCreated	= new ArrayList<String>();
		filesToBeDeleted	= new ArrayList<String>();
		
		for (Delta delta : diffOriginalFiles.getDeltas()) {
			Chunk org = delta.getOriginal();
			Chunk rev = delta.getRevised();
			if (org.size() == 0) {
				for (Object o : rev.getLines()) {
					filesToBeCreated.add(o.toString());
				}
			} else if (rev.size() == 0) {
				for (Object o : org.getLines()) {
					filesToBeDeleted.add(o.toString());
				}
			} else {
				throw new InternalError("原文同士の delta の要素がありません");
			}
		}
		// 原文と訳文ファイルの対応性を確認しておく
		
	}
	
	private List<String> listFiles(File directory)
					throws IOException {
		List<String> result = new ArrayList<String>();
		
		listFilesImpl(directory.getCanonicalPath(), directory, result);
		
		result.sort(null);
		return result;
	}
	
	private void listFilesImpl(String rootDir, File file, List<String> list)
					throws IOException {
		if (file.isDirectory()) {
			String[] files = file.list();
			for (String fname : files) {
				File f = new File(file, fname);
				listFilesImpl(rootDir, f, list);
			}
		} else {
			String path = file.getCanonicalPath();
			if (!path.startsWith(rootDir)) {
				throw new InternalError("file が rootDir の子になっていません：root : " + rootDir + " file : " + path);
			}
			list.add(path.substring(rootDir.length())); // 相対path
		}
	}
}
