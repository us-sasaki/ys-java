package com.ntt.tc.data.users;

import com.ntt.tc.data.C8yData;

/**
 * InventoryAssignmentCollection class
 *
 * /user/&lt;tenant&gt;/users/&lt;username&gt;/roles/inventory
 * でアクセスされる、ユーザに割り当てられるインベントリロールを保持するクラス
 * 非公開APIのため、RESTレスポンスから項目を抽出。
 *
 * @version		September 4, 2018
 * @author		Yusuke Sasaki
 */
public class InventoryAssignmentCollection extends C8yData {
	
	/**
	 * Link to this resource
	 */
	public String self;
	
	/**
	 * 割り当てられるインベントリロールリスト
	 */
	public InventoryAssignment[] inventoryAssignments;
}
