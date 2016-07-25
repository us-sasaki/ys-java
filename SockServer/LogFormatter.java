import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * シンプルなログフォーマッタ
 */
public class LogFormatter extends Formatter {
	/**
	 * 定数
	 */
	
	/** 改行コード */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	/**
	 * 日付フォーマット (yyyyMMdd HH:mm:ss.SSS)
	 */
    private final SimpleDateFormat sdFormat 
        = new SimpleDateFormat("yyyyMMdd,\"HH:mm:ss.SSS\",");
	
/*-----------
 * overrides
 */
    public String format(final LogRecord argLogRecord) {
        final StringBuffer buf = new StringBuffer();

        buf.append(sdFormat.format(
            new Date(argLogRecord.getMillis())));
        buf.append(" ");
		
		Level level = argLogRecord.getLevel();
		
		if (level == Level.FINEST) buf.append("FNST,");
		else if (level == Level.FINER) buf.append("FINR,");
		else if (level == Level.FINE) buf.append("FINE,");
		else if (level == Level.CONFIG) buf.append("CONF,");
		else if (level == Level.INFO) buf.append("INFO,");
		else if (level == Level.WARNING) buf.append("WARN,");
		else if (level == Level.SEVERE) buf.append("SEVR,");
		else {
            buf.append(Integer.toString(argLogRecord.getLevel()
                .intValue()));
            buf.append(" ");
        }
//		buf.append(" ");
//		buf.append(argLogRecord.getLoggerName());
        buf.append("-,");
        buf.append(argLogRecord.getMessage());
        buf.append(LINE_SEPARATOR);

        return buf.toString();
    }
}
