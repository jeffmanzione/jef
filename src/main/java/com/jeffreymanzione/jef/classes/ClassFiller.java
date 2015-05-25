package com.jeffreymanzione.jef.classes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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
	private Map<String, Class<? extends JEFEntity<?>>> classes;
	private Map<String, Class<?>> enums;
	{
		classes = new LinkedHashMap<String, Class<? extends JEFEntity<?>>>();
		enums = new HashMap<String, Class<?>>();
	}

	@SafeVarargs
	public final boolean addEntityClass(Class<? extends JEFEntity<?>>... cls) {
		boolean success = true;
		for (Class<? extends JEFEntity<?>> cl : cls) {
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

	private <T extends JEFEntityTuple> T create(TupleValue val, Class<T> cls) throws ClassFillingException {
		T obj;
		try {
			obj = cls.newInstance();
			for (Pair<Integer, ?> p : val) {
				obj.set(p.getKey(), parseToObject(p.getValue()));
			}

			return obj;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new CouldNotAssembleClassException("Failed on newInstance() for val=" + val + ", class=" + cls
					+ " Are we allowed to use the default constructor?");
		}

	}

	private <T extends JEFEntityMap> T create(MapValue val, Class<T> cls) throws ClassFillingException {
		T obj;
		try {
			obj = cls.newInstance();
			for (Pair<String, ?> p : val) {
				obj.set(p.getKey(), parseToObject(p.getValue()));
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
			//System.out.println("TUP " + tupVal + " " + tupVal.getEntityID());
			if (classes.containsKey(tupVal.getEntityID())) {
				return (T) create(tupVal, (Class<? extends JEFEntityTuple>) classes.get(tupVal.getEntityID()));
			} else {
				Object[] objs = new Object[tupVal.size()];
				for (int i = 0; i < objs.length; i++) {
					objs[i] = tupVal.get(i);
				}
				return (T) objs;
			}
		} else /* if MapValue */{
			MapValue mapVal = (MapValue) value;
			if (classes.containsKey(mapVal.getEntityID())) {
				return (T) create(mapVal, (Class<? extends JEFEntityMap>) classes.get(mapVal.getEntityID()));
			} else {
				Map<String, Object> result = new HashMap<String, Object>();
				for (Pair<String, ?> pair : mapVal) {
					result.put(pair.getKey(), parseToObject(pair.getValue()));
				}
				return (T) result;
			}
		}
	}

	public final String convertToJEFEntityFormat(Map<String, Object> map) throws IllegalArgumentException,
			IllegalAccessException {
		String result = "";
		for (Entry<String, Class<?>> entry : enums.entrySet()) {
			result += JEFEntity.toJEFEnumHeader(entry.getValue()) + "\n";
		}
		for (Entry<String, Class<? extends JEFEntity<?>>> entry : classes.entrySet()) {
			result += JEFEntity.toJEFEntityHeader(entry.getValue()) + "\n";
		}

		String entities = JEFEntity.getValueFromObject(map, -1, false, -1);
		result += entities.substring(1, entities.length() - 2);
		return result;
	}

	public final String convertToJEFEntityFormat(Map<String, Object> map, boolean useSpaces, int spacesPerTab)
			throws IllegalArgumentException, IllegalAccessException {
		String result = "";
		for (Entry<String, Class<?>> entry : enums.entrySet()) {
			result += JEFEntity.toJEFEnumHeader(entry.getValue()) + "\n";
		}
		for (Entry<String, Class<? extends JEFEntity<?>>> entry : classes.entrySet()) {
			result += JEFEntity.toJEFEntityHeader(entry.getValue()) + "\n";
		}

		String entities = JEFEntity.getValueFromObject(map, -1, useSpaces, spacesPerTab);
		result += entities.substring(1, entities.length() - 2);
		return result;
	}

	public final void writeToFile(Map<String, Object> map, boolean useSpaces, int spacesPerTab, File file)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		if (!file.exists() && !file.createNewFile()) {
			throw new FileNotFoundException();
		} else {
			OutputStream stream = new FileOutputStream(file);
			writeToStream(map, useSpaces, spacesPerTab, stream);
		}
	}

	public final void writeToFile(Map<String, Object> map, File file) throws IOException, IllegalArgumentException,
			IllegalAccessException {
		if (!file.exists() && !file.createNewFile()) {
			throw new FileNotFoundException();
		} else {
			OutputStream stream = new FileOutputStream(file);
			writeToStream(map, false, -1, stream);
		}
	}

	public final void writeToStream(Map<String, Object> map, OutputStream stream) throws IllegalArgumentException,
			IllegalAccessException {
		try (PrintWriter out = new PrintWriter(stream)) {
			out.print(convertToJEFEntityFormat(map, false, -1));
		}
	}

	public final void writeToStream(Map<String, Object> map, boolean useSpaces, int spacesPerTab, OutputStream stream)
			throws IllegalArgumentException, IllegalAccessException {
		try (PrintWriter out = new PrintWriter(stream)) {
			out.print(convertToJEFEntityFormat(map, useSpaces, spacesPerTab));
		}
	}
}
