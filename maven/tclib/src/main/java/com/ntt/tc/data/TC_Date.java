package com.ntt.tc.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;
import abdom.data.json.object.JValue;

/**
 * C8y で使用される、日付のフォーマットです。
 * java.util.Date との相互変換が可能です。
 * 文字列では、"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" のフォーマットを使用します。
 * JSON との相互変換は、本フォーマットの文字列として行われます。
 *
 * @version		November 19, 2016
 * @author		Yusuke Sasaki
 */
public class TC_Date extends C8yValue implements Comparable<TC_Date> {
	/**
	 * スレッドごとに SimpleDateFormat インスタンスを分ける必要があるため、
	 * ThreadLocal 利用。
	 */
	protected static ThreadLocal<DateFormat> sdf =
			new ThreadLocal<DateFormat>() {
				@Override
				protected DateFormat initialValue() {
					return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
				}
			};
	
	protected static ThreadLocal<AtomicInteger> sdfVer =
			new ThreadLocal<AtomicInteger>() {
				@Override
				protected AtomicInteger initialValue() {
					return new AtomicInteger(0);
				}
			};
	
	/** 内部的には java.util.Date として値を保持します */
	protected Date date;
	
	/** toJson() を高速化するためのキャッシュ */
	protected JsonValue dateCache = null;
	protected int ver = -1;
	
/*-------------
 * Constructor
 */
	/**
	 * デフォルトコンストラクタでは、現在時刻を設定します。
	 */
	public TC_Date() {
		date = new Date();
	}
	
	/**
	 * 与えられた Date の日付を示すインスタンスを生成します。
	 *
	 * @param	date	設定する Date 値
	 */
	public TC_Date(Date date) {
		this.date = date;
	}
	
	/**
	 * 与えられた文字列形式の日付を示すインスタンスを生成します。
	 *
	 * @param	date	文字列形式
	 */
	public TC_Date(String date) {
		set(date);
	}
	
	/**
	 * ミリ秒を与えてインスタンスを生成します。
	 * これによって、このオブジェクトは、「エポック」(すなわち、1970 年 1 月
	 * 1 日 00:00:00 GMT) である標準時からの指定されたミリ秒数を表します。
	 *
	 * @param	date	ミリ秒
	 */
	public TC_Date(long date) {
		this.date = new Date(date);
	}
	
/*---------------
 * class methods
 */
	/**
	 * デフォルトで使用する DateFormat を指定します。
	 * この指定は、ThreadLocal 変数に格納されるため、スレッドごとに
	 * 指定が必要です。何も指定しない場合のフォーマットは ISO8601 の
	 * SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") です。
	 *
	 * @param		df		以降このスレッドで日付解析に利用する
	 *						デフォルトの DateFormat オブジェクト
	 */
	public static void setDefaultFormat(DateFormat df) {
		sdf.set(df);
		sdfVer.get().incrementAndGet();
	}
	
	/**
	 * デフォルトで使用する TimeZone を指定します。
	 * この指定は、ThreadLocal 変数に格納されるため、スレッドごとに
	 * 指定が必要です。この設定は setDefaultFormat() によって上書き
	 * されます。
	 *
	 * @param		timeZone	以降このスレッドで日付解析に利用する
	 *						デフォルトの TimeZone オブジェクト
	 */
	public static void setTimeZone(TimeZone timeZone) {
		sdf.get().setTimeZone(timeZone);
		sdfVer.get().incrementAndGet();
	}
	
	/**
	 * デフォルトで使用する TimeZone を指定します。
	 * この指定は、ThreadLocal 変数に格納されるため、スレッドごとに
	 * 指定が必要です。この設定は setDefaultFormat() によって上書き
	 * されます。
	 *
	 * @param		id	以降このスレッドで日付解析に利用する
	 *						デフォルトの TimeZone を示す３文字の文字列
	 */
	public static void setTimeZone(String id) {
		setTimeZone(TimeZone.getTimeZone(id));
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 文字列表現でインスタンスの日付を変更します。
	 * 文字列では、"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" のフォーマットを使用します。
	 * getValue() と互換性があります。
	 *
	 * @param	date	文字列形式
	 */
	public void set(String date) {
		try {
			this.date = sdf.get().parse(date);
		} catch (ParseException pe) {
			throw new C8yFormatException(pe.toString());
		}
		//dateCache = null;
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
		return sdf.get().format(date);
	}
	
	/**
	 * java.util.Date 値を取得します。
	 *
	 * @return	このオブジェクトの java.util.Date 値の clone
	 */
	public Date toDate() {
		return (Date)date.clone();
	}
	
	/**
	 * java.util.Date により、このオブジェクトを設定します。
	 *
	 * @param		date		設定する時刻を保持する Date オブジェクト
	 */
	public void fromDate(Date date) {
		setTime(date.getTime());
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
	
	/**
	 * 1970 年 1 月 1 日 00:00:00 GMT からのミリ秒値でこのオブジェクトを
	 * 設定します。
	 *
	 * @param		time		ミリ秒
	 */
	strictfp // JSON プロパティから除外
	public void setTime(long time) {
		date.setTime(time);
		sdfVer.get().incrementAndGet();
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
		if (dateCache == null || sdfVer.get().get() != ver) {
			dateCache = new JsonValue(sdf.get().format(date));
			ver = sdfVer.get().get();
		}
		return dateCache;
	}
	
	/**
	 * もうひとつの TC_Date と大小比較します。
	 *
	 * @param		another		比較対象の TC_Date
	 * @return		比較結果
	 */
	@Override
	public int compareTo(TC_Date another) {
		return this.date.compareTo(another.date);
	}
}
