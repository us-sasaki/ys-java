package misc;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Chunk;

/**
 * en-old, en-new, ja-old の md ファイルイメージから、ja-new suggestion を
 * 生成します。DiffMd ツールの中心となる処理を行います(constructor)。
 * また、suggestion をテキストとしてフォーマットします。
 */
public class DiffMdInTranslate {
	
	// blocks を生成するための中間情報
	
	/** en-old, en-new の diff 結果 */
	protected Patch newOldDiff;
	
	/** en-old と ja-old の LooseMap */
	protected LooseMap enJaMap;
	
	
	/**
	 * ja-new を生成するための情報
	 * en-old と en-new 差分情報、および差分箇所に対応する ja-old 情報を含みます
	 */
	protected List<Block> blocks;
	
	protected int originalDocSize;
	protected int revisedDocSize;
	protected int originalTranslatedDocSize;
	
	protected int orgRevDiffCount;
	
/*--------------------
 * inner static class
 */
	/**
	 * Block は更新のある英文箇所に対応する日本語のブロックを示す。
	 * en-old ja-old の対応は 1:1 でなく LooseMap によるため、
	 * 日本語のブロックは一般に複数行からなる。
	 * 複数行からなるため、英語の更新が複数含まれる場合がある。
	 * このプログラムでは、まず LooseMap の en-old ja-old 対応で
	 * 日本語(0行/1行/複数行)単位で Block を作成し、次にこの日本語部分に
	 * 対応する英語更新部分を格納する処理を行う。
	 */
	protected static class Block {
		/** 訳文の開始行(含む, start>end の場合は含まない) */
		int start;
		/** 訳文の終了行(含む, start>end の場合は含まない) */
		int end;
		
		/** このブロックに含まれる英文同士の Delta */
		List<Delta>	deltas;
		
		/** このブロックに含まれる訳文の各行 */
		List<String> lines;
		
	}
	
/*-------------
 * constructor
 */
	/**
	 * en-old, en-new, ja-old の文書イメージから、ja-new を作成するための
	 * 情報(blocks)を作成します。
	 *
	 * @param		originalDoc		en-old ファイル
	 * @param		revisedDoc		en-new ファイル
	 * @param		originalTranslatedDoc	ja-old ファイル
	 */
	public DiffMdInTranslate(List<String> originalDoc,
							List<String> revisedDoc,
							List<String> originalTranslatedDoc) {
		// 各文書のサイズを取得
		originalDocSize	= originalDoc.size();
		revisedDocSize	= revisedDoc.size();
		originalTranslatedDocSize = originalTranslatedDoc.size();
		
		// 英文同士の diff をとる
		newOldDiff = DiffUtils.diff(originalDoc, revisedDoc);
		
		// 英文同士の diff で一致度を計算するための diff 行数をカウント
		orgRevDiffCount = 0;
		for (Delta delta : newOldDiff.getDeltas()) {
			orgRevDiffCount += delta.getOriginal().size();
			orgRevDiffCount += delta.getRevised().size();
		}
		
		// 原文→訳文　の LooseMap を生成する
		//
		// ①MarkExtractor で英語、日本語の比較可能部分を抽出
		// ②LooseMap で 英語→日本語 の行間マッピングを取得
		MarkExtractor me = new MarkExtractor();
		
		List<String> enMark = me.extract(originalDoc);
		List<String> jaMark = me.extract(originalTranslatedDoc);
		
		//
		// enJaMap は en-old → ja-old への写像。
		// ただし、ja-old は範囲[min,max]になる。
		// en-old の i 行は ja-old の enJaMap.min(i)～enJaMap.max(i) 行に
		// 対応する
		//
		enJaMap = new LooseMap(enMark, jaMark);
		
		prepareBlocks(originalTranslatedDoc);
		diffForTranslate();
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 英語ー日本語の対応箇所の塊(Block)のリストを作成
	 * LooseMap の 値域(日本語, 1行/複数行) に対して Block を枠として設置
	 * 対応する日本語がない場合も Block は用意する。
	 *
	 * @param		originalTranslatedDoc	ja-old の各行
	 */
	private void prepareBlocks(List<String> originalTranslatedDoc) {
		blocks = new ArrayList<Block>();
		int lastMin = -1;
		int lastMax = -1;
		for (int i = 0; i < enJaMap.domainSize(); i++) {
			int jaMin = enJaMap.min(i);
			int jaMax = enJaMap.max(i);
			if (lastMin == jaMin && lastMax == jaMax) continue;
			if (jaMax == jaMin-1) continue; // いるか？
			// 新しい日本語行が出たら Block 追加。
			// 同じうちは追加しない(同一 Block に入れる)。
			lastMin = jaMin;
			lastMax = jaMax;
			Block block = new Block();
			block.start	= jaMin;
			block.end	= jaMax;
			block.deltas = new ArrayList<Delta>();
			block.lines = new ArrayList<String>();
			for (int j = block.start; j <= block.end; j++) {
				block.lines.add(originalTranslatedDoc.get(j));
			}
			blocks.add(block);
		}
	}
	
	/**
	 * 英文同士の diff を検出し、該当する Block に入れていく
	 */
	private void diffForTranslate() {
		// 英文同士の差分を含む Block を検索する
		for (Delta delta : newOldDiff.getDeltas() ) {
			// delta が改行のみの場合、スキップする
			boolean skip =
				delta.getOriginal().getLines().stream()
									.allMatch( line -> "".equals(line) )
				&&	// getLines() で 0 行の場合、allMatch は true
				delta.getRevised().getLines().stream()
									.allMatch( line -> "".equals(line) );
			if (skip) continue;
			
			// 差分処理開始
			
			// en-old の最初の差分位置を取得
			int deltaStart	= delta.getOriginal().getPosition();
			int deltaEnd	= delta.getOriginal().last();
			
			// 差分を含む最初のインデックスを検索
			// 差分を含む、とは
			// - en-old に対応する enJaMap の行を含む Block
			//   (Block が空行の場合を含む)
			int startBlockIndex = 0;
			int jaMin = enJaMap.min(deltaStart);
			for (;startBlockIndex < blocks.size(); startBlockIndex++) {
				if (blocks.get(startBlockIndex).end >= jaMin) break;
			}
			// startBlockIndex は、delta(en-old)を含むブロック番号
			if (startBlockIndex == blocks.size()) startBlockIndex--;
			
			// 差分を含む最後のインデックスを検索
			int endBlockIndex = startBlockIndex + 1;
			int jaMax = enJaMap.max(deltaEnd);
			for (;endBlockIndex < blocks.size(); endBlockIndex++) {
				if (blocks.get(endBlockIndex).start > jaMax) {
					endBlockIndex--;
					break;
				}
			}
			if (endBlockIndex == blocks.size()) endBlockIndex--;
			
			// ブロックをマージする(block index は変更される)
			if (startBlockIndex < endBlockIndex)
				mergeBlocks(startBlockIndex, endBlockIndex);
			
			//  startBlockIndex 側にマージされる
			
			// ブロックに Delta を登録する
			blocks.get(startBlockIndex).deltas.add(delta);
		}
	}
	
	/**
	 * 指定された範囲のブロックをマージします。
	 *
	 * @param		start		マージ開始ブロック番号(含む)
	 * @param		end			マージ終了ブロック番号(含む)
	 */
	private void mergeBlocks(int start, int end) {
		if (start >= end) return;
		
		Block merged = blocks.get(start);
		merged.end = blocks.get(end).end;
		
		for (int i = start + 1; i <= end; i++) {
			// start の次の block をマージ
			Block target = blocks.get(start+1);
			
			// 既存の delta/lines を add する
			for (Delta delta : target.deltas) merged.deltas.add(delta);
			for (String line : target.lines) merged.lines.add(line);
			// start の次の block を消去
			blocks.remove(start+1);
		}
	}
	
	public double getDiffRate() {
		return (double)orgRevDiffCount / (double)(originalDocSize + revisedDocSize);
	}
	
/*------------------------
 * テキスト出力メソッド群
 */
	/**
	 * 結果をテキスト形式で出力します。テキスト形式は等幅フォントを利用して
	 * 見ることを前提とします。
	 */
	public List<String> toText() {
		List<String> result = new ArrayList<String>();
		for (Block block : blocks) {
			if (block.deltas.size() > 0) {
				// 変更を含むブロック
				result.add("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
				result.add("┃　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい　　　　　　　┃");
				int count = 1;
				for (Delta delta : block.deltas) {
					Chunk org = delta.getOriginal();
					Chunk rev = delta.getRevised();
					String[] wordwiseDiffed = putDiffMark(org, rev);
					
					String s = "━━━━━━━━━━ 原文更新前("+count+")：" + (org.getPosition()+1) + "行目 ━━━━━━━━━━";
					result.addAll(putFrame(s));
					//printChunk(org, result);
					String[] lines = wordwiseDiffed[0].split("\n");
					for (String line : lines)
						result.addAll(putFrame(line));
					s = "━━━━━━━━━━ 原文更新後("+count+")：" + (rev.getPosition()+1) + "行目 ━━━━━━━━━━";
					result.addAll(putFrame(s));
					//printChunk(rev, result);
					lines = wordwiseDiffed[1].split("\n");
					for (String line : lines)
						result.addAll(putFrame(line));
					count++;
				}
				result.add("┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫");
				for (String line : block.lines) {
					result.add(line);
				}
				result.add("┃　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃");
				result.add("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
			} else {
				// 変更を含まないブロック
				for (String line : block.lines) {
					result.add(line);
				}
			}
		}
		return result;
	}
	
	protected void printChunk(Chunk c, List<String> buff) {
		for (Object o : c.getLines()) {
			buff.addAll(putFrame(o.toString()));
		}
	}
	
	/**
	 * 与えられた行に枠(罫線)をつけ、必要なら複数行にして返却します
	 */
	protected List<String> putFrame(String target) {
		int maxWidth = 78;
		List<String> result = new ArrayList<String>();
		
		String[] token = target.split(" ");
		if (token.length == 0) token = new String[] {""};
		
		int tokenIndex = 0;
	loop:
		while (true) {
			int width = 0;
			StringBuilder sb = new StringBuilder();
			sb.append("┃"); width += 2;
			while (true) {
				sb.append(token[tokenIndex]);
				width += widthInFixedPitch(token[tokenIndex]);
				int nextW = (tokenIndex == token.length - 1)?maxWidth:width+3+widthInFixedPitch(token[tokenIndex+1]);
				if (nextW >= maxWidth) {
					// 次の単語を追加するとはみ出すとき
					int len = maxWidth - width - 2;
					len = (len < 0)?0:len;
					sb.append("                                                                                ".substring(0, len));
					sb.append("┃");
					result.add(sb.toString());
					tokenIndex++;
					if (tokenIndex == token.length) break loop;
					break;
				}
				sb.append(' ');
				width++;
				tokenIndex++;
				if (tokenIndex == token.length) break loop;
			}
		}
		return result;
	}
	
	protected static int widthInFixedPitch(String str) {
		int len = 0;
		for (int i = 0; i < str.length(); i++) {
			int c = str.charAt(i);
			if (c < 128) len += 1;
			else len += 2;
		}
		return len;
	}
	
	/**
	 * 単語単位の diff をとります。
	 */
	protected static String[] putDiffMark(Chunk original, Chunk revised) {
		StringBuilder orgStr = new StringBuilder();
		for (Object o : original.getLines()) {
			orgStr.append(o.toString());
			orgStr.append(" \n");
		}
		StringBuilder revStr = new StringBuilder();
		for (Object o : revised.getLines()) {
			revStr.append(o.toString());
			revStr.append(" \n");
		}
		
		List<String> org = Arrays.asList(orgStr.toString().split(" "));
		List<String> rev = Arrays.asList(revStr.toString().split(" "));
		
		Patch p = DiffUtils.diff(org, rev);
		StringBuilder orgPut = new StringBuilder();
		StringBuilder revPut = new StringBuilder();
		
		int orgCnt = 0; // 次に挿入するカウント
		int revCnt = 0;
		// delta に対してマーク【】を添付する
		for (Delta delta : p.getDeltas()) {
			int orgStart = delta.getOriginal().getPosition();
			int revStart = delta.getRevised().getPosition();
			// delta の手前まで append しておく
			for (;orgCnt < orgStart; orgCnt++) {
				orgPut.append(org.get(orgCnt));
				orgPut.append(' ');
			}
			for (;revCnt < revStart; revCnt++) {
				revPut.append(rev.get(revCnt));
				revPut.append(' ');
			}
			// delta 部分
			orgPut.append('【');
			revPut.append('【');
			for (Object o : delta.getOriginal().getLines()) {
				orgPut.append(o.toString());
				orgPut.append(' ');
				orgCnt++;
			}
			for (Object o : delta.getRevised().getLines()) {
				revPut.append(o.toString());
				revPut.append(' ');
				revCnt++;
			}
			if (orgPut.charAt(orgPut.length()-1) == ' ')
				orgPut.deleteCharAt(orgPut.length()-1); // 最後のスペースを削除
			if (revPut.charAt(revPut.length()-1) == ' ')
				revPut.deleteCharAt(revPut.length()-1);
			orgPut.append("】 ");
			revPut.append("】 ");
		}
		// 最後の append
		for (;orgCnt < org.size(); orgCnt++) {
			orgPut.append(org.get(orgCnt));
			orgPut.append(' ');
		}
		for (;revCnt < rev.size(); revCnt++) {
			revPut.append(rev.get(revCnt));
			revPut.append(' ');
		}
		if (orgPut.charAt(orgPut.length()-1) == ' ')
			orgPut.deleteCharAt(orgPut.length()-1); // 最後のスペースを削除
		if (revPut.charAt(revPut.length()-1) == ' ')
			revPut.deleteCharAt(revPut.length()-1);
		
		String[] result = { orgPut.toString(), revPut.toString() };
		
		return result;
	}
}
