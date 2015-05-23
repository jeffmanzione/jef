package com.jeffreymanzione.jef.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jeffreymanzione.jef.parsing.value.ListValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.Pair;
import com.jeffreymanzione.jef.parsing.value.PrimitiveValue;
import com.jeffreymanzione.jef.parsing.value.SetValue;
import com.jeffreymanzione.jef.parsing.value.TupleValue;
import com.jeffreymanzione.jef.parsing.value.Value;

public class ClassFiller {
	private Map<String, Class<? extends JEFEntity>> classes;

	{
		classes = new HashMap<String, Class<? extends JEFEntity>>();
	}

	@SafeVarargs
	public final boolean addEntityClass(Class<? extends JEFEntity>... cls) {
		boolean success = true;
		for (Class<? extends JEFEntity> cl : cls) {
			success &= classes.put(cl.getAnnotation(JEFClass.class).name(), cl) != null;
		}
		return success;
	}

	private <T extends JEFEntity> T create(MapValue val, Class<T> cls) throws InstantiationException,
			IllegalAccessException {
		T obj = cls.newInstance();

		for (Pair<?> p : val) {
			obj.addToMap(p.getKey(), parseToObject(p.getValue()));

		}

		return obj;
	}

	@SuppressWarnings("unchecked")
	public <T> T parseToObject(Value<?> value) throws InstantiationException, IllegalAccessException {
		// System.out.println(value.getClass());
		if (value instanceof PrimitiveValue) {
			// System.out.println(">>> " + value.getValue());
			return (T) value.getValue();
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

}
