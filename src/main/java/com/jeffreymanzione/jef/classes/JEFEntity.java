package com.jeffreymanzione.jef.classes;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class JEFEntity {

	public abstract boolean setField(String fieldName, Object val) throws CouldNotUpdateEntityMapException;

	public abstract Object getField(String fieldName) throws CouldNotUpdateEntityMapException;

	public abstract Class<?> getFieldType(String fieldName) throws CouldNotUpdateEntityMapException;

	public abstract String toJEFEntityFormat(int indents, boolean useSpaces, int spacesPerTab) throws IllegalArgumentException,
			IllegalAccessException;

	
	protected boolean setFieldAnnot(Field field, String fieldName, Object val) throws CouldNotUpdateEntityMapException {
		if (field.isAnnotationPresent(JEFField.class)) {
			JEFField annot = field.getAnnotation(JEFField.class);
			if (annot.key().equals(fieldName)) {
				field.setAccessible(true);

				if (field.getType().isInstance(val)) {
					try {
						field.set(this, val);
						return true;
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new CouldNotUpdateEntityMapException("Could not set object " + this + " with field "
								+ field + " to val " + val
								+ ". Either the security settings prevented it or you did something silly.");
					}
				} else {
					throw new CouldNotUpdateEntityMapException("Could not set object " + this + " with field " + field
							+ " to val " + val + ". Value was not instanceof field.");
				}
			}
		}
		return false;
	}

	private boolean matches(Field field, Object val) {
		if (field.getType().isInstance(val)) {
			return true;
		} else {
			if ((field.getType().equals(int.class) || field.getType().equals(long.class)) && val instanceof Long) {
				return true;
			} else if ((field.getType().equals(float.class) || field.getType().equals(double.class))
					&& val instanceof Double) {
				return true;
			}
		}
		return false;
	}

	protected boolean setField(Field field, String fieldName, Object val) throws CouldNotUpdateEntityMapException {
		if (field.getName().equals(fieldName)) {
			field.setAccessible(true);

			// System.out.println(field.getType() + " " + val.getClass().getName());
			if (matches(field, val)) {
				// System.out.println(field.getName() + " " + fieldName);

				setSafe(field, val);
				return true;

			} else {
				throw new CouldNotUpdateEntityMapException("Could not match types between field " + field + " and val "
						+ val + ".");
			}
		}
		return false;
	}

	private void setSafe(Field field, Object val) throws CouldNotUpdateEntityMapException {
		try {
			if (field.getType().equals(int.class)) {
				field.set(this, ((Long) val).intValue());
			} else if (field.getType().equals(float.class)) {
				field.set(this, ((Double) val).floatValue());
			} else {
				field.set(this, val);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new CouldNotUpdateEntityMapException("Could not set object " + this + " with field " + field
					+ " to val " + val + ". Either the security settings prevented it or you did something silly.");
		}
	}

	@SuppressWarnings("unchecked")
	protected static String writeBody(Object obj, Field field, int indents, boolean useSpaces, int spacesPerTab)
			throws IllegalArgumentException, IllegalAccessException {
		String tab;
		if (useSpaces) {
			tab = "";
			for (int i = 0; i < spacesPerTab; i++) {
				tab += " ";
			}
		} else {
			tab = "\t";
		}
		String indent = "";
		for (int i = 0; i < indents; i++) {
			indent += tab;
		}
		field.setAccessible(true);
		String result = "";
		// System.out.println(field.getType());
		if (JeffEntityMap.class.isAssignableFrom(field.getType())) {
			result += "{\n" + ((JeffEntityMap) field.get(obj)).toJEFEntityFormat(indents + 1, useSpaces, spacesPerTab)
					+ indent + "}";
		} else if (fieldIsPrimitive(field)) {
			result += field.get(obj).toString();
		} else if (field.getType().equals(String.class)) {
			result += "'" + field.get(obj).toString() + "'";
		} else if (field.getType().isEnum()) {
			result += "$" + field.get(obj).toString();
		} else if (List.class.isAssignableFrom(field.getType())) {
			result += "<\n";
			for (Object entry : ((List<Object>) field.get(obj))) {
				result += getValueFromObject(entry, indents + 1, useSpaces, spacesPerTab);
			}
			result += "\n" + indent + ">";
		} else if (Map.class.isAssignableFrom(field.getType())) {
			result += "{\n" + getValueFromObject(field.get(obj), indents + 1, useSpaces, spacesPerTab) + indent + "}\n";
		}
		result += "\n";
		return result;
	}

	@SuppressWarnings("unchecked")
	protected static String getValueFromObject(Object obj, int indents, boolean useSpaces, int spacesPerTab)
			throws IllegalArgumentException, IllegalAccessException {
		String tab;
		if (useSpaces) {
			tab = "";
			for (int i = 0; i < spacesPerTab; i++) {
				tab += " ";
			}
		} else {
			tab = "\t";
		}
		String indent = "";
		for (int i = 0; i < indents; i++) {
			indent += tab;
		}

		String result = indent;

		if (obj instanceof JEFEntity) {
			result += "{\n" + ((JeffEntityMap) obj).toJEFEntityFormat(indents + 1, useSpaces, spacesPerTab) + indent
					+ "}";
		} else if (classIsPrimitive(obj.getClass())) {
			result += obj.toString();
		} else if (obj instanceof String) {
			result = "'" + obj.toString() + "'";
		} else if (obj.getClass().isEnum()) {
			result = "$" + obj.toString();
		} else if (obj instanceof List) {
			result = "<\n";
			for (Object entry : ((List<Object>) obj)) {
				result += getValueFromObject(entry, indents + 1, useSpaces, spacesPerTab);
			}
			result += "\n" + indent + ">";
		} else if (obj instanceof Map) {
			for (Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {
				
				String typeName = "";
				if (JeffEntityMap.class.isAssignableFrom(entry.getValue().getClass())) {
					typeName = " : " + toTypeName(entry.getValue().getClass());
					
				}
				result += indent + entry.getKey() + typeName + " = "
						+ getValueFromObject(entry.getValue(), indents + 1, useSpaces, spacesPerTab);

			}
		}
		result += "\n";

		return result;
	}

	private static boolean fieldIsPrimitive(Field field) {
		return classIsPrimitive(field.getType());
	}

	private static boolean classIsPrimitive(Class<?> cls) {
		return Number.class.isAssignableFrom(cls) || cls.isPrimitive();
	}

	public static final String toJEFEntityHeader(Class<?> cls) {
		String name;
		if (cls.isAnnotationPresent(JEFClass.class)) {
			name = cls.getAnnotation(JEFClass.class).name();
		} else {
			name = cls.getClass().getName();
		}
		String type = /* this.getClass().isEnum() ? "enum" : */"type";

		return type + " : " + name + " " + toHeader(cls);

	}

	protected static String toHeader(Class<?> cls) {
		String result = "{";
		boolean first = true;
		for (Field field : cls.getDeclaredFields()) {
			String typeName = "", valName = "";

			if (!(field.isAnnotationPresent(JEFField.class) && field.getAnnotation(JEFField.class).ignore())) {
				typeName = JEFEntity.toTypeName(field);

				if (field.isAnnotationPresent(JEFField.class)) {
					valName = field.getAnnotation(JEFField.class).key();
				} else {
					valName = field.getName();
				}

				result += (first ? "" : ", ") + typeName + " " + valName;
				first = false;
			} else {
				continue;
			}
		}
		return result + "}";
	}

	protected static String toTypeName(Field field) {
		Class<?> cls = field.getType();

		if (List.class.isAssignableFrom(cls)) {
			//System.out.println(field);
			//System.out.println(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
			return toTypeName((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])
					+ "<>";
		} else if (Map.class.isAssignableFrom(cls)) {
			//System.out.println(field);
			//System.out.println(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1]);
			return toTypeName((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1])
					+ "{}";
		} else {
			return toTypeName(cls);
		}
	}

	protected static String toTypeName(Class<?> cls) {
		if (cls.isAnnotationPresent(JEFClass.class)) {
			return cls.getAnnotation(JEFClass.class).name();
		} else {
			if (cls.equals(double.class) || cls.equals(Double.class)) {
				return "FLOAT";
			} else if (classIsPrimitive(cls) || cls.equals(String.class)) {
				return cls.getSimpleName().toUpperCase();
			} else {
				return cls.getSimpleName();
			}
		}
	}

	public static String toJEFEnumHeader(Class<?> cls) {
		String name;
		if (cls.isAnnotationPresent(JEFClass.class)) {
			name = cls.getAnnotation(JEFClass.class).name();
		} else {
			name = cls.getClass().getName();
		}
		String type = "enum";

		return type + " : " + name + " of [" + asEnum(cls) + "]";
	}

	private static String asEnum(Class<?> cls) {
		String result = "";
		boolean first = true;
		for (Object obj : cls.getEnumConstants()) {
			result += (first ? "" : ", ") + obj.toString();
			first = false;
		}
		return result;
	}
}
