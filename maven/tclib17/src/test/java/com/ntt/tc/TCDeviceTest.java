package com.ntt.tc;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.ntt.tc.data.*;
import com.ntt.tc.data.inventory.*;
import com.ntt.tc.data.device.*;
import com.ntt.tc.net.Rest;

/**
 * TC_Data のテスト。
 */
public class TCDeviceTest extends TestCase{
	private Rest rest;
	
	private Rest getRest() {
		if (rest == null) rest = new Rest("https://nttcom.cumulocity.com", "management", "", "");
		return rest;
	}
	
	public void testDeleteDevice() throws IOException {
		
	}
	public void testDeviceCredential() throws IOException {
		
		DeviceCredentials dc = new DeviceCredentials();
		dc.id = "tcDeviceTest";
		Rest r = new Rest("https://nttcom.cumulocity.com", "management", "devicebootstrap", "Fhdt1bb1f");
//		Rest.Response resp = r.post("/devicecontrol/deviceCredentials", "deviceCredentials", dc.toJson());
//		System.out.println("resp.code = " + resp.code);
//		System.out.println("resp.msg  = " + resp.message);
//		System.out.println("resp.toJson() = " + resp.toJson());
//		assertEquals(resp.code, 404);
	}
	
	public void testCreateAndDeleteNewDevice() {
		
	}
	public void testPop() {
	}
}
