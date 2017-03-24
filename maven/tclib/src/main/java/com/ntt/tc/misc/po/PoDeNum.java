package com.ntt.tc.misc.po;

import java.io.IOException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

public class PoDeNum {
	public static void main(String[] args) throws Exception {
		// ファイル読み込み
		List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(args[0]), StandardCharsets.UTF_8);
		
		// 
		int count = 1;
		PrintWriter p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[0]+".denumbered.txt"), "UTF-8"));
		
		for (String line : lines) {
			if (line.startsWith("msgstr")) {
				int index = line.indexOf(" \"");
				if (index == -1) throw new RuntimeException(" \"がありません：" + line);
				int ind2 = line.indexOf("_", index);
				try {
					if (ind2 > 0) {
						int dummy = Integer.parseInt(line.substring(index+2, ind2));
						// _ がなければ何もしない
						line = line.substring(0, index+2) + line.substring(ind2+1);
					}
				} catch (NumberFormatException ignored) {
System.out.println("skipped : " + line.substring(index+2, ind2));
				}
			}
			p.print(line);
			p.print("\n");
		}
		p.close();
	}
}
