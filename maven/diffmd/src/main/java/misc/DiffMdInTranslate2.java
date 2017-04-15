package misc;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Chunk;

/**
 * Text のフォーマットを変えたサブクラスです。
 */
public class DiffMdInTranslate2 extends DiffMdInTranslate {
	
/*-------------
 * constructor
 */
	public DiffMdInTranslate2(List<String> originalDoc,
							List<String> revisedDoc,
							List<String> originalTranslatedDoc) {
		super(originalDoc, revisedDoc, originalTranslatedDoc);
	}
	
/*------------------------
 * テキスト出力メソッド群
 */
	/**
	 * 結果をテキスト形式で出力します。テキスト形式は等幅フォントを利用して
	 * 見ることを前提とします。
	 */
	@Override
	public List<String> toText() {
		List<String> result = new ArrayList<String>();
		for (Block block : blocks) {
			if (block.deltas.size() > 0) {
				// 変更を含むブロック
				//result.add("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
				result.add("//　　　　　　　更新箇所。原文を確認し、翻訳文を修正して下さい");
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
				result.add("//━━━━━━━━━━━　元の翻訳文(修正して下さい)　━━━━━━━━━━━");
				for (String line : block.lines) {
					result.add(line);
				}
				result.add("//　　　　↑　　　　↑　　　　要修正：ここまで　　　　　↑　　　　↑　　　　┃");
				//result.add("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
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
			sb.append("//"); width += 2;
			while (true) {
				sb.append(token[tokenIndex]);
				width += widthInFixedPitch(token[tokenIndex]);
				int nextW = (tokenIndex == token.length - 1)?maxWidth:width+3+widthInFixedPitch(token[tokenIndex+1]);
				if (nextW >= maxWidth) {
					// 次の単語を追加するとはみ出すとき
					int len = maxWidth - width - 2;
					len = (len < 0)?0:len;
					sb.append("                                                                                ".substring(0, len));
					//sb.append("┃");
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
	
}
