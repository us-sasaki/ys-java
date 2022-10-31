package com.ntt.analytics.fileusage;

import java.util.List;
import java.util.Date;

/** 各種処理用に便利な値を追加. keyはpath */
public class FileEntry {
	public String		path;
	public int			level;		// 深さ
	public boolean		isDirectory; // ディレクトリか？
	public List<Long>	sizeList;	// 過去のサイズ履歴
	public long		size;
	public long		increase;	// 直近の増分
	
	// 以下、拡張フィールドで昔の csv には含まれていない
	public String		owner	= "unknown";	// ファイル所有者 since 16/05/27
	public long		lastModified	= 614201724000000L;	// 最終更新日 since 16/06/03
	
/*-----------
 * overrides
 */
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("    path   :"+path+"\n");
		s.append("   level   :"+level+"\n");
		s.append("isDirectory:"+isDirectory+"\n");
		s.append("  sizeList :{");
		for (Long l: sizeList) {
			s.append(l);
			s.append(" ");
		}
		s.append("}\n");
		s.append("   size    :"+size+"\n");
		s.append(" increase  :"+increase+"\n");
		s.append("   owner   :"+owner+"\n");
		s.append("lastModified:"+new Date(lastModified)+"\n");
		
		return s.toString();
	}
}
