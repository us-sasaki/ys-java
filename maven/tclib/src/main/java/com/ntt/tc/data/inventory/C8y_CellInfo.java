package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;

/**
 * c8y_CellInfo provides detailed information about the closest mobile
 * cell towers. When the functionality is activated, the location of the
 * device is determined based on this fragment, in order to track the
 * device whereabouts when GPS tracking is not available.
 * inventory / ManagedObject では Object * 扱いで、
 * Device Management Library に記載されるフィールド。
 */
public class C8y_CellInfo extends C8yData {
	/**
	 * The radio type of this cell tower. (Optional)
	 */
	public String radioType;
	/**
	 * Detailed information about the neighbouring cell towers.
	 */
	public C8y_CellTower[] cellTowers;
	
}
