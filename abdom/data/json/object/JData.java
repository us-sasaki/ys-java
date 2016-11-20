package abdom.data.json.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;

import abdom.data.json.JsonType;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;
import abdom.data.json.JsonValue;

/**
 * Json �I�u�W�F�N�g�� Java �I�u�W�F�N�g�ɂ���Ė͕킵�܂��B
 * ���̃N���X���p�����邱�ƂŁAJava �I�u�W�F�N�g�� JSON �`���̑��ݕϊ���
 * �e�ՂɂȂ�܂��B
 * �����o�ϐ��Ƃ��Ď��̃t�B�[���h(JData�J�e�S��)���w��ł��܂��B<pre>
 *
 * boolean, int, double, String, JValue, JsonType
 * ����сA�����̌^�̔z�� �AList<JData>
 *
 * </pre>�Öق̃t�B�[���h�Ƃ��āA_fragment (JsonType�^, JsonObject �܂���
 * JsonArray) �����݂��Afill() �̍ۂɖ���`�̃t�B�[���h�l�͂��ׂĂ�����
 * �i�[����܂��B
 * �܂��AtoJson() �ł� _fragment �t�B�[���h�͑��݂���(not null)�ꍇ�̂�JSON
 * �����o�Ƃ��Č���܂��B
 * �q�N���X�ŁAJSON�`���Ƃ̑��ݕϊ��ΏۊO�̕ϐ����`�������ꍇ�A
 * transient �C���q�����ĉ������B
 *
 * @version	November 12, 2016
 * @author	Yusuke Sasaki
 */
public abstract class JData extends JValue {

	/** fill �ł��Ȃ������l���i�[����\��̈� */
	protected transient JsonObject _fragment;
	
	/**
	 * �q�N���X�̃R���X�g���N�^�� super() ���ĂіY�ꂽ�Ƃ��ɗ�O��
	 * ���������邽�߂̃t���O�B
	 * ���񉻂Ɋ֌W�Ȃ����߁Atransient �B
	 */
	private transient boolean fieldChecked = false;
	
/*-------------
 * constructor
 */
	/**
	 * ���̃R���X�g���N�^�́A�q�N���X�̃R���X�g���N�^�ŕK���Ă�ł��������B
	 * �Ă΂Ȃ��ꍇ�Afill, toJson ���\�b�h�� IllegalStateException ��
	 * �X���[����܂��B
	 * �C���X�^���X���̍ۂɁA�C���X�^���X�ϐ��� JData �J�e�S���̂��̂��ǂ���
	 * ���`�F�b�N���܂��B
	 */
	protected JData() {
		Field[] fields = this.getClass().getFields();
		
		for (Field f : fields) {
			// static �͏��O
			if (Modifier.isStatic(f.getModifiers())) continue;
			// transient �����O
			if (Modifier.isTransient(f.getModifiers())) continue;
			
			Class type = f.getType();
			
			if (isJDataCategory(type)) continue;
			throw new IllegalFieldTypeException("Illegal type " + type + " has found. JData field must consist of boolean, int, double, String, JValue, JsonType and their arrays.");
		}
		fieldChecked = true;
	}
	
/*------------------
 * instance methods
 */
	/**
	 * �w�肳�ꂽ Class �I�u�W�F�N�g�� JData �J�e�S���Ɋ܂܂�邩
	 * �`�F�b�N���܂��B
	 */
	private boolean isJDataCategory(Class type) {
		// �v���~�e�B�u�AJsonType, JValue
		if ( Boolean.TYPE.isAssignableFrom(type) ||
			Integer.TYPE.isAssignableFrom(type) ||
			Double.TYPE.isAssignableFrom(type) ||
			String.class.isAssignableFrom(type) ||
			JValue.class.isAssignableFrom(type) ||
			JsonType.class.isAssignableFrom(type) ) return true;
		
		// �z��
		if (boolean[].class.isAssignableFrom(type) ||
			int[].class.isAssignableFrom(type) ||
			double[].class.isAssignableFrom(type) ||
			String[].class.isAssignableFrom(type) ||
			JValue[].class.isAssignableFrom(type) ||
			JsonType[].class.isAssignableFrom(type) ) return true;
		
		// List<JData>
		if (List.class.isAssignableFrom(type)) return true;
		
		return false;
	}
	
	/**
	 * fragment �������ǂ����e�X�g���܂��B
	 *
	 * @return	fragment �����ꍇ�Atrue
	 */
	public boolean hasFragments() {
		return (_fragment != null);
	}
	
	/**
	 * fragment �� keySet ��ԋp���܂��B�Ȃ��ꍇ�Anull �ƂȂ�܂��B
	 *
	 * @return	fragment �̃L�[
	 */
	public Set getFragmentKeySet() {
		if (_fragment == null) return null;
		return _fragment.map.keySet();
	}
	
	/**
	 * fragment �A�N�Z�X���\�b�h�ŁAJsonType �l���擾���܂��B
	 *
	 * @param	key	fragment �� key ���
	 * @return	key �ɑΉ�����l
	 */
	public JsonType getFragment(String key) {
		if (_fragment == null) return null;
		return _fragment.get(key);
	}
	
	/**
	 * fragment �A�N�Z�X���\�b�h�ŁAJsonType �l��ݒ肵�܂��B
	 *
	 * @param	key	fragment �� key ���
	 * @param	jt	�ݒ肷��l
	 */
	public void putFragment(String key, JsonType jt) {
		if (_fragment == null) _fragment = new JsonObject();
		_fragment.put(key, jt);
	}
	
	/**
	 * fragment �I�u�W�F�N�g(JsonObject)��ԋp���܂��B
	 *
	 * @return	fragment �I�u�W�F�N�g(JsonObject)�Bnull �̏ꍇ������܂��B
	 */
	public JsonObject getFragments() {
		return _fragment;
	}
	
	/**
	 * �w�肳�ꂽ JsonObject �̓��e�����̃I�u�W�F�N�g�ɐݒ肵�܂��B
	 * �����̌^�́A���֐��̂��� JsonType �Ƃ��Ă��܂����AJsonObject
	 * �ȊO���w�肷��ƁAClassCastException ���X���[����܂��B
	 * ���̃��\�b�h�͒l��ǉ����A�����l�͏㏑������Ȃ���Εۑ������
	 * ���Ƃɒ��ӂ��Ă��������B_fragment �����l�ł��B
	 *
	 * @param	json	���̃I�u�W�F�N�g�ɒl��^���� JsonType
	 */
	@Override
	public void fill(JsonType json) {
		if (!fieldChecked)
			throw new IllegalStateException("It is necessary to invoke the constructor of super class(JData).");
		JsonObject jobj = (JsonObject)json; // may throw ClassCastException
		
		Field[] fields = this.getClass().getFields();
		Map<String, Field> fmap = new TreeMap<String, Field>();
		for (Field f: fields) {
			// static �ϐ��͏��O
			if (Modifier.isStatic(f.getModifiers())) continue;
			// transient �ϐ������O
			if (Modifier.isTransient(f.getModifiers())) continue;
			fmap.put(f.getName(), f);
		}
		
		for (String key : jobj.map.keySet()) {
			Field f = fmap.get(key);
			if (f == null) {
				// Field ���Ȃ��ꍇ�A_fragment �Ɋi�[
				if (_fragment == null) _fragment = new JsonObject();
				_fragment.put(key, jobj.get(key));
			} else {
				try {
					fillMember(f, jobj.get(key));
				} catch (InstantiationException ie) {
					throw new IllegalFieldTypeException(ie.toString());
				} catch (IllegalAccessException iae) {
					throw new IllegalFieldTypeException(iae.toString());
				}
			}
		}
	}
	
	/**
	 * �����o�ϐ��P�̒l�� val �Őݒ肵�܂��B�ݒ�Ώۂ� JsonType ��
	 * �^�Ƀ~�X�}�b�`���������ꍇ�AIllegalFieldTypeException ���X���[
	 * ����܂��B
	 *
	 * @param	f	�ݒ�Ώۂ� Field
	 * @param	val	�ݒ�ΏۂɑΉ�����l�������Ă��� JsonType
	 */
	private void fillMember(Field f, JsonType val) throws InstantiationException, IllegalAccessException {
		// �t�B�[���h�����擾����
		String name = f.getName();
		
		// �^���擾����
		Class type = f.getType();
		
		// �^�ɂ��A�K�؂ɕϊ����Ċi�[
		if (Boolean.TYPE.isAssignableFrom(type)) {

			// boolean �^�̏ꍇ
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonValue(boolean) instead of type " + type);
			switch (val.toString()) {
			case "true":
				f.setBoolean(this, true);
				break;
			case "false":
				f.setBoolean(this, false);
				break;
			default:
				throw new IllegalFieldTypeException(name + " field is boolean while json value is " + val);
			}
		} else if (Integer.TYPE.isAssignableFrom(type)) {
		
			// int �^�̏ꍇ
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonValue(number/int) instead of type " + type);
			f.setInt(this, Integer.parseInt(val.toString()));
		} else if (Double.TYPE.isAssignableFrom(type)) {
		
			// double �^�̏ꍇ
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonValue(number/double) instead of type " + type);
			f.setDouble(this, Double.parseDouble(val.toString()));
			
		} else if (String.class.isAssignableFrom(type)) {
			
			// String �^�̏ꍇ
			if (!(val instanceof JsonValue))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonValue(string) instead of type " + type);
			String str = val.toString(); // "" �ň͂܂�Ă���͂�
			if (!str.startsWith("\"") || !str.endsWith("\"") )
				throw new IllegalFieldTypeException(name + " field is expected as Json string. The value: " + str);
			f.set(this, str.substring(1, str.length() - 1));
		} else if (JValue.class.isAssignableFrom(type)) {
		
			// JValue �^�̏ꍇ
			if (!(val instanceof JsonType))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonType instead of type " + type);
			
			Object instance = f.get(this);
			if (instance == null) instance = type.newInstance();
			((JValue)instance).fill(val);
			f.set(this, instance);
		} else if (JsonType.class.isAssignableFrom(type)) {
		
			// JsonType �^�̏ꍇ
//System.out.println("type = "+type+" f = " + f+" val = "+val);
			f.set(this, JsonType.parse(val.toString()));
		} else if (boolean[].class.isAssignableFrom(type)) {
		
			//
			// �z��̏ꍇ
			//
			
			// boolean[] �^�̏ꍇ
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(boolean) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			boolean[] instance = new boolean[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonValue(boolean) instead of type " + type);
				
				switch (j.toString()) {
				case "true":
					instance[i++] = true;
					break;
				case "false":
					instance[i++] = false;
					break;
				default:
					throw new IllegalFieldTypeException(name + " array-field is boolean while json value is " + val);
				}
			}
			f.set(this, instance);
		} else if (int[].class.isAssignableFrom(type)) {
			
			// int[] �^�̏ꍇ
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(int) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			int[] instance = new int[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonValue(int) instead of type " + type);
				instance[i++] = Integer.parseInt(j.toString());
			}
			f.set(this, instance);
		} else if (double[].class.isAssignableFrom(type)) {
			
			// double[] �^�̏ꍇ
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(double) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			double[] instance = new double[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonValue(double) instead of type " + type);
				instance[i++] = Double.parseDouble(j.toString());
			}
			f.set(this, instance);
		} else if (String[].class.isAssignableFrom(type)) {
			
			// String[] �^�̏ꍇ
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(String) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			String[] instance = new String[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				if (!(j instanceof JsonValue))
					throw new IllegalFieldTypeException(name + " array-field is expected as type of JsonValue(String) instead of type " + type);
				String str = j.toString();
				if (!str.startsWith("\"") || !str.endsWith("\"") )
					throw new IllegalFieldTypeException(name + " field is expected as Json string. The value: " + str);
				instance[i++] = str.substring(1, str.length() - 1);
			}
			f.set(this, instance);
		} else if (JValue[].class.isAssignableFrom(type)) {
			
			// JValue[] �^�̏ꍇ
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray instead of type " + type);
			JsonArray ja = (JsonArray)val;
			
			// �q�N���X�Ő錾����Ă���^�ł̔z��𐶐����A�Ƃ肠���� JValue[]
			// �^�ŕێ�����
			Class comptype = type.getComponentType();
			JValue[] instance = (JValue[])Array.newInstance(comptype, ja.size());
			int i = 0;
			for (JsonType j : ja.array) {
				JValue elm = (JValue)comptype.newInstance();
				elm.fill(j);
				instance[i++] = elm;
			}
			f.set(this, instance);
			
		} else if (JsonType[].class.isAssignableFrom(type)) {
			
			// JsonType[] �^�̏ꍇ
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(JsonType) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			JsonType[] instance = new JsonType[ja.size()];
			int i = 0;
			for (JsonType j : ja.array) {
				JsonType elm = JsonType.parse(j.toString()); // deep copy
				instance[i++] = elm;
			}
			f.set(this, instance);
		} else if (List.class.isAssignableFrom(type)) {
			
			// List �^�̏ꍇ
			// List<JData> �Ǝq�N���X�Ő錾����Ă��Ă��AJData �^��
			// ���s���Ɏ󂯎�邱�Ƃ͂ł��Ȃ��B(<JData>�̓R���p�C��
			// �R���e�L�X�g�ł̂ݕێ�����鑮���̂���)
			// ���������āA�{�����ł́@List�^�� List<JData> �Ƃ݂Ȃ��B
			//
			// List.toArray(new String[]{}) �ȂǁA������n������A
			// �W�F�l���b�N�^���o�C���h�����N���X���ł́Anew E[]{} ��
			// class.getComponentClass ���ĂԂ��Ƃœ��邱�Ƃ͉\�B
			if (!(val instanceof JsonArray))
				throw new IllegalFieldTypeException(name + " field is expected as type of JsonArray(JsonObject) instead of type " + type);
			JsonArray ja = (JsonArray)val;
			List<JData> instance = new ArrayList<JData>();
			for (JsonType j : ja.array) {
				JData elm = (JData)type.getComponentType().newInstance();
				elm.fill(j);
				instance.add(elm);
			}
			f.set(this, instance);
		} else {
			new IllegalFieldTypeException(name + " field is not an element of JData category :" + type);
		}
	}
	
	/**
	 * JSON�`���̕�����Ńt�B�[���h�𖄂߂܂��B�����I�ɂ́A�����񂩂�
	 * JsonType ���\�����Afill(JsonType) ���Ă�ł��܂��B
	 *
	 * @param	jsonString	�l��ێ����� JSON ������
	 */
	public void fill(String jsonString) {
		fill(JsonType.parse(jsonString));
	}
	
	/**
	 * ���̃I�u�W�F�N�g�� JsonObject �ɕϊ����܂��B
	 *
	 * @return	JsonObject
	 */
	@Override
	public JsonType toJson() {
		if (!fieldChecked)
			throw new IllegalStateException("It is inevitable to invoke the constructor of super class(JData).");
		try {
			return toJsonImpl();
		} catch (IllegalAccessException iae) {
			throw new IllegalFieldTypeException("..");
		}
	}
	
	/**
	 * toJson �̎����{�̂ł��B
	 */
	private JsonObject toJsonImpl() throws IllegalAccessException {
		JsonObject result = new JsonObject();
		
		Field[] fields = this.getClass().getFields();
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers())) continue;
			if (Modifier.isTransient(f.getModifiers())) continue;
			if (f.get(this) == null) continue;
			String name = f.getName();
			Class type = f.getType();
			
			try {
				// �v���~�e�B�u�^
				if (Boolean.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getBoolean(this)));
				} else if (Integer.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getInt(this)));
				} else if (Double.TYPE.isAssignableFrom(type)) {
					result.put(name, new JsonValue(f.getDouble(this)));
					
				// String, JValue, JsonType
				} else if (String.class.isAssignableFrom(type)) {
					result.put(name, new JsonValue((String)f.get(this)));
				} else if (JValue.class.isAssignableFrom(type)) {
					result.put(name, ((JValue)f.get(this)).toJson());
				} else if (JsonType.class.isAssignableFrom(type)) {
					result.put(name, (JsonType)f.get(this));
					
				// �z��^
				} else if (boolean[].class.isAssignableFrom(type)) {
					boolean[] v = (boolean[])f.get(this);
					result.put(name, new JsonArray(v));
				} else if (int[].class.isAssignableFrom(type)) {
					int[] v = (int[])f.get(this);
					result.put(name, new JsonArray(v));
				} else if (double[].class.isAssignableFrom(type)) {
					double[] v = (double[])f.get(this);
					result.put(name, new JsonArray(v));
				} else if (String[].class.isAssignableFrom(type)) {
					String[] v = (String[])f.get(this);
					result.put(name, new JsonArray(v));
				} else if (JValue[].class.isAssignableFrom(type)) {
					JValue[] v = (JValue[])f.get(this);
					JsonArray ja = new JsonArray();
					for (JValue jd : v) ja.push(jd.toJson());
					result.put(name, ja);
				} else if (JsonType[].class.isAssignableFrom(type)) {
					JsonType[] v = (JsonType[])f.get(this);
					result.put(name, new JsonArray(v));
					
				// List (List<JData> �Ƃ݂Ȃ�)
				} else if (List.class.isAssignableFrom(type)) {
					@SuppressWarnings("unchecked")
					List<JData> v = (List<JData>)f.get(this);
					JsonArray ja = new JsonArray();
					for (JData jd : v) ja.push(jd.toJson());
					result.put(name, ja);
				} else {
					throw new IllegalFieldTypeException("Unexpected type " + type + "is found.");
				}
			} catch (NullPointerException npe) {
System.out.println(npe);
				result.put(name, (String)null);
			}
		}
		// _fragment ��ǉ�
		if (_fragment != null) {
			for (String key : _fragment.map.keySet()) {
				result.put(key, _fragment.get(key));
			}
		}
		return result;
	}
	
/*-----------
 * overrides
 */
	@Override
	public String toString() {
		return toJson().toString();
	}
	
}
