package abdom.util;

/**
 * ���\����Ȃǂŗ��p����ȈՃX�g�b�v�E�I�b�`(msec �I�[�_�[)�B
 * static method ��񋟂��܂��B
 */
public class SW {
	private static long t0 = -1L;
	
	/**
	 * �X�g�b�v�E�H�b�`���J�n���܂��B
	 */
	public static void start() {
		t0 = System.currentTimeMillis();
	}
	
	/**
	 * ���b�v�o�ߎ���(msec) ��\�����܂��B���s���܂���B
	 */
	public static void print() {
		System.out.print(lap());
	}
	
	/**
	 * �w�胁�b�Z�[�W�̌�Ƀ��b�v�o�ߎ���(msec) ��\�����܂��B���s���܂���B
	 */
	public static void print(String msg) {
		System.out.print(msg);
		print();
	}
	
	/**
	 * ���b�v�o�ߎ���(msec)��\�����܂��B
	 */
	public static void println() {
		print();
		System.out.println();
	}
	
	/**
	 * �w�胁�b�Z�[�W�̌�Ƀ��b�v�o�ߎ���(msec) ��\�����܂��B
	 */
	public static void println(String msg) {
		System.out.print(msg);
		println();
	}
	
	/**
	 * ���b�v�o�ߎ���(start() ����̌o�ߎ���)�� long �l�Ŏ擾���܂��B
	 * @return	�o�ߎ���(msec)
	 */
	public static long lap() {
		if (t0 == -1) start();
		return System.currentTimeMillis() - t0;
	}
}

