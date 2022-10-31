package com.ntt.tc.misc.po;

import java.io.*;
import java.util.*;

import abdom.data.json.*;

/**
 * PO ファイルのエントリを JsonObject の JsonArray に変換します。
 * msgid, msgid_plural, msgstr, msgstr[i] のようなタグはオブジェクトの
 * キー名となり、文字列は JsonValue(string) に変換されます。
 *
 * @version		2017/3/16
 * @author		Yusuke Sasaki
 */
public class Po2Json {
	
	/**
	 * 指定された po ファイルを読み込み、JsonArray として返却します。
	 *
	 * @param	fname	読み込み対象のファイル名
	 * @return	JsonArray に変換した結果
	 */
	public static JsonArray read(String fname) throws Exception {
		BufferedReader br = new BufferedReader(
								new InputStreamReader(
									new FileInputStream(fname),
									"UTF-8"));
		//if (!fname.endsWith(".po"))
		//	throw new RuntimeException("po ファイルを指定してください: "+fname);
		
		JsonObject empty = new JsonObject();
		
		JsonArray result = new JsonArray();
		JsonObject jo = new JsonObject();
		String tagname = "";
		String value = "";
		
		while (true) {
			String line = br.readLine();
			if (line == null || line.equals("") ) {
				// 空行は区切り行とみなす
				if (!jo.equals(empty)) result.push(jo);
				if (line == null) break;
				jo = new JsonObject();
				tagname = "";
				value = "";
				continue;
			}
			if (line.startsWith("#")) {
				// # ではじまる行は、key に対して null を設定
				jo.put(line, (String)null);
				continue;
			}
			if (line.startsWith("\"")) {
				// " ではじまる行は、複数行に分割されたエントリ
				if (tagname.equals(""))
					throw new RuntimeException("フォーマット異常:"+fname+":"+line);
				//
				jo.put(tagname, jo.get(tagname).getValue() + line.substring(1, line.length()-1));
				continue;
			}
			value = "";
			int index = line.indexOf(' ');
			if (index == -1)
				throw new RuntimeException("フォーマット異常:"+fname+":"+line);
			tagname = line.substring(0, index);
			value = line.substring(index+2, line.length()-1);
			jo.put(tagname, value);
		}
		
		br.close();
		return result;
	}
	
	/**
	 * 指定されたファイル名で、po 形式で JsonArray を出力します。
	 *
	 * @param	fname	出力するファイル名
	 * @param	data	po 形式の JsonArray
	 */
	public static void write(String fname, JsonType data) throws IOException {
		PrintWriter p = new PrintWriter(
							new OutputStreamWriter(
								new FileOutputStream(fname),
								"UTF-8"
							));
		for (JsonType j : data) {
			for (String key : j.keySet()) {
				p.print(key);
				JsonType value = j.get(key);
				if (value.toString().equals("null")) {
					p.print("\n");
				} else {
					p.print(" \"");
					String valstr = j.get(key).getValue();
					int index = valstr.indexOf("\\n");
					if (index == -1 || !key.startsWith("msg")) {
						p.print(valstr);
						p.print("\"\n");
					} else {
						//
						// ここで、msgstr の行が出るが、\n を含む場合複数行とする
						// 処理を入れる。複数行は、
						// msgstr ""
						// ".....\n"
						// "........\n"
						// の形式。msgstr[0] 等も同様
						// new JsonValue((String)null).getValue() == null である。
						//
						p.print("\""); // 空文字列
						p.print("\n");
						while (true) {
							String s = valstr.substring(0, index);
							p.print("\"");
							p.print(s);
							p.print("\\n\"\n");
							if (index + 2 >= valstr.length()) break;
							valstr = valstr.substring(index+2);
							index = valstr.indexOf("\\n");
							if (index == -1) {
								p.print("\"" + valstr + "\"\n");
								break;
							}
						}
					}
				}
				
			}
			p.print("\n");
		}
		p.close();
	}
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		JsonType jt = read("admin ja.po");
		
		for (JsonType j : jt) {
			for (String key : j.keySet()) {
				System.out.print(key);
				JsonType value = j.get(key);
				if (value.toString().equals("null")) {
					System.out.println();
				} else {
					System.out.print(" \"");
					System.out.print(j.get(key).getValue());
					System.out.println("\"");
				}
			}
			System.out.println();
		}
	}
}
