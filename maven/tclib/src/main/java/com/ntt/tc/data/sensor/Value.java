package com.ntt.tc.data.sensor;

import com.ntt.tc.data.C8yData;

/**
 * Value �� Measurement �̒��ŕp�ɂɗ��p�����A���l�ƒP�ʂ�
 * �g�ݍ��킹�B
 * docs �ł͖����I�Ȓ�`���Ȃ����A�p�o�̂��߃N���X���B
 */
public class Value extends C8yData {
	public double value;
	public String unit;
	
	public Value() {
		super();
	}
	public Value(double value, String unit) {
		super();
		this.value	= value;
		this.unit	= unit;
	}
}
