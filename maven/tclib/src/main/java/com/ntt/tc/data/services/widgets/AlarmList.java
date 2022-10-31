package com.ntt.tc.data.services.widgets;

import abdom.data.json.JsonArray;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ID;

/**
 * ダッシュボードに配置されるアラームリスト widget です。
 * データ構造を定義しています。
 * 未検証
 *
 * @version		September 20, 2019
 * @author		Yusuke Sasaki
 */
public class AlarmList extends C8yData {
	public static class AlarmListConfig extends C8yData {
		C8yData options;
		{
			options = new C8yData();
			options.set("orderMode", "ACTIVE_FIRST");
			options.set("status.ACTIVE", true);
			options.set("status.ACKNOWLEDGED", true);
			options.set("status.CLEARED", true);
			options.set("severity.WARNING", true);
			options.set("severity.MINOR", true);
			options.set("severity.MAJOR", true);
			options.set("types", new JsonArray());
			options.set("device", "");
		}
		ID device;
	}
	/** name */
	public String name = "Alarm list";
	/** 恐らく定数 */
	public String templateUrl = "core_alarms/widget.html";
	/** 恐らく定数 */
	public String configTemplateUrl = "core_alarms/widget-config.html";
	/** 画面上に見えるタイトル */
	public String title;
	
	public int col;
	public C8yData config;
	public String id;
	public int position = 0;
}
