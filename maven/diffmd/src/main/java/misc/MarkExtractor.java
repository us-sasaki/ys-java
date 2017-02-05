package misc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * 翻訳された Markdown と原文の各行間の大まかな対応を Markdown 書式文字列を
 * 抽出することにより把握するための便利関数です。
 * 将来的に Markdown 書式を現在抽出していますが、日本語と英語で変わらない
 * コード部分を抽出することにより行間の対応の粒度を細かくすることができます。
 *
 * @version		February 4, 2017
 * @author		Yusuke Sasaki
 */
class MarkExtractor {
	
	/**
	 * Markdown の先頭にある書式文字列を定義。
	 * extract では、この文字列で始まるものを、この文字列のみに mapping する。
	 * mapping は先頭のものが優先される
	 */
	static final List<String> HEAD_MARKS;
	static {
		HEAD_MARKS = Arrays.asList( new String[] {
			"# ","## ","### ","#### ","##### ",
			"* ","- ",
			"1. ","2. ","3. ","4. ","5. ", "6. ","7. ","8. ","9. ","10. ",
			"    ",
			"|:-", "|"
		} );
	}
	
	/**
	 * Markdown を英語/日本語によらない書式文字列に変換します。
	 * 1行を1行にマッピングします。
	 * これにより、異なる言語の２ファイルの書式が同一であるか、行の間の
	 * 対応関係を把握することができるようになります。
	 *
	 * @param		target	抽出元の文書の各行の List
	 * @return		抽出された書式文字列のみからなる各行の List。
	 *				行数と順序は元の文書と同一。
	 */
	List<String> extract(List<String> target) {
		List<String> result = new ArrayList<String>();
		for (String line : target) {
			int index = 0;
			for (String mark : HEAD_MARKS) {
				if (line.startsWith(mark)) break;
				index++;
			}
			if (index < HEAD_MARKS.size()) result.add(HEAD_MARKS.get(index));
			// 空行は乱数に置き換え
			// (HEAD_MARKSと違い、対応性が保証されないと仮定)
			else if (line.equals("")) result.add(""+Math.random());
			// 通常の文章はそのまま
			// 対応性が保証される場合(コード部分など)がある
			else result.add(line);
			//else result.add("sentence:"+Math.random());
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath("ja-netcommwireless.html.md"), Charset.defaultCharset());
        
        MarkExtractor me = new MarkExtractor();
        
        List<String> extracted = me.extract(lines);
        for (String line : extracted) {
        	System.out.println(line);
        }
	}
}
