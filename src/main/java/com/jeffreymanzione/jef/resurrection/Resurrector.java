package com.jeffreymanzione.jef.resurrection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jeffreymanzione.jef.parsing.value.EnumValue;
import com.jeffreymanzione.jef.parsing.value.ListValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.Pair;
import com.jeffreymanzione.jef.parsing.ArrayDefinition;
import com.jeffreymanzione.jef.parsing.Definition;
import com.jeffreymanzione.jef.parsing.ListDefinition;
import com.jeffreymanzione.jef.parsing.MapDefinition;
import com.jeffreymanzione.jef.parsing.SingletonDefintion;
import com.jeffreymanzione.jef.parsing.TupleDefinition;
import com.jeffreymanzione.jef.parsing.value.ArrayValue;
import com.jeffreymanzione.jef.parsing.value.TupleValue;
import com.jeffreymanzione.jef.parsing.value.Value;
import com.jeffreymanzione.jef.parsing.value.primitive.PrimitiveValue;
import com.jeffreymanzione.jef.resurrection.annotations.JEFClass;
import com.jeffreymanzione.jef.resurrection.exceptions.ClassFillingException;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotAssembleClassException;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotTranformValueException;

public class Resurrector {
  private Map<String, Class<? extends JEFEntity<?>>> classes;

  private Map<String, Class<?>>                      enums;

  {
    classes = new LinkedHashMap<String, Class<? extends JEFEntity<?>>>();
    enums = new HashMap<String, Class<?>>();
  }

  @SafeVarargs
  public final boolean addEntityClass (Class<? extends JEFEntity<?>>... cls) {
    boolean success = true;
    for (Class<? extends JEFEntity<?>> cl : cls) {
      success &= classes.put(
          cl.isAnnotationPresent(JEFClass.class)
              ? cl.getAnnotation(JEFClass.class).name() : cl.getSimpleName(),
          cl) != null;
    }
    return success;
  }

  @SafeVarargs
  public final boolean addEnumClass (Class<?>... enums) throws Exception {
    boolean success = true;
    for (Class<?> enu : enums) {
      if (enu.isEnum()) {
        success &= this.enums.put(enu.getAnnotation(JEFClass.class).name(),
            enu) != null;
      } else {
        throw new Exception("Expected enum class, but was not an enum: " + enu);
      }
    }
    return success;
  }

  @SafeVarargs
  public final boolean addTransformer (
      BuiltInResurrector.Transformer<?>... transformers) {
    for (BuiltInResurrector.Transformer<?> trans : transformers) {
      BuiltInResurrector.addTransformer(trans);
    }
    return true;
  }

  private boolean hasClassForDefinition (String name) {
    if (classes.containsKey(name) || enums.containsKey(name)) {
      return true;
    } else {
      return false;
    }
  }

  private Class<?> getClassForName (String name) throws ClassNotFoundException {
    if (classes.containsKey(name)) {
      return classes.get(name);
    } else if (enums.containsKey(name)) {
      return enums.get(name);
    } else {
      return Class.forName(name);
    }
  }

  private void checkIfDefaultConstructorExists (Value<?> val, Class<?> cls)
      throws CouldNotAssembleClassException {
    try {
      if (!cls.getConstructor().isAccessible()) {
        cls.getConstructor().setAccessible(true);
      }
    } catch (SecurityException | NoSuchMethodException e) {
      throw new CouldNotAssembleClassException(
          "Cannot create an instance for val=" + val + ", class=" + cls
              + " because there is no default constructor.");
    }
  }

  private <T extends JEFEntityTuple> T create (TupleValue val, Class<T> cls)
      throws ClassFillingException {
    T obj;
    try {
      checkIfDefaultConstructorExists(val, cls);

      obj = cls.newInstance();
      for (Pair<Integer, ?> p : val) {
        obj.set(p.getKey(), parseToObject(p.getValue()));
      }

      obj.initialize();
      return obj;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new CouldNotAssembleClassException(
          "Failed on newInstance() for val=" + val + ", class=" + cls
              + " Are we allowed to use the default constructor?");
    }

  }

  private <T extends JEFEntityMap> T create (MapValue val, Class<T> cls)
      throws ClassFillingException {
    T obj;
    try {
      checkIfDefaultConstructorExists(val, cls);

      obj = cls.newInstance();
      for (Pair<String, ?> p : val) {
        obj.set(p.getKey(), parseToObject(p.getValue()));
      }

      obj.initialize();
      return obj;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new CouldNotAssembleClassException(
          "Failed on newInstance() for val=" + val + ", class=" + cls
              + " Are we allowed to use the default constructor?");
    }

  }

  @SuppressWarnings("unchecked")
  public <T> T parseToObject (Value<?> value) throws ClassFillingException {

    if (BuiltInResurrector.containsTrasformer(value)) {
      return (T) BuiltInResurrector.transformValue(value);
    } else if (value instanceof PrimitiveValue) {
      return (T) value.getValue();
    } else if (value instanceof EnumValue) {
      return parseToEnum(value);
    } else if (value instanceof ListValue) {
      return (T) parseToList(value);
    } else if (value instanceof ArrayValue) {
      return (T) parseToArray(value);
    } else if (value instanceof TupleValue) {
      return (T) parseToTuple(value);
    } else if (value instanceof MapValue) {
      return (T) parseToMap(value);
    } else {
      System.err.println("Resurrected this value=" + value
          + " as null because it did not match any of the known value types. "
          + "This may not be what you intended.");
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private Object parseToMap (Value<?> value) throws ClassFillingException {
    MapValue mapVal = (MapValue) value;
    if (classes.containsKey(mapVal.getEntityID())) {
      return create(mapVal,
          (Class<? extends JEFEntityMap>) classes.get(mapVal.getEntityID()));
    } else {
      Map<String, Object> result = new HashMap<String, Object>();
      for (Pair<String, ?> pair : mapVal) {
        result.put(pair.getKey(), parseToObject(pair.getValue()));
      }
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  private Object parseToTuple (Value<?> value) throws ClassFillingException {
    TupleValue tupVal = (TupleValue) value;
    if (classes.containsKey(tupVal.getEntityID())) {
      return create(tupVal,
          (Class<? extends JEFEntityTuple>) classes.get(tupVal.getEntityID()));
    } else {
      Object[] objs = new Object[tupVal.size()];
      for (int i = 0; i < objs.length; i++) {
        objs[i] = parseToObject(tupVal.get(i));
      }
      return objs;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T[] parseToArray (Value<?> value) throws ClassFillingException {

    if (!(value instanceof ArrayValue)) {
      throw new ClassFillingException(
          "Expected that value would be an ArrayValue.");
    }

    ArrayValue arrVal = (ArrayValue) value;

    T[] result;
    try {
      result = (T[]) Array.newInstance(
          definitionToClass(arrVal.getDefinedType()), arrVal.size());
      int i = 0;
      for (Value<?> val : arrVal) {
        result[i++] = parseToObject(val);
      }
      return result;
    } catch (NegativeArraySizeException | ClassNotFoundException e) {
      e.printStackTrace();
      throw new ClassFillingException("FAILED HERE.");
    }
  }

  private Class<?> definitionToClass (Definition definedType)
      throws ClassNotFoundException {
    if (definedType == null) {
      return Object.class;
    } else if (definedType instanceof SingletonDefintion) {
      return ((SingletonDefintion) definedType).getRepresentedClass();
    } else if (hasClassForDefinition(definedType.getName())) {
      return getClassForName(definedType.getName());
    } else if (definedType instanceof MapDefinition) {
      return HashMap.class;
    } else if (definedType instanceof ListDefinition) {
      return ArrayList.class;
    } else if (definedType instanceof ArrayDefinition) {
      // TODO: THIS IS A HORRIBLE WAY TO DO THIS. FIND A BETTER ONE.
      return Array
          .newInstance(
              definitionToClass(((ArrayDefinition) definedType).getType()), 0)
          .getClass();
    } else if (definedType instanceof TupleDefinition) {
      return Array.newInstance(Object.class, 0).getClass();
    } else {
      return getClassForName(definedType.getName());
    }
  }

  private List<Object> parseToList (Value<?> value)
      throws ClassFillingException {
    ListValue listVal = (ListValue) value;
    List<Object> list = new ArrayList<>();
    for (Value<?> val : listVal) {
      list.add(parseToObject(val));
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  private <T> T parseToEnum (Value<?> value)
      throws CouldNotAssembleClassException {
    EnumValue enumVal = (EnumValue) value;
    if (enumVal.hasEntityID() && (enumVal.getEntityID().equals("Bool")
        || enumVal.getEntityID().equals("Boolean"))) {
      return (T) (Boolean) Boolean.parseBoolean(value.getValue().toString());
    } else if (enums.containsKey(enumVal.getEntityID())) {

      for (Object enu : enums.get(enumVal.getEntityID()).getEnumConstants()) {
        if (((Enum<?>) enu).name().equals(enumVal.getValue())) {
          return (T) enu;
        }
      }
      throw new CouldNotAssembleClassException(
          "Could not find '" + enumVal.getValue() + "' in enum "
              + enums.get(enumVal.getEntityID() + "."));

    } else {
      throw new CouldNotAssembleClassException("Could not find enum "
          + enumVal.getEntityID()
          + " in enums. Did you add the enum class to the ClassFiller with ClassFiller.addEnumClass()?");

    }
  }

  public final String convertToJEFEntityFormat (Map<String, Object> map)
      throws IllegalArgumentException, IllegalAccessException,
      CouldNotTranformValueException {
    return convertToJEFEntityFormat(map, false, -1);
  }

  public final String convertToJEFEntityFormat (Map<String, Object> map,
      boolean useSpaces, int spacesPerTab) throws IllegalArgumentException,
          IllegalAccessException, CouldNotTranformValueException {
    String result = "";
    for (Entry<String, Class<?>> entry : enums.entrySet()) {
      result += JEFEntity.toJEFEntityHeader(entry.getValue()) + "\n";
    }
    for (Entry<String, Class<? extends JEFEntity<?>>> entry : classes
        .entrySet()) {
      result += JEFEntity.toJEFEntityHeader(entry.getValue()) + "\n";
    }

    String entities = JEFEntity.getValueFromObject(map, -1, useSpaces,
        spacesPerTab);
    result += entities.substring(1, entities.length() - 2);
    return result;
  }

  public final void writeToFile (Map<String, Object> map, boolean useSpaces,
      int spacesPerTab, File file) throws IOException, IllegalArgumentException,
          IllegalAccessException, CouldNotTranformValueException {
    if (!file.exists() && !file.createNewFile()) {
      throw new FileNotFoundException();
    } else {
      OutputStream stream = new FileOutputStream(file);
      writeToStream(map, useSpaces, spacesPerTab, stream);
    }
  }

  public final void writeToFile (Map<String, Object> map, File file)
      throws IOException, IllegalArgumentException, IllegalAccessException,
      CouldNotTranformValueException {
    writeToFile(map, false, -1, file);
  }

  public final void writeToStream (Map<String, Object> map, boolean useSpaces,
      int spacesPerTab, OutputStream stream) throws IllegalArgumentException,
          IllegalAccessException, CouldNotTranformValueException {
    try (PrintWriter out = new PrintWriter(stream)) {
      out.print(convertToJEFEntityFormat(map, useSpaces, spacesPerTab));
    }
  }

  public final void writeToStream (Map<String, Object> map, OutputStream stream)
      throws IllegalArgumentException, IllegalAccessException,
      CouldNotTranformValueException {
    writeToStream(map, false, -1, stream);
  }

}
