import java.net.*;
import java.io.*;
import java.util.Properties;
import java.util.logging.*;

/**
 * Socket の通信内容をロギングするツール
 * Socket は同時接続分を並行処理する
 */
public class SockServer {
	
	protected static final Properties DEFAULT_PROP;
	static {
		DEFAULT_PROP = new Properties();
		DEFAULT_PROP.setProperty("log.file.name", "SampleLogging%u.%g.log");
		DEFAULT_PROP.setProperty("log.level", "INFO");
		DEFAULT_PROP.setProperty("log.dump.size", "32");
		DEFAULT_PROP.setProperty("log.partial.dump.size", "32");
		DEFAULT_PROP.setProperty("server.port", "1521");
		DEFAULT_PROP.setProperty("server.backlog", "10");
	}
	
    /**
     * ログ設定プロパティファイルのファイル内容
     */
	protected static final String LOGGING_PROPERTIES_DATA
        = "handlers=java.util.logging.ConsoleHandler, java.util.logging.FileHandler\n"
        + ".level=INFO\n"
        + "java.util.logging.ConsoleHandler.level=INFO\n"
        + "java.util.logging.ConsoleHandler.formatter=LogFormatter\n"
        
		+ "java.util.logging.FileHandler.level=INFO\n"
		+ "java.util.logging.FileHandler.pattern=SampleLogging%u.%g.log\n"
		+ "java.util.logging.FileHandler.formatter=LogFormatter\n"
		+ "java.util.logging.FileHandler.count=10";

	
	protected Properties prop;
	protected String	serverAddr;
	protected int		port;
	protected Logger	logger;
	protected int		serverPort;
	protected int		backlog;
	
	
/*-------------
 * constructor
 */
	public SockServer(String propFileName) {
		prop = DEFAULT_PROP;
		try {
			FileInputStream fin = new FileInputStream(propFileName);
			prop.load(fin);
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				FileOutputStream fout = new FileOutputStream(propFileName);
				prop.store(fout, "# SocketLogger プロパティファイル");
			} catch (IOException ignored) {
			}
		}
		
		//
		try {
			serverPort	= Integer.parseInt(prop.getProperty("server.port"));
		} catch (IllegalArgumentException ignored) {
		}
		backlog		= Integer.parseInt(prop.getProperty("server.backlog"));
		
		// 実行中 prop ファイル変更不可
		StreamLogger.setDumpSize(Integer.parseInt(prop.getProperty("log.dump.size")));
		StreamLogger.setPartialDumpSize(Integer.parseInt(prop.getProperty("log.partial.dump.size")));
	}
	
/*------------------
 * instance methods
 */
	public void main() {
		String logprop
		        = "handlers=java.util.logging.ConsoleHandler, java.util.logging.FileHandler\n"
        + ".level=" + prop.getProperty("log.level") + "\n"
        + "java.util.logging.ConsoleHandler.level="
        		+ prop.getProperty("log.level") + "\n"
        + "java.util.logging.ConsoleHandler.formatter=LogFormatter\n"
        
		+ "java.util.logging.FileHandler.level="
				+ prop.getProperty("log.level") + "\n"
		+ "java.util.logging.FileHandler.pattern="
				+ prop.getProperty("log.file.name") + "\n"
		+ "java.util.logging.FileHandler.formatter=LogFormatter\n"
		+ "java.util.logging.FileHandler.count=10";
		
		try {
			LogManager.getLogManager().readConfiguration(
				new ByteArrayInputStream(logprop.getBytes("UTF-8")));
			
			logger = Logger.getLogger("SocketLogger");
			logger.info("SocketLogger,,,起動。ログ取得開始");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("例外が発生しましたが、サーバ起動します");
		}
		
		//
		try {
			ServerSocket	ss = new ServerSocket(serverPort, backlog);
			int id	= 10000;
			
			while (true) {
				Socket sock = ss.accept();
				
				// 接続された場合
				logger.info("SocketLogger,"+id+",,accept");
				
				new SocketHandler(sock, id, logger).start();
				
				id++;
			}
		} catch (IOException e) {
			logger.severe(e.toString() + " SocketLogger IOException");
		}
	}
	
/*
 * main
 */
	public static void main(String[] args) throws Exception {
		SockServer sl = new SockServer("SocketLogger.prop");
		sl.main();
	}
	
}


