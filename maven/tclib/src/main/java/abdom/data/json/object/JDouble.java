package abdom.data.json.object;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

/**
 * オブジェクトとしての double 値(nullを取りうる)をラップします。
 * 初期状態、および JsonValue(null) で fill した場合、toJson() は
 * JsonType.NULL を返却します。
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
	 */
	public double doubleValue() {
		if (value == null) return Double.NaN;
		return value;
	}
	
/*-----------
 * overrides
 */
	@Override
	public JsonType toJson() {
		if (value == null) return JsonType.NULL;
		if (cachedValue == null)
			cachedValue = new JsonValue(value.doubleValue());
		
		return cachedValue;
	}
	
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
			this.value = Double.parseDouble(j.getValue());
			cachedValue = null;
			break;
		
		case JsonType.TYPE_DOUBLE:
			this.value = j.doubleValue();
			cachedValue = (JsonValue)j; // j is immutable.
			break;
		
		case JsonType.TYPE_INT:
			this.value = j.doubleValue();
			cachedValue = null;
			break;
		
		default:
			throw new IllegalFieldTypeException("JDouble に指定できない値を指定しました:"+j);
		}
	}
}
