package com.ntt.tc.misc.po;

import java.io.IOException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import abdom.data.json.*;

public class PoDeNum {
	public static void main(String[] args) throws Exception {
		JsonType f = Po2Json.read(args[0]); // f is Array
		JsonType result = new JsonArray();
		
		for (JsonType elem : f) {
			for (String key : elem.keySet()) {
				if (!key.startsWith("msgstr")) continue;
				
				String val = elem.get(key).getValue();
				int index = val.indexOf("_");
				try {
					if (index > 0) {
						// _ がある場合
						int dummy = Integer.parseInt(val.substring(0, index));
						// 数字_ の形のみ切り詰める(文中にあるときは削らない)
						val = val.substring(index+1);
						elem.put(key, val); // 上書き
					}
				} catch (NumberFormatException ignored) {
				}
			}
			result.push(elem);
		}
		String outfname = args[0]+".denumbered.txt";
		if (args[0].endsWith(".po.numbered.txt"))
			outfname = args[0].substring(0, args[0].indexOf(".numbered.txt"));
		Po2Json.write(outfname, result);
	}
	
	private static void oldMain(String[] args) throws Exception {
		// ファイル読み込み
		List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(args[0]), StandardCharsets.UTF_8);
		
		// 
		int count = 1;
		String outfname = args[0]+".denumbered.txt";
		if (args[0].endsWith(".po.numbered.txt"))
			outfname = args[0].substring(0, args[0].indexOf(".numbered.txt"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter p = new PrintWriter(new OutputStreamWriter(baos, "UTF-8"));
		
		for (String line : lines) {
			if (line.startsWith("msgstr")) {
				int index = line.indexOf(" \"");
				if (index == -1) throw new RuntimeException(" \"がありません：" + line);
				int ind2 = line.indexOf("_", index);
				try {
					if (ind2 > 0) {
						// _ がある場合
						int dummy = Integer.parseInt(line.substring(index+2, ind2));
						// 数字_ の形のみ切り詰める(文中にあるときは削らない)
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
		
		OutputStream o = new FileOutputStream(outfname);
		o.write(baos.toByteArray());
		o.close();
	}
}
