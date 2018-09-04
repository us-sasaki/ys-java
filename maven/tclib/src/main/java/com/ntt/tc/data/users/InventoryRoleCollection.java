package com.ntt.tc.data.users;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * InventoryRole class
 *
 * インベントリロールを表すクラス。
 * 非公開APIのため、RESTレスポンスから項目を抽出。
 *
 * @version		September 4, 2018
 * @author		Yusuke Sasaki
 */
public class InventoryRoleCollection extends C8yData {
	
	/**
	 * Link to this Resource
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Inventory Role のリスト
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public InventoryRole[] roles;
	
	/**
	 * Information about the paging statistics
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a possible previous page with additional roles
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a possible next page with additional roles
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
	
}
