package abdom.data.json.object;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;

/**
 * オブジェクトとしての float 値(nullを取りうる)をラップします。
 * 初期状態、および JsonValue(null) で fill した場合、toJson() は
 * JsonType.NULL の値 (JsonValue(null)) を返却します。
 *
 * @author		Yusuke Sasaki
 */
public class JFloat extends JValue {
	protected Float value;
	protected JsonValue cachedValue = null;
	
/*-------------
 * constructor
 */
	/**
	 * デフォルトの値のない(JsonValue(null)値を持つ) JFloat を
	 * 生成します。
	 */
	public JFloat() {
	}
	
	/**
	 * 指定値を持つ JFloat を生成します。
	 *
	 * @param	v		初期値
	 */
	public JFloat(float v) {
		value = v;
	}
	
	/**
	 * 指定値を持つ JDouble を生成します。
	 * Double 値として認識できる( = Float.parseFloat() が成功する)場合
	 * その値となり、それ以外の場合、NumberFormatException がスローされます。
	 *
	 * @param	v		初期値
	 */
	public JFloat(String v) {
		if (!"".equals(v)) {
			value = Float.parseFloat(v);
		}
	}
	
/*------------------
 * instance methods
 */
	/**
	 * 値を取得します。
	 * 値がない場合、Float.NaN が返却されます。
	 *
	 * @return	float値
	 */
	public float floatValue() {
		if (value == null) return Float.NaN;
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
			cachedValue = new JsonValue(value.floatValue());
		
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
				this.value = Float.parseFloat(j.getValue());
				cachedValue = null;
				break;
			} catch (NumberFormatException nfe) {
				throw new IllegalFieldTypeException("JFloat に指定できない値を指定しました:"+j);
			}
		
		case JsonType.TYPE_DOUBLE:
			this.value = j.floatValue();
			cachedValue = null;
			break;
		
		case JsonType.TYPE_INT:
			this.value = j.floatValue();
			cachedValue = null;
			break;
		
		default:
			throw new IllegalFieldTypeException("JFloat に指定できない値を指定しました:"+j);
		}
	}
}
