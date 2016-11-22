package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * この measurement の変数には +,- といった Java で利用できない文字を含んで
 * います。
 * このため、すべて fragment として取り扱います。
 *
 * <pre>
 * measurement の要素
 * A+	kWh	Total active energy, in
 * A-	kWh	Total active energy, out
 * P+	W	Total active power, in
 * P-	W	Total active power, out
 *
 * 利用方法
 * C8y_SinglePhaseElectricityMeasurement m;
 * m._fragment.get("A+").put("value", 123);
 * m._fragment.get("A+").put("unit", "kWh");
 * </pre>
 * 使いづらそうなので、直接 get/set できるメソッドを準備するかも知れません。
 * (transient を利用)
 */
public class C8y_SinglePhaseElectricityMeasurement extends C8yData {
}
