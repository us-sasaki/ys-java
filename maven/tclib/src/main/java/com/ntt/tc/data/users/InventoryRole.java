package com.ntt.tc.data.users;

import com.ntt.tc.data.C8yData;

/**
 * InventoryRole class
 *
 * インベントリロールを表すクラス。
 * 非公開APIのため、RESTレスポンスから項目を抽出。
 *
 * @version		September 4, 2018
 * @author		Yusuke Sasaki
 */
public class InventoryRole extends C8yData {
	
	/**
	 * 説明。ロール設定画面で設定、表示される内容。
	 * 改行は LF。
	 */
	public String description;
	
	/**
	 * 順番に割り振られるらしい id
	 */
	public int id;
	
	/**
	 * 画面から設定可能なロールの名称
	 */
	public String name;
	
	/**
	 * パーミッション。ロール設定画面から設定、表示可能
	 */
	public Permission[] permissions;
	
}
