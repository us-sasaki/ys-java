package com.ntt.tc.net;

import java.util.List;

class C8yEventHandler extends Thread {
	List<C8yEventListener> listeners;
	List<String> clientId;
	API api;
	
	
	
	void addC8yEventListener(C8yEventListener listener) {
		listeners.add(listener);
		// handshake
		
		// subscribe
		
	}
	
	@Override
	public void run() {
		// connect
		
	}
	
}
