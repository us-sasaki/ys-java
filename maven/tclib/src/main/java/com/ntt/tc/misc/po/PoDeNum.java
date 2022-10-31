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
		if (args.length != 1) {
			System.out.println("podenum ツール - poファイルの各エントリに付与された [数字]_ 形式の接頭辞を削除する。結果は、入力ファイル名+\".denumbered.txt\"となる。ただし、入力ファイル名が\".numbered.txt\"で終わっている場合、これを除いたものとする");
			System.out.println();
			System.out.println("java PoDeNum [poファイル名]");
			System.out.println();
			System.out.println("  po : 番号接頭辞を消したい poファイルの名前");
			System.out.println();
			System.exit(-1);
		}
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
	
}
