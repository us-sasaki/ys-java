package com.ntt.tc.data;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;
import abdom.data.json.object.JValue;

/**
 * C8y で使用される、日付のフォーマットです。
 * java.util.Date との相互変換が可能です。
 * 文字列では、"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" のフォーマットを使用します。
 *
 * @version		November 19, 2016
 * @author		Yusuke Sasaki
 */
public class TC_Date extends C8yValue {
	/**
	 * スレッドごとに SimpleDateFormat インスタンスを分ける必要があるため、
	 * ThreadLocal 利用。
	 */
	protected static ThreadLocal<SimpleDateFormat> sdf =
			new ThreadLocal<SimpleDateFormat>() {
				@Override
				protected SimpleDateFormat initialValue() {
					return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
				}
			};
	
	/** 内部的には java.util.Date として値を保持します */
	protected Date date;
	
	/** toJson() を高速化するためのキャッシュ */
	protected JsonValue dateCache = null;
	
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
	 */
	public TC_Date(long date) {
		this.date = new Date(date);
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
		dateCache = null;
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
	
	/**
	 * 1970 年 1 月 1 日 00:00:00 GMT からのミリ秒値でこのオブジェクトを
	 * 設定します。
	 */
	strictfp // JSON プロパティから除外
	public void setTime(long time) {
		date.setTime(time);
		dateCache = null;
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
		if (dateCache == null)
			dateCache = new JsonValue(sdf.get().format(date));
		return dateCache;
	}
	
}
