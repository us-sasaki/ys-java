package com.ntt.tc.data.device;

import com.ntt.tc.data.C8yData;

/**
 * OperationRepresentation class
 * c8y docs にないため、新規作成。
 * 次は BulkOperation の記述。
 * Operation to be executed for every device in a group.
 * POST: Mandatory, PUT: No
 *
 */
public class OperationRepresentation extends C8yData {
	// 例では {"test"=>"TEST1"} と表記されているが、JsonObject と仮定。
	// _extra のみのオブジェクト
	//
	// 2017/6/18 c8y docs ではこの表記が修正されている。
	// 以下が例として挙げられている。
	// {
	//    "description": "Restart device",
	//    "c8y_Restart": {}
	// }
	//
	//
}
