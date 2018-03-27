package com.ntt.tc.net;

import com.ntt.tc.data.inventory.ManagedObject;

public interface InventoryListener {
	void inventoryUpdated(ManagedObject object);
}
