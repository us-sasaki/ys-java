package com.ntt.tc.data;

import abdom.data.json.object.JValue;

/**
 * Cumulocity �̃f�[�^�̃X�[�p�[�N���X�ł��B
 * �J�e�S�������̂ق��AJValue �̒��񉻁Ac8y ���L�̃��[���ɑΉ����郁�\�b�h��
 * �񋟂��܂��B
 */
public abstract class C8yValue extends JValue {
	
	/**
	 * 
	 */
	public void clearSelf() {
	}
	
//	public C8yData getDifference(C8yData a, C8yData b) {
//		if (!a.getClass().isAssignableFrom(b.getClass()))
//			throw new ClassCastException("Can not cast type of b to type of a.");
//		C8yData result = C8yData.getClass().newInstance();
//	}
	
}
