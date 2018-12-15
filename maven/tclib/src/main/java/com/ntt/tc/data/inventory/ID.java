package com.ntt.tc.data.inventory;

import com.ntt.tc.data.C8yData;

/**
 * Id class.
 * id のみにメンバを限定したクラス。
 * Measurement/Event/Alarm 等の要素では他の要素が含まれないことが多いため
 * メモリ効率を考慮し設定。
 */
public class ID extends C8yData {
	/**
	 * Unique identifier of the object, automatically allocated when the object
	 * is created (see above).
	 * <pre>
	 * Occurs : 1
	 * PUT/POST : No
	 * </pre>
	 */
	public String id;
	
/*-------------
 * constructor
 */
	public ID() {
	}
	
	/**
	 * String で id を指定してオブジェクトを生成します。
	 *
	 * @param		id		本オブジェクトに設定する (ManagedObject)Id
	 */
	public ID(String id) {
		this.id = id;
	}
	/**
	 * ManagedObject を指定してオブジェクトを生成します。
	 * Id 以外のフィールドは捨てられます。
	 *
	 * @param		mo		本オブジェクトに設定する (ManagedObject)
	 */
	public ID(ManagedObject mo) {
		this.id = mo.id;
	}
	
}
