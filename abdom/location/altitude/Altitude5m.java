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
 * ���y��ʏȁ@���y�n���@�@��Ւn�}���@�_�E�����[�h�T�[�r�X�ɂ�����A
 * 5m ���b�V��
 * http://fgd.gsi.go.jp/download/menu.php
 * ��Ւn�}���@���l�W�����f��
 * ��L�f�[�^��ǂݍ��݁A�ܓx�A�o�x��񂩂�W����ԋp����API���쐬����B
 * ���̃f�[�^�͖c��(�]�ʁA�����q�ߕӂ�����1G���x)�Ȃ��߁AOutOfMemory ��
 * �o���Ȃ��悤�A���I�Ƀt�@�C����ǂ݁A�ǂ܂Ȃ��Ȃ����̈�͊J������悤��
 * ����Ƃ��ׂ��B
 *
 * 2016/10/25
 * �f�[�^�����K������ 150*225 �Ȃ��ꍇ�����邱�Ƃ������������߁A�C���B
 * ��̃f�[�^���ȗ�����邱�Ƃ�����B�O�̃f�[�^���ȗ������ꍇ�A�f�[�^��
 * �u��Ɂv���Ă��� startPoint ����͂��܂��Ă���B
 * ��҂̂��߁A��������f�[�^��ǂݍ��񂾌�ɂ��炷���������邱�Ƃ�����B
 * �f�[�^�ǂݍ��݂͉񐔂����Ȃ����炢�����B
 *
 * �e�X�g�p�f�[�^
 * 35�x37��53.95�b 139�x22��13.58�b
 * 35.631652,139.370440  
 * ���x131m
 *
 *
 * @author	Yusuke Sasaki
 * @version	23, October 2016
 */
public class Altitude5m {
	/**
	 * <1�����b�V��><2�����b�V��><3�����b�V��> �𐔒l�������l�@���@�W��
	 * �̃e�[�u��
	 * ���l���K���F
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
	 * ��L�̂W�̐������Ȃ�ׁA�����ƌ��Ȃ������̂��L�[�ƂȂ�B
	 */
	protected Map<Integer, Float2D> table;
	protected Map<Integer, String> fileIndex; // �t�@�C��path�̃e�[�u��
	
	protected static Altitude5m theInstance = null;
	
	/**
	 * Float[][] �Ƃ���ƃ�������H�����߁Afloat[][] �����b�s���O
	 *
	 * �\���_�̔z�񏇏���CV_SequenceRule�f�[�^�^�ŕ\���B
	 * ��Ւn�}���ł́Ctype�����l=�hLinear�h�CscanDirection�����l="+x-y"
	 * �Ɛݒ肷��B
	 * ���̐ݒ�l�́C�擪�Z���͖k���[�ɂ����āC�z�񏇏���x���̐������i�������̏��j
	 * �֏��ɕ���ł���C���[�ɒB����Ǝ��ɁCy���̕������i�k����̏��j�ɐi�ޕ�����
	 * �쓌�[�Ɏ���z��ł��邱�Ƃ������Ă���B
	 * �����I�Ƀ�������ߖ񂷂�ꍇ�Ashort �Ƃ��Ă��ǂ���������Ȃ��B
	 *
	 * float �̓Y�������́A[x(lng)][y(lat)] �Ƃ���B
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
	/** �t�@�C�����Ɋւ���R���p�C���ς� Pattern */
	private static final Pattern p = Pattern.compile("FG-GML-[0-9]{4}-[0-9]{2}-[0-9]{2}-.+\\.(?i)xml$");
	
	/**
	 * �w�肳�ꂽ�f�B���N�g���ȉ��̂��ׂẴt�@�C���ɂ��ăC���f�b�N�X��
	 * �쐬���܂��B
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
			// file FG-GML-nnnn-nn-nn-*.xml �̌`���̂���
			String name = f.getName();
			if (p.matcher(name).matches()) {
//System.out.println(f.getName() + " is found.");
				// �t�@�C�����C���f�b�N�X�Ƀt���p�X��ǉ�
				int idx = Integer.parseInt(name.substring(7,11)
										 + name.substring(12,14)
										 + name.substring(15,17) );
				// absolute path �Ȃ̂ŁA�����񂪑����B���΂ɂ��邱�Ƃ�
				// �������̍팸���ł���B
				String prev = fileIndex.put(idx, f.getAbsolutePath());
				if (prev != null) {
					System.out.println("���ꃁ�b�V���R�[�h�̃t�@�C��������܂��B��"+prev+" �V"+f.getAbsolutePath());
				}
			}
		}
	}
	
	/**
	 * �w�肳�ꂽ tag ���܂ލs���X�L�������܂��B
	 * tag ���������ꍇ�A���e(tag ����I���^�O�A�܂��͍s���܂ł̕�����)
	 * ��ԋp���܂��B
	 * tag ���Ȃ��ꍇ�Anull ��ԋp���܂��B
	 * BufferedReader �� tag ���܂ލs�̎��̍s(�Ȃ��ꍇEOF)�Ɉړ����܂��B
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
	 * Reader ���� tag �^�O�𒊏o���A���e�� value �Ɉ�v���邱�Ƃ�
	 * �m�F���܂��B value �� null �̏ꍇ�A��v�`�F�b�N�͍s���܂���B
	 *
	 * @param	r	�ǂݍ��݌� Reader
	 * @param	fname	�ǂݍ��݃t�@�C����(��O���b�Z�[�W�p)
	 * @param	tag		��������^�O
	 * @param	value	��v�`�F�b�N��r�p������
	 * @return	tag �^�O�̓��e
	 */
	private String check(BufferedReader r, String fname, String tag, String value)
										throws IOException {
		String v = scan(r, tag);
		if (v == null) {
			throw new RuntimeException(fname + " �t�@�C���� "+tag+" �t�B�[���h������܂���B");
		}
		if (  (value != null)&&(!v.equals(value))  ) {
			throw new RuntimeException(fname + " �t�@�C���� "+tag+" �� "+value+" �ł͂���܂���B("+v+")");
		}
		return v;
	}
	
	/**
	 * xml �t�@�C����ǂݍ��݁Atable �ɒǉ����܂��B
	 * xml �t�@�C���� 5m ���b�V���i�W���j�ŁAlow 0 0, high 224 149 �ł���K�v��
	 * ����܂��B
	 */
	private void read(String fname) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fname), "MS932"));
		
		String filename = new File(fname).getName();
		
		try {
			// type �t�B�[���h�� 5m���b�V���i�W���j �ƂȂ��Ă��邱�Ƃ��m�F���܂��B
			check(r, fname, "type", "5m���b�V���i�W���j");
			
			// lowerCorner, upperCorder ��ǂݍ��݂܂��B
			String lowerCorner = check(r, fname, "gml:lowerCorner", null);
			String upperCorner = check(r, fname, "gml:upperCorner", null);
			
			// low �t�B�[���h�� 0 0 �ƂȂ��Ă��邱�Ƃ��m�F���܂��B
			check(r, fname, "gml:low", "0 0");
			// high �t�B�[���h�� 224 149 �ƂȂ��Ă��邱�Ƃ��m�F���܂��B
			check(r, fname, "gml:high", "224 149");
			
			// 
			String s = scan(r, "gml:tupleList");
			if (s == null) throw new RuntimeException("�f�[�^�{�f�B(gml:tupleList)������܂���");
			
			boolean ended = false;
			float[][] d = new float[225][150];
			for (int y = 149; y >= 0; y--) {
				for (int x = 0; x < 225; x++) {
					if (ended) {
						d[x][y] = -0.5f; // �C�ƌ��Ȃ�
						continue;
					}
					String line = r.readLine();
					int comma = line.indexOf(',');
					if (comma == -1) {
						if (!"</gml:tupleList>".equals(line))
							throw new RuntimeException("�f�[�^�`���ُ� �t�@�C����:"+
									fname+" read:"+line+" index(x+(149-y)*225):"+
									(x+(149-y)*225));
						// �I���ꍇ������
						ended = true;
						d[x][y] = -0.5f; //�@�C�Ƃ݂Ȃ�
						continue;
					}
					d[x][y] = Float.parseFloat(line.substring(comma+1));
					if (d[x][y] < 0) {
						String type = line.substring(0,comma);
						switch (type) {
						
						case "�n�\��":
							d[x][y] = 0.5f;
							break;
						case "�\�w��":
							d[x][y] = 1.5f;
							break;
						case "�C����":
							d[x][y] = -0.5f;
							break;
						case "������":
							d[x][y] = 40.5f;
							break;
						case "�f�[�^�Ȃ�":
							d[x][y] = 0.95f;
							break;
							
						case "���̑�":
							d[x][y] = 2.5f;
							break;
						
						default:
							throw new RuntimeException("�f�[�^�^�C�v�ُ� �t�@�C����:"+fname+" read:"+line);
						}
					}
				}
			}
			// sequenceRule �����݂��Aorder +x-y, Linear �ł��邱�Ƃ��m�F���܂�
			check(r, fname, "gml:sequenceRule order=\"+x-y\"","Linear</gml:sequenceRule>");
			// gml:startPoint ���擾���܂�
			String startPoint = scan(r, "gml:startPoint");
			if (startPoint == null) throw new RuntimeException("gml:startPoint ������܂���:"+fname);
			if (!"0 0".equals(startPoint)) {
				// ���בւ�  (0 0)�łȂ��p�x�͏��Ȃ��Ǝv����
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
					d[sx][sy] = -0.5f; // �C�ƌ��Ȃ�
				}
			}
			// �t�@�C�����C���f�b�N�X
			int idx = Integer.parseInt(filename.substring(7,11)
									 + filename.substring(12,14)
									 + filename.substring(15,17) );
			Float2D f = new Float2D();
			f.d = d;
			Float2D previous = table.put(idx, f);
			if (previous != null) {
				System.out.println("table�d���o�^�����o���܂����B" + idx);
				for (int x = 0; x < 225; x++) {
					for (int y = 0; y < 150; y++) {
						if (previous.d[x][y] != f.d[x][y]) {
							System.out.println("���e���قȂ��Ă��܂�");
							break;
						}
					}
				}
			}
			
		} catch (RuntimeException re) {
			System.out.println(re.toString() + " �t�@�C���Ǎ����X�L�b�v���܂�");
		}
		r.close();
	}
	
	/**
	 * 
	 */
	public float getAltitude(double latitude, double longitude) {
		//
		// ���b�V���R�[�h�����߂�
		//
		
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
		
		// table �ɂ��łɓǂݍ��ݍς݂�
//System.out.println("mesh:"+(m1*10000+m2*100+m3));
		Float2D f = table.get(m1*10000+m2*100+m3);
		if (f == null) {
			// �ǂݍ��ݍς݂łȂ��ꍇ�AfileIndex ��p���ăt�@�C���Ǎ�
			String fname = fileIndex.get(m1*10000+m2*100+m3);
			if (fname == null) {
				// �Y���t�@�C�����Ȃ��ꍇ�AAltitudeMesh3�̒l��ԋp
				return AltitudeMesh3.getInstance().getAltitude(latitude, longitude);
			}
			// �t�@�C���Ǎ��Atable �ǉ�
			try {
				read(fname);
			} catch (IOException ioe) {
				throw new RuntimeException("fileIndex �ɂ���t�@�C��:" + fname + " �ňُ킪�������܂���:"+ioe);
			}
			// �Ď擾(fileIndex �Ƀt�@�C�������邱�Ƃ��킩���Ă��邽�߁A�ʏ��΂�
			// �l������͂�)
			f = table.get(m1*10000+m2*100+m3);
			if (f == null)
				// �������A�l���Ȃ�����( = �ǂݍ��ݎ��s�����t�@�C�����������A
				// �������ɃA�N�Z�X�ł��Ȃ��Ȃ���)
				throw new RuntimeException("fileIndex �Ǝ��ۂ̃t�@�C���ɕs���������o���܂����B���b�V���R�[�h:" + (m1*10000+m2*100+m3));
		}
		// Float2D ����Y���̒l�𒊏o���ĕԋp
		lngRest *= 225; // 0-224
		latRest *= 150; // 0-149
		
		return f.d[(int)lngRest][(int)latRest];
	}

/*------
 * main
 */
	public static void main(String[] args) throws Exception {
		Altitude5m a = Altitude5m.getInstance();
		System.out.println("�Ƃ̍��x(5m   ) : " + a.getAltitude(35.631697, 139.370460));
		AltitudeMesh3 b = AltitudeMesh3.getInstance();
		System.out.println("�Ƃ̍��x(mesh3) : " + b.getAltitude(35.631697, 139.370460));
	}
}
