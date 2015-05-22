package com.jeffreymanzione.jef.classes;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AbstractJeffEntity implements JEFEntity {

	private Map<String, Object> mappings;
	private Map<String, Class<?>> classes;

	{
		mappings = new HashMap<>();
		classes = new HashMap<>();
	}

	private boolean setFieldAnnot(Field field, String fieldName, Object val) {
		if (field.isAnnotationPresent(JEFField.class)) {
			JEFField annot = field.getAnnotation(JEFField.class);
			if (annot.key().equals(fieldName)) {
				field.setAccessible(true);

				if (field.getType().isInstance(val)) {
					try {
						field.set(this, val);
						return true;
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				} else {

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

	private boolean setField(Field field, String fieldName, Object val) {
		if (field.getName().equals(fieldName)) {
			field.setAccessible(true);

			//System.out.println(field.getType() + " " + val.getClass().getName());
			if (matches(field, val)) {
				// System.out.println(field.getName() + " " + fieldName);
				try {
					setSafe(field, val);
					return true;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			} else {

			}
		}
		return false;
	}

	private void setSafe(Field field, Object val) throws IllegalArgumentException, IllegalAccessException {
		if (field.getType().equals(int.class)) {
			field.set(this, ((Long) val).intValue());
		} else if (field.getType().equals(float.class)) {
			field.set(this, ((Double) val).floatValue());
		} else {
			field.set(this, val);
		}

	}

	@Override
	public boolean addToMap(String fieldName, Object val) {
		for (Field field : this.getClass().getDeclaredFields()) {
			if (this.setFieldAnnot(field, fieldName, val)) {
				return true;
			}
		}
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			//System.out.println(">|< " + field.getName() + ", " + fieldName + ", " + val);
			if (field != null && this.setField(field, fieldName, val)) {
				return true;
			}
		} catch (NoSuchFieldException e) {
			if (mappings.containsKey(fieldName)) {
				if (val.getClass().isInstance(classes.get("fieldName"))) {
					mappings.put(fieldName, val);
					return true;
				} else {
					System.out.println("Could not set field's value in map as it was already set to a different type "
							+ " nor in the map: fieldName='" + fieldName + "' and val=" + val + " expected="
							+ mappings.get(fieldName) + ".");
				}
			} else {
				classes.put(fieldName, val.getClass());
				mappings.put(fieldName, val);
			}
		} catch (SecurityException e) {
			System.out.println("Unexpected security exception: fieldName='" + fieldName + "' and val=" + val
					+ ".\n See Exception in the stack trace.");
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Object getFromMap(String fieldName) throws IllegalArgumentException, IllegalAccessException {
		for (Field field : this.getClass().getFields()) {
			if (field.isAnnotationPresent(JEFField.class)) {
				JEFField annot = field.getAnnotation(JEFField.class);
				if (annot.key().equals(fieldName)) {
					field.setAccessible(true);
					return field.get(fieldName);
				}
			}
		}
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			if (field != null) {
				if (field.getName().equals(fieldName)) {
					field.setAccessible(true);
					return field.get(fieldName);
				}
			}
		} catch (NoSuchFieldException e) {
			if (mappings.containsKey(fieldName)) {
				return mappings.get(fieldName);
			} else {
				System.out.println("Could not set field's value in map as it was not a field"
						+ " nor in the map: fieldName='" + fieldName + ".");
			}
		} catch (SecurityException e) {
			System.out.println("Unexpected security exception: fieldName='" + fieldName + ".");
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Class<?> getFromMapType(String fieldName) {
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
				System.out.println("Could not set field's value in map as it was not a field"
						+ " nor in the map: fieldName='" + fieldName + ".");
			}
		} catch (SecurityException e) {
			System.out.println("Unexpected security exception: fieldName='" + fieldName + ".");
			e.printStackTrace();
		}
		return null;
	}

}
