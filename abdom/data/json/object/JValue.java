package abdom.data.json.object;

import abdom.data.json.JsonValue;

/**
 * Json �o�����[�� Java �I�u�W�F�N�g�ɂ���Ė͕킵�܂��B
 * ���̃N���X���p�����邱�ƂŁAJData �̃����o�Ƃ��Ē�`���邱�Ƃ��ł��܂��B
 * JData �Ƃ̈Ⴂ�̈�́A�L�[�o�����[�^�łȂ��A�o�����[�݂̂���Ȃ�_�ł��B
 * JsonValue �Ƃ̑��ݕϊ����\�b�h�Ƃ��āAfill(JsonValue), toJson()
 * ��������K�v������܂��B
 * JData �̂悤�Ƀ����o�ϐ��͒��񉻂��ꂸ�A�O�L�Q���\�b�h��ʂ��Ē���
 * ���s���܂��B
 *
 * @version	November 15, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JValue {
	
	/**
	 * JsonType �ɕϊ����܂�
	 *
	 * @return	�ϊ����ꂽ JsonType (JsonValue�^)
	 */
	public abstract JsonValue toJson();
	
	/**
	 * JsonValue �ɂ���ăC���X�^���X��Ԃ𖄂߂܂��B
	 *
	 * @param	value �Ƃ��� null �l��AJSON �ɂ����� null ���w�肳���ꍇ��
	 *			����A�����O���Ɏ������ĉ������B
	 */
	public abstract void fill(JsonValue value);
}
