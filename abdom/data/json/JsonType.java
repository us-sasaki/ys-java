package abdom.data.json;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Json�`���ɂ�����^���(var)��\���܂��B�܂��A�X�g���[���A�����񂩂�� parse 
 * ���\�b�h��񋟂��܂��B
 * ���֐��̂��߁A�L���X�g�����ɗ��p����A�N�Z�X���\�b�h��񋟂��܂��B
 * ���p�ł��Ȃ��I�y���[�V�����ł������ꍇ�AClassCastException ���������܂��B
 *
 * @version		November 19, 2016
 * @author		Yusuke Sasaki
 */
public abstract class JsonType implements Iterable<JsonType> {
	/** getType() �ŕԋp�����AJavaScript �ł̌^ void(null) ��\���萔�ł� */
	public static final int TYPE_VOID = 0;
	
	/** getType() �ŕԋp�����AJavaScript �ł̌^ boolean ��\���萔�ł� */
	public static final int TYPE_BOOLEAN = 1;
	
	/** getType() �ŕԋp�����AJavaScript �ł̌^ int ��\���萔�ł� */
	public static final int TYPE_INT = 2;
	
	/** getType() �ŕԋp�����AJavaScript �ł̌^ double ��\���萔�ł� */
	public static final int TYPE_DOUBLE = 3;
	
	/** getType() �ŕԋp�����AJavaScript �ł̌^ string ��\���萔�ł� */
	public static final int TYPE_STRING = 4;
	
	/** getType() �ŕԋp�����AJavaScript �ł̌^ array ��\���萔�ł� */
	public static final int TYPE_ARRAY = 10;
	
	/** getType() �ŕԋp�����AJavaScript �ł̌^ object ��\���萔�ł� */
	public static final int TYPE_OBJECT = 20;
	
	/**
	 * getType() �ŕԋp�����A�ǂ̌^�ł��Ȃ����Ƃ�\���萔�ł��B
	 * ���̒l���ԋp���邱�Ƃ͒ʏ킠��܂���BJsonType ���p�������V����
	 * �N���X���쐬������AJsonValue ���p������ value, quote �ɐV�����l��
	 * ��`�����ꍇ�ɕԋp�����\��������܂��B
	 */
	public static final int TYPE_UNKNOWN = 99;
	
	/**
	 * �������̂��߁ASystem.getProperty("line.separator")
	 * �̒l��ێ����܂��B
	 */
	protected static final String LS = System.getProperty("line.separator");
	
	/**
	 * JsonValue �Ƃ��Ă̒l�𕶎���Ŏ擾���܂��B���̃I�u�W�F�N�g��
	 * JsonValue �łȂ��ꍇ�AClassCastException ���X���[����܂��B
	 *
	 * @return	JsonValue �Ƃ��Ă̕�����l
	 */
	public String getValue() {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁AgetValue �ł��܂���");
	}
	
	/**
	 * JsonValue �Ƃ��Ă̒l�𐮐��l�Ŏ擾���܂��B���̃I�u�W�F�N�g��
	 * JsonValue �łȂ��ꍇ�AClassCastException ���X���[����܂��B
	 * �܂��AJsonValue �ł������Ƃ��ĔF���ł��Ȃ��ꍇ(Integer.parseInt ��
	 * ���s)�ANumberFormatException ���X���[����܂��B
	 *
	 * @return	JsonValue �Ƃ��Ă� int �l
	 */
	public int getIntValue() {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁AgetIntValue �ł��܂���");
	}
	
	/**
	 * JsonValue �Ƃ��Ă̒l��double�l�Ŏ擾���܂��B���̃I�u�W�F�N�g��
	 * JsonValue �łȂ��ꍇ�AClassCastException ���X���[����܂��B
	 * �܂��AJsonValue �ł� double �Ƃ��ĔF���ł��Ȃ��ꍇ
	 * (Double.parseDouble �����s)�ANumberFormatException ���X���[����܂��B
	 *
	 * @return	JsonValue �Ƃ��Ă� double �l
	 */
	public double getDoubleValue() {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁AgetDoubleValue �ł��܂���");
	}
	
	/**
	 * JsonType �Ƃ��Ă̒l�������Ă��邩�e�X�g���܂��B
	 * false �ƂȂ�͈̂ȉ��̏ꍇ�ł��B<pre>
	 * JsonObject �ŁA��I�u�W�F�N�g�̏ꍇ
	 * JsonArray �ŁA��z��̏ꍇ
	 * JsonValue �ŁA�l�� false �̏ꍇ
	 * </pre>
	 * �ق��̏ꍇ�Atrue ���ԋp����܂��B
	 *
	 * @return	�l�������Ă���A�܂��� false �l�łȂ��ꍇ true
	 */
	public boolean isTrue() {
		if (this instanceof JsonObject) {
			return (((JsonObject)this).keySet().size() > 0);
		} else if (this instanceof JsonArray) {
			return (((JsonArray)this).array.size() > 0);
		} else if (this instanceof JsonValue) {
			return !"\"false\"".equals(toString());
		} else {
			// never fall back here
			return true;
		}
	}
	
	/**
	 * JsonObject �Ƃ��āA�w�肳�ꂽ�L�[�̒l���擾���܂��B
	 * JsonObject �łȂ��ꍇ�AClassCastException ���X���[����܂��B
	 *
	 * @param	key		�l���擾�������L�[��
	 * @return	�擾�����l(JsonType)
	 */
	public JsonType get(String key) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aget(String) �̓T�|�[�g����܂���");
	}
	
	/**
	 * JsonObject �Ƃ��āA�w�肳�ꂽ�L�[�̒l���擾���A�폜���܂��B(cut)
	 * JsonObject �łȂ��ꍇ�AClassCastException ���X���[����܂��B
	 *
	 * @param	key		�l���擾���A�폜�������L�[��
	 * @return	�擾�����l(JsonType)�B�L�[�����݂��Ȃ��ꍇ�Anull
	 */
	public JsonType cut(String key) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Acut(String) �̓T�|�[�g����܂���");
	}
	
	/**
	 * JsonArray �Ƃ��āA�w�肳�ꂽ index �̒l���擾���܂��B
	 * JsonArray �łȂ��ꍇ�AClassCastException ���X���[����܂��B
	 *
	 * @param	index	index�l( 0 �` size()-1 )
	 * @return	�擾�����l(JsonType)
	 */
	public JsonType get(int index) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aget(int) �̓T�|�[�g����܂���");
	}
	
	/**
	 * JsonArray �Ƃ��āA�v�f����ԋp���܂��B
	 * JsonArray �łȂ��ꍇ�AClassCastException ���X���[����܂��B
	 *
	 * @return	�v�f��
	 */
	public int size() {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asize() �̓T�|�[�g����܂���");
	}
	
	public boolean isArray() {	return (this instanceof JsonArray);	}
	public boolean isObject() {	return (this instanceof JsonObject); }
	public boolean isValue() { return (this instanceof JsonValue); }
	public boolean isNumber() {
		int type = getType();
		return (type == TYPE_INT || type == TYPE_DOUBLE);
	}
	
	/**
	 * ���� JsonType �� JavaScript �̂ǂ̌^�ł��邩�������萔��ԋp���܂��B
	 *
	 * @return	�^�������萔
	 * @see		#TYPE_VOID
	 * @see		#TYPE_BOOLEAN
	 * @see		#TYPE_INT
	 * @see		#TYPE_DOUBLE
	 * @see		#TYPE_STRING
	 * @see		#TYPE_ARRAY
	 * @see		#TYPE_OBJECT
	 * @see		#TYPE_UNKNOWN
	 */
	public int getType() {
		if (this instanceof JsonValue) {
			JsonValue j = (JsonValue)this;
			if ("\"".equals(j.quote)) return TYPE_STRING;
			if ("null".equals(j.value)) return TYPE_VOID;
			if ("true".equals(j.value)) return TYPE_BOOLEAN;
			if ("false".equals(j.value)) return TYPE_BOOLEAN;
			try {
				Integer.parseInt(j.value);
				return TYPE_INT;
			} catch (NumberFormatException nfe) {
				try {
					Double.parseDouble(j.value);
					return TYPE_DOUBLE;
				} catch (NumberFormatException nfe2) {
				}
			}
			return TYPE_UNKNOWN;
		} else if (this instanceof JsonArray) {
			return TYPE_ARRAY;
		} else if (this instanceof JsonObject) {
			return TYPE_OBJECT;
		}
		return TYPE_UNKNOWN;
	}

/*
 * set(array)
 */
	public void set(JsonType... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(String... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(byte... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(char... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(short... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(int... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(long... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(float... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(double... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}
	public void set(boolean... array) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aset �ł��܂���");
	}	
/*
 * add methods
 */
	public JsonObject add(String name, JsonType t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, String t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, boolean t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, byte t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, char t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, short t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, int t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, long t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, float t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, double t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
	public JsonObject add(String name, JsonType[] t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aadd �ł��܂���");
	}
/*
 * put methods (add �Ɠ��������A�l���㏑��)
 */
	public JsonObject put(String name, JsonType t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, String t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, boolean t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, byte t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, char t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, short t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, int t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, long t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, float t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, double t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
	public JsonObject put(String name, JsonType[] t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aput �ł��܂���");
	}
/*
 * push methods (�z��̍Ō���ɒl�ǉ�)
 */
	public JsonArray push(JsonType t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(String t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(boolean t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(byte t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(char t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(short t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(int t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(long t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(float t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(double t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	public JsonArray push(JsonType[] t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apush �ł��܂���");
	}
	
/*
 * pop methods (�z��̍Ō�̗v�f���擾���A�폜)
 */
	public JsonType pop() {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Apop �ł��܂���");
	}
	
/*
 * shift methods (�z��̍ŏ��ɒl�ǉ�)
 */
	public JsonArray shift(JsonType t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(String t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(boolean t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(byte t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(char t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(short t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(int t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(long t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(float t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(double t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	public JsonArray shift(JsonType[] t) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Ashift �ł��܂���");
	}
	
/*
 * unshift methods (�z��̍ŏ��̗v�f���擾���A�폜)
 */
	public JsonType unshift() {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aunshift �ł��܂���");
	}
	
	/**
	 * JavaScript �ɂ����� slice ����ł��B
	 * 
	 * @param	s	�R�s�[����ŏ��̃C���f�b�N�X(�܂݂܂�)
	 * @param	e	�R�s�[���閖���̃C���f�b�N�X(�܂݂܂���)
	 * @return	�؂����� JsonArray (�v�f�͎Q��(shallow copy)�ł�)
	 */
	public JsonArray slice(int s, int e) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aslice �ł��܂���");
	}
	
	/**
	 * JavaScript �ɂ����� concat (�����A���̒l��ۂ�) �ł��B
	 */
	public JsonArray concat(JsonType target) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aconcat �ł��܂���");
	}
	
	public JsonArray splice(int index, int delete, JsonType... toAdd) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	
	public JsonArray splice(int index, int delete, String... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	public JsonArray splice(int index, int delete, byte... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	public JsonArray splice(int index, int delete, char... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	public JsonArray splice(int index, int delete, short... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	public JsonArray splice(int index, int delete, int... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	public JsonArray splice(int index, int delete, long... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	public JsonArray splice(int index, int delete, float... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	public JsonArray splice(int index, int delete, double... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	public JsonArray splice(int index, int delete, boolean... val) {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Asplice �ł��܂���");
	}
	/**
	 * JsonObject �Ƃ��ẴL�[(keySet)���擾���܂��B
	 *
	 * @return	�L�[�W��(Set<String>)
	 */
	public Set<String> keySet() {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁AkeySet �������܂���");
	}
	
	/**
	 * ������\����ԋp���܂��B������\���́A���s��X�y�[�X
	 * �������܂܂Ȃ� JSON �`���ł��B
	 * string �^ (JsonValue �ŕێ�����l�� String �̏ꍇ) �ł�
	 * ���ʂ� ""(�_�u���N�I�[�e�[�V����) �Ŋ����邱�Ƃɒ��ӂ��Ă��������B
	 *
	 * @return	���̃I�u�W�F�N�g�� JSON �`��(������)
	 */
	public abstract String toString();
	
	/**
	 * �l�����₷���C���f���g���܂񂾌`���ŕ����񉻂��܂��B
	 * �ő剡���̓f�t�H���g�l(80)���ݒ肳��܂��B
	 *
	 * @param	indent	�C���f���g(�����̃X�y�[�X��^�u)
	 * @return	�C���f���g�A���s���܂ޕ�����
	 */
	public String toString(String indent) {
		return toString(indent, 80);
	}
	
	/**
	 * �l�����₷���C���f���g���܂�JSON�`���ŕ����񉻂��܂��B
	 * JsonObject, JsonArray �l����s�ŕ\����Ȃ���s�����Ȃ����߂́A��s��
	 * ���������w�肵�܂��B
	 *
	 * @param	indent		�C���f���g(�����̃X�y�[�X��^�u)
	 * @param	textwidth	object, array �Ɋւ��A���̕������Ɏ��܂�ꍇ
	 *						�����s�ɕ����Ȃ��������s�����߂�臒l�B
	 *						0 �ȉ����w�肷��ƁA��s�������݂��A��ɕ����s��
	 *						����܂��B(���̕�������)
	 * @return	�C���f���g�A���s���܂ޕ�����
	 */
	public final String toString(String indent, int textwidth) {
		return toString("", indent, textwidth, false);
	}
	
	
	/**
	 * �l�����₷���C���f���g���܂�JSON�`���ŕ����񉻂��܂��B
	 * �C���f���g���T�|�[�g���邽�߁A���݂̃C���f���g������ indent,
	 * ���̃C���f���g����邽�߂� indentStep, �s�������Ȃ�Ȃ��ꍇ��
	 * ��s�����邽�߂� textwidth, JsonObject �ɂ��� "name" : ���
	 * { �𓯍s�ɔz�u������Ꮘ�������邽�߂̃t���O(objElement)��
	 * �����Ă��܂��B
	 * �����s�ɕ����邽�߂̉��s�R�[�h�́AJsonType.LS �Ƃ��ĕێ�����Ă��܂��B
	 * <pre>
	 *
	 * [indent]*�J�n�ʒu(objElement==true �̎��� indent �����Ȃ�)
	 * [indent][indentStep]*�C���f���g�t�̎��̍s�̊J�n�ʒu
	 * -------------------------(textwidth�܂ł͈�s������邱�Ƃ���)-----
	 * </pre>
	 * 
	 * @param	indent		�C���f���g(�������̃X�y�[�X)
	 * @param	indentStep	�C���f���g��񕪂̃X�y�[�X��^�u
	 * @param	textwidth	object, array �Ɋւ��A���̕������Ɏ��܂�ꍇ
	 *						�����s�ɕ����Ȃ��������s�����߂�臒l�B
	 *						0 �ȉ����w�肷��ƁA��s�������݂��A��ɕ����s��
	 *						����܂��B(���̕�������)
	 * @param	objElement	true..�I�u�W�F�N�g�̗v�f���̌��
	 * @return	���s�A�X�y�[�X�Ȃǂ��܂� String
	 */
	protected abstract String toString(String indent, String indentStep,
						int textwidth, boolean objElement);
	
/*---------------
 * class methods
 */
	/*
	 * new JsonObject �𓾂邽�߂֗̕��֐��ł��B
	 * ������(�^�C�v��)�����炷�ړI�Őݒ肳��Ă��܂��B
	 * new JsonObject().add("name", "value") ��
	 * JsonType.o("name", "value") �Ŏ擾�ł��܂��B
	 *
	 * @param	name	�L�[��
	 * @param	t		�o�����[
	 * @return	�V�����������ꂽJsonObject
	 */
	public static JsonObject o(String name, JsonType t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, String t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, boolean t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, byte t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, char t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, short t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, int t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, long t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, float t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, double t) {
		return new JsonObject().put(name, t);
	}
	public static JsonObject o(String name, JsonType[] t) {
		return new JsonObject().put(name, t);
	}
	
	/**
	 * �w�肳�ꂽ JSON �����񂩂� JsonType �𐶐����܂��B
	 *
	 * @param	str	Json������
	 * @return	�w�肳�ꂽ������̕\�� JsonType
	 */
	public static JsonType parse(String str) {
		try {
			return parse(new StringReader(str));
		} catch (IOException e) {
			throw new InternalError("StringReader �� IOException ���������܂���"+e);
		}
	}
	
	/**
	 * �w�肳�ꂽ Reader ���� JSON value ���P�ǂݍ��݂܂��B
	 * Reader ��JSON value�I���ʒu�܂œǂݍ��܂�Aclose() ����܂���B
	 * Reader �͓����I�� PushbackReader �Ƃ��ė��p����܂��B
	 *
	 * @param	in	Json���������͂��� Reader�B
	 * @return	�������ꂽ JsonType
	 */
	public static JsonType parse(Reader in) throws IOException {
		PushbackReader pr = new PushbackReader(in);
		return parseValue(pr);
	}
	
	/**
	 * �X�y�[�X�������X�L�b�v���܂��B
	 * �X�y�[�X�����́Aspace, tab, cr, lf �ł��B
	 */
	private static void skipspaces(PushbackReader pr) throws IOException {
		while (true) {
			int c = pr.read();
			switch (c) {
			case ' ': continue;
			case '\t': continue;
			case '\r': continue;
			case '\n': continue;
			case -1: return;
			default:
				pr.unread(c);
				return;
			}
		}
	}
	
	/**
	 * �w�肳�ꂽ PushbackReader ���� JSON value ���P�ǂݍ��݁A
	 * JsonType �Ƃ��ĕԋp���܂��B
	 */
	private static JsonType parseValue(PushbackReader pr) throws IOException {
		skipspaces(pr);
		int c = pr.read();
		JsonType jt = null;
		switch (c) {
		case '-':
			pr.unread(c);
			jt = parseNumber(pr);
			break;
		case '{':
			jt = parseObject(pr);
			break;
		case '[':
			jt = parseArray(pr);
			break;
		case 't':
			expect(pr, "true");
			jt = new JsonValue(true);
			break;
		case 'f':
			expect(pr, "false");
			jt = new JsonValue(false);
			break;
		case 'n':
			expect(pr, "null");
			jt = new JsonValue(null);
			break;
		case '\"':
			jt = parseString(pr);
			break;
		default:
			if (c >= '0' && c <= '9') {
				pr.unread(c);
				jt = parseNumber(pr);
			}
		}
		if (jt == null)	throw new JsonParseException("value �̐擪�������s���ł� : " + (char)c);
		return jt;
	}
	
	/**
	 * �w�肵��������ƂȂ��Ă��邱�Ƃ��`�F�b�N���܂��B
	 * �X�g���[���̏I�������o������A�w�肵��������ƈقȂ��Ă���ꍇ�A
	 * JsonParseException ���X���[���܂��B
	 */
	private static void expect(PushbackReader pr, String expected) throws IOException {
		for (int i = 1; i < expected.length(); i++) {
			int c = pr.read();
			if (c == -1) throw new JsonParseException("�\�����Ȃ��I�������o���܂����B�\������������:"+expected);
			if (expected.charAt(i) != (char)c) throw new JsonParseException("�\�����Ȃ����������o���܂���:"+(char)c+" �\����������:"+expected.charAt(i)+" �\������������:"+expected);
		}
	}
	
	/**
	 * ���l�̉\���̂���g�[�N��(0-9, -+.eE ����Ȃ镶����)�𒊏o���܂��B
	 */
	private static String readNumberToken(PushbackReader pr) throws IOException {
		StringBuilder result = new StringBuilder();
		while (true) {
			int c = pr.read();
			if (c == -1) break;
			if ((c >= '0' && c <= '9') || (c == '-' || c == '+' ||
					c == '.' || c == 'e' || c == 'E')) {
				result.append((char)c);
			} else {
				pr.unread(c);
				break;
			}
		}
		return result.toString();
	}
	
	/**
	 * ���l��ǂݍ��݂܂��B���l�łȂ��ꍇ�AJsonParseException ���X���[���܂��B
	 */
	private static JsonValue parseNumber(PushbackReader pr) throws IOException {
		String token = readNumberToken(pr);
		try {
			if (token.indexOf('.')>-1||token.indexOf('e')>-1||token.indexOf('E')>-1) {
				double v = Double.parseDouble(token);
				return new JsonValue(v);
			} else {
				int v = Integer.parseInt(token);
				return new JsonValue(v);
			}
		} catch (NumberFormatException nfe) {
			throw new JsonParseException("���l�t�H�[�}�b�g�ُ� : " + token);
		}
	}
	
	/**
	 * " �̎��̕����ɃX�g���[��������O��ŁA������������擾���܂��B
	 */
	private static JsonValue parseString(PushbackReader pr) throws IOException {
		return new JsonValue(readString(pr));
	}
	
	private static String readString(PushbackReader pr) throws IOException {
		StringBuilder result = new StringBuilder();
		while (true) {
			int c = pr.read();
			if (c == -1) throw new JsonParseException("������̓r���ŗ\�����Ȃ��I�������m���܂���");
			if (c == '\"') return result.toString();
			if (c < 32) throw new JsonParseException("������̓r���ŉ��s�Ȃǂ̃R���g���[���R�[�h�����m���܂����Bcode = " + c);
			if (c == '\\') {
				c = pr.read();
				if (c == -1) throw new JsonParseException("\\ �̎��ɗ\�����Ȃ��I�������m���܂���");
				switch (c) {
				case '\"':	result.append(c); continue;
				case '\\':	result.append(c); continue;
				case '/':	result.append(c); continue;
				case 'b':	result.append('\b'); continue;
				case 'f':	result.append('\f'); continue;
				case 'n':	result.append('\n'); continue;
				case 'r':	result.append('\r'); continue;
				case 't':	result.append('\t'); continue;
				case 'u':
					int u = 0;
					for (int i = 0; i < 4; i++) {
						c = pr.read();
						if (c >= '0' && c <= '9') u = 16*u + (c-'0');
						else if (c >= 'A' && c <= 'F') u = 16*u + (c-'A') +10;
						else if (c >= 'a' && c <= 'f') u = 16*u + (c-'a') +10;
						else throw new JsonParseException("\\u�̌�̕����񂪕s���ł� : " + (char)c);
					}
					result.append((char)u);
					continue;
				}
			}
			result.append((char)c);
		}
	}
	/**
	 * [ ������O��(Reader�̌��݈ʒu�� [ �̎�)�ŁA�����z����擾���܂��B
	 */
	private static JsonArray parseArray(PushbackReader pr) throws IOException {
		List<JsonType> array = new ArrayList<JsonType>();
		skipspaces(pr);
		int c = pr.read();
		if (c == ']') {
			return new JsonArray(array.toArray(new JsonType[0]));
		}
		pr.unread(c);
		while (true) {
			skipspaces(pr);
			JsonType j = parseValue(pr);
			array.add(j);
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("�z��̏I��̑O�ɏI�������m���܂���");
			if (c == ']') {
				return new JsonArray(array.toArray(new JsonType[0]));
			}
			if (c != ',') throw new JsonParseException("�z����ɕs���ȕ��������o���܂��� : " + (char)c);
		}
	}
	
	/**
	 * { ������O��(Reader�̌��݈ʒu�� { �̎�)�ŁA�����I�u�W�F�N�g���擾
	 * ���܂��B
	 */
	private static JsonObject parseObject(PushbackReader pr) throws IOException {
		JsonObject result = new JsonObject();
		skipspaces(pr);
		int c = pr.read();
		if (c == '}') return result; // ��̃I�u�W�F�N�g
		pr.unread(c);
		
		while (true) {
			skipspaces(pr);
			c = pr.read();
			if (c != '\"') throw new JsonParseException("�I�u�W�F�N�g���̗v�f���� \" �Ŏn�܂��Ă��܂���");
			String name = readString(pr);
			// ������ name �Ƃ��ē����Ă��Ă͂Ȃ�Ȃ��������`�F�b�N
			// �������A�K���������ĂȂ��̂Ŏ蔲��
			// RFC4627 �ɂ��ƁAstring �Ƃ���A�Ȃ�ł�OK�炵��
			// "." �� OK
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("�I�u�W�F�N�g�̗v�f���̌�ɗ\�����Ȃ��I�������m���܂���");
			if (c != ':') throw new JsonParseException("�I�u�W�F�N�g�̗v�f���̌�ɗ\�����Ȃ����������m���܂��� : "+(char)c);
			skipspaces(pr);
			JsonType jt = parseValue(pr);
			result.add(name, jt);
			skipspaces(pr);
			c = pr.read();
			if (c == -1) throw new JsonParseException("�I�u�W�F�N�g�̏I��̑O�ɗ\�����Ȃ��I�������m���܂���");
			if (c == ',') continue;
			if (c == '}') return result;
			throw new JsonParseException("�I�u�W�F�N�g���ɕs���ȕ��������o���܂��� : " + (char)c);
		}
	}

/*----------------------
 * implements(Iterable)
 */
	public java.util.Iterator<JsonType> iterator() {
		throw new ClassCastException("���� JsonType �� " + getClass() + " �̂��߁Aiterator �������܂���");
	}
}
