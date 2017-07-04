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

/**
 * 国土交通省　国土数値情報における、統一フォーマットの高度情報(３次メッシュ)
 * メタデータファイル識別子 : KS-META-G04-56M
 * http://nlftp.mlit.go.jp/ksj/
 * 旧 統一フォーマット形式 昭和56年度
 * 上記データを読み込み、緯度、経度情報から標高を返却するAPIを作成する。
 *
 * 凡例：
 *「I2」は整数型2桁、「A2」は文字型2桁を表します。
 *「N2」は全角漢字1文字を表します。漢字は、文字型2桁で1文字を表します。
 */
public class AltitudeMesh3 {
	protected List<Header> header;
	protected Map<Integer, Mesh> mesh;
	
	protected static AltitudeMesh3 theInstance = null;
	
/*-------------
 * constructor
 */
	protected AltitudeMesh3() {
		header	= new ArrayList<Header>();
		mesh	= new TreeMap<Integer, Mesh>();
	}
	
/*---------------
 * class methods
 */
	/**
	 * 指定した文字列の指定した位置の文字を数値と見なし、int として
	 * 返却します。指定位置の文字は String::trim() が施されます。
	 */
	protected static int parse(String s, int a, int b) {
		s = s.substring(a, b).trim();
		if ("".equals(s)) return 0;
		return Integer.parseInt(s);
	}
	
	/**
	 * データを保持するインスタンスは、このメソッドで取得して下さい。
	 */
	public static AltitudeMesh3 getInstance() {
		if (theInstance != null) return theInstance;
		try {
			theInstance = new AltitudeMesh3();
			theInstance.readAllFilesUnder("G:\\programs\\abdom\\location\\altitude\\data");
			return theInstance;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.toString());
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 指定されたディレクトリ以下のすべてのファイルを読み込みます。
	 * 読み込むファイルは、 G04-56M*.txt の形式のファイルです。
	 *
	 * @param	dirname	ファイル読込を行うトップディレクトリ
	 */
	private void readAllFilesUnder(String dirname) throws IOException {
		File f = new File(dirname);
		if (f.isDirectory()) {
			String[] list = f.list();
			if (list == null) return;
			for (String s : list) {
				readAllFilesUnder(f+"/"+s);
			}
		} else {
			// file "G04-56M*.txt" の形式のもの(extはcaps free)
			if (f.getName().matches("G04-56M.+\\.(?i)txt$")) {
				read(f.getAbsolutePath());
			}
		}
	}
	
	/**
	 * 指定されたファイルを読み込みます。
	 * 同一のファイル名がすでに読み込まれていた場合、スキップされます。
	 *
	 * @param	fname	ファイル名(フルパス可)
	 */
	private void read(String fname) throws IOException {
//System.out.println("reading.. " + fname);
		String filename = new File(fname).getName();
		for (Header h : header) {
			if (h.filename.equals(filename)) {
				System.out.println("すでに"+fname+"は読み込まれています。同一ファイル名の二重読み込みはできません。スキップします。");
				return;
			}
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
		String s = br.readLine();
		Header h = new Header(filename, s);
		header.add(h);
		int count = h.rows-1; // ヘッダ分除く
		
		// body部読み込み
		for (int i = 0; i < count; i++) {
			String line = br.readLine();
			if (line == null) {
				System.out.println(fname + "のヘッダ上、行が"+count+"あるはずですが、"+i+"行目が読み込めません。");
				break;
			}
			Mesh m = new Mesh(line);
			int index = m.m1*10000 + m.m2*100 + m.m3;
			
			Mesh lm = mesh.put(index, m);
			// おそらく県境にある情報は両方に入っており、大量に重複があるが、
			// データは同一なため、チェックしないこととする
//			if (lm != null) {
//				System.out.println("同一メッシュ(1～3次)の値が重複しています" + index + "追加mesh" + m + " 既存mesh" + lm);
//			}
		}
		
		// 残りがあるかどうかをチェック(ないはず)
		while (true) {
			String line = br.readLine();
			if (line == null) break;
			System.out.println(fname + "のヘッダ上、行が"+count+"しかないはずですが、余分な行があります。:"+line);
		}
		br.close();
	}
	
	/**
	 * 日本の与えられた地点の高さを平均値を用いてなめらか(線形)になるように算出します。
	 *<pre>
	 *        N
	 *   a    |    b
	 *        |
	 * W------+-------E
	 *        |p
	 *   c    |    d
	 *        S
	 *</pre>
	 * a, b, c, d : 1/4メッシュ領域の中心。								<br>
	 * p : 高度を求めたい地点											<br>
	 * c を原点、d(1,0) a(0,1) のように x-y 座標を決め、それぞれの高度を
	 * h(a), h(b), h(c), h(d) のように表したとき、						<br>
	 *																	<br>
	 * h(p) = (1-y){(1-x) h(c) + x h(d)} + y{(1-x) h(a) + x h(b)}		<br>
	 *																	<br>
	 * のように求めます。
	 * まだ、埋め立て地や海面などの特殊値処理をしていないため、これらの地域の
	 * 近くでは標高がすごく高くなります。
	 * →　特殊値処理を行うようにしました。
	 */
	private static final double LAT_STEP = 1d/3d/8d/10d/4d;
	private static final double LNG_STEP = 0.5d/8d/10d/4d;
	public float getAltitude(double latitude, double longitude) {
//		return getAltitudeImpl(latitude, longitude);

//System.out.println("alt = " + getAltitudeImpl(latitude, longitude));
		float ha = getAltitudeImpl(latitude + LAT_STEP, longitude - LNG_STEP);
//System.out.print("ha = " + ha);
		float hb = getAltitudeImpl(latitude + LAT_STEP, longitude + LNG_STEP);
//System.out.println("  hb = " + hb);
		float hc = getAltitudeImpl(latitude - LAT_STEP, longitude - LNG_STEP);
//System.out.print("hc = " + hc);
		float hd = getAltitudeImpl(latitude - LAT_STEP, longitude + LNG_STEP);
//System.out.println("  hd = " + hd);
		
		double x = (double)((longitude - LNG_STEP - (int)((longitude - LNG_STEP) / 2.0 / LNG_STEP) * 2.0 * LNG_STEP)/2.0/LNG_STEP);
//System.out.print(" x="+x);
		double y = (double)((latitude - LAT_STEP - (int)((latitude - LAT_STEP) / 2.0 / LAT_STEP) * 2.0 * LAT_STEP)/2.0/LAT_STEP);
//System.out.println(" y="+y);
//System.out.println("x="+x+",y="+y);
		
		return (float)(  (1f-y)*((1f-x)*hc + x*hd) + y*((1f-x)*ha + x*hb)  );

	}
	
	/**
	 * 指定された緯度、経度の地点の高さ(m)を返却します。
	 * 
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
	 *
	 * @param	latitude	緯度
	 * @param	longitude	経度
	 * @return	高さ(m)
	 */
	private float getAltitudeImpl(double latitude, double longitude) {
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
		
		//
		// ここで Map から検索
		//
		Mesh m = mesh.get(m1*10000 + m2*100 + m3);
		if (m == null) return -1f; // データがない場合 -1f (海や外国)
		
		// 1/4 細分区画の index (0-15) を求める
		// 1/4 だけ　南から北、西から東、としていた座標が　北から南、西から東と
		// なっているようで、確認が必要(富士山でわかるかも)
		latRest *= 4d;
		lngRest *= 4d;
		
		int latn = (int)latRest;
		int lngn = (int)lngRest;
		
		int number = (3-latn)*4 + lngn; // 0 - 15 (index)
		
		// 平均値を返す
		int result = m.dmesh[number].altitude;
		int gtype  = m.dmesh[number].groundType;
		// result の特例値
		// 埋立地:6666 等高線のない物:7777 海水:8888 陸水:9999 (海面下標高地は絶対値)
		if (result == 6666) return 5f; // 埋立地は一律標高5mとする。
		if (result == 7777) return 30f; // 等高線のない物は一律30mとする。
		if (result == 8888) return 0f; // 海は0m。
		if (result == 9999) return 40f; // 陸水は40m。
		
		// gtype の特例値
		// 陸水:1 海水:2 等高線のないもの:3 埋立地:4 海面下の地域:5 その他の地域:0
		if (gtype == 5) result = -result;
		return (float)result;
	}
	
/*---------------------------------
 * static inner classes(structure)
 */
	private static class Header {
		/** ファイル名(A3) */
		public String	filename;
		/** レイヤコード(I2) */
//		public String	layerCode;
		/** 作成機関(A10) */
//		public String	organization; // 作成機関
		/** データコード(A10) */
//		public String	dataCode; // "G04-56M"
		/** データ種類(I2 4..mesh) */
//		public int		type; // " 4"
		/** 作成年度(I4) */
//		public int		year; // "1975"
		/** １行の桁数(I4) */
//		public int		chars; // "278"
		/** データ全体の行数(I8 Mesh の数) */
		public int		rows; // "nnnnnnnn" 行数
		
		public Header() {
		}
		public Header(String filename, String s) {
			fill(filename, s);
		}
		public void fill(String filename, String s) {
			this.filename = filename;
			String layerCode = s.substring(0,3);
			// 以下で例外はなかった(2016/10/8)
			if (!layerCode.equals("H  ")) System.out.println("★★例外ヘッダ:"+layerCode+filename);
			String organization = s.substring(3,13);
			if (!organization.equals("GSI       ")) System.out.println("★★例外ヘッダo:"+organization+filename);
			String dataCode = s.substring(13,23);
			if (!dataCode.equals(    "G04-56M   ")) System.out.println("★★例外ヘッダd:"+layerCode+filename);
			int type = parse(s, 23,25);
			if (type != 4) System.out.println("★★例外ヘッダt:"+type+filename);
			int year = parse(s, 25,29);
			if (year != 1975 && year != 1981) System.out.println("★★例外ヘッダy:"+year+filename);
			int chars = parse(s,29,33);
			if (chars != 278) System.out.println("★★例外ヘッダc:"+chars+filename);
			rows = parse(s,33,41);
		}
	}
	
	private static class Mesh {
		/** レイヤコード(A3) */
		public String layerCode;
		/** メッシュの大きさ(I2) */
//		public int size;
		/** １次メッシュコード(I4) */
		public int m1;
		/** ２次メッシュコード(I2) */
		public int m2;
		/** ３次メッシュコード(I2) */
		public int m3;
		/** 平均標高(I5, 0.1m単位) */
		public int altitude;
		/** 最高標高(I5, 0.1m単位) */
//		public int maxAltitude;
		/** 最低標高(I4, 1m単位) */
//		public int minAltitude;
		/** 最低標高コード(I1 海面下..5 その他..0) */
//		public int minAltCode;
		/** 最大傾斜、角度(I3, 0.1度) */
//		public int maxGradient;
		/** 最大傾斜、方向(I2, ８方向、北を1として時計回り */
//		public int maxDirection;
		/** 最小傾斜、角度(I3, 0.1度) */
//		public int minGradient;
		/** 最小傾斜、方向(I2, ８方向、北を1として時計回り */
//		public int minDirection;
		/** 1/4細分区画情報 */
		public DMesh[] dmesh = new DMesh[16];
		
		public Mesh() {
		}
		public Mesh(String s) {
			fill(s);
		}
		public void fill(String s) {
			layerCode = s.substring(0,3);
//			size = parse(s,3,5);
			m1 = parse(s,5,9);
			m2 = parse(s,9,11);
			m3 = parse(s,11,13);
			altitude = parse(s,13,18);
//			maxAltitude = parse(s,18,23);
//			minAltitude = parse(s,23,27);
//			minAltCode = parse(s,27,28);
//			maxGradient = parse(s,28,31);
//			maxDirection = parse(s,31,33);
//			minGradient = parse(s,33,36);
//			minDirection = parse(s,36,38);
			for (int i = 0; i < 16; i++) {
				dmesh[i] = new DMesh(s.substring(38+i*15,53+i*15));
			}
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("(m1="+m1+",m2="+m2+",m3="+m3+",alt="+altitude+")");
			for (int i = 0; i < 16; i++) {
				sb.append("("+i+")"+dmesh[i].altitude);
			}
			return sb.toString();
		}
	}
	
	private static class DMesh {
		/** 標高値(I4, m) */
		public int altitude;
		/** 測定コード */
		public int groundType;
		/** 最大傾斜、角度(I3, 0.1度) */
//		public int maxGradient;
		/** 最大傾斜、方向(I2, ８方向、北を1として時計回り */
//		public int maxDirection;
		/** 最小傾斜、角度(I3, 0.1度) */
//		public int minGradient;
		/** 最小傾斜、方向(I2, ８方向、北を1として時計回り */
//		public int minDirection;
		
		public DMesh() {
		}
		public DMesh(String s) {
			fill(s);
		}
		public void fill(String s) {
			altitude = parse(s,0,4);
			groundType = parse(s,4,5);
//			maxGradient = parse(s,5,8);
//			maxDirection = parse(s,8,10);
//			minGradient = parse(s,10,13);
//			minDirection = parse(s,13,15);
		}
	}
}
