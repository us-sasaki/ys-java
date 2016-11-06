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
 * ���y��ʏȁ@���y���l���ɂ�����A����t�H�[�}�b�g�̍��x���(�R�����b�V��)
 * ���^�f�[�^�t�@�C�����ʎq : KS-META-G04-56M
 * http://nlftp.mlit.go.jp/ksj/
 * �� ����t�H�[�}�b�g�`�� ���a56�N�x
 * ��L�f�[�^��ǂݍ��݁A�ܓx�A�o�x��񂩂�W����ԋp����API���쐬����B
 *
 * �}��F
 *�uI2�v�͐����^2���A�uA2�v�͕����^2����\���܂��B
 *�uN2�v�͑S�p����1������\���܂��B�����́A�����^2����1������\���܂��B
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
	 * �w�肵��������̎w�肵���ʒu�̕����𐔒l�ƌ��Ȃ��Aint �Ƃ���
	 * �ԋp���܂��B�w��ʒu�̕����� String::trim() ���{����܂��B
	 */
	protected static int parse(String s, int a, int b) {
		s = s.substring(a, b).trim();
		if ("".equals(s)) return 0;
		return Integer.parseInt(s);
	}
	
	/**
	 * �f�[�^��ێ�����C���X�^���X�́A���̃��\�b�h�Ŏ擾���ĉ������B
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
	 * �w�肳�ꂽ�f�B���N�g���ȉ��̂��ׂẴt�@�C����ǂݍ��݂܂��B
	 * �ǂݍ��ރt�@�C���́A G04-56M*.txt �̌`���̃t�@�C���ł��B
	 *
	 * @param	dirname	�t�@�C���Ǎ����s���g�b�v�f�B���N�g��
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
			// file "G04-56M*.txt" �̌`���̂���(ext��caps free)
			if (f.getName().matches("G04-56M.+\\.(?i)txt$")) {
				read(f.getAbsolutePath());
			}
		}
	}
	
	/**
	 * �w�肳�ꂽ�t�@�C����ǂݍ��݂܂��B
	 * ����̃t�@�C���������łɓǂݍ��܂�Ă����ꍇ�A�X�L�b�v����܂��B
	 *
	 * @param	fname	�t�@�C����(�t���p�X��)
	 */
	private void read(String fname) throws IOException {
//System.out.println("reading.. " + fname);
		String filename = new File(fname).getName();
		for (Header h : header) {
			if (h.filename.equals(filename)) {
				System.out.println("���ł�"+fname+"�͓ǂݍ��܂�Ă��܂��B����t�@�C�����̓�d�ǂݍ��݂͂ł��܂���B�X�L�b�v���܂��B");
				return;
			}
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
		String s = br.readLine();
		Header h = new Header(filename, s);
		header.add(h);
		int count = h.rows-1; // �w�b�_������
		
		// body���ǂݍ���
		for (int i = 0; i < count; i++) {
			String line = br.readLine();
			if (line == null) {
				System.out.println(fname + "�̃w�b�_��A�s��"+count+"����͂��ł����A"+i+"�s�ڂ��ǂݍ��߂܂���B");
				break;
			}
			Mesh m = new Mesh(line);
			int index = m.m1*10000 + m.m2*100 + m.m3;
			
			Mesh lm = mesh.put(index, m);
			// �����炭�����ɂ�����͗����ɓ����Ă���A��ʂɏd�������邪�A
			// �f�[�^�͓���Ȃ��߁A�`�F�b�N���Ȃ����ƂƂ���
//			if (lm != null) {
//				System.out.println("���ꃁ�b�V��(1�`3��)�̒l���d�����Ă��܂�" + index + "�ǉ�mesh" + m + " ����mesh" + lm);
//			}
		}
		
		// �c�肪���邩�ǂ������`�F�b�N(�Ȃ��͂�)
		while (true) {
			String line = br.readLine();
			if (line == null) break;
			System.out.println(fname + "�̃w�b�_��A�s��"+count+"�����Ȃ��͂��ł����A�]���ȍs������܂��B:"+line);
		}
		br.close();
	}
	
	/**
	 * ���{�̗^����ꂽ�n�_�̍����𕽋ϒl��p���ĂȂ߂炩(���`)�ɂȂ�悤�ɎZ�o���܂��B
	 *<pre>
	 *        N
	 *   a    |    b
	 *        |
	 * W------+-------E
	 *        |p
	 *   c    |    d
	 *        S
	 *</pre>
	 * a, b, c, d : 1/4���b�V���̈�̒��S�B								<br>
	 * p : ���x�����߂����n�_											<br>
	 * c �����_�Ad(1,0) a(0,1) �̂悤�� x-y ���W�����߁A���ꂼ��̍��x��
	 * h(a), h(b), h(c), h(d) �̂悤�ɕ\�����Ƃ��A						<br>
	 *																	<br>
	 * h(p) = (1-y){(1-x) h(c) + x h(d)} + y{(1-x) h(a) + x h(b)}		<br>
	 *																	<br>
	 * �̂悤�ɋ��߂܂��B
	 * �܂��A���ߗ��Ēn��C�ʂȂǂ̓���l���������Ă��Ȃ����߁A�����̒n���
	 * �߂��ł͕W���������������Ȃ�܂��B
	 * ���@����l�������s���悤�ɂ��܂����B
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
	 * �w�肳�ꂽ�ܓx�A�o�x�̒n�_�̍���(m)��ԋp���܂��B
	 * 
	 * <pre>���y���l���@�_�E�����[�h�T�[�r�X ���
	 * ���b�V���R�[�h�ɂ���
	 * ���b�V���R�[�h�́A���b�V���f�[�^�̊e���ɑ΂�����U��ꂽ�R�[�h�ŁA�ȉ���
	 * �K��ɏ]���Ă��܂��B
	 * ��1���n�����4���̃R�[�h�Ŏ��ʂ���A��2���́A���b�V���̓쐼�[�̈ܓx��
	 * 1.5�{���������A��2���͓����_�̌o�x�̉�2���̐��ł��B
	 * ��2�����b�V�����̈ʒu�́A����̑�����1�����b�V�������s��Ɍ����Ă�ƁA
	 * �삩��k�Ɍ�����0����7�܂ŐU��ꂽ�s�ԍ��Ɛ����瓌�Ɍ�����0����7�܂ŐU���
	 * ����ԍ���g�ݍ��킹���ԍ�������̑�����1�����b�V���R�[�h�ɑ����Ď�����܂��B
	 * ��3�����b�V�����̈ʒu�́A����̑�����2�����b�V�������s��Ɍ����Ă�ƁA
	 * �삩��k�Ɍ�����0����9�܂ŐU��ꂽ�s�ԍ��Ɛ����瓌�Ɍ�����0����9�܂ŐU���
	 * ����ԍ���g�ݍ��킹���ԍ�������̑�����2�����b�V���R�[�h�ɑ����Ď�����܂��B
	 * �Ⴆ�� 5438-23-23 �Ƃ���3�����b�V���R�[�h�i��n�惁�b�V���R�[�h�j��5438��
	 * ����1���n���� ���̓삩��3�Ԗڐ�����4�Ԗڂɂ���2���n���撆�̂���ɓ삩��
	 *  3�Ԗڐ����� 4�Ԗڂ� 3���n����������Ă��邱�ƂɂȂ�܂��B
	 * </pre>
	 *
	 * @param	latitude	�ܓx
	 * @param	longitude	�o�x
	 * @return	����(m)
	 */
	private float getAltitudeImpl(double latitude, double longitude) {
		// �P�����b�V���R�[�h�̒l�����߂�
		int higher = (int)(latitude*1.5);
		int lower  = ((int)(longitude))%100;
		int m1 = higher * 100 + lower;
		
		double latRest = latitude*1.5 - higher; // 0<= latRest < 1
		double lngRest = longitude - (int)longitude; // 0<= lngRest <1
		
		// �Q�����b�V���R�[�h�̒l�����߂�
		latRest *= 8d;
		lngRest *= 8d;
		int m2 = ((int)(latRest))*10 + (int)(lngRest);
		latRest -= (int)latRest;
		lngRest -= (int)lngRest;
		
		// �R�����b�V���R�[�h�̒l�����߂�
		latRest *= 10d;
		lngRest *= 10d;
		int m3 = ((int)(latRest))*10 + (int)(lngRest);
		latRest -= (int)latRest;
		lngRest -= (int)lngRest;
		
		//
		// ������ Map ���猟��
		//
		Mesh m = mesh.get(m1*10000 + m2*100 + m3);
		if (m == null) return -1f; // �f�[�^���Ȃ��ꍇ -1f (�C��O��)
		
		// 1/4 �ו����� index (0-15) �����߂�
		// 1/4 �����@�삩��k�A�����瓌�A�Ƃ��Ă������W���@�k�����A�����瓌��
		// �Ȃ��Ă���悤�ŁA�m�F���K�v(�x�m�R�ł킩�邩��)
		latRest *= 4d;
		lngRest *= 4d;
		
		int latn = (int)latRest;
		int lngn = (int)lngRest;
		
		int number = (3-latn)*4 + lngn; // 0 - 15 (index)
		
		// ���ϒl��Ԃ�
		int result = m.dmesh[number].altitude;
		int gtype  = m.dmesh[number].groundType;
		// result �̓���l
		// �����n:6666 �������̂Ȃ���:7777 �C��:8888 ����:9999 (�C�ʉ��W���n�͐�Βl)
		if (result == 6666) return 5f; // �����n�͈ꗥ�W��5m�Ƃ���B
		if (result == 7777) return 30f; // �������̂Ȃ����͈ꗥ30m�Ƃ���B
		if (result == 8888) return 0f; // �C��0m�B
		if (result == 9999) return 40f; // ������40m�B
		
		// gtype �̓���l
		// ����:1 �C��:2 �������̂Ȃ�����:3 �����n:4 �C�ʉ��̒n��:5 ���̑��̒n��:0
		if (gtype == 5) result = -result;
		return (float)result;
	}
	
/*---------------------------------
 * static inner classes(structure)
 */
	private static class Header {
		/** �t�@�C����(A3) */
		public String	filename;
		/** ���C���R�[�h(I2) */
//		public String	layerCode;
		/** �쐬�@��(A10) */
//		public String	organization; // �쐬�@��
		/** �f�[�^�R�[�h(A10) */
//		public String	dataCode; // "G04-56M"
		/** �f�[�^���(I2 4..mesh) */
//		public int		type; // " 4"
		/** �쐬�N�x(I4) */
//		public int		year; // "1975"
		/** �P�s�̌���(I4) */
//		public int		chars; // "278"
		/** �f�[�^�S�̂̍s��(I8 Mesh �̐�) */
		public int		rows; // "nnnnnnnn" �s��
		
		public Header() {
		}
		public Header(String filename, String s) {
			fill(filename, s);
		}
		public void fill(String filename, String s) {
			this.filename = filename;
			String layerCode = s.substring(0,3);
			// �ȉ��ŗ�O�͂Ȃ�����(2016/10/8)
			if (!layerCode.equals("H  ")) System.out.println("������O�w�b�_:"+layerCode+filename);
			String organization = s.substring(3,13);
			if (!organization.equals("GSI       ")) System.out.println("������O�w�b�_o:"+organization+filename);
			String dataCode = s.substring(13,23);
			if (!dataCode.equals(    "G04-56M   ")) System.out.println("������O�w�b�_d:"+layerCode+filename);
			int type = parse(s, 23,25);
			if (type != 4) System.out.println("������O�w�b�_t:"+type+filename);
			int year = parse(s, 25,29);
			if (year != 1975 && year != 1981) System.out.println("������O�w�b�_y:"+year+filename);
			int chars = parse(s,29,33);
			if (chars != 278) System.out.println("������O�w�b�_c:"+chars+filename);
			rows = parse(s,33,41);
		}
	}
	
	private static class Mesh {
		/** ���C���R�[�h(A3) */
		public String layerCode;
		/** ���b�V���̑傫��(I2) */
//		public int size;
		/** �P�����b�V���R�[�h(I4) */
		public int m1;
		/** �Q�����b�V���R�[�h(I2) */
		public int m2;
		/** �R�����b�V���R�[�h(I2) */
		public int m3;
		/** ���ϕW��(I5, 0.1m�P��) */
		public int altitude;
		/** �ō��W��(I5, 0.1m�P��) */
//		public int maxAltitude;
		/** �Œ�W��(I4, 1m�P��) */
//		public int minAltitude;
		/** �Œ�W���R�[�h(I1 �C�ʉ�..5 ���̑�..0) */
//		public int minAltCode;
		/** �ő�X�΁A�p�x(I3, 0.1�x) */
//		public int maxGradient;
		/** �ő�X�΁A����(I2, �W�����A�k��1�Ƃ��Ď��v��� */
//		public int maxDirection;
		/** �ŏ��X�΁A�p�x(I3, 0.1�x) */
//		public int minGradient;
		/** �ŏ��X�΁A����(I2, �W�����A�k��1�Ƃ��Ď��v��� */
//		public int minDirection;
		/** 1/4�ו������ */
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
		/** �W���l(I4, m) */
		public int altitude;
		/** ����R�[�h */
		public int groundType;
		/** �ő�X�΁A�p�x(I3, 0.1�x) */
//		public int maxGradient;
		/** �ő�X�΁A����(I2, �W�����A�k��1�Ƃ��Ď��v��� */
//		public int maxDirection;
		/** �ŏ��X�΁A�p�x(I3, 0.1�x) */
//		public int minGradient;
		/** �ŏ��X�΁A����(I2, �W�����A�k��1�Ƃ��Ď��v��� */
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
