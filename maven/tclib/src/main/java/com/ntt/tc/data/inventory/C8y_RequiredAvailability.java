package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;

/**
 * オブジェクトを定義したほうがよいのか、概念、利用例を要確認
 */
public class C8y_RequiredAvailability extends C8yData {
	/**
	 * 予期されるレスポンス時間(分) -32768～32767
	 */
	public int responseInterval;
}
