package misc;

import java.util.List;
import java.util.ArrayList;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.Delta;
import difflib.Chunk;

/**
 * MarkExtractor の Markdown 書式情報に基づいて、原文ファイルの各行と
 * 翻訳ファイルの各行のマッピングを生成します。
 * 書式情報に基づくため、必ずしも行ごとに１：１の対応せず、対応する可能性の
 * ある翻訳ファイルの先頭行、最終行にマッピングされます。
 * マッピングは、次の写像です。
 * (原文ファイル行) → (jmin, jmax) ただし、jmin: 対応する翻訳ファイルの先頭行,
 * jmax: 対応する翻訳ファイルの最終行
 *
 * <pre>
 *    マッピングのイメージは以下の通りです
 *
 *       原文　　　　 　　翻訳文
 *   ------------- ＼ +-------------+
 *   ------------- → |             |
 *   ------------- ／ +-------------+
 *          :                :
 *   ------------- ＼ +-------------+
 *   ------------- → |             |
 *   ------------- ／ +-------------+
 *   ------------- ／
 *   ------------- → ---------------
 *   ------------- ／
 *
 *   重要な特徴
 *     翻訳文側は範囲になる
 *     どちらも順序は保たれる
 *     Diff で一致判定が出た行は 1:1 対応
 *     Diff で一行が一行に置換された場合 1:1 対応
 * </pre>
 *
 * @version		February 4, 2017
 * @author		Yusuke Sasaki
 */
class LooseMap {
	
	/**
	 * List の要素番号 : en の行番号
	 * 要素の min : 対応する ja の行番号(最小値) (含む)
	 * 要素の max : 対応する ja の行番号(最大値) (含む)
	 *
	 * en の行番号に対応する日本語がない場合、最大値は最小値より 1
	 * 小さい値になる。
	 * 例： en : 9 行目　と　ja : 9 行目　が対応
	 * 　　 en :10 行目 には ja : なし　　の場合
	 *   map.get(9) = [9,9]
	 *   map.get(10)= [10,9]
	 * 　のようになる。
	 */
	List<Row> map;
	
	static class Row {
		int min;
		int max;
		
		Row() {
		}
		
		Row(int min, int max) {
			this.min = min;
			this.max = max;
		}
	}
	
/*-------------
 * constructor
 */
	/**
	 * 指定された原文(行のリスト)、翻訳文(行のリスト)から、
	 * DiffUtils によって取得した diff 情報を元に、原文各行に対応する、
	 * ゆるいマッピング(LooseMap)情報を構築します。
	 *
	 * @param	domain	原文(markdown 形式、行のリスト)
	 * @param	range	翻訳分(markdown 形式、行のリスト)
	 */
	LooseMap(List<String> domain, List<String> range) {
		map(domain, range);
	}
	
/*-----------------
 * private method
 */
	/**
	 * 対応 map(原文ファイルの行　→　翻訳ファイルの複数行) を生成します。
	 *
	 * @param	domain	原文の各行の List
	 * @param	range	翻訳分の各行の List
	 */
	private void map(List<String> domain, List<String> range) {
		map = new ArrayList<Row>();
//domain.stream().forEach(System.out::println);
//range.stream().forEach(System.out::println);
		int ji = 0; // 現在処理中の行(ja)
		int ei = 0; // 現在処理中の行(eng)
		
		// まず、書式抽出された原文、訳文の diff をとる
		Patch patch = DiffUtils.diff(domain, range);
		//
		for (Delta delta : patch.getDeltas()) {
			
			// ja/eng それぞれの差分の先頭行
			int jDeltaStart	= delta.getRevised().getPosition();
			int eDeltaStart	= delta.getOriginal().getPosition();
			
			// ja/eng それぞれの差分の最終行 / last() は要素数でなく、行番号
			int jDeltaEnd	= delta.getRevised().last();
			int eDeltaEnd	= delta.getOriginal().last();
			// End = Start(1:1対応) や End = Start-1(対応部分なし)
			// となる場合がある
			
//System.out.println("jΔ=["+jDeltaStart+","+jDeltaEnd+"]");
//System.out.println("eΔ=["+eDeltaStart+","+eDeltaEnd+"]");
//Chunk jc = delta.getRevised();
//System.out.println("----------jΔ--------");
//jc.getLines().stream().forEach(System.out::println);
//Chunk ec = delta.getOriginal();
//System.out.println("----------eΔ--------");
//ec.getLines().stream().forEach(System.out::println);
			
			// 完全一致部分を登録(eDeltaStart までは完全一致)
			for (; ei < eDeltaStart; ei++) {
				// j側は1行分入る
				map.add(new Row(ji, ji));
				ji++;
			}
			// 不確定部分(Delta部分)を登録
			for (; ei <= eDeltaEnd; ei++) {
				// j側は同じ値が毎回入る(同一範囲をポイントする)
				map.add(new Row(jDeltaStart, jDeltaEnd));
			}
			ji = jDeltaEnd + 1; // j側、次の行に移動する
		}
		// Delta にならない最後の完全一致部分
		for (; ei < domain.size(); ei++) {
			map.add(new Row(ji, ji));
			ji++;
		}
		// ji のみ残る場合があるので、map の最後を書き換え
		// この処理がなく、bugだった
		map.get( map.size()-1 ).max = range.size()-1;
		
//System.out.println("---完成した map---");
//int index = 0;
//for (Row r : map) {
//	System.out.println("en-old "+(index++)+" -- ja-old["+r.min+"-"+r.max+"]");
//}
	}
	
/*-----------------
 * instance method
 */
	/**
	 * 指定された原文ファイル行に対応する翻訳ファイル行の最小値を
	 * 取得します。
	 *
	 * @param	dRow	原文の行番号
	 * @return	翻訳の行番号(最小値、含む)
	 */
	int min(int dRow) {
		// dRow == map.size() になることがあるため、応急処置
		if (dRow < map.size())
			return map.get(dRow).min;
		return map.get(map.size()-1).max;
	}
	
	/**
	 * 指定された原文ファイル行に対応する翻訳ファイル行の最大値を
	 * 取得します。
	 *
	 * @param	dRow	原文の行番号
	 * @return	翻訳の行番号(最大値、含む)
	 */
	int max(int dRow) {
		// original の最初の行が空行の場合、-1 になることがあるので、
		// 応急処置
		if (dRow < 0) dRow = 0;
		// こっちは一応　応急処置
		if (dRow < map.size())
			return map.get(dRow).max;
		return map.get(map.size()-1).max;
	}
	
	/**
	 * domain のサイズ(en-old の行数)を返します。
	 *
	 * @return	domain のサイズ
	 */
	int domainSize() {
		return map.size();
	}
}
