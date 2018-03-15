import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

/**
 * MdLinkChecker テスト JUnit 5 ベース
 */
@DisplayName("MdLinkCheckerクラス")
class MdLinkCheckerTest {
	@Nested
	class コンストラクタ {
		MdLinkChecker md;
		
		@BeforeEach
		void init() throws Exception {
			md = new MdLinkChecker("http://hogehoge.com", "forTest");
		}
		//
		@Test
		void コンストラクタ設定() throws Exception {
			assertEquals("http://hogehoge.com/", md.rootUrl);
		}
		//
		@Test
		void ファイル読み込み() throws Exception {
			md.readAllLines(new File("./forTest/src/documents/html/d1.html.md"));
			MdLinkChecker.Entry<String, List<String>> e = md.lines.get(0);
			assertEquals("\\src\\documents\\html\\d1.html.md", e.key);
			assertEquals("[サンプルMDファイル, , コメントテスト<!-- コメント -->コメントテスト, , 複数行のコメント, hoge<!-- hoge hoge2, hoge 3 --> hoge4, , 一行に複数, 1<!-- 2 -->3<!--  3 -->4<!-- -- 5 - -->6<!--, てきとう -->てきとう<!-- 終わり  -->, , ]", e.value.toString());
		}
		//
		@Test
		void 全ファイル読み込み() throws Exception {
			md.read(md.docDir);
			MdLinkChecker.Entry<String, List<String>> e = md.lines.get(0);
			assertEquals("\\src\\documents\\html\\d1.html.md", e.key);
		}
		//
		@Test
		void cutCommentsテスト() throws Exception {
			md.readAllLines(new File("./forTest/src/documents/html/d1.html.md"));
			MdLinkChecker.Entry<String, List<String>> e = md.lines.get(0);
			md.cutComments(e);
			
			assertEquals("\\src\\documents\\html\\d1.html.md", e.key);
			assertEquals("[サンプルMDファイル, , コメントテストコメントテスト, , 複数行のコメント, hoge,  hoge4, , 一行に複数, 1346, てきとう, , ]", e.value.toString());		}
		//
		@Test
		void toUrlPathテスト() throws Exception {
			md.readAllLines(new File("./forTest/src/documents/html/d1.html.md"));
			MdLinkChecker.Entry<String, List<String>> e = md.lines.get(0);
			assertEquals("html/d1.html.md", md.toUrlPath(e.key));
		}
		
		@Test
		void cutCommentsまで通しテスト() throws Exception {
			md.read(md.docDir);
			md.cutComments();
			
			MdLinkChecker.Entry<String, List<String>> e = md.lines.get(0);
			assertEquals("\\src\\documents\\html\\d1.html.md", e.key);
			assertEquals("[サンプルMDファイル, , コメントテストコメントテスト, , 複数行のコメント, hoge,  hoge4, , 一行に複数, 1346, てきとう, , ]", e.value.toString());
			
		}
	}
	
	@Nested
	class パターンマッチング {
		MdLinkChecker md;
		String text;
		
		@BeforeEach
		void init() throws Exception {
			md = new MdLinkChecker("http://hogehoge.com", "forTest");
			text = "0123 [textlink1](http://hoge1.com) [ hoge ] ( bar ) [ \n1(2 ]3 )![imagelink1](http://hoge.com/images/fig1.png)\n <a href=\"http://hoge2.com\">hoge.com</a> <IMG SRC=\"https://bar.com/images/fig2.png\">";
		}
		
		@Test
		void MDリンクの正規表現() {
			List<String> toAdd = new ArrayList<String>();
			md.findImpl(text, toAdd, md.mdlink, md.pickString1, "");
			assertEquals("[http://hoge1.com]", toAdd.toString());
		}
		@Test
		void MD画像の正規表現() {
			List<String> toAdd = new ArrayList<String>();
			md.findImpl(text, toAdd, md.mdimage, md.pickString1, "");
			assertEquals("[http://hoge.com/images/fig1.png]", toAdd.toString());
		}
		@Test
		void HTMLリンクの正規表現() {
			List<String> toAdd = new ArrayList<String>();
			md.findImpl(text, toAdd, md.htmllink, md.pickString2, "");
			assertEquals("[http://hoge2.com]", toAdd.toString());
		}
		@Test
		void HTML画像の正規表現() {
			List<String> toAdd = new ArrayList<String>();
			md.findImpl(text, toAdd, md.htmlimage, md.pickString2, "");
			assertEquals("[https://bar.com/images/fig2.png]", toAdd.toString());
		}
	}
	
	@Nested
	class 実際の文書 {
		MdLinkChecker md;
		
		@BeforeEach
		void init() throws Exception {
			md = new MdLinkChecker("http://developer.ntt.com", "forTest2");
		}
		
		@Test
		void test() throws Exception {
			md.read(md.docDir);
//md.lines.stream().forEach( (e) -> {	System.out.println(e.key); } );
			md.pickImageFiles(md.imageDir);
			assertEquals(24, md.imageFiles.size());
//System.out.println("----- イメージファイル -----");
//md.imageFiles.stream().forEach(System.out::println);
			md.cutComments();
			assertEquals(244, md.lines.get(0).value.size());
//System.out.println("----- コメントカット後 -----");
//md.lines.get(0).value.stream().forEach(System.out::println);
			md.pickResources();
			assertEquals(24, md.resources.size());
//System.out.println("----- リソース -----");
//md.resources.stream().forEach(System.out::println);
			md.findTokens();
			assertEquals(10, md.links.size());
			assertEquals(21, md.linkedImages.size());
//System.out.println("----- リンク -----");
//md.links.stream().forEach(System.out::println);
//System.out.println("----- 画像リンク -----");
//md.linkedImages.stream().forEach(System.out::println);
			
			md.check();
		}
	}
	
}
