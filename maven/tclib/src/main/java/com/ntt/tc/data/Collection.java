package com.ntt.tc.data;

import com.ntt.tc.data.rest.PagingStatistics;

/**
 * 各オブジェクトに準備されている Collection のスーパークラスです。
 * このオブジェクトを継承することで、CollectionIterator で iterator
 * を生成することができるようになります。
 *
 * @version			February 10, 2019
 * @author			Yusuke Sasaki
 */
public class Collection extends C8yData {
	/**
	 * Link to this resource.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public String self;
	
	/**
	 * Information about paging statistics.
	 * <pre>
	 * Occurs : 1
	 * </pre>
	 */
	public PagingStatistics statistics;
	
	/**
	 * Link to a potential previous page of managed objects.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String prev;
	
	/**
	 * Link to a potential next page of managed objects.
	 * <pre>
	 * Occurs : 0..1
	 * </pre>
	 */
	public String next;
	
}
