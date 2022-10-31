package com.ntt.tc.data.real;

import com.ntt.tc.data.Collection;

/**
 * ModuleCollection class
 * c8y docs では Collection[] modules となっていたが、Module[] modules
 * に変更
 */
public class ModuleCollection extends Collection {
	/**
	 * List of modules, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Module[] modules;
}
