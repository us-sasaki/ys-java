package com.ntt.tc.data.rest;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.Statistics;
import com.ntt.tc.data.ManagedObject;

public class ManagedObjectCollectionResp extends C8yData {
	public String self;
	public ManagedObject[] managedObjects;
	public Statistics statistics;
	public String next;
	public String prev;
}
