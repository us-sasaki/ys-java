package misc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

public class DiffMd {
	
	private File oldOriginalDir;
	private File newOriginalDir;
	private File oldTranslatedDir;
	private File newTranslatedDir;
	
	private List<File> revisedOriginal;
	private int newFileCount;
	private int modifiedFileCount;
	private int deletedFileCount;
	
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
	public void diffDirectories() {
	}
}
