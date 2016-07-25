import java.io.*;
import java.util.logging.*;

/**
 * 入力ストリーム内容を監視し、ロギングする
 */
public class StreamLogger extends Thread {
	/** 入力ストリームのバッファ */
	protected static final int BUFFER_SIZE = 32768;

	/** 入力ストリームを識別する名前 */
	protected String	name;
	
	/** 監視対象の入力ストリーム */
	protected InputStream in;
	
	/** ログ */
	protected Logger	log;
	
	/** 入力ストリームを通ったデータのサイズ */
	protected long		size;
	
	/** 内部バッファ */
	private byte[]		buf;
	
	/** ログ出力のための先頭数バイト書き込み先 */
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
		
		// 毎回 new しないつくりとしたい
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
		// ロギング開始
		log.finer(name + "入力ストリームロギング開始");
		
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
					log.finer(name + "一部入力,(size=,"+c+",),"+ByteArray.toString(buf, 0, n));
					
					// 先頭を記録する
					if (beginningSize < beginning.length) {
						// まだ先頭を記録していない
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
		
		log.info(name + "入力ストリーム終了,(size=," + size
					+ ",)," + ByteArray.toString(beginning, 0, beginningSize) );
		try {
			in.close();
		} catch (IOException e) {
			log.severe(name + ":failed closing in:" + e.toString());
		}
	}
	
}
 