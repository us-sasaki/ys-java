package com.ntt.tc.data.services;

import com.ntt.tc.data.C8yData;

/**
 * Dashboard class.
 * 動作確認未
 *
 * @version		September 20, 2019
 * @author		Yusuke Sasaki
 */
public class Dashboard extends C8yData {
	public C8y_Dashboard c8y_Dashboard;
	public C8yData c8y_Global;
	
	public static class C8y_Dashboard extends C8yData {
		public String name;
		public int priority;
		public String icon;
		public boolean global;
		
		public C8y_Dashboard(String name, int priority, String icon) {
			this.name = name;
			this.priority = priority;
			this.icon = icon;
		}
	}
	
/*-------------
 * constructor
 */
	/**
	 * ダッシュボードを生成する際、このオブジェクトを
	 * POST /inventory/managedObjects/{group-id}/childAdditions/
	 * とする。
	 *
	 * @param		name		ダッシュボード名
	 * @param		priority	プライオリティ
	 * @param		icon		アイコン名
	 */
	public Dashboard(String name, int priority, String icon) {
		c8y_Dashboard = new C8y_Dashboard(name, priority, icon);
		c8y_Global = new C8yData();
	}
}
