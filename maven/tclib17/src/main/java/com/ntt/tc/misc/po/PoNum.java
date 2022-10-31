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
		if (args.length != 1) {
			System.out.println("ponum ツール - poファイルの各エントリに付与された [数字]_ 形式の接頭辞を付与する。結果は、入力ファイル名+\".numbered.txt\"となる。");
			System.out.println();
			System.out.println("java PoNum [poファイル名]");
			System.out.println();
			System.out.println("  po : 番号接頭辞を付与したい poファイルの名前");
			System.out.println();
			System.exit(-1);
		}
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
	
}
