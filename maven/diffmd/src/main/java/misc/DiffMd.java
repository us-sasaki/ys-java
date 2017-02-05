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
