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
 * Java �I�u�W�F�N�g�� JSON �̑��ݕϊ��Ɋւ��� static ���\�b�h��񋟂��܂��B
 * �ϊ��ΏۂƂȂ� Java �I�u�W�F�N�g�̓����o�ϐ��Ƃ��Ď��̌^(JData�J�e�S��)��
 * �w��ł��܂��B<pre>
 *
 * boolean, int, long, double, String, JValue(,JData), JsonObject
 * ����сA�����̌^�̔z��
 *
 * </pre>
 * JSON�`���Ƃ̑��ݕϊ��ΏۊO�Ƃ���ϐ����`�������ꍇ�A
 * transient �C���q�����ĉ������B
 * <pre>
 * null �l�ɂ��ẮA���̂悤�Ɏ�舵���܂��B
 *       Java Object                    toJson()
 *  Object null;              ->   ����Ȃ�
 *  JsonObject null;          ->   ����Ȃ�
 *
 *       �@�@JSON                    fill()
 *  ����Ȃ�                  ->   �ݒ肵�Ȃ�
 *  null                      ->   Object null; ��ݒ�
 *                                 JsonObject null; ��ݒ�(JsonObject �̂���)
 * </pre>
 *
 * @version	December 24, 2016
 * @author	Yusuke Sasaki
 */
public class Jsonizer {

	/**
	 * Field �`�F�b�N�̓N���X���ƂɂP�x�����s���΂悢���߁A
	 * �s�������ǂ������N���X�P�ʂŕێ�����B
	 * ���� Set �Ɋ܂܂�� Class �̓`�F�b�N�ρB
	 */
	private static Map<Class<?>, Map<String, Accessor>> _fieldAccessors;
	static {
		_fieldAccessors = new HashMap<Class<?>, Map<String, Accessor>>();
	}
	
	private static class MethodPair {
		Method getter;
		List<Method> setter = new ArrayList<Method>();
	}
	
/*---------------
 * class methods
 */
	public static JsonObject fill(Object instance, JsonType arg) {
		if (instance instanceof JValue && !(instance instanceof JData)) {
			((JValue)instance).fill(arg);
			return null; // extra �͂Ȃ��H
		}
		
		Map<String, Accessor> accessors = getAccessors(instance);
		
		JsonObject extra = null;
		try {
			JsonObject hoe = (JsonObject)arg;
		} catch (ClassCastException cce) {
			throw new RuntimeException("instance = " + instance + " class = " + instance.getClass() + "arg = " + arg + " " + arg.getClass());
		}
		JsonObject jobj = (JsonObject)arg; // may throw ClassCastException
		
		for (String name : jobj.keySet()) {
			Accessor a = accessors.get(name);
			if (a == null) {
				// Field ���Ȃ��ꍇ�A_extra �Ɋi�[
				if (extra == null) extra = new JsonObject();
				extra.put(name, jobj.get(name));
			} else {
				a.set(instance, jobj.get(name));
			}
		}
		return extra;
	}
	
	public static JsonType toJson(Object instance) {
		if (instance instanceof JValue && !(instance instanceof JData))
			return ((JValue)instance).toJson();
		
		Map<String, Accessor> accessors = getAccessors(instance);
		
		JsonObject result = new JsonObject();
		for (String name : accessors.keySet()) {
			Accessor a = accessors.get(name);
			JsonType value = a.get(instance);
			if (value != null) result.put(name, value);
		}
		return result;
	}
	
	/**
	 * ���̃C���X�^���X�̃N���X�Ɋ֘A���� Accessor (�l�擾/�ݒ�I�u�W�F�N�g)
	 * ���擾���܂��B
	 * �Ȃ��ꍇ�A�������܂��B
	 *
	 * @param	instance	Json�ϊ����s���C���X�^���X
	 */
	static Map<String, Accessor> getAccessors(Object instance) {
		Class<?> cls = instance.getClass();
		
		// JValue �̃C���X�^���X���AJData �̃C���X�^���X�łȂ�
		// �ꍇ(JValue �𒼐ڌp��)�AAccessor �ɂ��ݒ�łȂ��A
		// fill(), toJson() �ɂ��ϊ����s�����ƂƂ��Anull ���ԋp�����B
		if (JValue.class.isAssignableFrom(cls) &&
			!JData.class.isAssignableFrom(cls) ) return null;
			
		synchronized (cls) {
			Map<String, Accessor> accessors = _fieldAccessors.get(cls);
			if (accessors != null) return accessors;
			
System.out.println("generate accessor of " + instance.getClass());
			//
			// Accessors �𐶐�����
			//
			accessors = new HashMap<String, Accessor>();
			
			// Accessor ��ݒ肷��B
			// �ȉ��̃��\�b�h�͓��ꖼ�ŏ㏑�����邽�߁A���ꖼ�̂ł�
			// method �� field �ɗD�悷�邱�ƂƂȂ�		
			addFieldAccessors(accessors, cls);
			addMethodAccessors(accessors, cls);
			
			synchronized (_fieldAccessors) {
				_fieldAccessors.put(cls, accessors);
			}
			return accessors;
		}
	}
	
	/**
	 * �^����ꂽ Accessor �Ɏw�肳�ꂽ�N���X�� public �t�B�[���h�ɑ΂���
	 * Accessor ��ǉ����܂��B
	 */
	private static void addFieldAccessors(
							Map<String, Accessor> accessors,
							Class<?> cls) {
		// public �t�B�[���h�𑖍�
		Field[] fields = cls.getFields(); // public field ���擾
		
		for (Field f : fields) {
			// static �͏��O
			if (Modifier.isStatic(f.getModifiers())) continue;
			// transient �����O
			if (Modifier.isTransient(f.getModifiers())) continue;
			
			Class type = f.getType();
			if (!isJDataCategory(type))
				throw new IllegalFieldTypeException("Illegal type \"" +
						type.getName() + "\" has found in field \""+
						f.getName()+ "\" of class \"" + cls.getName() +
						"\". JData field must consist of boolean, int, long, double, String, JValue, JsonObject, their arrays. To prevent the field from Jsonizing, set transient.");
			String name = f.getName();
			if (type.isArray()) {
System.out.println("field put array " + name);
				accessors.put(name, new ArrayAccessor(f));
			} else {
System.out.println("field put " + name);
				accessors.put(name, new SimpleAccessor(f));
			}
		}
	}
	
	/**
	 * �^����ꂽ Accessor �Ɏw�肳�ꂽ�N���X�� public setter/getter ���\�b�h
	 * �ō\�������v���p�e�B�ւ� Accessor ��ǉ����܂��B
	 */
	private static void addMethodAccessors(
							Map<String, Accessor> accessors,
							Class<?> cls) {
		// getter/setter ���\�b�h�𑖍�
		Method[] methods = cls.getMethods(); // public methods ���擾
		
		// �y�A(���)���i�[
		Map<String, MethodPair> pairs = new HashMap<String, MethodPair>();
		
		for (Method m : methods) {
			// static �͏��O
			if (Modifier.isStatic(m.getModifiers())) continue;
			
			// �����^�A���^�[���^���`�F�b�N
			// 
			String methodName = m.getName();
			if (methodName.length() < 4) continue; // ���\�b�h���S���������͏��O
			char c = methodName.charAt(3);
			if (Character.isLowerCase(c)) continue; // 4�����ڏ������͏��O
			// geta() �� getA() ���قȂ郁�\�b�h��������v���p�e�B�ƂȂ邽��
			
			// �v���p�e�B���� Java Beans �K���ɂ̂��Ƃ萶��
			String name;
			if (methodName.length() == 4) name = methodName;
			else {
				if (Character.isUpperCase(methodName.charAt(4))) {
					// �񕶎��ڂ��啶���̏ꍇ�A���̂܂�
					// �� getURL() / setURL()�@-> URL
					name = methodName.substring(3);
				} else {
					// �ꕶ���ڂ���������
					// �� getCount() / setCount() -> count
					name = ""+Character.toLowerCase(c)+methodName.substring(4);
				}
			}
			
			Class<?> retType  = m.getReturnType();
			Class<?>[] params = m.getParameterTypes();
			
			if (methodName.startsWith("get")) { // get
			
				if (params.length != 0) continue; // �����t���͏��O
				if (!isJDataCategory(retType)) continue; // JData cat�łȂ����̂͏��O
				// set �ƃy�A�ɂȂ�܂ł͏��O
				// �����̂Ȃ� get ���\�b�h��1�����Ȃ�(overload���Ȃ�)
				MethodPair mp = pairs.get(name);
				if (mp == null) mp = new MethodPair();
				mp.getter	= m;
				pairs.put(name, mp); // getter ��o�^
				
			} else if (methodName.startsWith("set")) { // set
			
				if (params.length != 1) continue; // �����͂P����
				if (!isJDataCategory(params[0])) continue; // JData cat�łȂ����̂͏��O
				// get �ƃy�A�ɂȂ�܂ł͏��O
				//
				MethodPair mp = pairs.get(name);
				if (mp == null) mp = new MethodPair();
				mp.setter.add(m);
				pairs.put(name, mp);
			}
		}
for (String name : pairs.keySet())
	System.out.println("method entry : " + name + " " + pairs.get(name).getter + "/" + pairs.get(name).setter);
		
		// get �� returnType �� set �� argType ������̂��̂�I��
		// Number getNumber() ��
		// setNumber(Integer) �̓}�b�`���Ȃ����Ƃƌ��߂�B
		// ���l�ɁAint getCount()  void setCount(Integer) ���}�b�`���Ȃ�
		for (String name : pairs.keySet()) {
			MethodPair mp = pairs.get(name);
			
			// �y�A���Ȃ��ꍇ�X�L�b�v
			if (mp.getter == null || mp.setter.size() == 0) continue;
			
			// �y�A�ƂȂ��Ă����ꍇ�Aget �� retType �� set �� paramType ��
			// ��v����g�ݍ��킹������
			Class<?> retType = mp.getter.getReturnType();
			Method theOther = null;
			for (Method setter : mp.setter) {
				if (setter.getParameterTypes()[0] == retType) {
					theOther = setter;
					break;
				}
			}
			if (theOther != null) {
				if (retType.isArray()) {
System.out.println("method put array " + name);
					accessors.put(name, new ArrayAccessor(mp.getter, theOther));
				} else {
System.out.println("method put " + name);
					accessors.put(name, new SimpleAccessor(mp.getter, theOther));
				}
			}
		}
	}
	
	/**
	 * �w�肳�ꂽ Class �I�u�W�F�N�g�� JData �J�e�S���Ɋ܂܂�邩
	 * �`�F�b�N���܂��B
	 */
	private static boolean isJDataCategory(Class type) {
		// �v���~�e�B�u�AString, JsonObject, JValue
		if ( boolean.class == type ||
			int.class == type ||
			long.class == type ||
			double.class == type ||
			String.class == type ||
			JValue.class.isAssignableFrom(type) ||
			JsonObject.class.isAssignableFrom(type) ) return true;
		
		// �z��
		if (boolean[].class == type ||
			int[].class == type ||
			long[].class == type ||
			double[].class == type ||
			String[].class == type ||
			JValue[].class.isAssignableFrom(type) ||
			JsonObject[].class.isAssignableFrom(type) ) return true;
		
		return false;
	}
	
	/**
	 * JsonType(JsonArray) ����AJData[] �𐶐�����֗��֐��ł��B
	 * �w�肷�� JsonType �́AJsonObject ��v�f�Ɏ��� JsonArray �ł���
	 * �K�v������܂��B
	 * �Ԃ����z��̎��s���̌^�́A�w�肳�ꂽ�z��̌^�ɂȂ�܂��B
	 * �w�肳�ꂽ�z��Ƀ��X�g�����܂�ꍇ�́A���̔z��ŕԂ���܂��B����
	 * �ȊO�̏ꍇ�́A�w�肳�ꂽ�z��̎��s���̌^�� JsonArray �̃T�C�Y��
	 * �g���ĐV�����z�񂪊��蓖�Ă��܂��B 
	 *
	 * @param	source	�l��ێ����Ă��� JsonType
	 * @param	array	�l���i�[����z��(�̌^)
	 * @return	JsonType �̒l���ݒ肳�ꂽ JData �̎q�N���X�̃C���X�^���X�̔z��
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JData> T[] toArray(JsonType source, T[] array) {
		int size = source.size(); // may throw class cast exception
		T[] result = null;
		Class compType = array.getClass().getComponentType();
		if (array.length >= size) {
			result = array;
		} else {
			result = (T[])Array.newInstance(compType, size);
		}
		for (int i = 0; i < size; i++) {
			try {
				result[i] = (T)compType.newInstance();
				result[i].fill(source.get(i));
			} catch (InstantiationException ie) {
				throw new JDataDefinitionException("Failed to instantiate \"" + compType.getName() + "\". Default constructor may not be accessible or defined.");
			} catch (IllegalAccessException iae) {
				throw new JDataDefinitionException(iae.toString());
			}
		}
		return result;
	}
	
	/**
	 * JSON �����񂩂�AJData[] �𐶐�����֗��֐��ł��B
	 * �w�肷�� JSON ������́Aobject �� array �ł���K�v������܂��B
	 * �Ԃ����z��̎��s���̌^�́A�w�肳�ꂽ�z��̌^�ɂȂ�܂��B
	 * �w�肳�ꂽ�z��Ƀ��X�g�����܂�ꍇ�́A���̔z��ŕԂ���܂��B����
	 * �ȊO�̏ꍇ�́A�w�肳�ꂽ�z��̎��s���̌^�� JSON array�̃T�C�Y��
	 * �g���ĐV�����z�񂪊��蓖�Ă��܂��B 
	 *
	 * @param	source	�l��ێ����Ă��� JsonType
	 * @param	array	�l���i�[����z��(�̌^)
	 * @return	JsonType �̒l�������ꂽ JData �̎q�N���X�̔z��
	 */
	public static <T extends JData> T[] toArray(String source, T[] array) {
		return toArray(JsonType.parse(source), array);
	}
}
