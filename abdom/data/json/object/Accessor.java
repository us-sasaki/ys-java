package abdom.data.json.object;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import abdom.data.json.JsonType;
import abdom.data.json.JsonValue;
import abdom.data.json.JsonArray;
import abdom.data.json.JsonObject;

/**
 * public �t�B�[���h�A�܂��� public getter/setter ���\�b�h�ŕ\�����
 * �I�u�W�F�N�g�̃v���p�e�B�ւ� get / set ���\�b�h��񋟂��܂��B
 * �t�B�[���h�A���\�b�h�𓝈�I�Ɉ������߂̃C���^�[�t�F�[�X�ł��B
 * �P��I�u�W�F�N�g�ł́ASimpleAccessor, �z��ł� ArrayAccessor ��
 * �C���X�^���X���̂Ƃ��ė��p���܂��B
 *
 * @version		23 December, 2016
 * @author		Yusuke Sasaki
 */
abstract class Accessor {
	/**
	 * �P��v���p�e�B�̒l�� JsonType �Ƃ��Ď擾���܂��B
	 * �I�u�W�F�N�g�������Ă���l�� null �̏ꍇ�AJsonValue(null) �ł͂Ȃ��A
	 * �P�� null �l���ԋp����܂��B
	 *
	 * @param	instance	�擾�Ώۂ̃C���X�^���X
	 * @return	�擾�l�� Json �\��
	 */
	abstract JsonType get(Object instance);
	
	/**
	 * �P��v���p�e�B�̒l�� JsonType �Őݒ肵�܂��B
	 *
	 * @param	instance	�ݒ�Ώۂ̃C���X�^���X
	 * @parame	arg			�ݒ�l������ JsonType
	 */
	abstract void set(Object instance, JsonType arg);
	
	/**
	 * type �^������ Java �I�u�W�F�N�g�� value �� JSON �\����ԋp���܂��B
	 * value �� null �̏ꍇ�AJsonValue(null) ���ԋp����܂��B
	 * JsonValue(null) �Ƃ���ƁAtoJson �� null �t�B�[���h���\�������
	 * ���܂��B
	 * null �Ƃ���ƁA
	 *
	 * @param	value	Object�^�� Java �C���X�^���X
	 * @param	type	value �̃I�u�W�F�N�g���ł̐錾�^(��value.getClass())
	 * @return	JSON �\��(JsonType)
	 */
	JsonType toJson(Object value, Class<?> type) {
		if (value == null) return new JsonValue(null);
		if (type == boolean.class)
			return new JsonValue( ((Boolean)value).booleanValue() );
		if (type == int.class)
			return new JsonValue( ((Integer)value).intValue() );
		if (type == double.class)
			return new JsonValue( ((Double)value).doubleValue() );
		if (type == String.class)
			return new JsonValue( (String)value );
		if (JsonType.class.isAssignableFrom(type))
			return (JsonType)value; // reference
		if (JValue.class.isAssignableFrom(type))
			return ((JValue)value).toJson();
		// ��ʃI�u�W�F�N�g�^
		
		return JData.toJson(value);
	}
}

/**
 * �z��łȂ��P��v���p�e�B�ւ� Accessor �ł��B
 * ���̃N���X�������ꍇ�A�v���p�e�B�̌^�� boolean, int, double, String,
 * JsonObject, JValue �ł��邱�Ƃ�ۏ؂��Ȃ���΂Ȃ�܂���B
 *
 * @version		23 December, 2016
 * @author		Yusuke Sasaki
 */
class SimpleAccessor extends Accessor {
	Property prop;
	
/*-------------
 * constructor
 */
	SimpleAccessor(Field field) {
		prop = new FieldProperty(field);
	}
	
	SimpleAccessor(Method getter, Method setter) {
		prop = new MethodProperty(getter, setter);
	}
	
/*------------------
 * instance methods
 */
	@Override
	JsonType get(Object instance) {
		Object value = prop.getObj(instance);
		if (value == null) return null;
		
		Class<?> type = prop.getType(); // �錾�^
		return toJson(value, type);
	}
	
	@Override
	void set(Object instance, JsonType arg) {
		Class<?> type = prop.getType(); // �錾�^
		
		if (type.isPrimitive()) {
			// primitive �̂Ƃ��� arg �� JsonValue �̕K�v������
			if (!(arg instanceof JsonValue))
				throw new IllegalFieldTypeException("\"" +
						prop.getName() + "\" field of class \"" +
						instance.getClass().getName() +
						"\" expects type of JsonValue. Specified value: " +
						arg);
			if (type == boolean.class) {
				switch (arg.toString()) {
				case "true":
					prop.setObj(instance, true);
					return;
				case "false":
					prop.setObj(instance, false);
					return;
				default:
					throw new IllegalFieldTypeException("\"" +
							prop.getName() + "\" field of class \"" +
							instance.getClass().getName() +
							"\" is boolean while json value is " + arg);
				}
			}
			if (type == int.class) {
				prop.setObj(instance, arg.getIntValue());
				return;
			}
			if (type == double.class) {
				prop.setObj(instance, arg.getDoubleValue());
				return;
			}
			// ��ʃN���X�Ń`�F�b�N���Ă��邽�߁A�����ɂ͗��Ȃ��͂�
			throw new JDataDefinitionException("\"" + prop.getName() +
					"\" field is not an element of JData category :" +
					instance.getClass().getName());
		}
		// primitive �łȂ��ꍇ
		
		// null ���ݒ肳���ꍇ������
		if (arg == null || arg.getType() == JsonType.TYPE_VOID) {
			prop.setObj(instance, null);
			return;
		}
		
		// null �łȂ��ꍇ
		if (type == String.class) {
			prop.setObj(instance, arg.getValue());
			return;
		}
		// JsonObject �̏ꍇ
		if (JsonObject.class.isAssignableFrom(type)) {
			if (!(arg instanceof JsonObject))
				throw new IllegalFieldTypeException("\"" +
						prop.getName() + "\" field of class \"" +
						instance.getClass().getName() +
						"\" expects type of JsonObject. Specified value: " +
						arg);
			prop.setObj(instance, arg);
			return;
		}
		// ��ʃI�u�W�F�N�g�̏ꍇ
		Object newInstance;
		try {
			newInstance = type.newInstance();
		} catch (ReflectiveOperationException roe) {
			throw new JDataDefinitionException(
					"Failed to instantiate \"" +
					prop.getName() +
					"\". Default constructor of class \"" +
					type.getName() +
					"\" may not be accessible and defined.");
		}
		// JValue �̓���
		if (JValue.class.isAssignableFrom(type)) {
			((JValue)newInstance).fill(arg);
		} else {
			JData.fill(newInstance, arg);
		}
		prop.setObj(instance, newInstance);
	}
}

/**
 * getter / setter �̑g�ŕ\�����v���p�e�B�ւ� Accessor �ł��B
 * ���\�b�h�͈ȉ��𖞂����Ă���K�v������܂��B
 * �������A�{�N���X���ł͍������̂��߂����̃`�F�b�N�͍s���Ă��炸�A
 * ��������Ƃ��Ă��܂��B��ʃN���X�Ŏ������邱�Ƃ�z�肵�Ă��܂��B
 * <pre>
 * 1. getter, setter �̃v���p�e�B����Java Beans �����K���ɍ��v���A
 * �@ ���̑g�œ��ꖼ�̂ƂȂ邱��
 * 2. �Ƃ��� public �� instance method �ł��邱��
 * 3. getter �̕ԋp�^�� setter �̑�2�����̌^������ł��邱��
 * </pre>
 *
 */
 
/**
 * �z��v���p�e�B�ւ� Accessor �ł��B
 *
 * @version		23 December, 2016
 * @author		Yusuke Sasaki
 */
class ArrayAccessor extends Accessor {
	Property prop;
	Class<?> compType;
	
/*-------------
 * constructor
 */
	ArrayAccessor(Field field) {
		prop = new FieldProperty(field);
		compType	= field.getType().getComponentType();
	}
	
	ArrayAccessor(Method getter, Method setter) {
		prop = new MethodProperty(getter, setter);
		compType	= getter.getReturnType().getComponentType();
	}
	
/*------------------
 * instance methods
 */
	@Override
	JsonArray get(Object instance) {
		Object array = prop.getObj(instance);
		
		if (array == null) return null;
		
		JsonArray result = new JsonArray();
		for (int i = 0; i < Array.getLength(array); i++) {
//			Object value = 
			// null �� JsonValue(null) �ƂȂ�BArray �̏ꍇ�� OK
			result.push(toJson(Array.get(array, i), compType));
		}
		return result;
	}
	
	@Override
	void set(Object instance, JsonType arg) {
		if (arg == null || arg.getType() == JsonType.TYPE_VOID) {
			prop.setObj(instance, null);
			return;
		}
		if (!(arg instanceof JsonArray))
			throw new IllegalFieldTypeException("\"" +
					prop.getName() + "\" field of class \"" +
					instance.getClass().getName() +
					"\" expects type of JsonArray. Specified value: " +
					arg);
		
		Object result = Array.newInstance(compType, arg.size());
		
		if (compType == boolean.class) {
			int i = 0;
			for (JsonType elm : arg) {
				switch (elm.toString()) {
				case "true":
					Array.setBoolean(result, i++, true);
					break;
				case "false":
					Array.setBoolean(result, i++, false);
					break;
				default:
					throw new IllegalFieldTypeException("\"" +
						prop.getName() + "\" field component of class \"" +
						instance.getClass().getName() +
						"\" is boolean while json value is " + elm);
				}
			}
		} else if (compType == int.class) {
			int i = 0;
			for (JsonType elm : arg)
				Array.setInt(result, i++, elm.getIntValue());
		} else if (compType == double.class) {
			int i = 0;
			for (JsonType elm : arg)
				Array.setDouble(result, i++, elm.getDoubleValue());
		} else if (compType == String.class) {
			int i = 0;
			for (JsonType elm : arg)
				Array.set(result, i++, elm.getValue());
		} else if (JsonObject.class.isAssignableFrom(compType)) {
			int i = 0;
			for (JsonType elm : arg)
				Array.set(result, i++, elm); // ���̂܂ܐݒ�
		} else {
			// ��ʂ̃I�u�W�F�N�g
			int i = 0;
			for (JsonType elm : arg) {
				if (elm == null || elm.getType() == JsonType.TYPE_VOID) {
					Array.set(result, i++, null);
					continue;
				}
				Object newObj;
				try {
					newObj = compType.newInstance();
				} catch (ReflectiveOperationException roe) {
					throw new JDataDefinitionException(
							"Failed to instantiate \"" +
							prop.getName() +
							"\". Default constructor of class \"" +
							compType.getName() +
							"\" may not be accessible and defined.");
				}
				// JValue �̓���
				if (JValue.class.isAssignableFrom(compType)) {
					((JValue)newObj).fill(elm);
				} else {
					JData.fill(newObj, elm);
				}
				Array.set(result, i++, newObj);
			}
		}
		prop.setObj(instance, result);
	}
}

/**
 * Property �́AJava �I�u�W�F�N�g�̕ϐ�(Field)�A���\�b�h(Method)�ɂ��
 * �l�̓��o�͂����b�v���A����C���^�[�t�F�[�X��񋟂��܂��B
 * ����ɂ��AAccessor �ɂ����āA�v���p�e�B���ϐ��ւ̒��ڎQ��/������A
 * getter / setter ���\�b�h���o�R���邩���ӎ�������舵���܂��B
 *
 * @version		23 December, 2016
 * @author		Yusuke Sasaki
 */
abstract class Property {
	/**
	 * �C���X�^���X�̃v���p�e�B(�ϐ��܂���getter/setter�ŕ\��)���擾����
	 * ���\�b�h�ł�
	 */
	abstract Object getObj(Object instance);
	
	/**
	 * �C���X�^���X�̃v���p�e�B(�ϐ��܂���getter/setter�ŕ\��)��ݒ肷��
	 * ���\�b�h�ł�
	 */
	abstract void setObj(Object instance, Object arg);
	
	/**
	 * �v���p�e�B�̐錾�^�ł��B
	 * Field.getType() , getter.getReturnType() �̒l�ł��B
	 */
	abstract Class<?> getType();
	
	/**
	 * �v���p�e�B�̐錾���ł��B
	 */
	abstract String getName();
}

/**
 * Field �ɂ�� Property �̋�ۃN���X�ł��B
 *
 * @version		23 December, 2016
 * @author		Yusuke Sasaki
 */
class FieldProperty extends Property {
	Field field;
	
/*-------------
 * constructor
 */
	FieldProperty(Field field) {
		this.field = field;
	}
	
/*-----------
 * overrides
 */
	/**
	 * Field �ł́A Field.get �ɂ��l���擾���܂��B
	 * �A�N�Z�X�ł��Ȃ��ꍇ�AJDataDefinitionException ���X���[����܂��B
	 */
	@Override
	Object getObj(Object instance) {
		try {
			return field.get(instance);
		} catch (IllegalAccessException iae) {
			throw new JDataDefinitionException(field.getName() +
					" field of the class " + instance.getClass().getName() +
					" is not accessible. :" + iae);
		}
	}
	
	/**
	 * Field �ł́A Field.set �ɂ��l��ݒ肵�܂��B
	 * �A�N�Z�X�ł��Ȃ��ꍇ�AJDataDefinitionException ���X���[����܂��B
	 */
	@Override
	void setObj(Object instance, Object arg) {
		try {
			field.set(instance, arg);
		} catch (IllegalAccessException iae) {
			throw new JDataDefinitionException(field.getType().getName() + 
					" field \"" + field.getName() + "\" of class \"" +
					instance.getClass().getName() + "\" is not accessible");
		}
	}
	
	@Override
	Class<?> getType() {
		return field.getType();
	}
	
	@Override
	String getName() {
		return field.getName();
	}
}

/**
 * Method �ɂ�� Property �̋�ۃN���X�ł��B
 *
 * @version		23 December, 2016
 * @author		Yusuke Sasaki
 */
class MethodProperty extends Property {
	Method getter;
	Method setter;
	
/*-------------
 * constructor
 */
	MethodProperty(Method getter, Method setter) {
		this.getter = getter;
		this.setter = setter;
	}
	
/*-----------
 * overrides
 */
	/**
	 * Method �ł́A getter.invoke �ɂ��l���擾���܂��B
	 * �A�N�Z�X�ł��Ȃ��ꍇ�AJDataDefinitionException ���X���[����܂��B
	 * ��ʂŃ`�F�b�N����Ă��邽�ߔ������Ȃ��͂��ł����A���S�������߂�
	 * ���ߓ�d�`�F�b�N�Ƃ��Ă��܂��B
	 */
	@Override
	Object getObj(Object instance) {
		try {
			return getter.invoke(instance);
		} catch (ReflectiveOperationException roe) {
			throw new JDataDefinitionException(getter.getName() +
					" method of the class " + instance.getClass().getName() +
					" is not accessible. :" + roe);
		}
	}
	
	/**
	 * Field �ł́A Field.set �ɂ��l��ݒ肵�܂��B
	 * �A�N�Z�X�ł��Ȃ��ꍇ�AJDataDefinitionException ���X���[����܂��B
	 * ��ʂŃ`�F�b�N����Ă��邽�ߔ������Ȃ��͂��ł����A���S�������߂�
	 * ���ߓ�d�`�F�b�N�Ƃ��Ă��܂��B
	 */
	@Override
	void setObj(Object instance, Object arg) {
		try {
			setter.invoke(instance, arg);
		} catch (ReflectiveOperationException roe) {
			throw new JDataDefinitionException(setter.getName() + 
					" method \"" + setter.getName() + "\" of class \"" +
					instance.getClass().getName() + "\" is not accessible");
		}
	}
	
	@Override
	Class<?> getType() {
		return getter.getReturnType();
	}
	
	@Override
	String getName() {
		return getter.getName() + "/"+setter.getName();
	}
}

