public interface FileEntrySelector {
	// 抽出したい FileEntry で true となるように実装
	public boolean hits(FileEntry fe);
}
