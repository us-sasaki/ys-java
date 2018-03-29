package abdom.data.json.object;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

/**
 * オブジェクトとしての int 値(nullを取りうる)をラップします。
 * 初期状態、および JsonValue(null) で fill した場合、toJson() は
 * JsonType.NULL の値 (JsonValue(null)) を返却します。
 *
 * @version		January 28, 2018
 * @author		Yusuke Sasaki
 */
public class JInteger extends JValue {
	protected Integer value;
	protected JsonValue cachedValue = null;
	
/*-------------
 * constructor
 */
	/**
	 * デフォルトの値のない(JsonValue(null)値を持つ) JInteger を
	 * 生成します。
	 */
	public JInteger() {
	}
	
	/**
	 * 指定値を持つ JDouble を生成します。
	 *
	 * @param	v		初期値
	 */
	public JInteger(int v) {
		value = v;
	}
	
	/**
	 * 指定値を持つ JInteger を生成します。
	 * Integer 値として認識できる( = Integer.parseInt() が成功する)場合
	 * その値となり、それ以外の場合、値のない JInteger が生成されます。
	 *
	 * @param	v		初期値
	 */
	public JInteger(String v) {
		if (!"".equals(v)) {
			try {
				value = Integer.parseInt(v);
			} catch (NumberFormatException nfe) {
			}
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 値を取得します。
	 * 値がない場合、Integer.MIN_VALUE が返却されます。
	 */
	public int intValue() {
		if (value == null) return Integer.MIN_VALUE;
		return value;
	}
	
/*-----------
 * overrides
 */
	/**
	 * JsonType 値を返却します。
	 *
	 * @return		値を持たない場合、JsonValue(null) を返却します。
	 *				値を持つ場合、その値を保持する JsonValue を返却します。
	 */
	@Override
	public JsonType toJson() {
		if (value == null) return JsonType.NULL;
		if (cachedValue == null)
			cachedValue = new JsonValue(value.intValue());
		
		return cachedValue;
	}
	
	/**
	 * Jsonizable 値を設定します。
	 * 型が JsonType における TYPE_VOID, TYPE_STRING, TYPE_DOUBLE, TYPE_INT
	 * 以外の場合、IllegalFieldTypeException がスローされます。
	 * TYPE_VOID であった場合、値をクリアします。
	 * TYPE_STRING であっても、数値と認識することができる場合は設定されます。
	 * 認識できない場合、IllegalFieldTypeException がスローされます。
	 * 
	 * @param		value	設定したい値
	 */
	@Override
	public void fill(Jsonizable value) {
		JsonType j = value.toJson();
		int type = j.getType();
		
		switch (type) {
		case JsonType.TYPE_VOID:
			this.value = null;
			cachedValue = null;
			break;
			
		case JsonType.TYPE_STRING:
			try {
				this.value = Integer.parseInt(j.getValue());
				cachedValue = null;
				break;
			} catch (NumberFormatException nfe) {
				throw new IllegalFieldTypeException("JInteger に指定できない値を指定しました:"+j);
			}
		
		case JsonType.TYPE_DOUBLE:
			this.value = j.intValue();
			cachedValue = (JsonValue)j; // j is immutable.
			break;
		
		case JsonType.TYPE_INT:
			this.value = j.intValue();
			cachedValue = null;
			break;
		
		default:
			throw new IllegalFieldTypeException("JInteger に指定できない値を指定しました:"+j);
		}
	}
}
