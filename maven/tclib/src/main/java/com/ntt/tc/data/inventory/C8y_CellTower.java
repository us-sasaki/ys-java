package com.ntt.tc.data.inventory;

import com.ntt.tc.data.TC_Int;
import com.ntt.tc.data.C8yData;

/**
 * Detailed information about the neighbouring cell towers.
 * inventory / ManagedObject では Object * 扱いで、
 * Device Management Library に記載されるフィールド。
 */
public class C8y_CellTower extends C8yData {
	/**
	 * The radio type of this cell tower. Can also be put directly in root
	 * JSON element if all cellTowers have same radioType. (Optional)
	 */
	public String radioType;
	
	/**
	 * The Mobile Country Code (MCC).
	 */
	public int mobileCountryCode;
	
	/**
	 * The Mobile Noetwork Code (MNC) for GSM, WCDMA and LTE. The SystemID
	 * (sid) for CDMA.
	 */
	public int mobileNetworkCode;
	
	/**
	 * The Location Area Code (LAC) for GSM, WCDMA and LTE. The Network ID for
	 * CDMA.
	 */
	public int locationAreaCode;
	
	/**
	 * The Cell ID (CID) for GSM, WCDMA and LTE. The Basestation ID for CDMA.
	 */
	public int cellId;
	
	/**
	 * The timing advance value for this cell tower when available. (Optional)
	 */
	public TC_Int timingAdvance;
	
	/**
	 * The signal strength for this cell tower in dBm. (Optional)
	 */
	public TC_Int signalStrength;
	
	/**
	 * The primary scrambling code for WCDMA and physical CellId for LTE.
	 * (Optional)
	 */
	public TC_Int primaryScramblingCode;
	
	/**
	 * Specify with 0/1 if the cell is serving or not. If not specified, the
	 * first cell is assumed to be serving. (Optional)
	 */
	public TC_Int serving;
}
