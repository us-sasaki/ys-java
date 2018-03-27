package com.ntt.tc.net;

import com.ntt.tc.data.alarms.Alarm;

public interface AlarmListener {
	void alarmRaised(Alarm alarm);
}
