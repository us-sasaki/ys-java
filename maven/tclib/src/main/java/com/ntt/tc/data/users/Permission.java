package com.ntt.tc.data.users;

import com.ntt.tc.data.C8yData;

/**
 * InventoryRole class
 *
 * インベントリロール中の Permission を表すクラス。
 * 非公開APIのため、RESTレスポンスから項目を抽出。
 *
 * @version		September 4, 2018
 * @author		Yusuke Sasaki
 */
public class Permission extends C8yData {
	
	/**
	 * 順番に割り振られるらしい id
	 */
	public int id;
	
	/**
	 * パーミッション。"ADMIN", "READ", "*" が格納されている。
	 * "ADMIN" は変更で読取を含まないと思われる。
	 */
	public String permission;
	
	/**
	 * スコープ。
	 * "OPERATION", "MANAGED_OBJECT", "MEASUREMENT", "ALARM", "EVENT", "*"
	 * が格納されている。"*" は UI 上、フルアクセスと記載されているもの。
	 */
	public String scope;
	
	/**
	 * 対象とするスコープのタイプ。"*", "c8y_Restart" が格納されている。
	 */
	public String type;
	
}
