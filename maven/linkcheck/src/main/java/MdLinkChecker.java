import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * md ファイルを読み込み、以下処理をします。<pre>
 *  (1) 外部リンクのリストを生成
 *  (2) 有効でない内部リンクのリストを生成
 *  (3) リンクされていない画像のリストを生成
 * 
 * ただし、<!-- ... --> のコメント部分は除く
 * リンクは []() または <A HREF= のいずれかを対象とする。
 * 画像は ![]() または <IMG SRC= のいずれかを対象とする。
 * </pre>
 * 
 * @version		March 5, 2018
 * @author		Yusuke Sasaki
 */
public class MdLinkChecker {
	private static final String ENCODING = "UTF-8";
	
	/**
	 * 公開時のベースURL(https://developer.ntt.com/ 等)
	 * 内部的にスラッシュで終わる必要があるため、ない場合コンストラクタで
	 * 追記します。
	 */
	protected String rootUrl;
	
	/**
	 * src ディレクトリ
	 * (C:/Users/user/Documents/gitlab/markdown/ 等)
	 */
	protected File srcDir;
	protected String srcPath;
	protected File docDir;
	protected File imageDir;
	
	/**
	 * この文書に含まれるリソースのリスト。リソースとは、<br>
	 * (1) md(html)ファイル (*.html)<br>
	 * (2) (1) の .html を省略したもの<br>
	 * (3) 文書中のタグ(a name) ( http://.../introduction#hoge など)<br>
	 *
	 * 値は一意にするため、フルパス形式とします。
	 */
	protected List<String> resources;
	
	/**
	 * この文書ディレクトリに含まれる画像のリスト
	 * 画像は、URL のフルパス指定とします。
	 */
	protected List<String> imageFiles;
	
	/**
	 * この文書からリンクされている画像リンクのリスト
	 * 値は URL のフルパス指定とします。
	 */
	protected List<String> linkedImages;
	
	/**
	 * この文書に含まれるリンクのリスト
	 * 値は http: などではじまる URL です。
	 */
	protected List<String> links;
	
	/**
	 * ファイルの Canonical Path(からsrcPathを取ったもの) -> mdの全行
	 * 順序保存したいため、Map.Entry の List とする。
	 */
	protected List<Entry<String, List<String>>> lines;
	
/*------------
 * 内部クラス
 */
	protected static class Entry<K, V> {
		protected K key;
		protected V value;
	}
	
/*-------------
 * constructor
 */
	public MdLinkChecker(String rootUrl, String srcDir) throws IOException {
		if (!rootUrl.endsWith("/")) rootUrl = rootUrl + "/";
		this.rootUrl = rootUrl;
		this.srcDir = new File(srcDir);
		// srcDir がディレクトリであることのチェック
		if (!this.srcDir.isDirectory())
			throw new IllegalArgumentException("ソースディレクトリ(" + srcDir + ") はディレクトリを指定してください");
		
		// srcDir に src ディレクトリが含まれていることのチェック
		if (!Arrays.asList(this.srcDir.list()).contains("src") ||
				!new File(this.srcDir, "src").isDirectory()) {
			throw new IllegalArgumentException("ソースディレクトリ(" + srcDir + ") 内には src ディレクトリが見つかりません");
		}
		this.srcPath	= this.srcDir.getCanonicalPath();
		
		this.docDir		= new File(this.srcDir, "src/documents");
		this.imageDir	= new File(this.srcDir, "src/files");
		
		this.resources	= new ArrayList<String>();
		this.linkedImages = new ArrayList<String>();
		this.imageFiles	= new ArrayList<String>();
		this.links		= new ArrayList<String>();
		this.lines		= new ArrayList<Entry<String, List<String>>>();
		
	}
	
/*------------------
 * instance methods
 */
	public void exec() throws IOException {
		read(docDir);
		pickImageFiles(imageDir);
		cutComments();
		pickResources();
		findTokens();
		check();
	}
	
	/**
	 * 指定されたディレクトリの markdown(*.html.md) を読み込み、lines
	 * に格納していきます。ファイル検索は再帰的に行います。
	 * *.html.md 以外のファイルはスキップされます。
	 */
	void read(File dir) throws IOException {
		String[] list = dir.list();
		for (String f : list) {
			File file = new File(dir, f);
			if (file.isDirectory()) {
				read(file);
				continue;
			}
			String name = file.getCanonicalPath();
			if (name.endsWith(".html.md")) readAllLines(file);
		}
	}
	
	/**
	 * 指定された File の全行を読み込み、ファイルパスとともに Entry として
	 * lines に追加します。
	 * 
	 * Entry の形式は次の通り：<br>
	 * key: canonicalPath のうち、srcPath からの相対パスを格納。￥開始。<br>
	 * value: md の各行の List<br>
	 * 
	 */
	void readAllLines(File file) throws IOException {
		Entry<String, List<String>> e = new Entry<String, List<String>>();
		e.key = file.getCanonicalPath();
		if (!e.key.startsWith(srcPath))
			throw new InternalError("e.key が srcPath の子ディレクトリではありません。e.key=" + e.key + " srcPath=" + srcPath);
		// srcPath からの相対パスにする。
		e.key = e.key.substring(srcPath.length());
		e.value = Files.readAllLines(file.toPath(), Charset.forName(ENCODING));
		lines.add(e);
	}
	
	/**
	 * HTML コメント( <!-- ～ --> )をカットします。
	 */
	void cutComments() {
		// 全ファイル
		for (Entry e : lines) {
			cutComments(e);
		}
	}
	
	/**
	 * 指定された Entry に対し、コメントカットします。
	 * Entry.value は変更されます。
	 */
	void cutComments(Entry<String, List<String>> e) {
		boolean inComment = false;
		// 各行に対する操作
		for (int i = 0; i < e.value.size(); i++) {
			String line = e.value.get(i);
			int index = 0;
			
		lp:
			while (true) {
				int lastIndex = index;
				if (!inComment) {
					index = line.indexOf("<!--", lastIndex);
					if (index == -1) {
						break lp;
					} else {
						inComment = true;
						lastIndex = index;
					}
				}
				if (inComment) {
					index = line.indexOf("-->", lastIndex);
					if (index == -1) {
						line = line.substring(0, lastIndex);
						break lp; // go to next line
					} else {
						line = line.substring(0, lastIndex) +
								line.substring(index + 3);
						index = lastIndex;
						inComment = false;
					}
				}
			}
			e.value.set(i, line);
		}
	}
	
	void pickResources() {
		for (Entry<String, List<String>> e : lines) {
			pickResources(e);
		}
	}
	
	/**
	 * markdown 内にある URL リソースを抽出します。
	 * 
	 */
	void pickResources(Entry<String, List<String>> e) {
		// このファイル自身(.html)を追加
		String htmlPath = rootUrl + toUrlPath(e.key.substring(0, e.key.length()-3));
		resources.add(htmlPath);
		// このファイル自身の .html を省略したものを追加
		String htmlPath2 = rootUrl + toUrlPath(e.key.substring(0, e.key.length()-8));
		resources.add(htmlPath2);
		
		// a タグがあるか見る
		for (String line : e.value) {
			if (line.indexOf("< ") >= 0) {
				int indexTName = line.indexOf("< ");
				indexTName += 2;
				while( line.charAt(indexTName++)==' '
						 && indexTName<=line.length() ) {}
				if (line.charAt(indexTName-1) == 'a'
					|| line.charAt(indexTName-1) == 'A')
						throw new RuntimeException("\"< a\"を検出しました。path="+e.key+" line="+line);
			}
			int index = line.indexOf("<a ");
			if (index == -1) index = line.indexOf("<A ");
			if (index >= 0) {
				// <a か <A がある
				int ind2 = line.indexOf("name", index);
				if (ind2 == -1) ind2 = line.indexOf("id=", index);
				if (ind2 > -1) {
					// <a name or <a id= の後
					// " を待つ
					ind2 = line.indexOf("\"", ind2);
					if (ind2 == -1) {
						System.out.println("<a name/id の後に \" がありませんpath="+e.key+" line="+line);
						continue;
					}
					int ei = line.indexOf("\"", ind2+1);
					String res = line.substring(ind2+1, ei);
					resources.add(htmlPath + "#" + res);
					resources.add(htmlPath2 + "#" + res);
					if (line.indexOf("<a", ei) > -1)
						throw new Error("warn: 2つ目の<a があります:" +
											e.key + ":" + line);
					if (line.indexOf("<A", ei) > -1)
						throw new Error("warn: 2つ目の<A があります:" +
											e.key + ":" + line);
					continue;
				}
				// name ではない
			}
		}
		
	}
	
	/**
	 * 指定されたディレクトリ以下にあるファイルを imageFiles に格納します。
	 * ファイルすべてが登録されるため、画像ファイルでない場合があります。
	 *
	 * @param		dir		対象ディレクトリ
	 */
	void pickImageFiles(File dir) throws IOException {
		String[] list = dir.list();
		for (String f : list) {
			File file = new File(dir, f);
			if (file.isDirectory()) {
				pickImageFiles(file);
				continue;
			}
			String name = file.getCanonicalPath();
			if (!name.startsWith(srcPath))
				throw new InternalError("name が srcPath の子ディレクトリではありません。name=" + name + " srcPath=" + srcPath);
			name = name.substring(srcPath.length());
			imageFiles.add(rootUrl + toUrlPath(name));
		}
	}
	
	/**
	 * lines に含まれる全てのリンク、画像リンクを抽出します。
	 * read / cutComments の後に呼ぶ必要がある。
	 */
	void findTokens() {
		for (Entry e : lines) {
			findTokens(e);
		}
	}
	
	/**
	 * 指定された Entry に含まれるリンク、画像リンクを抽出します。
	 * read / cutComments の後に呼ぶ必要がある。
	 *
	 * @param	e	対象の Entry (mdファイル)
	 */
	void findTokens(Entry<String, List<String>> e) {
		StringBuilder t = new StringBuilder();
		e.value.stream().forEachOrdered( (a) -> {
				t.append(a);
				t.append("\n");
			});
		String text = t.toString();
		
		// 検出 []() リンク
		findMdLink(text, e.key);
		
		// 検出 <a href= リンク
		findHtmlLink(text, e.key);
		
		// 検出 ![]() リンク
		findMdImage(text, e.key);
		
		// 検出 <img src= リンク
		findHtmlImage(text, e.key);
		
	}
	
	//
	// RegEx
	//
	static String urlStringRegEx1 = "\\([\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+\\)";
	static String urlStringRegEx2 = "\\\"[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+\\\"";
	
	static Pattern mdlink = Pattern.compile("[^!]\\[.+\\]"+urlStringRegEx1);
	static Pattern mdimage = Pattern.compile("!\\[.+\\]"+urlStringRegEx1);
	static Pattern htmllink = Pattern.compile("<\\s*(?i)a(?-i)\\s+(?i)href(?-i)="+urlStringRegEx2);
	static Pattern htmlimage = Pattern.compile("<\\s*(?i)img(?-i)\\s+(?i)src(?-i)="+urlStringRegEx2);
	static Pattern pickString1 = Pattern.compile(urlStringRegEx1);
	static Pattern pickString2 = Pattern.compile(urlStringRegEx2);
	
	/**
	 * text に含まれる、markdown 形式のリンク[..](..)を抽出し、links
	 * に追加します。
	 *
	 * @param		text	対象文書テキスト(markdown)
	 * @param		key		markdown の格納されている相対パス
	 */
	void findMdLink(String text, String key) {
		findImpl(text, links, mdlink, pickString1, key);
	}
	
	/**
	 * text に含まれる、markdown 形式の画像リンク ![..](..)を抽出し、
	 * linkedImages に追加します。
	 *
	 * @param		text	対象文書テキスト(markdown)
	 * @param		key		markdown の格納されている相対パス
	 */
	void findMdImage(String text, String key) {
		findImpl(text, linkedImages, mdimage, pickString1, key);
	}
	
	/**
	 * text に含まれる、html 形式のリンク ＜a href="..."＞ を抽出し、
	 * links に追加します。
	 *
	 * @param		text	対象文書テキスト
	 * @param		key		markdown の格納されている相対パス
	 */
	void findHtmlLink(String text, String key) {
		findImpl(text, links, htmllink, pickString2, key);
	}
	
	/**
	 * text に含まれる、html 形式の画像リンク ＜img src="..."＞ を抽出し、
	 * linkedImages に追加します。
	 *
	 * @param		text	対象文書テキスト
	 * @param		key		markdown の格納されている相対パス
	 */
	void findHtmlImage(String text, String key) {
		findImpl(text, linkedImages, htmlimage, pickString2, key);
	}
	
	/**
	 * 正規表現を用いて、text からタグを抽出し、タグ内の該当部分を
	 * 指定された List に追加します。
	 * 
	 * @param		text	対象文書テキスト
	 * @param		toAdd	追加先の List
	 * @param		tagPicker	タグを抽出する正規表現(Pattern)
	 * @param		linkPicker	タグから List に追加する部分を抽出するのに
	 *							用いられる正規表現(Pattern)
	 * @param		key		markdown の格納されている相対パス
	 */
	void findImpl(String text,
					List<String> toAdd,
					Pattern tagPicker,
					Pattern linkPicker,
					String key) {
		Matcher m = tagPicker.matcher(text);
		while (m.find()) {
			String picked = m.group();
			Matcher link = linkPicker.matcher(picked);
			link.find();
			String s = link.group();
			toAdd.add(toFullUrl(s.substring(1, s.length()-1), key));
		}
	}
	
	/**
	 * 指定された srcPath からの相対ファイルパスを、rootUrl からの相対URLパス
	 * (/ではじまらない)に変換します
	 */
	static final String DOC_PATH = "/src/documents/";
	static final String IMG_PATH = "/src/files/";
	
	/**
	 * srcPath からの相対ファイルパスを rootUrl からの相対 URL パスに
	 * 変換します。ファイルパスは、文書パスとファイルパスの2通りがありますが、
	 * いずれも変換します。
	 * filepath が /src/documents/ /src/files/ のいずれにも含まれない場合、
	 * IllegalArgumentException をスローします。
	 * markdown ファイルのパスを指定した場合、/...../hoge.html.md
	 * のように、実際には参照できない .html.md つきの結果になることに
	 * 注意して下さい。
	 *
	 * @param	filepath	変換対象の srcPath からの相対ファイルパス
	 */
	String toUrlPath(String filepath) {
		String path = filepath.replace(System.getProperty("file.separator"), "/");
		if (path.startsWith(DOC_PATH))
			return path.substring(DOC_PATH.length());
		if (path.startsWith(IMG_PATH))
			return path.substring(IMG_PATH.length());
		throw new IllegalArgumentException("指定された filepath("+filepath+
					"が、src/documents/ src/files/ のいずれにも含まれません。");
	}
	
	/**
	 * 指定された URL 文字列を http:// ではじまるフルURL に変換します。
	 * もともとフルURL だった場合は何もしません。
	 *
	 * @param		url		変換対象のURL
	 * @param		key		markdown の格納されている相対パス
	 */
	String toFullUrl(String url, String key) {
		// url をフルURL化
		if (url.startsWith("http://")) { // すでにフルURL
		} else if (url.startsWith("https://")) {
		} else if (url.startsWith("/")) { // 相対URL
			url = rootUrl + url.substring(1); // / を1つ減らす
		} else if (url.startsWith("#")) { // アンカー
			String u = toUrlPath(key);
			if (u.endsWith(".html.md")) u = u.substring(0,u.length()-8);
			String base = rootUrl + u;
			url = base + url;
		} else if (url.startsWith("localhost:")) { // localhost
		}
		return url;
	}
	
	/**
	 * md ファイルを読み込み、以下処理をします。<pre>
	 *  (1) 外部リンクのリストを生成
	 *  (2) 有効でない内部リンクのリストを生成
	 *  (3) リンクされていない画像のリストを生成
	 */
	void check() {
		// (1) 外部リンクのリストを生成
		List<String> outerLinks = new ArrayList<String>();
		
		for (String link : links) {
			if (!link.startsWith(rootUrl))
				outerLinks.add(link);
		}
		
		// (2) 有効でない内部リンクのリストを生成
		List<String> absentLinks = new ArrayList<String>();
		Set<String> resourseSet = resources.stream().collect(Collectors.toSet());
		
		for (String link : links) {
			if (link.startsWith(rootUrl) && !(resourseSet.contains(link)))
				absentLinks.add(link);
		}
		// (3) リンクされていない画像のリストを生成
		//     同時に、使われていない画像ファイルのリストを生成
		List<String> absentImages = new ArrayList<String>();
		List<String> nolinkImages = new ArrayList<String>();
		Set<String> ifile = imageFiles.stream().collect(Collectors.toSet());
		Set<String> limage = linkedImages.stream().collect(Collectors.toSet());
		
		for (String image : imageFiles) {
			if (!limage.contains(image)) // どこからも使われていないファイル
				nolinkImages.add(image); 
		}
		for (String image : linkedImages) {
			if (!ifile.contains(image)) // リンクされているが存在しない
				absentImages.add(image);
		}
		
		// 表示する
		System.out.println("-------- 外部リンク --------");
		System.out.println("総数 : " + outerLinks.size());
		outerLinks.stream().forEach(System.out::println);
		
		System.out.println("-------- 有効でない内部リンク --------");
		System.out.println("総数 : " + absentLinks.size());
		absentLinks.stream().forEach(System.out::println);
		
		System.out.println("-------- リンク切れ画像(要追加) --------");
		System.out.println("総数 : " + absentImages.size());
		absentImages.stream().forEach(System.out::println);
		
		System.out.println("-------- 使われていない画像ファイル(削除可) --------");
		System.out.println("総数 : " + nolinkImages.size());
		nolinkImages.stream().forEach(System.out::println);
	}
}
