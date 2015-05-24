package com.jeffreymanzione.jef.classes;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JeffEntityMap extends JEFEntity {

	private Map<String, Object> mappings;
	private Map<String, Class<?>> classes;

	{
		mappings = new HashMap<>();
		classes = new HashMap<>();
	}

	@Override
	public boolean setField(String fieldName, Object val) throws CouldNotUpdateEntityMapException {
		for (Field field : this.getClass().getDeclaredFields()) {
			if (this.setFieldAnnot(field, fieldName, val)) {
				return true;
			}
		}
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			// System.out.println(">|< " + field.getName() + ", " + fieldName + ", " + val);
			if (field != null && this.setField(field, fieldName, val)) {
				return true;
			}
		} catch (NoSuchFieldException e) {
			if (mappings.containsKey(fieldName)) {
				if (val.getClass().isInstance(classes.get("fieldName"))) {
					mappings.put(fieldName, val);
					return true;
				} else {
					throw new CouldNotUpdateEntityMapException(
							"Could not set field's value in map as it was already set to a different type "
									+ " nor in the map: fieldName='" + fieldName + "' and val=" + val + " expected="
									+ mappings.get(fieldName) + ".");
				}
			} else {
				System.err.println("Suspicious: Tried to add (key=" + fieldName + ",val=" + val + ") to a "
						+ this.getClass().getName()
						+ ", but it does not have a field that matches it. Going to add it to the auxilliary map, "
						+ "but check to see if this is a mistake.");

				classes.put(fieldName, val.getClass());
				mappings.put(fieldName, val);
			}
		} catch (SecurityException e) {
			throw new CouldNotUpdateEntityMapException("Unexpected security exception: fieldName='" + fieldName
					+ "' and val=" + val + ".\n See Exception in the stack trace.");
		}
		return false;
	}

	@Override
	public Object getField(String fieldName) throws CouldNotUpdateEntityMapException {
		for (Field field : this.getClass().getFields()) {
			if (field.isAnnotationPresent(JEFField.class)) {
				JEFField annot = field.getAnnotation(JEFField.class);
				if (annot.key().equals(fieldName)) {
					field.setAccessible(true);
					try {
						return field.get(this);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new CouldNotUpdateEntityMapException("Field " + field + " in " + this
								+ " could not be read. Check the security settings.");

					}
				}
			}
		}
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			if (field != null) {
				if (field.getName().equals(fieldName)) {
					field.setAccessible(true);
					try {
						return field.get(this);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new CouldNotUpdateEntityMapException("Field " + field + " in " + this
								+ " could not be read. Check the security settings.");
					}
				}
			}
		} catch (NoSuchFieldException e) {
			if (mappings.containsKey(fieldName)) {
				return mappings.get(fieldName);
			} else {
				throw new CouldNotUpdateEntityMapException("Could not set field's value in map as it was not a field"
						+ " nor in the map: fieldName='" + fieldName + ".");
			}
		} catch (SecurityException e) {
			throw new CouldNotUpdateEntityMapException("Unexpected security exception: fieldName='" + fieldName + ".");
		}
		return false;
	}

	@Override
	public Class<?> getFieldType(String fieldName) throws CouldNotUpdateEntityMapException {
		for (Field field : this.getClass().getFields()) {
			if (field.isAnnotationPresent(JEFField.class)) {
				JEFField annot = field.getAnnotation(JEFField.class);
				if (annot.key().equals(fieldName)) {
					field.setAccessible(true);
					return field.getClass();
				}
			}
		}
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			if (field != null) {
				if (field.getName().equals(fieldName)) {
					field.setAccessible(true);
					return field.getClass();
				}
			}
		} catch (NoSuchFieldException e) {
			if (mappings.containsKey(fieldName)) {
				return classes.get(fieldName);
			} else {
				throw new CouldNotUpdateEntityMapException("Could not set field's value in map as it was not a field"
						+ " nor in the map: fieldName='" + fieldName + ".");
			}
		} catch (SecurityException e) {
			throw new CouldNotUpdateEntityMapException("Unexpected security exception: fieldName='" + fieldName + ".");
			// e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toJEFEntityFormat(int indents, boolean useSpaces, int spacesPerTab) throws IllegalArgumentException,
			IllegalAccessException {
		String result = "";
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

		for (Field field : this.getClass().getDeclaredFields()) {
			JEFField jf = field.getAnnotation(JEFField.class);

			String name;
			if (jf != null && !jf.ignore()) {
				name = jf.key();
			} else if (jf == null) {
				name = field.getName();
			} else {
				continue;
			}

			String typeName = "";
			if (JeffEntityMap.class.isAssignableFrom(field.getType())) {
				typeName = " : " + toTypeName(field);

			}
			result += indent + name + typeName + " = " + writeBody(this, field, indents, useSpaces, spacesPerTab);
		}
		for (Entry<String, Object> entry : this.mappings.entrySet()) {
			String name = entry.getKey();

			String typeName = "";
			if (JeffEntityMap.class.isAssignableFrom(classes.get(entry.getKey()))) {
				typeName = " : " + toTypeName(classes.get(entry.getKey()));

			}
			result += indent + name + typeName + " = "
					+ getValueFromObject(entry.getValue(), indents, useSpaces, spacesPerTab);
		}
		// result += "\n";

		return result;
	}

}
