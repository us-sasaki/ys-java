package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;

public class C8y_Relay extends C8yData {
	/**
	 * OPEN commands the relay in to the open position, CLOSED commands it
	 * to the closed position.
	 * value "OPEN", "CLOSED"
	 */
	public String relayState;
}
