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
	
	public JDouble() {
		super();
	}
	
	public JDouble(double v) {
		super();
		value = v;
	}
	
	public JDouble(String v) {
		if (!"".equals(v)) {
			try {
				value = Double.parseDouble(v);
			} catch (NumberFormatException nfe) {
			}
		}
	}
	
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
