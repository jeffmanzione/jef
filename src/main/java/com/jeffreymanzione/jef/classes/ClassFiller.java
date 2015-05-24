package com.jeffreymanzione.jef.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jeffreymanzione.jef.parsing.value.EnumValue;
import com.jeffreymanzione.jef.parsing.value.ListValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.Pair;
import com.jeffreymanzione.jef.parsing.value.PrimitiveValue;
import com.jeffreymanzione.jef.parsing.value.SetValue;
import com.jeffreymanzione.jef.parsing.value.TupleValue;
import com.jeffreymanzione.jef.parsing.value.Value;

public class ClassFiller {
	private Map<String, Class<? extends JEFEntity>> classes;
	private Map<String, Class<?>> enums;
	{
		classes = new LinkedHashMap<String, Class<? extends JEFEntity>>();
		enums = new HashMap<String, Class<?>>();
	}
	
	@SafeVarargs
	public final boolean addEntityClass(Class<? extends JEFEntity>... cls) {
		boolean success = true;
		for (Class<? extends JEFEntity> cl : cls) {
			success &= classes.put(cl.getAnnotation(JEFClass.class).name(), cl) != null;
		}
		return success;
	}

	@SafeVarargs
	public final boolean addEnumClass(Class<?>... enums) throws Exception {
		boolean success = true;
		for (Class<?> enu : enums) {
			if (enu.isEnum()) {
				success &= this.enums.put(enu.getAnnotation(JEFClass.class).name(), enu) != null;
			} else {
				throw new Exception("Expected enum class, but was not an enum: " + enu);
			}
		}
		return success;
	}

	private <T extends JEFEntity> T create(MapValue val, Class<T> cls) throws ClassFillingException {
		T obj;
		try {
			obj = cls.newInstance();
			for (Pair<?> p : val) {
				obj.setField(p.getKey(), parseToObject(p.getValue()));
			}

			return obj;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new CouldNotAssembleClassException("Failed on newInstance() for val=" + val + ", class=" + cls
					+ " Are we allowed to use the default constructor?");
		}

	}

	@SuppressWarnings("unchecked")
	public <T> T parseToObject(Value<?> value) throws ClassFillingException {
		// System.out.println(value.getClass());
		if (value instanceof PrimitiveValue) {
			// System.out.println(">>> " + value.getValue());
			return (T) value.getValue();
		} else if (value instanceof EnumValue) {
			EnumValue enumVal = (EnumValue) value;
			if (enums.containsKey(enumVal.getEntityID())) {
				for (Object enu : enums.get(enumVal.getEntityID()).getEnumConstants()) {
					if (((Enum<?>) enu).name().equals(enumVal.getValue())) {
						return (T) enu;
					}
				}
				throw new CouldNotAssembleClassException("Could not find '" + enumVal.getValue() + "' in enum "
						+ enums.get(enumVal.getEntityID() + "."));
			} else {
				throw new CouldNotAssembleClassException("Could not find enum " + enumVal.getEntityID()
						+ " in enums. Did you add the enum class to the ClassFiller with ClassFiller.addEnumClass()?");

			}
		} else if (value instanceof ListValue) {
			ListValue listVal = (ListValue) value;
			List<Object> list = new ArrayList<>();
			for (Value<?> val : listVal) {
				list.add(parseToObject(val));
			}
			return (T) list;
		} else if (value instanceof SetValue) {
			SetValue setVal = (SetValue) value;
			Set<Object> result = new HashSet<Object>();
			for (Value<?> val : setVal) {
				result.add(parseToObject(val));
			}
			return (T) result;
		} else if (value instanceof TupleValue) {
			TupleValue tupVal = (TupleValue) value;
			Object[] objs = new Object[tupVal.size()];
			for (int i = 0; i < objs.length; i++) {
				objs[i] = tupVal.get(i);
			}
			return (T) objs;
		} else /* if MapValue */{
			MapValue mapVal = (MapValue) value;
			if (classes.containsKey(mapVal.getEntityID())) {
				return (T) create(mapVal, classes.get(mapVal.getEntityID()));
			} else {
				Map<String, Object> result = new HashMap<String, Object>();
				for (Pair<?> pair : mapVal) {
					result.put(pair.getKey(), parseToObject(pair.getValue()));
				}
				return (T) result;
			}
		}
	}
	

	public final String convertToJEFEntityFormat(Map<String, Object> map, boolean useSpaces, int spacesPerTab)
			throws IllegalArgumentException, IllegalAccessException {
		String result = "";
		for (Entry<String, Class<?>> entry : enums.entrySet()) {
			result += JEFEntity.toJEFEnumHeader(entry.getValue()) + "\n";
		}
		for (Entry<String, Class<? extends JEFEntity>> entry : classes.entrySet()) {
			result += JEFEntity.toJEFEntityHeader(entry.getValue()) + "\n";
		}
		result += "\n";
		
		result += JEFEntity.getValueFromObject(map, -1, useSpaces, spacesPerTab);
		
		return result;
	}
}
