package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;

/**
 * NewDeviceRequest class
 * status に登録できる文字列を固定値化し、getter/setter を作成
 */
public class NewDeviceRequest extends C8yData {
	public static final String WAITING_FOR_CONNECTION =
							"WAITING_FOR_CONNECTION";
	public static final String PENDING_ACCEPTANCE =
							"PENDING_ACCEPTANCE";
	public static final String ACCEPTED = "ACCEPTED";
	
	/**
	 * Device identifier. Max: 1000 characters. E.g. IMEI
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String id;
	
	/**
	 * Status of registration, one of: WAITING_FOR_CONNECTION,
	 * PENDING_ACCEPTANCE, ACCEPTED
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	protected String status;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		if (!WAITING_FOR_CONNECTION.equals(status) &&
			!PENDING_ACCEPTANCE.equals(status) &&
			!ACCEPTED.equals(status) )
				throw new IllegalArgumentException("NewDeviceRequest の status には、"+WAITING_FOR_CONNECTION+
					", "+PENDING_ACCEPTANCE+", "+ACCEPTED+
					" のいずれかのみ設定できます。指定値:"+status);
		this.status = status;
	}
	
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
}
