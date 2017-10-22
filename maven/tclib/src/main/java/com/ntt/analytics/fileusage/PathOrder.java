package com.ntt.analytics.fileusage;

import java.util.Comparator;

/**
 * パス名 の辞書式順序で並べる Comparator
 */
public class PathOrder implements Comparator<FileEntry> {
	@Override
	public int compare(FileEntry a, FileEntry b) {
		return a.path.compareTo(b.path);
	}
}
