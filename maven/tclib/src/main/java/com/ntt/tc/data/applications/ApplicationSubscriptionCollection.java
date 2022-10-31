package com.ntt.tc.data.applications;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * ApplicationSubscriptionCollection class
 * /application/currentApplication/subscriptions の返却値。
 * Microservice で bootstrapuser から User 情報を得るのに使用。
 * Guide 上にオブジェクト形式の定義がないため、名前含め想定項目とする。
 *
 * @version		25 Nov, 2019
 */
public class ApplicationSubscriptionCollection extends C8yData {
	/**
	 * self, link to this resource
	 */
	public String self;
	
	/**
	 * サブスクリプションしているユーザー一覧。
	 */
	public ApplicationUser[] users;
	
	/**
	 * ページ統計情報
	 */
	public PagingStatistics statistics;
	
	public String prev;
	public String next;
}
