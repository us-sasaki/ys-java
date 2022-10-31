import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * md ファイルを読み込み、リンクが切れていないかチェックする。
 * http で始まるものは外部リンクとして
 *
 */
public class MdLinkChecker {
	private static final String ENCODING = "UTF-8";
	
	protected String rootUrl;
	protected String base; // ファイルのルートディレクトリ
	protected File basefile;
	
	protected List<String> resources;
	protected List<Link> links;
	protected Set<String> imageResources;
	
	protected static class Link {
		String link;
		String fname;
		String line;
	}
	
/*-------------
 * constructor
 */
	public MdLinkChecker(String rootUrl, String base) {
		this.rootUrl = rootUrl;
		this.base = base;
		this.basefile = new File(base);
		
		resources = new ArrayList<String>();
		links = new ArrayList<Link>();
		imageResources = new TreeSet<String>();
	}
	
	/**
	 * base をルートパス、fname を相対パスとして md ファイルを読み込む
	 *
	 * @param	fname	mdファイルの相対パス
	 */
	public void read(String fname) throws IOException {
		// 自身を登録
		if (!fname.endsWith(".md")) return;
		String htmlPath = fname.substring(0, fname.length()-3);
		String htmlPath2 = htmlPath.substring(0, htmlPath.length()-5);
		resources.add(rootUrl + htmlPath);
		resources.add(rootUrl + htmlPath2);
		
		// md ファイルを解析し、resource, link を追加していく
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(base + fname), ENCODING));
		
		while (true) {
			String line = br.readLine();
			if (line == null) break;
			
			//
			// <a があるかどうかを見る
			//
			if (line.indexOf("< ") > -1)
				System.out.println("warn: < がありました。resource 追加されません：" + base + fname + ":" + line);
			int index = line.indexOf("<a ");
			if (index == -1) index = line.indexOf("<A ");
			if (index >= 0) {
				// <a か <A がある
				// 注意 < a や < A 等は見逃される。regex を使うべき
				// name タグを見つける
				int ind2 = line.indexOf("name", index);
				if (ind2 == -1) ind2 = line.indexOf("id=", index);
				if (ind2 > -1) {
					ind2 = line.indexOf("\"", ind2);
					int ei = line.indexOf("\"", ind2+1);
					String res = line.substring(ind2+1, ei);
					resources.add(rootUrl + htmlPath + "#" + res);
					resources.add(rootUrl + htmlPath2 + "#" + res);
					if (line.indexOf("<a", ei) > -1)
						System.out.println("warn: 2つ目の<a があります:" + base + fname + ":" + line);
					if (line.indexOf("<A", ei) > -1)
						System.out.println("warn: 2つ目の<A があります:" + base + fname + ":" + line);
					continue;
				}
				// name ではない
				
			}
			index = line.indexOf("href");
			if (index > -1)
				System.out.println("warn: href があります：" + base + fname + ":" + line);
			
			// # ではじまる行は id(name) がつく
			if (line.startsWith("#")) {
				int i = line.indexOf(" ");
				if (i > -1) {
					resources.add(rootUrl + htmlPath + "#" + line.substring(i + 1, line.length()));
					resources.add(rootUrl + htmlPath2 + "#" + line.substring(i + 1, line.length()));
				}
				continue;
			}
			
			// ]( を探す
			index = 0;
			while (true) {
				index = line.indexOf("](", index+1);
				if (index == -1) break;
				// [ 以下はリンク先, ただし画像を除く
				int ind2 = line.indexOf(")", index);
				if (ind2 == -1) {
					System.out.println("err : ]( の後に ) が見つかりません：" + base + fname + ":" + line);
					continue;
				}
				String link = line.substring(index + 2, ind2);
				int exti = link.lastIndexOf(".");
				if (exti > -1) {
					// 画像の拡張子はリンクでない
					String ext = link.substring(exti);
					if (ext.equalsIgnoreCase(".png") ||
							ext.equalsIgnoreCase(".jpg") ||
							ext.equalsIgnoreCase(".gif") ||
							ext.equalsIgnoreCase(".svg") ) {
						imageResources.add(link);
						continue;
					}
				}
				if (link.startsWith("http")) {
					Link l = new Link();
					l.link = link;
					l.fname = fname;
					l.line = line;
					links.add(l); // フルパス
					System.out.println("外部link? : " + link + " : " + fname); //  + ":"+ line);
				} else if (link.startsWith("/iot/")) {
					Link l = new Link();
					l.link = rootUrl + link.substring(4);
					l.fname = fname;
					l.line = line;
					links.add(l); // 相対パス
				} else if (link.startsWith("/guides/")) { // これはダメパターン
					Link l = new Link();
					l.link = rootUrl + link;
					l.fname = fname;
					l.line = line;
					links.add(l); // 相対パス
				} else if (link.startsWith("localhost:")) {
					// link でも外部リンクでもない。スキップする。
				} else {
					Link l = new Link();
					l.link = rootUrl + htmlPath + link;
					l.fname = fname;
					l.line = line;
					links.add(l); // 相対パス
					if (!link.startsWith("/") && !link.startsWith("#")
						&& !link.startsWith("localhost:"))
						System.out.println("warn : link が /, #, localhost ではじまっていません。不正なURLを参照しています：" + base + fname + ":" + line);
				}
			}
		}
		br.close();
	}
	
	/**
	 * resources, links の内容でチェック
	 */
	public void check() {
		int missingLinks = 0;
		// すべての link(フルパス) に対し、resources があることを確認する。
		for (Link l : links) {
//			System.out.println("link : " + l.link);
			if (l.link.startsWith(rootUrl)) {
				// 内部リンク
				// check する
				boolean found = false;
				for (String resource : resources) {
					if (l.link.equalsIgnoreCase(resource)) {
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("------------------------------------");
					System.out.println("err : リンク切れ : " + l.link);
					System.out.println("      ファイル名 : " + l.fname);
					System.out.println("      該当行     : " + l.line);
					missingLinks++;
				}
			} else {
				// 外部リンク
				// 特に処理しない
			}
		}
		System.out.println("リンク切れ:" + missingLinks);
		System.out.println("リンクされているイメージファイル");
		
		for (String resource : imageResources) {
			System.out.println(resource);
		}
	}
	
	public void readDirectory() throws IOException {
		readDirImpl("");
	}
	
	private void readDirImpl(String path) throws IOException {
		File f = new File(basefile, path);
		if (!f.isDirectory()) throw new IOException("" + f + " はディレクトリではありません");
		String[] lists = f.list();
		for (String list : lists) {
			File file = new File(f, list);
			if (file.isDirectory()) {
				readDirImpl(path + "/" + list);
			} else {
				if (list.endsWith(".md")) {
					System.out.println("info : ファイルを読みます: " + path + "/" + list);
					read(path + "/" + list);
				} else {
					System.out.println("warn : ファイル読み込みをスキップします: " + path + "/" + list);
				}
			}
		}
	}
	
/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		MdLinkChecker mc = new MdLinkChecker("https://developer.ntt.com/iot", new File("C:\\Users\\Yusuke\\Documents\\GitHub\\gitlab\\markdown\\src\\documents\\iot").getCanonicalPath());
		
		mc.readDirectory();
		mc.check();
	}
}
