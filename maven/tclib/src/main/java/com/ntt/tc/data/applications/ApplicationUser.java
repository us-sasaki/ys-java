package com.ntt.tc.data.applications;

import com.ntt.tc.data.C8yData;

/**
 * ApplicationUser class
 * microservice で /application/applications/{microservice-applicationId}/bootstrapUser
 * で取得されるオブジェクト。
 * DeviceCredentials とデータ項目は似ているが、異なる。
 *
 * @version		25 Nov, 2019
 */
public class ApplicationUser extends C8yData {
	/**
	 * ユーザー名
	 */
	public String name;
	
	/**
	 * パスワード
	 */
	public String password;
	
	/**
	 * テナント名
	 */
	public String tenant;
}
