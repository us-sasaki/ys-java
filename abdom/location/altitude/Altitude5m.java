package abdom.location.altitude;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
//import java.util.regex.Matcher;
//import abdom.location.altitude.AltitudeMesh3;

/**
 * 国土交通省　国土地理院　基盤地図情報　ダウンロードサービスにおける、
 * 5m メッシュ
 * http://fgd.gsi.go.jp/download/menu.php
 * 基盤地図情報　数値標高モデル
 * 上記データを読み込み、緯度、経度情報から標高を返却するAPIを作成する。
 * このデータは膨大(江別、八王子近辺だけで1G程度)なため、OutOfMemory を
 * 出さないよう、動的にファイルを読み、読まなくなった領域は開放するような
 * つくりとすべき。
 *
 * 2016/10/25
 * データ数が必ずしも 150*225 個ない場合があることが分かったため、修正。
 * 後のデータが省略されることがある。前のデータが省略される場合、データの
 * 「後に」ついている startPoint からはじまっている。
 * 後者のため、いったんデータを読み込んだ後にずらす処理が入ることがある。
 * データ読み込みは回数が少ないからいいか。
 *
 * テスト用データ
 * 35度37分53.95秒 139度22分13.58秒
 * 35.631652,139.370440  
 * 高度131m
 *
 *
 * @author	Yusuke Sasaki
 * @version	23, October 2016
 */
public class Altitude5m {
	/**
	 * <1次メッシュ><2次メッシュ><3次メッシュ> を数値化した値　→　標高
	 * のテーブル
	 * 数値化規則：
	 * <pre>国土数値情報　ダウンロードサービス より
	 * メッシュコードについて
	 * メッシュコードは、メッシュデータの各区域に対し割り振られたコードで、以下の
	 * 規約に従っています。
	 * 第1次地域区画は4桁のコードで識別され、上2桁は、メッシュの南西端の緯度を
	 * 1.5倍した数字、下2桁は同じ点の経度の下2桁の数です。
	 * 第2次メッシュ区画の位置は、それの属する1次メッシュ区画を行列に見立てると、
	 * 南から北に向けて0から7まで振られた行番号と西から東に向けて0から7まで振られ
	 * た列番号を組み合わせた番号をそれの属する1次メッシュコードに続けて示されます。
	 * 第3次メッシュ区画の位置は、それの属する2次メッシュ区画を行列に見立てると、
	 * 南から北に向けて0から9まで振られた行番号と西から東に向けて0から9まで振られ
	 * た列番号を組み合わせた番号をそれの属する2次メッシュコードに続けて示されます。
	 * 例えば 5438-23-23 という3次メッシュコード（基準地域メッシュコード）は5438と
	 * いう1次地域区画 中の南から3番目西から4番目にある2次地域区画中のさらに南から
	 *  3番目西から 4番目の 3次地域区画を示していることになります。
	 * </pre>
	 * 上記の８つの数字をならべ、整数と見なしたものがキーとなる。
	 */
	protected Map<Integer, Float2D> table;
	protected Map<Integer, String> fileIndex; // ファイルpathのテーブル
	
	protected static Altitude5m theInstance = null;
	
	/**
	 * Float[][] とするとメモリを食うため、float[][] をラッピング
	 *
	 * 構成点の配列順序をCV_SequenceRuleデータ型で表す。
	 * 基盤地図情報では，type属性値=”Linear”，scanDirection属性値="+x-y"
	 * と設定する。
	 * この設定値は，先頭セルは北西端にあって，配列順序がx軸の正方向（西→東の順）
	 * へ順に並んでおり，東端に達すると次に，y軸の負方向（北→南の順）に進む方式で
	 * 南東端に至る配列であることを示している。
	 * 将来的にメモリを節約する場合、short としても良いかもしれない。
	 *
	 * float の添え字順は、[x(lng)][y(lat)] とする。
	 */
	protected static class Float2D {
		protected float[][] d;
	}
	
/*-------------
 * constructor
 */
	protected Altitude5m() {
		table = new TreeMap<Integer, Float2D>();
		fileIndex = new TreeMap<Integer, String>();
	}
	
/*---------------
 * class methods
 */
	public static Altitude5m getInstance() {
		if (theInstance != null) return theInstance;
		try {
			theInstance = new Altitude5m();
			theInstance.makeFileIndex("G:\\programs\\abdom\\location\\altitude\\PackDLMap5m");
			return theInstance;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}
	
/*------------------
 * instance methods
 */
	/** ファイル名に関するコンパイル済み Pattern */
	private static final Pattern p = Pattern.compile("FG-GML-[0-9]{4}-[0-9]{2}-[0-9]{2}-.+\\.(?i)xml$");
	
	/**
	 * 指定されたディレクトリ以下のすべてのファイルについてインデックスを
	 * 作成します。
	 */
	private void makeFileIndex(String dirname) throws IOException {
		File f = new File(dirname);
		if (f.isDirectory()) {
			String[] list = f.list();
			if (list == null) return;
			for (String s : list) {
				makeFileIndex(f+"/"+s);
			}
		} else {
			// file FG-GML-nnnn-nn-nn-*.xml の形式のもの
			String name = f.getName();
			if (p.matcher(name).matches()) {
//System.out.println(f.getName() + " is found.");
				// ファイル名インデックスにフルパスを追加
				int idx = Integer.parseInt(name.substring(7,11)
										 + name.substring(12,14)
										 + name.substring(15,17) );
				// absolute path なので、文字列が多い。相対にすることで
				// メモリの削減ができる。
				String prev = fileIndex.put(idx, f.getAbsolutePath());
				if (prev != null) {
					System.out.println("同一メッシュコードのファイルがあります。旧"+prev+" 新"+f.getAbsolutePath());
				}
			}
		}
	}
	
	/**
	 * 指定された tag を含む行をスキャンします。
	 * tag があった場合、内容(tag から終了タグ、または行末までの文字列)
	 * を返却します。
	 * tag がない場合、null を返却します。
	 * BufferedReader は tag を含む行の次の行(ない場合EOF)に移動します。
	 */
	private String scan(BufferedReader r, String tag) throws IOException {
		String line;
		String t1 = "<"+tag+">";
		String t2 = "</"+tag+">";
		while ( (line = r.readLine()) != null) {
			int idx1 = line.indexOf(t1);
			if (idx1 == -1) continue;
			int idx2 = line.indexOf(t2);
			if (idx2 == -1) return line.substring(idx1 + t1.length());
			return line.substring(idx1 + t1.length(), idx2);
		}
		return null;
	}
	
	/**
	 * Reader から tag タグを抽出し、内容が value に一致することを
	 * 確認します。 value が null の場合、一致チェックは行いません。
	 *
	 * @param	r	読み込み元 Reader
	 * @param	fname	読み込みファイル名(例外メッセージ用)
	 * @param	tag		検索するタグ
	 * @param	value	一致チェック比較用文字列
	 * @return	tag タグの内容
	 */
	private String check(BufferedReader r, String fname, String tag, String value)
										throws IOException {
		String v = scan(r, tag);
		if (v == null) {
			throw new RuntimeException(fname + " ファイルに "+tag+" フィールドがありません。");
		}
		if (  (value != null)&&(!v.equals(value))  ) {
			throw new RuntimeException(fname + " ファイルの "+tag+" が "+value+" ではありません。("+v+")");
		}
		return v;
	}
	
	/**
	 * xml ファイルを読み込み、table に追加します。
	 * xml ファイルは 5m メッシュ（標高）で、low 0 0, high 224 149 である必要が
	 * あります。
	 */
	private void read(String fname) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fname), "MS932"));
		
		String filename = new File(fname).getName();
		
		try {
			// type フィールドが 5mメッシュ（標高） となっていることを確認します。
			check(r, fname, "type", "5mメッシュ（標高）");
			
			// lowerCorner, upperCorder を読み込みます。
			String lowerCorner = check(r, fname, "gml:lowerCorner", null);
			String upperCorner = check(r, fname, "gml:upperCorner", null);
			
			// low フィールドが 0 0 となっていることを確認します。
			check(r, fname, "gml:low", "0 0");
			// high フィールドが 224 149 となっていることを確認します。
			check(r, fname, "gml:high", "224 149");
			
			// 
			String s = scan(r, "gml:tupleList");
			if (s == null) throw new RuntimeException("データボディ(gml:tupleList)がありません");
			
			boolean ended = false;
			float[][] d = new float[225][150];
			for (int y = 149; y >= 0; y--) {
				for (int x = 0; x < 225; x++) {
					if (ended) {
						d[x][y] = -0.5f; // 海と見なす
						continue;
					}
					String line = r.readLine();
					int comma = line.indexOf(',');
					if (comma == -1) {
						if (!"</gml:tupleList>".equals(line))
							throw new RuntimeException("データ形式異常 ファイル名:"+
									fname+" read:"+line+" index(x+(149-y)*225):"+
									(x+(149-y)*225));
						// 終わる場合がある
						ended = true;
						d[x][y] = -0.5f; //　海とみなす
						continue;
					}
					d[x][y] = Float.parseFloat(line.substring(comma+1));
					if (d[x][y] < 0) {
						String type = line.substring(0,comma);
						switch (type) {
						
						case "地表面":
							d[x][y] = 0.5f;
							break;
						case "表層面":
							d[x][y] = 1.5f;
							break;
						case "海水面":
							d[x][y] = -0.5f;
							break;
						case "内水面":
							d[x][y] = 40.5f;
							break;
						case "データなし":
							d[x][y] = 0.95f;
							break;
							
						case "その他":
							d[x][y] = 2.5f;
							break;
						
						default:
							throw new RuntimeException("データタイプ異常 ファイル名:"+fname+" read:"+line);
						}
					}
				}
			}
			// sequenceRule が存在し、order +x-y, Linear であることを確認します
			check(r, fname, "gml:sequenceRule order=\"+x-y\"","Linear</gml:sequenceRule>");
			// gml:startPoint を取得します
			String startPoint = scan(r, "gml:startPoint");
			if (startPoint == null) throw new RuntimeException("gml:startPoint がありません:"+fname);
			if (!"0 0".equals(startPoint)) {
				// 並べ替え  (0 0)でない頻度は少ないと思われる
				String[] xy = startPoint.split(" ");
				int x = Integer.parseInt(xy[0]);
				int y = Integer.parseInt(xy[1]);
				int offset = x + (149-y) * 225;
				for (int i = 150*225-1; i >= offset; i--) {
					int dx = i % 225;
					int dy = 149 - (i / 225);
					int sx = (i-offset) % 225;
					int sy = 149 - ((i-offset) / 225);
					d[dx][dy] = d[sx][sy];
				}
				for (int i = 0; i < offset; i++) {
					int sx = i % 225;
					int sy = 149 - (i / 225);
					d[sx][sy] = -0.5f; // 海と見なす
				}
			}
			// ファイル名インデックス
			int idx = Integer.parseInt(filename.substring(7,11)
									 + filename.substring(12,14)
									 + filename.substring(15,17) );
			Float2D f = new Float2D();
			f.d = d;
			Float2D previous = table.put(idx, f);
			if (previous != null) {
				System.out.println("table重複登録を検出しました。" + idx);
				for (int x = 0; x < 225; x++) {
					for (int y = 0; y < 150; y++) {
						if (previous.d[x][y] != f.d[x][y]) {
							System.out.println("内容が異なっています");
							break;
						}
					}
				}
			}
			
		} catch (RuntimeException re) {
			System.out.println(re.toString() + " ファイル読込をスキップします");
		}
		r.close();
	}
	
	/**
	 * 
	 */
	public float getAltitude(double latitude, double longitude) {
		//
		// メッシュコードを求める
		//
		
		// １次メッシュコードの値を求める
		int higher = (int)(latitude*1.5);
		int lower  = ((int)(longitude))%100;
		int m1 = higher * 100 + lower;
		
		double latRest = latitude*1.5 - higher; // 0<= latRest < 1
		double lngRest = longitude - (int)longitude; // 0<= lngRest <1
		
		// ２次メッシュコードの値を求める
		latRest *= 8d;
		lngRest *= 8d;
		int m2 = ((int)(latRest))*10 + (int)(lngRest);
		latRest -= (int)latRest;
		lngRest -= (int)lngRest;
		
		// ３次メッシュコードの値を求める
		latRest *= 10d;
		lngRest *= 10d;
		int m3 = ((int)(latRest))*10 + (int)(lngRest);
		latRest -= (int)latRest;
		lngRest -= (int)lngRest;
		
		// table にすでに読み込み済みか
//System.out.println("mesh:"+(m1*10000+m2*100+m3));
		Float2D f = table.get(m1*10000+m2*100+m3);
		if (f == null) {
			// 読み込み済みでない場合、fileIndex を用いてファイル読込
			String fname = fileIndex.get(m1*10000+m2*100+m3);
			if (fname == null) {
				// 該当ファイルがない場合、AltitudeMesh3の値を返却
				return AltitudeMesh3.getInstance().getAltitude(latitude, longitude);
			}
			// ファイル読込、table 追加
			try {
				read(fname);
			} catch (IOException ioe) {
				throw new RuntimeException("fileIndex にあるファイル:" + fname + " で異常が発生しました:"+ioe);
			}
			// 再取得(fileIndex にファイルがあることがわかっているため、通常絶対に
			// 値があるはず)
			f = table.get(m1*10000+m2*100+m3);
			if (f == null)
				// しかし、値がなかった( = 読み込み失敗したファイルだったか、
				// 処理中にアクセスできなくなった)
				throw new RuntimeException("fileIndex と実際のファイルに不整合を検出しました。メッシュコード:" + (m1*10000+m2*100+m3));
		}
		// Float2D から該当の値を抽出して返却
		lngRest *= 225; // 0-224
		latRest *= 150; // 0-149
		
		return f.d[(int)lngRest][(int)latRest];
	}

/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		Altitude5m a = Altitude5m.getInstance();
		System.out.println("家の高度(5m   ) : " + a.getAltitude(35.631697, 139.370460));
		AltitudeMesh3 b = AltitudeMesh3.getInstance();
		System.out.println("家の高度(mesh3) : " + b.getAltitude(35.631697, 139.370460));
	}
}
