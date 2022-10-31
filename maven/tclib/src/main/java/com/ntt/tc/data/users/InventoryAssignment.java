package com.ntt.tc.data.users;

import com.ntt.tc.data.C8yData;

/**
 * InventoryAssignment class
 *
 * デバイス(アセット)とインベントリロールの対応を表すクラス。
 * 非公開APIのため、RESTレスポンスから項目を抽出。
 *
 * @version		September 4, 2018
 * @author		Yusuke Sasaki
 */
public class InventoryAssignment extends C8yData {
	
	/**
	 * 
	 */
	public String self;
	
	/**
	 * 順に割り当てられると思われる id
	 */
	public int id;
	
	/**
	 * 紐づけられる managedObject (アセット)の id
	 */
	public String managedObject;
	
	/**
	 * 割り当てられるインベントリロール
	 */
	public InventoryRole[] roles;
	
}
