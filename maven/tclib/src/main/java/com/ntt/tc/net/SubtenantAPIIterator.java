package com.ntt.tc.net;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.ntt.tc.data.tenants.Tenant;

/**
 * Management テナントの Rest オブジェクトから、
 * Suspend でないサブテナントの API を取得する Iterator です。
 * 全テナントに対する処理を記述するのに便利な Iterator です。
 *
 * @author		Yusuke Sasaki
 * @version		May 27, 2019
 */
class SubtenantAPIIterator implements Iterator<API> {
	API managementAPI;
	String url;
	String supportUser;
	String supportPassword;
	Iterator<Tenant> tenants;
	Tenant nextTenant = null;
	
/*-------------
 * constructor
 */
	SubtenantAPIIterator(API managementAPI) {
		tenants = managementAPI.tenants().iterator();
		Rest r = managementAPI.getRest();
		url = r.getLocation();
		supportUser = r.getUser() + "$";
		supportPassword = r.getPassword();
	}
	
/*------------------
 * instance methods
 */
	private void fetch() {
		while (true) {
			if (!tenants.hasNext()) {
				nextTenant = null;
				break;
			}
			nextTenant = tenants.next();
			if (!"SUSPENDED".equals(nextTenant.status)) break;
		}
	}
	
	@Override
	public boolean hasNext() {
		if (nextTenant == null) fetch();
		return (nextTenant != null);
	}
	
	@Override
	public API next() {
		if (nextTenant == null) fetch(); // 最初または終了後では fetch()
		if (nextTenant == null)
				throw new NoSuchElementException("SubtenantAPIIterator が終わりに達している状態で next() を呼び出しました。");
		API result = new API(url, nextTenant.id, supportUser, supportPassword);
		fetch(); // 次のを準備
		return result;
	}
}
