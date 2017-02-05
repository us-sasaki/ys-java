package misc;

import java.util.List;
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
		MarkExtractor me = new MarkExtractor();
		
		List<String> enMark = me.extract(originalDoc);
		List<String> jaMark = me.extract(originalTranslatedDoc);
		
		enJaMap = new LooseMap(enMark, jaMark);
		
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
	}
	
/*------------------
 * instance methods
 */
	public void diffForTranslate() {
		// 英文同士の差分を含む Block を検索する
		for (Delta delta : newOldDiff.getDeltas() ) {
			int deltaStart	= delta.getOriginal().getPosition();
			int deltaEnd	= delta.getOriginal().last();
			
			//
			// 以下、LooseMap を使っていないのでずれている！
			//
			
			// 差分を含む最初のインデックスを検索
			int startBlockIndex = 0;
			for (;startBlockIndex < blocks.size(); startBlockIndex++) {
				// 問題の比較。左辺は翻訳文の行番号、
				// 右辺は原文の行番号。
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
	
	public List<String> toText() {
		List<String> result = new ArrayList<String>();
		for (Block block : blocks) {
			if (block.deltas.size() > 0) {
				// 変更を含むブロック
				result.add("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
				result.add("↓■■■■要修正：更新されました。下を確認し、翻訳文を修正して下さい■■■■↓");
				int count = 1;
				for (Delta delta : block.deltas) {
					Chunk org = delta.getOriginal();
					Chunk rev = delta.getRevised();
					String s = "┃>>>>>>>> 原文更新前("+count+")：" + (org.getPosition()+1) + "行目";
					result.add(s);
					printChunk(org, result);
					result.add("┃<<<<<<<< 原文更新後("+count+")：" + (rev.getPosition()+1) + "行目");
					printChunk(rev, result);
					count++;
				}
				result.add("┣━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━┫");
				for (String line : block.lines) {
					result.add(line);
				}
				result.add("↑■■■■↑■■■■↑■■■■要修正：ここまで　■■■■↑■■■■↑■■■■↑");
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
			buff.add("┃" + o);
		}
	}
}
