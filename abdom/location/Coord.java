package abdom.location;

/**
 * �n����̂Q�n�_�̈ܓx�A�o�x���狗�������߂�B
 *
 * http://yamadarake.jp/trdi/report000001.html
 * �̂��̂�]�L���������ŁA�������Ă���̂��͂킩���B
 *
 * android ��œ��삳����v���O�����ł�
 * android.location.Location.distanceBetween() �Ȃǂ��łɂ���@�\�ł悢
 *
 * 2016/5/4
 */
public class Coord {
	
	/**
	 * ���{�ɂ����鏀���ȉ~�̂́A2002�N�܂Ńx�b�Z���ɂ��Z�o���ꂽ�l
	 *�i�x�b�Z���ȉ~�́j���̗p���Ă����i�u���{���n�n�v�ƌď́j
	 */
    public static final double BESSEL_A = 6377397.155;
    public static final double BESSEL_E2 = 0.00667436061028297;
    public static final double BESSEL_MNUM = 6334832.10663254;
	
	/**
	 * �C�}�̍��ۗ��p�␸���Ȉʒu���ɂ��ƂÂ�GIS�f�[�^�̐����̏�Q�ɂȂ��
	 * ���������߁A2002�N4��1�����琢�E���n�n�Ƃ���GRS80�n���ȉ~�̂������ȉ~��
	 * �Ƃ��č̗p���ꂽ
	 * ���̐V���������ȉ~�̂̒����a�i�ԓ����a�ja �y�ѝG���� f �̒l�́A���ʖ@
	 * �{�s�ߑ�3���ɂ���`����AGRS80�ȉ~�̂̒l�ł���
	 */
    public static final double GRS80_A = 6378137.000;
    public static final double GRS80_E2 = 0.00669438002301188;
    public static final double GRS80_MNUM = 6335439.32708317;
    
    /**
     * �C��̑��n�n��WGS84��p���邱�Ƃ������BWGS84�ȉ~�̝̂G���� f �́AGRS80
     * �ȉ~�̂Ƃ͂����͂��قȂ��Ă���B
     */
    public static final double WGS84_A = 6378137.000;
    public static final double WGS84_E2 = 0.00669437999019758; // ���S��
    public static final double WGS84_MNUM = 6335439.32729246;
	
	/** type ��\���萔(BESSEL=0) */
    public static final int BESSEL = 0;
    
	/** type ��\���萔(GRS80=1) */
    public static final int GRS80 = 1;
    
	/** type ��\���萔(WGS84=2) */
    public static final int WGS84 = 2;
	
	/**
	 * �p�x�@���@���W�A�� �ϊ�
	 */
    public static double deg2rad(double deg){
        return deg * Math.PI / 180.0;
    }
	
	/**
	 * �Q�_�̈ܓx�A�o�x�Aa, e2, mnum ���狗��(m)�����߂�
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
	 * �Q�_�̈ܓx�A�o�x���狗��(m)��GRS80��p���ċ��߂�
	 */
    public static double calcDistHubeny(double lat1, double lng1,
                                        double lat2, double lng2){
        return calcDistHubeny(lat1, lng1, lat2, lng2,
                GRS80_A, GRS80_E2, GRS80_MNUM);
    }
	
    /**
     * LatLngReader.Plot ������ǉ� �n�_ab�Ԃ̋���(m)��GRS80��p���ċ��߂�
     *
     * @param a �n�_a
     * @param b �n�_b
     * @return �n�_ab�Ԃ̋���(m)
     */
    public static double calcDistHubeny(Plot a, Plot b) {
        return calcDistHubeny(a.latitude, a.longitude, b.latitude, b.longitude);
    }
	
	/**
	 * type ���w�肵�ĂQ�_�Ԃ̋���(m)�����߂�
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
 * test �p main
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
