package com.ntt.tc.misc.po;

import java.io.*;
import java.util.*;

import abdom.data.json.*;

/**
 * java PoPatch file1 file2
 * で、file1 のエントリを用いて file2 の同一 msgid の値を上書きする。
 * file2 に該当がない場合はスキップとなる。
 *
 * 翻訳した po ファイルの結果を他の po ファイルに適用する場合、
 * 同一 po ファイルで、バージョンアップの際に以前の値を適用する場合
 * に使用することを想定している。
 */
public class PoPatch {
	public static JsonType select(JsonType jt, String key, String value) {
		for (JsonType j : jt) {
			JsonType a = j.get(key);
			if (a == null) continue;
			if (a.getValue().equals(value)) return j;
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		JsonType source = Po2Json.read(args[0]);
		JsonType target = Po2Json.read(args[1]);
		int count = 0;
		
		for (JsonType src : source) {
			// source の要素の msgid と equal な target の要素を抽出
			String val = src.get("msgid").getValue();
			
			JsonType tgt = select(target, "msgid", val);
			if (tgt == null) continue; // なければ次へ(patch しない)
			
			// あった場合
			// msgstr を上書き
			try {
				String msgstr = src.get("msgstr").getValue(); // may be null
				String org = tgt.get("msgstr").getValue();
				// もともと同一だったら無処理
				if (!msgstr.equals(org)) { // may throw NullPointerException
					tgt.put("msgstr", msgstr);
					count++;
					System.out.println(msgstr);
				}
			} catch (NullPointerException ignored) {
			}
			// msgstr[0] も確認し、上書き
			try {
				String msgstr0 = src.get("msgstr[0]").getValue();
				String org = tgt.get("msgstr[0]").getValue();
				// もともと同一だったら無処理
				if (!msgstr0.equals(org)) {
					tgt.put("msgstr[0]", msgstr0);
					count++;
					System.out.println(msgstr0);
				}
			} catch (NullPointerException ignored) {
			}
		}
		
		Po2Json.write("PoPatch.result.txt", target);
		System.out.println(count + "個上書きしました");
	}
}
