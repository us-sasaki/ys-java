package com.ntt.tc.data.services;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.TC_Date;
import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.inventory.ID;

/**
 * Simulator class.
 * 非公開の API /service/ で使用されるオブジェクト。
 * 各 Simulator を生成するためのファクトリメソッドを含みます。
 *
 * @version		February 8, 2019
 * @author		Yusuke Sasaki
 */
public class Simulator extends C8yData {
	public String id;
	public int instances;
	public String name;
	public String state; // PAUSED
	
	// 他に以下がある(温度シミュレーター)
	//JsonObject supportedOperations
	//JsonArray commandQueue
	//JsonObject curentIndex
/*-----------------
 * Factory methods
 */
}
