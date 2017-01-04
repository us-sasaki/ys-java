package com.ntt.tc.data;

import abdom.data.json.object.JValue;

/**
 * Cumulocity のデータのスーパークラスです。
 * カテゴリ分けのほか、JValue の直列化、c8y 特有のルールに対応するメソッドを
 * 提供します。
 */
public abstract class C8yValue extends JValue {
	
	/**
	 * 
	 */
	public void clearSelf() {
	}
	
//	public C8yData getDifference(C8yData a, C8yData b) {
//		if (!a.getClass().isAssignableFrom(b.getClass()))
//			throw new ClassCastException("Can not cast type of b to type of a.");
//		C8yData result = C8yData.getClass().newInstance();
//	}
	
}
