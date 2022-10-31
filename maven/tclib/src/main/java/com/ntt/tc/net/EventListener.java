package com.ntt.tc.net;

import com.ntt.tc.data.events.Event;

public interface EventListener {
	void eventReceived(Event event);
}
