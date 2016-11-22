package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * ���� measurement �̕ϐ��ɂ� +,- �Ƃ����� Java �ŗ��p�ł��Ȃ��������܂��
 * ���܂��B
 * ���̂��߁A���ׂ� fragment �Ƃ��Ď�舵���܂��B
 *
 * <pre>
 * measurement �̗v�f
 * A+	kWh	Total active energy, in
 * A-	kWh	Total active energy, out
 * P+	W	Total active power, in
 * P-	W	Total active power, out
 *
 * ���p���@
 * C8y_SinglePhaseElectricityMeasurement m;
 * m._fragment.get("A+").put("value", 123);
 * m._fragment.get("A+").put("unit", "kWh");
 * </pre>
 * �g���Â炻���Ȃ̂ŁA���� get/set �ł��郁�\�b�h���������邩���m��܂���B
 * (transient �𗘗p)
 */
public class C8y_SinglePhaseElectricityMeasurement extends C8yData {
}
