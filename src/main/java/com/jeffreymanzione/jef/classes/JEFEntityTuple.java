package com.jeffreymanzione.jef.classes;

import java.lang.reflect.Field;

public abstract class JEFEntityTuple extends JEFEntity<Integer> {

	public abstract int size();

	@Override
	public boolean set(Integer key, Object val) throws CouldNotUpdateEntityException {
		if (key < 0 || key > size()) {
			throw new CouldNotUpdateEntityException("Could not set field because index was " + key
					+ " and not in range [0," + size() + "). Maybe you used indexing starting with 1 instead of 0?");
		} else {
			for (Field field : this.getClass().getDeclaredFields()) {
				// System.out.println(this.getClass().getSimpleName() + "\t" + field.getName() + "\t" +
				// field.isAnnotationPresent(JEFTuple.class));
				if (field.isAnnotationPresent(JEFTuple.class) && field.getAnnotation(JEFTuple.class).index() == key) {
					// System.out.println(">>> " + field.getAnnotation(JEFTuple.class).index() + " " + key);
					if (matches(field, val)) {
						field.setAccessible(true);
						setSafe(field, val);
						return true;
					} else {
						throw new CouldNotUpdateEntityException("Found index " + key + " in tuple, but it was a "
								+ "different type from its associated field. Expected "
								+ field.getType().getSimpleName() + " but was " + val.getClass().getSimpleName() + ".");
					}
				}
			}
			throw new CouldNotUpdateEntityException("Could not find key! This means the class was constructed "
					+ "incorrectly. Either no field is labeled with this index or the size() method is wrong. index="
					+ key + " and val=" + val + ".");
		}
	}

	@Override
	public Object get(Integer key) throws CouldNotUpdateEntityException {
		if (key < 0 || key > size()) {
			throw new CouldNotUpdateEntityException("Could not set field because index was " + key
					+ " and not in range [0," + size() + ").");
		} else {
			for (Field field : this.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(JEFTuple.class) && field.getAnnotation(JEFTuple.class).index() == key) {
					field.setAccessible(true);
					try {
						return field.get(this);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new CouldNotUpdateEntityException("Unexpected security exception: fieldName='"
								+ field.getName() + "' index=" + key + ".\n See Exception in the stack trace.");
					}
				}
			}
			throw new CouldNotUpdateEntityException("Could not find key! This means the class was constructed "
					+ "incorrectly. Either no field is labeled with this index or the size() method is wrong. index="
					+ key + ".");
		}
	}

	@Override
	public Class<?> getType(Integer key) throws CouldNotUpdateEntityException {
		if (key < 0 || key > size()) {
			throw new CouldNotUpdateEntityException("Could not set field because index was " + key
					+ " and not in range [0," + size() + ").");
		} else {
			for (Field field : this.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(JEFTuple.class) && field.getAnnotation(JEFTuple.class).index() == key) {
					field.setAccessible(true);
					return field.getType();
				}
			}
			throw new CouldNotUpdateEntityException("Could not find key! This means the class was constructed "
					+ "incorrectly. Either no field is labeled with this index or the size() method is wrong. index="
					+ key + ".");
		}
	}

	@Override
	public String toJEFEntityFormat(int indents, boolean useSpaces, int spacesPerTab) throws IllegalArgumentException,
			IllegalAccessException {
		String[] arr = new String[size()];

		for (Field field : this.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(JEFTuple.class)) {
				field.setAccessible(true);
				arr[field.getAnnotation(JEFTuple.class).index()] = JEFEntity.getValueFromObject(field.get(this),
						indents, useSpaces, spacesPerTab);
			}
		}
		String result = "(" + arr[0];

		for (int i = 1; i < size(); i++) {
			result += ", " + arr[i];
		}

		return result + ")";

	}

}
