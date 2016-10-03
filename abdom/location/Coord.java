package abdom.location;

/**
 * 地球上の２地点の緯度、経度から距離を求める。
 *
 * http://yamadarake.jp/trdi/report000001.html
 * のものを転記しただけで、何をしているのかはわからん。
 *
 * android 上で動作させるプログラムでは
 * android.location.Location.distanceBetween() などすでにある機能でよい
 *
 * 2016/5/4
 */
public class Coord {
	
	/**
	 * 日本における準拠楕円体は、2002年までベッセルにより算出された値
	 *（ベッセル楕円体）を採用していた（「日本測地系」と呼称）
	 */
    public static final double BESSEL_A = 6377397.155;
    public static final double BESSEL_E2 = 0.00667436061028297;
    public static final double BESSEL_MNUM = 6334832.10663254;
	
	/**
	 * 海図の国際利用や精密な位置情報にもとづくGISデータの整備の障害になりつつ
	 * あったため、2002年4月1日から世界測地系としてGRS80地球楕円体が準拠楕円体
	 * として採用された
	 * この新しい準拠楕円体の長半径（赤道半径）a 及び扁平率 f の値は、測量法
	 * 施行令第3条により定義され、GRS80楕円体の値である
	 */
    public static final double GRS80_A = 6378137.000;
    public static final double GRS80_E2 = 0.00669438002301188;
    public static final double GRS80_MNUM = 6335439.32708317;
    
    /**
     * 海域の測地系はWGS84を用いることが多い。WGS84楕円体の扁平率 f は、GRS80
     * 楕円体とはごく僅か異なっている。
     */
    public static final double WGS84_A = 6378137.000;
    public static final double WGS84_E2 = 0.00669437999019758; // 離心率
    public static final double WGS84_MNUM = 6335439.32729246;
	
	/** type を表す定数(BESSEL=0) */
    public static final int BESSEL = 0;
    
	/** type を表す定数(GRS80=1) */
    public static final int GRS80 = 1;
    
	/** type を表す定数(WGS84=2) */
    public static final int WGS84 = 2;
	
	/**
	 * 角度　→　ラジアン 変換
	 */
    public static double deg2rad(double deg){
        return deg * Math.PI / 180.0;
    }
	
	/**
	 * ２点の緯度、経度、a, e2, mnum から距離(m)を求める
	 */
    public static double calcDistHubeny(double lat1, double lng1,
                                        double lat2, double lng2,
                                        double a, double e2, double mnum){
        double my = deg2rad((lat1 + lat2) / 2.0);
        double dy = deg2rad(lat1 - lat2);
        double dx = deg2rad(lng1 - lng2);

        double sin = Math.sin(my);
        double w = Math.sqrt(1.0 - e2 * sin * sin);
        double m = mnum / (w * w * w);
        double n = a / w;

        double dym = dy * m;
        double dxncos = dx * n * Math.cos(my);

        return Math.sqrt(dym * dym + dxncos * dxncos);
    }
	
	/**
	 * ２点の緯度、経度から距離(m)をGRS80を用いて求める
	 */
    public static double calcDistHubeny(double lat1, double lng1,
                                        double lat2, double lng2){
        return calcDistHubeny(lat1, lng1, lat2, lng2,
                GRS80_A, GRS80_E2, GRS80_MNUM);
    }
	
    /**
     * LatLngReader.Plot 引数を追加 地点ab間の距離(m)をGRS80を用いて求める
     *
     * @param a 地点a
     * @param b 地点b
     * @return 地点ab間の距離(m)
     */
    public static double calcDistHubeny(Plot a, Plot b) {
        return calcDistHubeny(a.latitude, a.longitude, b.latitude, b.longitude);
    }
	
	/**
	 * type を指定して２点間の距離(m)を求める
	 */	
    public static double calcDistHubery(double lat1, double lng1,
                                        double lat2, double lng2, int type){
        switch(type){
            case BESSEL:
                return calcDistHubeny(lat1, lng1, lat2, lng2,
                        BESSEL_A, BESSEL_E2, BESSEL_MNUM);
            case WGS84:
                return calcDistHubeny(lat1, lng1, lat2, lng2,
                        WGS84_A, WGS84_E2, WGS84_MNUM);
            default:
                return calcDistHubeny(lat1, lng1, lat2, lng2,
                        GRS80_A, GRS80_E2, GRS80_MNUM);
        }
    }

/*--------------
 * test 用 main
 */
    public static void main(String[] args){
        System.out.println("Coords Test Program");
        double lat1, lng1, lat2, lng2;

        lat1 = Double.parseDouble(args[0]);
        lng1 = Double.parseDouble(args[1]);
        lat2 = Double.parseDouble(args[2]);
        lng2 = Double.parseDouble(args[3]);

        double d = calcDistHubeny(lat1, lng1, lat2, lng2);

        System.out.println("Distance = " + d + " m");
    }
}
