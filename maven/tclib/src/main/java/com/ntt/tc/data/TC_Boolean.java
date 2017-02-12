package com.ntt.tc.data;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.Jsonizable;
import abdom.data.json.object.IllegalFieldTypeException;

/**
 * TC_Boolean は、boolean のラッパーです。
 * Java オブジェクトではプリミティブ型は必ず値を持ち、null を指定できません。
 * JData 実装では、JSON 変換の際　null でない値は JSON フィールドとして現れる
 * ため、undefined (null) を指定し、JSON フィールドとして「無指定(=表示され
 * ない)」を設定できるようオブジェクト化しています。 
 */
public class TC_Boolean extends C8yValue {
	private static final JsonType CACHED_TRUE = new JsonValue(true);
	private static final JsonType CACHED_FALSE = new JsonValue(false);
	
	protected boolean value;
	
	public void fill(Jsonizable arg) {
		JsonType jt = arg.toJson();
		if (jt.getType() != JsonType.TYPE_BOOLEAN)
			throw new IllegalFieldTypeException();
			
		value = jt.getValue().equals("true");
	}
	
	public JsonType toJson() {
		return (value)? CACHED_TRUE : CACHED_FALSE;
	}
	
	public TC_Boolean() {
	}
	
	public TC_Boolean(boolean value) {
		this.value = value;
	}
	
}
