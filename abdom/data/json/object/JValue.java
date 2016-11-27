package abdom.data.json.object;

import abdom.data.json.JsonType;

/**
 * JSON �� Java �̊Ԃ̑��ݕϊ��Ɋւ���N���X�̃e���v���[�g�ł��B
 * �ʏ�AJsonData ���p�����Ă��������B
 *
 * ���̃N���X�𒼐ڌp������̂́A�ȉ��̂悤�ȏꍇ�ł��B<br/>
 * JsonValue �� Java �I�u�W�F�N�g�ɂ���Ė͕킷��ꍇ<br/>
 * ����� JsonObject/JsonArray �\�����܂Ƃ܂����Ӗ��������A1��Java�I�u�W�F
 * �N�g�Ƃ��ĕ\�������ꍇ<br/>
 * ���ڌp������ꍇ�� JData �̂悤�ȃ����o�ϐ��̒��񉻋@�\�͎����Ȃ����߁A
 * JsonType �Ƃ̑��ݕϊ����\�b�h�Ƃ��āAfill(JsonValue), toJson() ����������
 * �K�v������܂��B
 *
 * @version	November 15, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JValue {
	
	/**
	 * JsonType �ɕϊ����܂�
	 *
	 * @return	�ϊ����ꂽ JsonType
	 */
	public abstract JsonType toJson();
	
	/**
	 * JsonType �ɂ���ăC���X�^���X��Ԃ𖄂߂܂��B
	 *
	 * @param	value �Ƃ��� null �l��AJSON �ɂ����� null ���w�肳���ꍇ��
	 *			����A�����O����(�ʏ�ANullPointerException���������Ȃ�
	 *			�悤�� null �̏ꍇ�͉������Ȃ������ɂȂ�܂�)�������ĉ������B
	 */
	public abstract void fill(JsonType value);
}
