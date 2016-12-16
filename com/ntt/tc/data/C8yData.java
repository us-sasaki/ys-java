package com.ntt.tc.data;

import abdom.data.json.object.JData;

/**
 * Cumulocity のデータのスーパークラスです。
 * カテゴリ分けのほか、JData の直列化、c8y 特有のルールに対応するメソッドを
 * 提供します。
 * C8yData は JData を継承しているため、JSON 直列化をサポートします。
 * 例えば、<pre>
 * System.out.println(new ManagedObject().toJson().toString("  "));
 * </pre>
 * を実行すると、ManagedObject の JSON 形式が得られます。
 */
public abstract class C8yData extends JData {
	
	/**
	 * フィールドをすべてクリア(Objecct = null, primitive = 0)とするメソッド
	 * いるか？ 毎回 new するでよいのでは。
	 */
	public void clearSelf() {
	}
	
//	public C8yData getDifference(C8yData a, C8yData b) {
//		if (!a.getClass().isAssignableFrom(b.getClass()))
//			throw new ClassCastException("Can not cast type of b to type of a.");
//		C8yData result = C8yData.getClass().newInstance();
//	}
	
}
