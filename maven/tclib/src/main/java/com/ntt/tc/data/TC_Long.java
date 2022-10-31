package com.ntt.tc.data;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;
import abdom.data.json.object.IllegalFieldTypeException;

/**
 * TC_Long は、long のラッパーです。
 * Java オブジェクトではプリミティブ型は必ず値を持ち、null を指定できません。
 * JData 実装では、JSON 変換の際　null でない値は JSON フィールドとして現れる
 * ため、undefined (null) を指定し、JSON フィールドとして「無指定(=表示され
 * ない)」を設定できるようオブジェクト化しています。 
 *
 * @author		Yusuke Sasaki
 * @version		August 20, 2018
 */

public class TC_Long extends C8yValue {
	protected long value;
	protected JsonValue cachedValue = null;
	
	public TC_Long() {
		value = 0;
	}
	
	public TC_Long(long value) {
		this.value = value;
	}
	
	public long longValue() {
		return value;
	}
	
	public void fill(Jsonizable arg) {
		JsonType jt = arg.toJson();
		if (jt.getType() != JsonType.TYPE_INT)
			throw new IllegalFieldTypeException();
			
		value = jt.longValue();
		cachedValue = null;
	}
	
	public JsonType toJson() {
		if (cachedValue == null) cachedValue = new JsonValue(value);
		return cachedValue;
	}
}
