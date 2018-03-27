package com.ntt.tc.misc.po;

import java.io.*;
import java.util.*;

import abdom.data.json.*;
import abdom.data.json.object.*;
import com.ntt.tc.data.*;

/**
 * po ファイルの要素を結合する。
 * java PoMerge [Po1] [Po2] ...
 * で、それぞれの msgid を含む Po ファイルをファイル名 PoMerge.txt で作成する。
 * コンフリクトは PoMerge.conflict.txt に出力する
 */
public class PoMerge {

	static void merge(JsonType result, JsonType po) {
		for (JsonType e : po) {
			// キーは msgid とする。msg_plural はキーではない。
			String key = e.get("msgid").getValue();
			
			// 
			JsonType rElem = PoPatch.select(result, "msgid", key);
			if (rElem == null) {
				// 存在しない場合、単純に追加する。
				result.push(e); // コピーでない
			} else {
				// 存在する場合、上書きして conflict に出力
				
				// msgid_plural
				JsonType s1 = e.get("msgid_plural");
				if (s1 != null) {
					// msgid_plural フィールドが e にある
					JsonType j = rElem.get("msgid_plural");
					if (j != null && (!s1.getValue().equals(j.getValue())) )
						rElem.add("conflict_plural", j);
					rElem.put("msgid_plural", s1); // 上書き
				}
				// msgstr
				JsonType s2 = e.get("msgstr");
				if (s2 != null) {
					JsonType j = rElem.get("msgstr");
					if (j != null && (!s2.getValue().equals(j.getValue())) )
						rElem.add("conflict", j);
					rElem.put("msgstr", s2); // 上書き
				}
				// msgstr[0]
				JsonType s3 = e.get("msgstr[0]");
				if (s3 != null) {
					JsonType j = rElem.get("msgstr[0]");
					if (j != null && (!s3.getValue().equals(j.getValue())) )
						rElem.add("conflict_msgstr0", j);
					rElem.put("msgstr[0]", s3); // 上書き
				}
			}
		}
	}
	
	public static PrintWriter getPrintWriter(String fname) throws IOException {
		return new PrintWriter(
				new OutputStreamWriter(
					new FileOutputStream(fname),
					"UTF-8"));
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("pomerge ツール - 複数の po ファイルをマージする");
			System.out.println();
			System.out.println("それぞれの msgid を含む Po ファイルをファイル名 PoMerge.txt で作成する。");
			System.out.println("コンフリクトは PoMerge.conflict.txt に出力される。");
			System.out.println();
			System.out.println("java PoMerge [file1] [file2] ...");
			System.out.println();
			System.out.println("  file(n) : マージ対象のファイル。後に指定したもので上書きしていく");
			System.exit(-1);
		}
		
		// ファイル読み込み
		JsonType[] pos = new JsonType[args.length];
		for (int i = 0; i < args.length; i++) {
			pos[i] = Po2Json.read(args[i]);
		}
		
		// 結果生成
		JsonType result = new JsonArray();
		
		// conflict 出力用
		PrintWriter p = getPrintWriter("PoMerge.conflict.txt");
		
		for (JsonType po : pos) {
			merge(result, po);
		}
		
		// conflict 出力 & 削除
		for (JsonType j : result) {
			JsonType conflict = j.get("conflict");
			if (conflict != null) {
				j.cut("conflict");
				p.println(j.get("msgid").getValue() + "/" + j.get("msgstr") + ":" + conflict);
			}
			conflict = j.get("conflict_plural");
			if (conflict != null) {
				j.cut("conflict_plural");
				p.println(j.get("msgid").getValue() + "/" + j.get("msgid_plural") + ":" + conflict);
			}
			conflict = j.get("conflict_msgstr0");
			if (conflict != null) {
				j.cut("conflict_msgstr0");
				p.println(j.get("msgid").getValue() + "/" + j.get("msgstr[0]") + ":" + conflict);
			}
		}
		p.close();
		
		// 書き出し
		Po2Json.write("PoMerge.txt", result);
	}
}
