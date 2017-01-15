package com.ntt.tc.data;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.object.IllegalFieldTypeException;

/**
 * TC_Int は、int のラッパーです。
 * Java オブジェクトではプリミティブ型は必ず値を持ち、null を指定できません。
 * JData 実装では、JSON 変換の際　null でない値は JSON フィールドとして現れる
 * ため、undefined (null) を指定し、JSON フィールドとして「無指定(=表示され
 * ない)」を設定できるようオブジェクト化しています。 
 */

public class TC_Int extends C8yValue {
	protected int value;
	
	public void fill(JsonType arg) {
		if (arg.getType() != JsonType.TYPE_INT)
			throw new IllegalFieldTypeException();
			
		value = arg.intValue();
	}
	
	public JsonType toJson() {
		return new JsonValue(value);
	}
	
	public TC_Int() {
		value = 0;
	}
	
	public TC_Int(int value) {
		this.value = value;
	}
	
	public int intValue() {
		return value;
	}
	
}
