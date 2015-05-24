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

	private boolean setFieldAnnot(Field field, String fieldName, Object val) throws CouldNotUpdateEntityMapException {
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

	private boolean setField(Field field, String fieldName, Object val) throws CouldNotUpdateEntityMapException {
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

	@Override
	public boolean addToMap(String fieldName, Object val) throws CouldNotUpdateEntityMapException {
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
	public Object getFromMap(String fieldName) throws CouldNotUpdateEntityMapException {
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
	public Class<?> getFromMapType(String fieldName) throws CouldNotUpdateEntityMapException {
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

}
