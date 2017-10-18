package com.ntt.tc.net;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.TC_Date;

/**
 * Real-time notification で受け取るイベントです。
 * REST 応答メッセージの中の data フィールドの１要素を保持します。
 *
 * @author	Yusuke Sasaki
 * @version	October 18, 2017
 */
public class C8yEvent extends C8yData {
	public String id;
	public TC_Date creationTime;
	public String type;
	public TC_Date time;
	public String text;
	
	/**
	 * ManagedObject 型ですが、軽量化のため JsonObject としています。
	 */
	public JsonObject source;
}
