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
 * public フィールド、または public getter/setter メソッドで表される
 * オブジェクトのプロパティへの get / set メソッドを提供します。
 * フィールド、メソッドを統一的に扱うためのインターフェースです。
 * 単一オブジェクトでは、SimpleAccessor, 配列では ArrayAccessor を
 * インスタンス実体として利用します。
 *
 * @version		23 December, 2016
 * @author		Yusuke Sasaki
 */
abstract class Accessor {
	/**
	 * 単一プロパティの値を JsonType として取得します。
	 * オブジェクトが持っている値が null の場合、JsonValue(null) ではなく、
	 * 単に null 値が返却されます。
	 *
	 * @param	instance	取得対象のインスタンス
	 * @return	取得値の Json 表現
	 */
	abstract JsonType get(Object instance);
	
	/**
	 * 単一プロパティの値を JsonType で設定します。
	 *
	 * @param	instance	設定対象のインスタンス
	 * @parame	arg			設定値を持つ JsonType
	 */
	abstract void set(Object instance, JsonType arg);
	
	/**
	 * type 型をもつ Java オブジェクトの value の JSON 表現を返却します。
	 * value が null の場合、JsonValue(null) が返却されます。
	 * JsonValue(null) とすると、toJson で null フィールドが表示されて
	 * しまう。
	 * null とすると、
	 *
	 * @param	value	Object型の Java インスタンス
	 * @param	type	value のオブジェクト内での宣言型(≒value.getClass())
	 * @return	JSON 表現(JsonType)
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
		// 一般オブジェクト型
		
		return JData.toJson(value);
	}
}

/**
 * 配列でない単一プロパティへの Accessor です。
 * このクラスを扱う場合、プロパティの型は boolean, int, double, String,
 * JsonObject, JValue であることを保証しなければなりません。
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
		
		Class<?> type = prop.getType(); // 宣言型
		return toJson(value, type);
	}
	
	@Override
	void set(Object instance, JsonType arg) {
		Class<?> type = prop.getType(); // 宣言型
		
		if (type.isPrimitive()) {
			// primitive のときは arg は JsonValue の必要がある
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
			// 上位クラスでチェックしているため、ここには来ないはず
			throw new JDataDefinitionException("\"" + prop.getName() +
					"\" field is not an element of JData category :" +
					instance.getClass().getName());
		}
		// primitive でない場合
		
		// null が設定される場合がある
		if (arg == null || arg.getType() == JsonType.TYPE_VOID) {
			prop.setObj(instance, null);
			return;
		}
		
		// null でない場合
		if (type == String.class) {
			prop.setObj(instance, arg.getValue());
			return;
		}
		// JsonObject の場合
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
		// 一般オブジェクトの場合
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
		// JValue の特例
		if (JValue.class.isAssignableFrom(type)) {
			((JValue)newInstance).fill(arg);
		} else {
			JData.fill(newInstance, arg);
		}
		prop.setObj(instance, newInstance);
	}
}

/**
 * getter / setter の組で表されるプロパティへの Accessor です。
 * メソッドは以下を満たしている必要があります。
 * ただし、本クラス内では高速化のためこれらのチェックは行っておらず、
 * 実装制約としています。上位クラスで実装することを想定しています。
 * <pre>
 * 1. getter, setter のプロパティ名がJava Beans 命名規則に合致し、
 * 　 この組で同一名称となること
 * 2. ともに public な instance method であること
 * 3. getter の返却型と setter の第2引数の型が同一であること
 * </pre>
 *
 */
 
/**
 * 配列プロパティへの Accessor です。
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
			// null は JsonValue(null) となる。Array の場合は OK
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
				Array.set(result, i++, elm); // そのまま設定
		} else {
			// 一般のオブジェクト
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
				// JValue の特例
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
 * Property は、Java オブジェクトの変数(Field)、メソッド(Method)による
 * 値の入出力をラップし、同一インターフェースを提供します。
 * これにより、Accessor において、プロパティが変数への直接参照/代入か、
 * getter / setter メソッドを経由するかを意識せず取り扱えます。
 *
 * @version		23 December, 2016
 * @author		Yusuke Sasaki
 */
abstract class Property {
	/**
	 * インスタンスのプロパティ(変数またはgetter/setterで表現)を取得する
	 * メソッドです
	 */
	abstract Object getObj(Object instance);
	
	/**
	 * インスタンスのプロパティ(変数またはgetter/setterで表現)を設定する
	 * メソッドです
	 */
	abstract void setObj(Object instance, Object arg);
	
	/**
	 * プロパティの宣言型です。
	 * Field.getType() , getter.getReturnType() の値です。
	 */
	abstract Class<?> getType();
	
	/**
	 * プロパティの宣言名です。
	 */
	abstract String getName();
}

/**
 * Field による Property の具象クラスです。
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
	 * Field では、 Field.get により値を取得します。
	 * アクセスできない場合、JDataDefinitionException がスローされます。
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
	 * Field では、 Field.set により値を設定します。
	 * アクセスできない場合、JDataDefinitionException がスローされます。
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
 * Method による Property の具象クラスです。
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
	 * Method では、 getter.invoke により値を取得します。
	 * アクセスできない場合、JDataDefinitionException がスローされます。
	 * 上位でチェックされているため発生しないはずですが、堅牢性を高める
	 * ため二重チェックとしています。
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
	 * Field では、 Field.set により値を設定します。
	 * アクセスできない場合、JDataDefinitionException がスローされます。
	 * 上位でチェックされているため発生しないはずですが、堅牢性を高める
	 * ため二重チェックとしています。
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

