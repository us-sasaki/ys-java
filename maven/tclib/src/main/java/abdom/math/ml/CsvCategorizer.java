package abdom.math.ml;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.util.CsvReader;

/**
 * CSV ファイルを読み込み、データセットをもとにカテゴライズを推論します。
 * また、数値の場合における基本統計量や、カテゴリーにおける値数などの
 * 基本量を算出し、Json 形式で保持します。
 * すべてメモリ上に読み込みます。
 *
 * @version		January 2, 2018
 * @author		Yusuke Sasaki
 */
public class CsvCategorizer {
	
	/**
	 * CSV データの各カラムの属性を保持する JsonArray です。
	 * JsonArray の要素は各列を表しており、次のような構造の JsonObject です。
	 * <pre>
	 * キー:バリュー
	 * index:カラムの番号(0～)
	 * name:カラム名
	 * type:データの型
	 * stats: 基本統計量などを示す JsonObject。構造は以下。
	 * ----
	 * stats の内容
	 * count: 要素数
	 * kinds: 値の種類数
	 *
	 * Number の場合、stats に以下が付加されます
	 * sum: 合計値
	 * min: 最小数
	 * max: 最大数
	 * mean: 平均値
	 * variance : 分散
	 * deviation: 偏差
	 * median: 中央値
	 * mode: 最頻値(のうちの１つ)
	 *
	 * Category の場合、以下が付加されます
	 * frequency : {値:頻度} の Array(頻度が多いものが若番)
	 * </pre>
	 */
	protected JsonType metaData;
	
	/**
	 * CSV データ内容を保持する JsonArray です。
	 * data:データArrayのArray(Boolean/Number/String)
	 */
	protected JsonType data;
	
	protected List<TypeMatcher> matchers;
	protected List<TypeMatcher> types;
	protected boolean headerExists = false;
	
/*-------------
 * constructor
 */
	public CsvCategorizer() {
		matchers = new ArrayList<TypeMatcher>();
		matchers.add(new LongMatcher());
		matchers.add(new DoubleMatcher());
		matchers.add(new DateMatcher());
		matchers.add(new StringMatcher()); // always matches
	}
	
/*------------------
 * instance methods
 */
	public void categorize(String filename) throws IOException {
		if (metaData != null || data != null)
			throw new IllegalStateException("すでにこのオブジェクトでファイルを読み込み済みです");
		
		metaData = new JsonArray();
		data = new JsonArray();
		
		// ファイル読み込み
		List<String[]> rows = CsvReader.readAll(filename);
		
		int cols = rows.get(0).length;
		
		//
		// 型判定(一行目以外)
		//
		boolean[][] unavailableTypes = new boolean[cols][matchers.size()];
		
		for (int r = 1; r < rows.size(); r++) {
			String[] columns = rows.get(r);
			for (int i = 0; i < cols; i++) {
				for (int j = 0; j < matchers.size(); j++) {
					// すでに対象外となっている型のチェックは行わない
					if (unavailableTypes[i][j]) continue;
					unavailableTypes[i][j] = !matchers.get(j).matches(columns[i]);
				}
			}
		}
		// ヘッダの型判定が上と整合するか(する=ヘッダなし、しない=ヘッダあり)
		String[] columns = rows.get(0);
		
	lp:
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < matchers.size(); j++) {
				// すでに対象外となっている型のチェックは行わない
				if (unavailableTypes[i][j]) continue;
				boolean available = matchers.get(j).matches(columns[i]);
				if (!available) {
					headerExists = true;
					break lp;
				}
				unavailableTypes[i][j] = !available;
			}
		}
		// meta data (共通部分)追加
		final String[] name;
		if (headerExists) {
			name = rows.get(0);
		} else {
			name = new String[rows.get(0).length];
			for (int i = 0; i < name.length; i++) {
				name[i] = String.valueOf(i);
			}
		}
		metaData = new JsonArray();
		types = new ArrayList<TypeMatcher>();
		
		for (int i = 0; i < cols; i++) {
			JsonType e = new JsonObject();
			e.put("index", i);
			e.put("name", name[i]);
			for (int j = 0; j < matchers.size(); j++) {
				if (unavailableTypes[i][j]) continue;
				e.put("type", matchers.get(j).getName());
				types.add(matchers.get(j));
				break;
			}
			metaData.push(e);
		}
		
		// stats 計算
		rows = rows.subList( headerExists?1:0, rows.size() );
		
		for (int i = 0; i < cols; i++) {
			JsonType attr = metaData.get(i);
			
			categorize(attr, rows, i);
		}
	}
	
/*------------------
 * instance methods
 */
	public JsonType getMetaData() {
		return metaData;
	}
	
	public JsonType getData() {
		return data;
	}
	
	@SuppressWarnings("unchecked")
	private void categorize(JsonType toPut, List<String[]> data, int col) {
		
		// 要素の種類数カウント用
		// 昇順にするため、TreeMap を利用
		Map<String, Long> kinds = new TreeMap<String, Long>();
		
		// 全走査
		double sum = 0d;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		int count = 0;
		JsonArray dataArray = new JsonArray();
		TypeMatcher t = types.get(col);
		
		for (String[] values : data) {
			String value = values[col];
			
			
			if ("".equals(value)) {
				dataArray.push(JsonType.NULL);
				continue;
			}
			dataArray.push(value); // JsonValue(String) として push
			
			count++;
			Long c = kinds.get(value);
			if (c == null) c = 0L;
			kinds.put(value, ++c);
			
			if (t instanceof DoubleMatcher) { // 数値と見なせる
				double l = ((DoubleMatcher)t).toDouble(value);
				sum += l;
				if (l > max) max = l;
				else if (l < min) min = l;
			}
		}
		double mean = sum / count;
		
		double v = 0d; // 2乗和
		if ( t instanceof DoubleMatcher ) {
			for (JsonType value : dataArray) {
				if (value.getType() == JsonType.TYPE_VOID) continue;
				double d = value.doubleValue() - mean;
				v += d*d;
			}
		}
		
		// 型判定、属性付加
		if ( t instanceof StringMatcher ) {
			// "" が多いような測定値でも CATEGORY となってしまう
			if (kinds.keySet().size() / (double)count < Math.min(Math.log(count)*5, 0.7) )
				t = new CategoryMatcher();
				types.set(col, t);
				toPut.put("type", t.getName());
		}
		
		JsonObject stats = new JsonObject();
		stats.put("count", count);
		stats.put("kinds", kinds.keySet().size());
		
		// stats に付加
		if (t instanceof DoubleMatcher) {
			stats.put("sum", sum);
			stats.put("min", min);
			stats.put("max", max);
			stats.put("mean", mean);
			stats.put("variance", v/count);
			stats.put("deviation", Math.sqrt(v/count));
			// median(中央値)
			double s = 0d;
			String median = null;
			for (JsonType value : dataArray) {
				if (value.getType() == JsonType.TYPE_VOID) continue;
				double val = value.doubleValue();
				s += val;
				if (s >= sum/2) {
					stats.put("median", val);
					break;
				}
			}
			
			// mode(最頻値) 間違っている(頻度の降順で並べる必要がある)
//			String[] modekeys = kinds.keySet().toArray(new String[0]);
//			String modekey = modekeys[modekeys.length-1];
//			stats.put("mode", Double.parseDouble(modekey));
		} else if (t instanceof CategoryMatcher) {
			// 値:頻度のArray
			// kinds を値で降順、キーで昇順に並び替える
			
			Map.Entry<String, Long>[] entry = kinds.entrySet().toArray(new Map.Entry[0]);
			Arrays.sort(entry, (a, b) -> 
					{
						int c = a.getValue().compareTo(b.getValue());
						if (c != 0) return -c;
						return a.getKey().compareTo(b.getKey());
					});
			
			JsonArray ja = new JsonArray();
			for (Map.Entry<String, Long> e : entry) {
				ja.push(JsonType.o(e.getKey(), e.getValue()));
			}
			stats.put("frequency", ja);
		}
		toPut.put("stats", stats);
		this.data.push(dataArray);
	}
	
}

