package com.ntt.tc.misc.po;

import java.io.*;
import java.util.*;

import abdom.data.json.*;
import abdom.data.json.object.*;
import com.ntt.tc.data.*;

/**
 * po ファイル同士の差分を抽出する。
 * java PoDiff [new Po] [old Po]
 * で、JSON 形式(object array)で (new Po) にあって (old Po) にない
 * オブジェクトを抽出する。
 * 集合としては、 {new Po} - {old Po} と等価。
 */
public class PoDiff {
	
	public static JsonType sub(JsonType newone, JsonType oldone) {
		// newone, oldone の 差を取る
		JsonType result = new JsonArray();
		
		for (JsonType jt : newone) {
			boolean found = false;
			for (JsonType jt2 : oldone) {
				if (jt.equals(jt2)) {
					found = true;
					break;
				}
			}
			if (!found) result.push(jt);
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("podiff ツール - 指定した２つのpoファイルの差分を抽出する。先に指定した po ファイルにあるエントリで、後に指定した po ファイルにあるエントリを含まないものを抽出し、PoDiff.result.txt の名前で出力する");
			System.out.println();
			System.out.println("java PoDiff [po A] [po B]");
			System.out.println();
			System.out.println("  po A  : poファイル");
			System.out.println("  po B  : poファイル");
			System.out.println();
			System.out.println("結果は、[po A] - [po B] のようなもので、集合としての差を抽出する。すなわち、{結果}={x | x は po A に含まれ、po B に含まれない}");
			System.exit(-1);
		}
		JsonType newone = Po2Json.read(args[0]);
		JsonType oldone = Po2Json.read(args[1]);
		
		Po2Json.write("PoDiff.result.txt", sub(newone, oldone));
	}
}
