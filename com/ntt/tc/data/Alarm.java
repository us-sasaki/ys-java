package com.ntt.tc.data;

import abdom.data.json.JsonObject;

import com.ntt.tc.data.Id;
import com.ntt.tc.data.sensor.C8y_SignalStrength;

/**
 * �P��� alarm.
 * POST /alarm/alarms �̗v���A�����ɂ����p�����B
 */
public class Alarm extends C8yData {
	/** �A���[������ӂɎ��ʂ��܂� */
	public String	id;
	/** ���̃��\�[�X�ւ̃����N */
	public String	self;
	/** �f�[�^�x�[�X�ɃA���[�����������ꂽ���� */
	public TC_Date	creationTime;
	/** �A���[���̌^�����ʂ��܂��Be.g. "com_cumulocity_events_TamperEvent" */
	public String	type;
	/** �A���[���̎��� */
	public TC_Date	time;
	/** �A���[���̐����� */
	public String	text;
	/** �A���[���𐶐�����managedObject��id �t�B�[���h */
	public Id	source;
	/** �A���[���̏d��x */
	public String	severity;
	/** �A���[���𑗐M������ */
	public int	count;
	/** �A���[�����ŏ��ɋN����������(i.e. "count"��1�̎���) */
	public TC_Date	firstOccurenceTime;
	/**
	 * �g���[�X����v���p�e�B�̕ύX����
	 * ��ł� history {...} �ƂȂ��Ă���^�����ʂł��Ȃ����߁AJsonObject
	 * �Ƃ��Ă����B
	 */
	public JsonObject	history;
}
