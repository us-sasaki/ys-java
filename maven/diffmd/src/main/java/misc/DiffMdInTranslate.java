package misc;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Chunk;

/**
 * 
 */
public class DiffMdInTranslate {

	protected Patch newOldDiff;
	protected LooseMap enJaMap;
	
	private List<Block> blocks;
	private int originalDocSize;
	private int revisedDocSize;
	private int originalTranslatedDocSize;
	
/*--------------------
 * inner static class
 */
	private static class Block {
		int start; // 含む
		int end; // 含む
		
		/** このブロックに含まれる英文同士の Delta */
		List<Delta>	deltas;
		
		/** このブロックに含まれる訳文の各行 */
		List<String> lines;
		
	}
	
/*-------------
 * constructor
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
		
		// 原文→訳文　の LooseMap を取得する
		//
		// ①MarkExtractor で英語、日本語の比較可能部分を抽出
		// ②LooseMap で 英語→日本語 の行間マッピングを取得
		MarkExtractor me = new MarkExtractor();
		
		List<String> enMark = me.extract(originalDoc);
		List<String> jaMark = me.extract(originalTranslatedDoc);
		
		enJaMap = new LooseMap(enMark, jaMark);
		
		// 
		// 英語ー日本語の対応箇所の塊(Block)のリストを作成
		// 
		blocks = new ArrayList<Block>();
		int lastRow = -1;
		for (int i = 0; i < enMark.size(); i++) {
			int jaRow = enJaMap.min(i);
			if (lastRow != jaRow) {
				// 新しい行が出たら追加。同じうちは追加しない。
				lastRow = jaRow;
				Block block = new Block();
				block.start	= jaRow;
				block.end	= enJaMap.max(i);
				block.deltas = new ArrayList<Delta>();
				block.lines = new ArrayList<String>();
				for (int j = block.start; j <= block.end; j++) {
					block.lines.add(originalTranslatedDoc.get(j));
				}
				blocks.add(block);
			}
		}
		diffForTranslate();
	}
	
/*------------------
 * instance methods
 */
	private void diffForTranslate() {
		// 英文同士の差分を含む Block を検索する
		for (Delta delta : newOldDiff.getDeltas() ) {
			// delta が改行のみの場合、スキップする
			boolean skip = true;
			for (Object line : delta.getOriginal().getLines() ) {
				if (!"".equals(line)) {
					skip = false;
					break;
				}
			}
			for (Object line : delta.getRevised().getLines() ) {
				if (!"".equals(line)) {
					skip = false;
					break;
				}
			}
			if (skip) continue;
			
			// 差分処理開始
			int deltaStart	= delta.getOriginal().getPosition();
			int deltaEnd	= delta.getOriginal().last();
			
			// 差分を含む最初のインデックスを検索
			int startBlockIndex = 0;
			for (;startBlockIndex < blocks.size(); startBlockIndex++) {
				if (blocks.get(startBlockIndex).end >= enJaMap.min(deltaStart)) {
					break;
				}
			}
			if (startBlockIndex == blocks.size()) startBlockIndex--;
			
			// 差分を含む最後のインデックスを検索
			int endBlockIndex = startBlockIndex + 1;
			for (;endBlockIndex < blocks.size(); endBlockIndex++) {
				if (blocks.get(endBlockIndex).start > enJaMap.max(deltaEnd)) {
					endBlockIndex--;
					break;
				}
			}
			if (endBlockIndex == blocks.size()) endBlockIndex--;
			
			// ブロックをマージする(block index は変更される)
			if (startBlockIndex < endBlockIndex)
				mergeBlocks(startBlockIndex, endBlockIndex);
			
			//  startBlockIndex がマージされたブロック
			
			// ブロックに Delta を登録する
			blocks.get(startBlockIndex).deltas.add(delta);
			
		}
	}
	
	private void mergeBlocks(int start, int end) {
		if (start >= end) return;
		
		Block merged = blocks.get(start);
		merged.end = blocks.get(end).end;
		
		for (int i = start + 1; i <= end; i++) {
			// block をマージ
			Block target = blocks.get(start+1);
			
			for (Delta delta : target.deltas) merged.deltas.add(delta);
			for (String line : target.lines) merged.lines.add(line);
			blocks.remove(start+1);
		}
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
	
	private void printChunk(Chunk c, List<String> buff) {
		for (Object o : c.getLines()) {
			buff.addAll(putFrame(o.toString()));
		}
	}
	
	/**
	 * 与えられた行に枠(罫線)をつけ、必要なら複数行にして返却します
	 */
	private List<String> putFrame(String target) {
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
	
	private static int widthInFixedPitch(String str) {
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
	private static String[] putDiffMark(Chunk original, Chunk revised) {
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
