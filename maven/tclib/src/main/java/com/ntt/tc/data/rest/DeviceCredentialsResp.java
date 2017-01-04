package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;

public class DeviceCredentialsResp extends C8yData {
	public String id;
	public String self;
	public String tenantId;
	public String username;
	public String password;
	
	public boolean isValid() {
		return (username != null && password != null &&
				!"".equals(username) && !"".equals(password) );
	}
}
