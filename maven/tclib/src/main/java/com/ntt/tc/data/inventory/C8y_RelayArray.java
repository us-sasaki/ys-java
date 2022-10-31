package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;

/**
 * オブジェクトを定義したほうがよいのか、概念、利用例を要確認
 */
public class C8y_RelayArray extends C8yData {
	/**
	 * OPEN commands the relay in to the open position, CLOSED commands it
	 * to the closed position.
	 * value "OPEN", "CLOSED"
	 */
	public String[] c8y_RelayArray;
}
