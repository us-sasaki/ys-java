package abdom.data.json.object;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

/**
 * 日付値をとる JValue オブジェクトです。
 * このオブジェクトは、フォーマット(DateFormat)の参照を保持しており、
 * これに基づいて JSON 変換が行われます。
 * フォーマットは defaultFormat として指定するか、オブジェクト生成時に
 * 指定します。defaultFormat はスレッドごとに保持されます。
 * このオブジェクト自体はスレッドセーフではありません。
 *
 * @version		January 9, 2018
 * @author		Yusuke Sasaki
 */
public class JDate extends JValue {
	protected static ThreadLocal<DateFormat> defaultFormat =
		new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			}
		};
	
	protected Date date;
	protected JsonValue cachedDate = null;
	protected DateFormat format;
	
/*-------------
 * constructor
 */
	/**
	 * デフォルトコンストラクタでは、現在時刻を設定します。
	 */
	public JDate() {
		date = new Date();
		format = defaultFormat.get();
	}
	
	/**
	 * 与えられた Date の日付を示すインスタンスを生成します。
	 *
	 * @param	date	設定する Date 値
	 */
	public JDate(Date date) {
		this.date = date;
		format = defaultFormat.get();
	}
	
	/**
	 * 与えられた文字列形式の日付を示すインスタンスを生成します。
	 *
	 * @param	date	文字列形式
	 */
	public JDate(String date) {
		format = defaultFormat.get();
		set(date);
	}
	
	/**
	 * ミリ秒を与えてインスタンスを生成します。
	 * これによって、このオブジェクトは、「エポック」(すなわち、1970 年 1 月
	 * 1 日 00:00:00 GMT) である標準時からの指定されたミリ秒数を表します。
	 */
	public JDate(long date) {
		this.date = new Date(date);
		format = defaultFormat.get();
	}
	
/*------------------
 * instance methods
 */
	/**
	 * デフォルトで使用する DateFormat を指定します。
	 * この指定は、ThreadLocal 変数に格納されるため、スレッドごとに
	 * 指定が必要です。
	 *
	 * @param		dateFormat		以降このスレッドで日付解析に利用する
	 *								デフォルトの DateFormat オブジェクト
	 */
	public static void setDefaultFormat(DateFormat df) {
		defaultFormat.set(df);
	}
	
	/**
	 * 文字列表現でインスタンスの日付を変更します。
	 * 文字列では、"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" のフォーマットを使用します。
	 * getValue() と互換性があります。
	 *
	 * @param	date	文字列形式
	 */
	public void set(String date) {
		try {
			this.date = format.parse(date);
		} catch (ParseException pe) {
			throw new IllegalArgumentException(pe.toString());
		}
		cachedDate = null;
	}
	
	/**
	 * 文字列値を取得します。
	 * 文字列では、"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" のフォーマットを使用します。
	 * set(String) と互換性があります。
	 *
	 * @return	"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" 形式の文字列表現
	 * @see		#set(String)
	 */
	public String getValue() {
		return format.format(date);
	}
	
	/**
	 * java.util.Date 値を取得します。
	 *
	 * @return	このオブジェクトの java.util.Date 値
	 */
	public Date toDate() {
		return date;
	}
	
	/**
	 * このオブジェクトで表される、1970 年 1 月 1 日 00:00:00 GMT からのミリ秒
	 * 数を返します。
	 *
	 * @return	この日付で表される、1970 年 1 月 1 日 00:00:00 GMT からの
	 *			ミリ秒数
	 */
	public long getTime() {
		return date.getTime();
	}
	
/*-----------
 * overrides
 */
	/**
	 * Json 形式(JsonValue (string))でインスタンスの値を変更します。
	 *
	 * @param	jt	値を持っている JsonType。JsonValue (string) である
	 *			必要があり、そうでない場合、ClassCastException がスロー
	 *			されます。
	 */
	@Override
	public void fill(Jsonizable jt) {
		JsonValue jv = (JsonValue)jt.toJson();
		String str = jv.getValue();
		set(str);
	}
	
	/**
	 * Json 表現を取得します。JsonValue (string) の型、
	 * "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" のフォーマットで返却されます。
	 *
	 * @return	JsonValue 値
	 */
	@Override
	public JsonType toJson() {
		if (cachedDate == null)
			cachedDate = new JsonValue(defaultFormat.get().format(date));
		return cachedDate;
	}
	
}
