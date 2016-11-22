package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * ���� measurement �̕ϐ��ɂ� +,- �Ƃ����� Java �ŗ��p�ł��Ȃ��������܂��
 * ���܂��B
 * ���̂��߁A���ׂ� fragment �Ƃ��Ď�舵���܂��B
 *
 * <pre>
 * measurement �̗v�f
 * A+	kWh	Total active energy in, summed across phases
 * A+:1(2, 3)	kWh	Active energy in for phase 1, 2 or 3
 * A-	kWh	Total active energy out, summed across phases
 * A-:1(2, 3)	kWh	Active energy out for phase 1, 2, 3
 * P+	W	Total active power in, summed across phases
 * P+:1(2, 3)	W	Active power in for phase 1, 2 or 3
 * P-:1(2, 3)	W	Active power out for phase 1, 2 or 3
 * Ri+	kVArh	Total reactive inductive energy, in
 * Ri-	kVArh	Total reactive inductive energy, out
 * Rc+	kVArh	Total reactive capacitive energy, in
 * Rc-	kVArh	Total reactive capacitive energy, out
 * Qi+	kVAr	Total reactive inductive power, in
 * Qi-	kVAr	Total reactive inductive power, out
 * Qc+	kVAr	Total reactive capacitive power, in
 * Qc-	kVAr	Total reactive capacitive power, out
 *
 * ���p���@
 * C8y_SinglePhaseElectricityMeasurement m;
 * m._fragment.get("A+").put("value", 123);
 * m._fragment.get("A+").put("unit", "kWh");
 * </pre>
 * �g���Â炻���Ȃ̂ŁA���� get/set �ł��郁�\�b�h���������邩���m��܂���B
 * (transient �𗘗p, fill, toJson ���I�[�o�[���C�h)
 */
public class C8y_ThreePhaseElectricityMeasurement extends C8yData {
}
