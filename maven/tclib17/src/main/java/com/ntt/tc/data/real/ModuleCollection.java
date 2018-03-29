package com.ntt.tc.data.real;

import com.ntt.tc.data.C8yData;
import com.ntt.tc.data.rest.PagingStatistics;

/**
 * ModuleCollection class
 * c8y docs では Collection[] modules となっていたが、Module[] modules
 * に変更
 */
public class ModuleCollection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * List of modules, see below.
	 * <pre>
	 * Occurs : 0..n
	 * </pre>
	 */
	public Module[] modules;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of modules.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of modules.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}
