package com.ntt.tc.net;

import com.ntt.tc.data.device.Operation;

public interface OperationListener {
	void operationPerformed(Operation event);
}
