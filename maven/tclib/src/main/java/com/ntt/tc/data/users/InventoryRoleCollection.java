package com.ntt.tc.data.users;

import com.ntt.tc.data.Collection;

/**
 * InventoryRole class
 *
 * インベントリロールを表すクラス。
 * 非公開APIのため、RESTレスポンスから項目を抽出。
 *
 * @version		September 4, 2018
 * @author		Yusuke Sasaki
 */
public class InventoryRoleCollection extends Collection {
	/**
	 * Inventory Role のリスト
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public InventoryRole[] roles;
	
}
