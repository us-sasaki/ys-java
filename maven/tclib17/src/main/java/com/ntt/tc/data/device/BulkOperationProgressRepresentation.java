package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;

/**
 * BulkOperationProgressRepresentation class
 * c8y docs にないため、新規作成
 */
public class BulkOperationProgressRepresentation extends C8yData {
	public int pending;
	public int failed;
	public int executing;
	public int successful;
	public int all;
}
