package abdom.data;

import java.io.*;
import java.util.*;
import abdom.util.CommentCutInputStream;

/**
 * ByteArray �N���X�́Cbyte[] �ɑ΂���e��֗̕��ȃI�y���[�V������񋟂��܂��B
 * abdom �p�b�P�[�W�Ɉړ����܂����B
 * 
 * @author		Yusuke Sasaki
 * @version		release		19, October 2016
 */
public class ByteArray {
	
	/**
	 * �Q�z���A������ byte �z����쐬���܂��B
	 *
	 * @param		a		�擪�Ɉʒu����z��
	 * @param		b		����ɘA�������z��
	 *
	 * @return		�A�����ꂽ�z��
	 */
	public static byte[] concat(byte[] a, byte[] b) {
		if (a == null) return b;
		if (b == null) return a;
		byte[] result = new byte[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		
		return result;
	}
	
	/**
	 * �Q�z��̓��e�����������`�F�b�N���܂��B
	 * �������قȂ�C�������͓��e���قȂ�ꍇ false ��Ԃ��܂��B
	 *
	 * @param		a		��r�Ώۂ̔z��
	 * @param		b		��r�Ώۂ̔z��
	 *
	 * @return		��v����ꍇ true, ��v���Ȃ��ꍇ false
	 */
	public static boolean compare(byte[] a, byte[] b) {
		if (a.length != b.length) return false;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) return false;
		}
		return true;
	}
	
	public static byte[] subarray(byte[] target, int beginIndex, int finishIndex) {
		if ( (beginIndex < 0) || (finishIndex < beginIndex) )
			return null;
		
		byte[] result = new byte[finishIndex - beginIndex];
		System.arraycopy(target, beginIndex, result, 0, finishIndex - beginIndex);
		
		return result;
	}
	
	/**
	 * 16�iASCII������̓��e��\�� byte[] ��Ԃ��܂��B
	 *
	 * @param		str		16�iASCII������ł��B"0102EEDC" �ȂǁB
	 * 
	 * @return		byte[] �ɕϊ����ꂽ���ʂł��B
	 */
	public static byte[] parse(String str)
					throws IllegalArgumentException, NumberFormatException {
		if ((str.length()%2) != 0)
			throw new IllegalArgumentException("16�iASCII�\���͋����������łȂ���΂Ȃ�܂���B");
		
		byte[] src = new byte[str.length()/2];
		
		for (int i = 0; i < src.length; i++) {
			src[i] = (byte)Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
		}
		
		return src;
	}
	
	/**
	 * byte[] �̓��e���w�L�T������ɕϊ����܂��Bparse(String)�̋t�ϊ��ł��B
	 * �P�o�C�g���Q�����ɕϊ�����A�ϊ���� 0 �` 9, a �` f(������) �ɕϊ�����܂��B
	 * 
	 * @param		d		�ϊ��Ώۂ� byte[] �ł��B
	 * 
	 * @return		16�iASCII������ł��B
	 */
	public static String toString(byte[] d) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < d.length; i++) {
			if ((d[i] & 0xF0) == 0) result.append("0");
			result.append(Integer.toString(d[i] & 0xFF, 16));
		}
		return result.toString();
	}
	
	/**
	 * byte[] �̓��e�̎w�肵���������w�L�T������ɕϊ����܂��B
	 * d[offset] �` d[offset + len - 1] �̊e�f�[�^���ϊ��ΏۂƂȂ�܂��B
	 * �P�o�C�g���Q�����ɕϊ�����A�ϊ���� 0 �` 9, a �` f(������) �ɕϊ�����܂��B
	 *
	 * @param		d		�ϊ��Ώۂ� byte[] �ł��B
	 * @param		offset	�ϊ����J�n���� d �̓Y�����ԍ��ł��B
	 * @param		len		�ϊ����s�������ł��B
	 *
	 * @return		16�iASCII������ł��B
	 */
	public static String toString(byte[] d, int offset, int len) {
		StringBuffer result = new StringBuffer();
		for (int i = offset; i < offset + len; i++) {
			if ((d[i] & 0xF0) == 0) result.append("0");
			result.append(Integer.toString(d[i] & 0xFF, 16));
		}
		return result.toString();
	}
	
	/**
	 * �w�肳�ꂽ�o�C�g�f�[�^���w�L�T������ɕϊ����܂��B
	 * �P�o�C�g���Q�����ɕϊ�����A�ϊ���� 0 �` 9, a �` f(������) �ɕϊ�����܂��B
	 * 
	 * @param		d		�ϊ��Ώۂ̃f�[�^���w�肵�܂��B
	 * @return		16�iASCII������ł��B
	 */
	public static String toString(byte d) {
		String result = "";
		if ((d & 0xF0) == 0) result = result + "0";
		result = result + Integer.toString(d & 0xFF, 16);
		return result;
	}
	
	public static String toString(long v) {
		StringBuffer st = new StringBuffer();
		st.append(toString((byte)(v >> 56)));
		st.append(toString((byte)(v >> 48)));
		st.append(toString((byte)(v >> 40)));
		st.append(toString((byte)(v >> 32)));
		st.append(toString((byte)(v >> 24)));
		st.append(toString((byte)(v >> 16)));
		st.append(toString((byte)(v >> 8)));
		st.append(toString((byte)(v)));
		
		return st.toString();
	}
	
	public static String toString(int v) {
		StringBuffer st = new StringBuffer();
		st.append(toString((byte)(v >> 24)));
		st.append(toString((byte)(v >> 16)));
		st.append(toString((byte)(v >> 8)));
		st.append(toString((byte)(v)));
		
		return st.toString();
	}
	
	public static String toString(short v) {
		StringBuffer st = new StringBuffer();
		st.append(toString((byte)(v >> 8)));
		st.append(toString((byte)(v)));
		
		return st.toString();
	}
	
	public static String toString(char v) {
		StringBuffer st = new StringBuffer();
		st.append(toString((byte)(v >> 8)));
		st.append(toString((byte)(v)));
		
		return st.toString();
	}
	
//	public static byte[] set(byte[] src, int value, int pos) {
//	}
	/**
	 * �w�肳�ꂽ������� byte ��𒊏o���A���ߌ��ʂ� byte[] ��ԋp���܂��B
	 * ���߂́AC/C++ �R�����g�����O���A�X�y�[�X�A�^�u�R�[�h�A�܂��͉��s�R�[�h(CR, LF)�ŋ�؂�ꂽ
	 * �����g�[�N�����w�L�T����(0�`9,A�`F,a�`f)�݂̂ō\������镔���𒊏o���܂��B
	 * ���o�������ʁA���������Ȃ��Ă����ꍇ�AIllegalArgumentException ���X���[����܂��B
	 *
	 * @param		data		���߂��s��������
	 *
	 * @return		���ߌ���
	 *
	 * @exception	java.lang.IllegalArgumentException	�w�L�T���������������Ȃ�
	 * @exception	java.lang.InternalError		������� Shift-JIS ���g�p�ł��Ȃ�
	 */
	public static byte[] parseMessage(String data) {
		
		byte[] buff = null;
		try {
			buff = data.getBytes("SJIS");
		} catch (UnsupportedEncodingException e) {
			throw new InternalError("SJIS���g�p�ł��܂���");
		}
		try {
			ByteArrayInputStream	src		= new ByteArrayInputStream(buff);
			InputStream				src3	= new CommentCutInputStream(src);
			
			StringBuffer dst = new StringBuffer();
			while (true) {
				int c = src3.read();
				if (c == -1) break;
				dst.append( (char)c );
			}
			src3.close();
			
			StringTokenizer st = new StringTokenizer(dst.toString(), " \t\r\n");
			StringBuffer tmp = new StringBuffer();
			
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (checkValidity(token))
					tmp.append(token);
			}
			
			return ByteArray.parse(tmp.toString());
		} catch (IOException e) {
			throw new InternalError(e.toString());
		}
	}
	
	/**
	 * �^����ꂽ�����񂪃w�L�T�����ƌ��Ȃ��邩�ǂ������`�F�b�N���܂��B
	 * 
	 * @param		target		�`�F�b�N�Ώۂ̕�����
	 * @return		�w�L�T�����Ƃ݂Ȃ���ꍇ�� true
	 */
	private static boolean checkValidity(String target) {
		for (int i = 0; i < target.length(); i++) {
			char c = target.charAt(i);
			
			if ( (c >= '0')&&(c <= '9') ) continue;
			if ( (c >= 'A')&&(c <= 'F') ) continue;
			if ( (c >= 'a')&&(c <= 'f') ) continue;
			return false;
		}
		return true;
	}
	
	/**
	 * �w�肳�ꂽ�o�C�g�z����_���v���X�g�`���̕�����ɕϊ�����֗��֐��ł��B
	 * ���ʑS�̂���������ɔz�u���邽�߁A�傫�ȃf�[�^���_���v���X�g�ɕϊ�����ꍇ�A
	 * com.ntt.io.BinaryDumpStream ���g�p�������������������͏㏸���܂��B
	 * �_���v���X�g�̃t�H�[�}�b�g�́Acom.ntt.io.BinaryDumpStream �ɂ����āAset7Bit(true)
	 * setLineNumbering(true) �����s�������̂ƂȂ�܂��B
	 *
	 * @param		data		�_���v���X�g�ɕϊ�����f�[�^
	 * @return		�_���v���X�g(���s�������܂�)
	 */
	public static String toDumpList(byte[] data) {
		return toDumpList(data, 0, data.length);
	}
	
	public static String toDumpList(byte[] data, int off, int len) {
		try {
			ByteArrayOutputStream		b	= new ByteArrayOutputStream();
			ByteArrayInputStream		src	= new ByteArrayInputStream(data, off, len);
			com.ntt.io.BinaryDumpStream	bds	= new com.ntt.io.BinaryDumpStream(b);
			bds.set7Bit(true);
			while (true) {
				int c = src.read();
				if (c == -1) break;
				bds.write(c);
			}
			bds.close();
			src.close();
			
			byte[] d = b.toByteArray();
			return new String(d, "SJIS");
		} catch (Exception e) {
			return "�����ُ킪�������܂���";
		}
	}
	
	static Random r = new Random();
	
	/**
	 * �����W�F�l���[�^���w�肵�܂��B�f�t�H���g�ł� java.util.Random �Ȃ̂ŁA
	 * ���x��v������ꍇ�Ɏg�p���܂��B
	 */
	public static void setRandom(Random newRandom) {
		r = newRandom;
	}
	
	public static byte[] randomData(int minSize, int maxSize) {
		int i = (int)((r.nextDouble() * (double)(maxSize - minSize)) + minSize);
		
		byte[] result = new byte[i];
		r.nextBytes(result);
		return result;
	}
	
	public static byte[] xor(byte[] a, byte[] b) {
		byte[] result;
		byte[] t;
		if (b.length < a.length) {
			t = b;
			result = new byte[a.length];
			System.arraycopy(a, 0, result, 0, a.length);
		} else {
			t = a;
			result = new byte[b.length];
			System.arraycopy(b, 0, result, 0, b.length);
		}
		
		for (int i = 0; i < t.length; i++) {
			result[i] ^= t[i];
		}
		
		return result;
	}
	
	public static byte[] reverse(byte[] target) {
		byte[] result = new byte[target.length];
		for (int i = 0; i < target.length; i++) {
			result[i] = target[target.length - 1 - i];
		}
		return result;
	}
	
	/**
	 * �^����ꂽ�f�[�^�ɂ��āA��p���e�B��t�����܂��B
	 */
	public static byte setOddParity(byte d) {
		// Parity �t��
		int parity = 0;
		int bit = 1;
		for (int j = 0; j < 8; j++) {
			if ( (d & bit) != 0 ) parity++;
			bit *= 2;
		}
		if ((parity % 2) == 0) d ^= 1;
		
		return d;
	}
	
	/**
	 * �^����ꂽ�f�[�^�ɂ��āA��p���e�B��t�����܂��B
	 * �����̃f�[�^�͊�p���e�B��t���������̂ɃA�b�v�f�[�g����܂��B
	 */
	public static byte[] setOddParity(byte[] d) {
		for (int i = 0; i < d.length; i++) {
			// Parity �t��
			int parity = 0;
			int bit = 1;
			for (int j = 0; j < 8; j++) {
				if ( (d[i] & bit) != 0 ) parity++;
				bit *= 2;
			}
			if ((parity % 2) == 0) d[i] ^= 1;
		}
		return d;
	}
}
