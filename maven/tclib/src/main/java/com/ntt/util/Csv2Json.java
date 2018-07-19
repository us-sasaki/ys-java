package com.ntt.util;

import java.util.List;

import abdom.data.json.*;
import abdom.util.CsvReader;

/**
 * CSVファイルをJsonArray に変換します。
 * 各カラムの型は、自動判定されます。
 * long ＞ double ＞ String の順に判定されます。
 * 将来的に自動型判定対象を増やすことを想定し、インスタンスモデルとしています。
 *
 * @version		2017/8/29
 * @author		Yusuke Sasaki
 */
public class Csv2Json {
	protected String[] type;
	
	/**
	 * 指定された csv ファイルを読み込み、JsonArray 形式で返却します。
	 * csv ファイルの一行目はタイトル行と見なされます。
	 * JsonArray の各要素は JsonObject で、カラム名:値 の形で格納されます。
	 * 2 パスで処理され、1 パス目で各カラムの型(Number / String)を判定します。
	 *
	 * @param		csv		csvファイル名
	 * @return		csv の JsonArray 化
	 */
	public JsonArray read(String csv) {
		String[] columnName = null;
		String[] type = null;
		this.type = null; // 自動 type 検知
		
		// pass 1  type 検知する
		// 優先順位 long > double > String(低い)
		int size = 0;
		boolean first = true;
		for (String[] row : CsvReader.rows(csv)) {
			// タイトル行？
			if (first) {
				// タイトル行
				first = false;
				columnName = row;
				type = new String[row.length];
				continue;
			}
			// タイトル行以外(通常データ)
			int i = 0;
			for (String column : row) {
				if (type[i] == null) type[i] = "long";
				if (type[i].equals("String")) continue;
				try {
					Double.parseDouble(column);
					type[i] = "double";
					Long.parseLong(column);
					type[i] = "long";
				} catch (NumberFormatException nfe) {
					type[i] = "String";
				}
				i++;
			}
			size++;
		}
		
		this.type = type;
		
		// データを読み込む
		JsonArray result = new JsonArray();
		first = true;
		for (String[] row : CsvReader.rows(csv)) {
			// タイトル行はスキップ
			if (first) {
				first = false;
				continue;
			}
			// 通常データの行
			JsonObject jo = new JsonObject();
			int i = 0;
			for (String column : row) {
				JsonType jt;
				if (type[i].equals("long")) {
					jt = new JsonValue(Long.parseLong(column));
				} else if (type[i].equals("double")) {
					jt = new JsonValue(Double.parseDouble(column));
				} else {
					jt = new JsonValue(column);
				}
				jo.put(columnName[i], jt);
				i++;
			}
			result.push(jo);
		}
		return result;
	}
	
	/**
	 * 直前に読み込んだ csv ファイルの各カラムの型を返却します。
	 * ファイルを読み込んでいない等で型が確定していない場合、null が
	 * 返却されます。
	 *
	 * @return		型情報(String/long/double 等)の配列
	 */
	public String[] getType() {
		return type;
	}
}
