package com.ntt.analytics.fileusage;

import java.util.Comparator;

/**
 * 増分, 深さ, パス名 の順に並べる Comparator
 */
public class IncreaseOrder implements Comparator<FileEntry> {
	@Override
	public int compare(FileEntry a, FileEntry b) {
		if (a.increase > b.increase) return 1;
		if (a.increase < b.increase) return -1;
		if (a.level != b.level) return a.level - b.level;
		return a.path.compareTo(b.path);
	}
}
