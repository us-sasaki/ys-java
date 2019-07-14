package abdom.data.json.object;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

/**
 * オブジェクトとしての double 値(nullを取りうる)をラップします。
 * 初期状態、および JsonValue(null) で fill した場合、toJson() は
 * JsonType.NULL の値 (JsonValue(null)) を返却します。
 *
 * @author		Yusuke Sasaki
 */
public class JDouble extends JValue {
	protected Double value;
	protected JsonValue cachedValue = null;
	
/*-------------
 * constructor
 */
	/**
	 * デフォルトの値のない(JsonValue(null)値を持つ) JDouble を
	 * 生成します。
	 */
	public JDouble() {
	}
	
	/**
	 * 指定値を持つ JDouble を生成します。
	 *
	 * @param	v		初期値
	 */
	public JDouble(double v) {
		value = v;
	}
	
	/**
	 * 指定値を持つ JDouble を生成します。
	 * Double 値として認識できる( = Double.parseDouble() が成功する)場合
	 * その値となり、それ以外の場合、値のない JDouble が生成されます。
	 *
	 * @param	v		初期値
	 */
	public JDouble(String v) {
		if (!"".equals(v)) {
			try {
				value = Double.parseDouble(v);
			} catch (NumberFormatException nfe) {
			}
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 値を取得します。
	 * 値がない場合、Double.NaN が返却されます。
	 *
	 * @return	double 値
	 */
	public double doubleValue() {
		if (value == null) return Double.NaN;
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
			cachedValue = new JsonValue(value.doubleValue());
		
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
				this.value = Double.parseDouble(j.getValue());
				cachedValue = null;
				break;
			} catch (NumberFormatException nfe) {
				throw new IllegalFieldTypeException("JDouble に指定できない値を指定しました:"+j);
			}
		
		case JsonType.TYPE_DOUBLE:
			this.value = j.doubleValue();
			cachedValue = (JsonValue)j; // j is immutable and double itself.
			break;
		
		case JsonType.TYPE_INT:
			this.value = j.doubleValue();
			cachedValue = (JsonValue)j; // j is immutable and considered as double
			break;
		
		default:
			throw new IllegalFieldTypeException("JDouble に指定できない値を指定しました:"+j);
		}
	}
}
