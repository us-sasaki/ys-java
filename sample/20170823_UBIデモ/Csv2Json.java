import abdom.data.json.*;
import abdom.util.CsvReader;

/**
 * CSVファイルをJsonArray に変換します。
 * 各カラムの型は、自動判定されます。
 * long > double > String の順に判定されます。
 * 将来的に自動型判定対象を増やすことを想定し、インスタンスモデルとしています。
 *
 * @version		2017/8/29
 * @author		Yusuke Sasaki
 */
public class Csv2Json {
	
	public JsonArray read(String csv) {
		String[] columnName = null;
		String[] type = null; // 自動 type 検知
		
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
	
/*---------------
 * main for test
 */
	/**
	 *
	 */
	public static void main(String[] args) {
		Csv2Json c2j = new Csv2Json();
		JsonArray ja = c2j.read("CarData.csv");
		System.out.println(ja.toString("  "));
	}
}
