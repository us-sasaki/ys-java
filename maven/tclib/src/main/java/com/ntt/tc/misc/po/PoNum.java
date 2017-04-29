package com.ntt.tc.misc.po;

import java.io.IOException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import abdom.data.json.*;

public class PoNum {
	public static void main(String[] args) throws Exception {
		// ファイル読み込み
		JsonType f = Po2Json.read(args[0]);
		int count = 1;
		for (JsonType elem : f) {
			for (String key : elem.keySet()) {
				if (!key.startsWith("msgstr")) continue;
				elem.put(key, String.valueOf(count++) + "_" + elem.get(key).getValue()); // 上書き
			}
		}
		Po2Json.write(args[0]+".numbered.txt", f);
	}
	
	public static void oldMain(String[] args) throws Exception {
		// ファイル読み込み
		List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(args[0]), StandardCharsets.UTF_8);
		
		// 
		int count = 1;
		PrintWriter p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[0]+".numbered.txt"), "UTF-8"));
		
		for (String line : lines) {
			if (line.startsWith("msgstr")) {
				int index = line.indexOf(" \"");
				if (index == -1) throw new RuntimeException(" \"がありません：" + line);
				line = line.substring(0, index+2) + String.valueOf(count++) + "_" + line.substring(index+2);
			}
			p.print(line);
			p.print("\n");
		}
		p.close();
	}
}
