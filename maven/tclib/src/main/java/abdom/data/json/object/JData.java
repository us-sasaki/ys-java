package abdom.data.json.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;

/**
 * JSON �I�u�W�F�N�g�� Java �I�u�W�F�N�g�ɂ���Ė͕킵�܂��B
 * ���̃N���X���p�����邱�ƂŁAJava �I�u�W�F�N�g�� JSON �`���̑��ݕϊ���
 * �e�ՂɂȂ�܂��B�܂�AJava �I�u�W�F�N�g�̃C���X�^���X�ϐ����A
 * JSON �`���Ƃ��Ē��񉻂ł��A�܂��t�� JSON �`������ Java �I�u�W�F�N�g��
 * �t�B�[���h��ݒ�ł���悤�ɂȂ�܂��B
 * Java �I�u�W�F�N�g�ɂ����Ď��ɒ�`����u�v���p�e�B�v���ϊ��ΏۂƂȂ�܂��B<br>
 * 1.public �����o�ϐ��B�v���p�e�B���͕ϐ����ɂȂ�܂��B<br>
 * 2.public getter, setter ���\�b�h�̑΁B�v���p�e�B���� Java Beans �����K��<br>
 *   �ɂ��܂��B����ɑ΂� getter �͈����Ȃ��Asetter �͈�������� getter <br>
 * �@�̕Ԓl�^�� setter �̈����^����v���AJData �J�e�S���Ɋ܂܂�����<br>
 * <br>
 * JData �J�e�S���́A�ȉ��̌^�ł��B<pre>
 *
 * boolean, int, long, float, double, String, JValue(,JData), JsonObject
 * ����сA�����̌^�̔z��
 *
 * </pre>�Öق̃t�B�[���h�Ƃ��āA_extra (JsonObject�^) �������Ă���
 * fill() �̍ۂɖ���`�̃t�B�[���h�l�͂��ׂĂ����Ɋi�[����܂��B
 * �܂��AtoJson() �ł� _extra �t�B�[���h�͑��݂���(not null)�ꍇ�̂�JSON
 * �����o�Ƃ��Č���܂��B
 * �q�N���X�ŁAJSON�`���Ƃ̑��ݕϊ��ΏۊO�Ƃ���ϐ����`�������ꍇ�A
 * transient �C���q�����ĉ������B
 *
 * @version	December 23, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JData extends JValue {

	/** fill �ł��Ȃ������l���i�[����\��̈� */
	protected transient JsonObject _extra;
	
/*-------------
 * constructor
 */
	protected JData() {
		Jsonizer.getAccessors(this);
	}
	
/*------------------
 * instance methods
 */
	/**
	 * extra �������ǂ����e�X�g���܂��B
	 *
	 * @return	extra �����ꍇ�Atrue
	 */
	public boolean hasExtras() {
		return (_extra != null);
	}
	
	/**
	 * extra �� keySet ��ԋp���܂��B�Ȃ��ꍇ�Anull �ƂȂ�܂��B
	 *
	 * @return	extra �̃L�[(extra �����݂��Ȃ��ꍇ�Anull)
	 */
	public Set getExtraKeySet() {
		if (_extra == null) return null;
		return _extra.keySet();
	}
	
	/**
	 * extra �A�N�Z�X���\�b�h�ŁAJsonType �l���擾���܂��B
	 * extra ���Ȃ��ꍇ�A�����Ă��w�肳�ꂽ�L�[�������Ȃ��ꍇ�A
	 * null ���ԋp����܂��B
	 *
	 * @param	key	extra �� key ���
	 * @return	key �ɑΉ�����l(null �̏ꍇ������܂�)
	 */
	public JsonType getExtra(String key) {
		if (_extra == null) return null;
		return _extra.get(key);
	}
	
	/**
	 * extra �A�N�Z�X���\�b�h�ŁAJsonType �l��ݒ肵�܂��B
	 *
	 * @param	key	extra �� key ���
	 * @param	jt	�ݒ肷��l
	 */
	public void putExtra(String key, JsonType jt) {
		if (_extra == null) _extra = new JsonObject();
		_extra.put(key, jt);
	}
	
	/**
	 * ���̃C���X�^���X������ extra �I�u�W�F�N�g(JsonObject)
	 * �̎Q�Ƃ�ԋp���܂��B���e�̎Q��/�ύX���ȕւɍs�����Ƃ�z�肵��
	 * ���\�b�h�ł��B
	 *
	 * @return	extra �I�u�W�F�N�g(JsonObject)�Bnull �̏ꍇ������܂��B
	 */
	public JsonObject getExtras() {
		return _extra;
	}
	
	
	/**
	 * �w�肳�ꂽ JsonObject �̓��e�����̃I�u�W�F�N�g�ɐݒ肵�܂��B
	 * �����̌^�́A���֐��̂��� JsonType �Ƃ��Ă��܂����AJsonObject
	 * �ȊO���w�肷��ƁAClassCastException ���X���[����܂��B
	 * ���̃��\�b�h�͒l��ǉ����A�����l�͏㏑������Ȃ���Εۑ������
	 * ���Ƃɒ��ӂ��Ă��������B_extra �����l�ł��B
	 *
	 * @param	json	���̃I�u�W�F�N�g�ɒl��^���� JsonType
	 */
	@Override
	public void fill(JsonType json) {
		JsonType rest = Jsonizer.fill(this, json);
		if (rest == null) return;
		if (_extra == null) _extra = (JsonObject)rest;
		else {
			for (String key : rest.keySet()) {
				JsonType val = rest.get(key);
				if (val.getType() == JsonType.TYPE_VOID) {
					if (_extra.get(key) != null) _extra.cut(key);
				} else {
					_extra.put(key, val);
				}
			}
		}
	}
	
	/**
	 * ���̃I�u�W�F�N�g�� JsonObject �ɕϊ����܂��B
	 *
	 * @return	JsonObject
	 */
	@Override
	public JsonType toJson() {
		JsonType json = Jsonizer.toJson(this);
		// _extra ��ǉ�
		if (_extra == null) return json;
		for (String key : _extra.keySet()) {
			json.put(key, _extra.get(key));
		}
		return json;
	}
	
}
