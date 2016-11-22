package com.ntt.tc.data;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.object.JValue;

/**
 * C8y �Ŏg�p�����A���t�̃t�H�[�}�b�g�ł��B
 * java.util.Date �Ƃ̑��ݕϊ����\�ł��B
 * ������ł́A"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" �̃t�H�[�}�b�g���g�p���܂��B
 *
 * @version		November 19, 2016
 * @author		Yusuke Sasaki
 */
public class TC_Date extends C8yValue {
	protected static final SimpleDateFormat SDF =
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	
	/** �����I�ɂ� java.util.Date �Ƃ��Ēl��ێ����܂� */
	protected Date date;
	
/*-------------
 * Constructor
 */
	/**
	 * �f�t�H���g�R���X�g���N�^�ł́A���ݎ�����ݒ肵�܂��B
	 */
	public TC_Date() {
		date = new Date();
	}
	
	/**
	 * �^����ꂽ Date �̓��t�������C���X�^���X�𐶐����܂��B
	 *
	 * @param	date	�ݒ肷�� Date �l
	 */
	public TC_Date(Date date) {
		this.date = date;
	}
	
	/**
	 * �^����ꂽ������`���̓��t�������C���X�^���X�𐶐����܂��B
	 *
	 * @param	date	������`��
	 */
	public TC_Date(String date) {
		set(date);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * ������\���ŃC���X�^���X�̓��t��ύX���܂��B
	 * ������ł́A"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" �̃t�H�[�}�b�g���g�p���܂��B
	 * toString() �ƌ݊���������܂��B
	 *
	 * @param	date	������`��
	 */
	public void set(String date) {
		try {
			this.date = SDF.parse(date);
		} catch (ParseException pe) {
			throw new C8yFormatException(pe.toString());
		}
	}
	
/*-----------
 * overrides
 */
	/**
	 * Json �`��(JsonValue (string))�ŃC���X�^���X�̒l��ύX���܂��B
	 *
	 * @param	jt	�l�������Ă��� JsonType�BJsonValue (string) �ł���
	 *			�K�v������A�����łȂ��ꍇ�AClassCastException ���X���[
	 *			����܂��B
	 */
	@Override
	public void fill(JsonType jt) {
		JsonValue jv = (JsonValue)jt;
		String str = jv.getValue();
		set(str);
	}
	
	/**
	 * Json �\�����擾���܂��BJsonValue (string) �̌^�A
	 * "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" �̃t�H�[�}�b�g�ŕԋp����܂��B
	 *
	 * @return	JsonValue �l
	 */
	@Override
	public JsonType toJson() {
		return new JsonValue(SDF.format(date));
	}
	
	/**
	 * ������\�����擾���܂��B
	 * ������ł́A"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" �̃t�H�[�}�b�g���g�p���܂��B
	 * set(String) �ƌ݊���������܂��B
	 *
	 * @return	"yyyy-MM-dd'T'HH:mm:ss.SSSXXX" �`���̕�����\��
	 * @see		#set(String)
	 */
	@Override
	public String toString() {
		return SDF.format(date);
	}
}
