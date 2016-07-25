import java.io.*;
import java.util.logging.*;

/**
 * ���̓X�g���[�����e���Ď����A���M���O����
 */
public class StreamLogger extends Thread {
	/** ���̓X�g���[���̃o�b�t�@ */
	protected static final int BUFFER_SIZE = 32768;

	/** ���̓X�g���[�������ʂ��閼�O */
	protected String	name;
	
	/** �Ď��Ώۂ̓��̓X�g���[�� */
	protected InputStream in;
	
	/** ���O */
	protected Logger	log;
	
	/** ���̓X�g���[����ʂ����f�[�^�̃T�C�Y */
	protected long		size;
	
	/** �����o�b�t�@ */
	private byte[]		buf;
	
	/** ���O�o�͂̂��߂̐擪���o�C�g�������ݐ� */
	private byte[]		beginning;
	private int			beginningSize;
	
	/** */
	protected static int partialDumpSize = 32;
	protected static int dumpSize = 32;
	
	protected int pds;
	protected int ds;
	
/*-------------
 * Constructor
 */
	public StreamLogger(
				String name,
				InputStream in,
				Logger log) {
		this.name	= name;
		this.in		= in;
		this.log	= log;
		this.size	= 0L;
		this.pds	= partialDumpSize;
		this.ds		= dumpSize;
		this.beginning = new byte[ds];
		
		// ���� new ���Ȃ�����Ƃ�����
		this.buf	= new byte[BUFFER_SIZE];
	}
	
/*----------------
 * static methods
 */
	public static void setPartialDumpSize(int pds) {
		if (pds > BUFFER_SIZE) pds = BUFFER_SIZE;
		partialDumpSize = pds;
	}
	
	public static void setDumpSize(int ds) {
		if (ds > BUFFER_SIZE) ds = BUFFER_SIZE;
		dumpSize = ds;
	}
	
/*-----------
 * overrides
 */
	public void run() {
		// ���M���O�J�n
		log.finer(name + "���̓X�g���[�����M���O�J�n");
		
		//
		try {
			while (true) {
				int c = 0;
				try {
					c = in.read(buf);
					if (c == -1) break;
					size += c;
					
					int n = c;
					if (n > pds) n = pds;
					log.finer(name + "�ꕔ����,(size=,"+c+",),"+ByteArray.toString(buf, 0, n));
					
					// �擪���L�^����
					if (beginningSize < beginning.length) {
						// �܂��擪���L�^���Ă��Ȃ�
						if (c >= beginning.length - beginningSize) {
							System.arraycopy(buf, 0,
											beginning, beginningSize,
											beginning.length - beginningSize);
							beginningSize += beginning.length - beginningSize;
						} else {
							System.arraycopy(buf, 0,
											beginning, beginningSize,
											c);
							beginningSize += c;
						}
					}
				} catch (java.net.SocketException ignored) {
					log.finest(name + ":" + ignored.toString());
					break;
				}
				// out.flush();
			}
		} catch (IOException e) {
			log.severe(name + ":" + e.toString());
		}
		
		log.info(name + "���̓X�g���[���I��,(size=," + size
					+ ",)," + ByteArray.toString(beginning, 0, beginningSize) );
		try {
			in.close();
		} catch (IOException e) {
			log.severe(name + ":failed closing in:" + e.toString());
		}
	}
	
}
 