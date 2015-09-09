package com.jeffreymanzione.jef.resurrection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.TreeMap;

import com.jeffreymanzione.jef.resurrection.annotations.JEFClass;
import com.jeffreymanzione.jef.resurrection.annotations.JEFField;
import com.jeffreymanzione.jef.resurrection.annotations.JEFTuple;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotTranformValueException;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotUpdateEntityException;

/**
 * 
 * Class which represents a JEF type.
 * 
 * @author Jeff Manzione
 *
 * @param <KEY>
 *          The key that the entity should use for lookup of values stored within the entity
 * 
 * @see JEFEntityMap
 * @see JEFEntityTuple
 */
public abstract class JEFEntity<KEY> {

  public JEFEntity() {

  }

  protected Map<KEY, Object>   mappings;
  protected Map<KEY, Class<?>> classes;

  {
    mappings = new HashMap<>();
    classes = new HashMap<>();
  }

  /**
   * Put here what needs to be done after this object is created to fully initialized.
   */
  protected void initialize() {

  }

  public abstract boolean set(KEY key, Object val) throws CouldNotUpdateEntityException;

  public abstract Object get(KEY key) throws CouldNotUpdateEntityException;

  public abstract Class<?> getType(KEY key) throws CouldNotUpdateEntityException;

  public abstract String toJEFEntityFormat(int indents, boolean useSpaces, int spacesPerTab)
      throws IllegalArgumentException, IllegalAccessException, CouldNotTranformValueException;

  protected boolean setFieldWithAnnotation(Field field, KEY key, Object val)
      throws CouldNotUpdateEntityException {

    JEFField annot = field.getAnnotation(JEFField.class);
    if (!field.isAnnotationPresent(JEFField.class) || !annot.key().equals(key.toString())) {
      return false;
    }

    field.setAccessible(true);

    if (!field.getType().isInstance(val)) {
      throw new CouldNotUpdateEntityException("Could not set object " + this + " with field "
          + field + " to val " + val + ". Value was not instanceof field.");
    }

    try {
      field.set(this, val);
      return true;
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new CouldNotUpdateEntityException(
          "Could not set object " + this + " with field " + field + " to val " + val
              + ". Either the security settings prevented it or you did something silly.");
    }
  }

  protected static boolean matches(Field field, Object val) {
    return matches(field.getType(), val.getClass());
    // if (field.getType().isInstance(val)) {
    // return true;
    // } else if (field.getType().isArray() && val.getClass().isArray()) {
    // Class<?> fieldCompType = field.getType().getComponentType(),
    // valCompType = val.getClass().getComponentType();
    // while (fieldCompType.isArray() && valCompType.isArray()) {
    // fieldCompType = fieldCompType.getComponentType();
    // valCompType = valCompType.getComponentType();
    // }
    // return matches(fieldCompType, valCompType);
    // } else {
    // return matchesPrimitive(field.getType(), val);
    // }
  }

  protected static boolean matches(Class<?> cls1, Class<?> cls2) {
    if (cls1.equals(cls2)) {
      return true;
    }

    if ((cls1.equals(int.class) || cls1.equals(Integer.class) || cls1.equals(Long.class)
        || cls1.equals(long.class))
        && (cls2.equals(int.class) || cls2.equals(Integer.class) || cls2.equals(Long.class)
            || cls2.equals(long.class))) {
      return true;
    } else if ((cls1.equals(double.class) || cls1.equals(Double.class) || cls1.equals(Float.class)
        || cls1.equals(float.class))
        && (cls2.equals(double.class) || cls2.equals(Double.class) || cls2.equals(Float.class)
            || cls2.equals(float.class))) {
      return true;
    } else if ((cls1.equals(boolean.class) || cls1.equals(Boolean.class))
        && (cls2.equals(boolean.class) || cls2.equals(Boolean.class))) {
      return true;
    } else if (Map.class.isAssignableFrom(cls1) && Map.class.isAssignableFrom(cls2)) {
      return true;
    } else if (List.class.isAssignableFrom(cls1) && List.class.isAssignableFrom(cls2)) {
      return true;
    } else if (cls1.isArray() && cls2.isArray()) {
      Class<?> cls1Type = cls1.getComponentType(), cls2Type = cls2.getComponentType();
      while (cls1Type.isArray() && cls2Type.isArray()) {
        cls1Type = cls1Type.getComponentType();
        cls2Type = cls2Type.getComponentType();
      }
      return matches(cls1Type, cls2Type);
    }
    return false;
  }

  protected static boolean matchesPrimitive(Class<?> cls, Object val) {
    if ((cls.equals(int.class) || cls.equals(long.class)) && val instanceof Integer) {
      return true;
    } else if ((cls.equals(float.class) || cls.equals(double.class)) && val instanceof Double) {
      return true;
    } else if ((cls.equals(boolean.class) || cls.equals(Boolean.class)) && val instanceof Boolean) {
      return true;
    } else {
      return false;
    }
  }

  protected boolean setField(Field field, KEY key, Object val)
      throws CouldNotUpdateEntityException {
    if (!field.getName().equals(key.toString())) {
      return false;
    }

    field.setAccessible(true);

    if (!matches(field, val)) {
      throw new CouldNotUpdateEntityException(
          "Could not match types between field '" + field.getName() + "' and val " + val + ".");
    }
    setSafe(field, val);
    return true;
  }

  protected Object handlePrimitives(Class<?> fieldType, Object val)
      throws CouldNotUpdateEntityException {
    Class<?> cls = val.getClass();
    if (classIsPrimitive(cls) && !cls.isPrimitive()) {
      if (cls.equals(Integer.class)) {
        return (int) val;
      } else if (cls.equals(Double.class)) {
        return (double) val;
      } else if (cls.equals(Boolean.class)) {
        return (boolean) val;
      }
    } else if (cls.isArray()) {
      return handleTypeConversion(fieldType, val);
    }
    return val;
  }

  private Object handleTypeConversion(Class<?> fieldType, Object val)
      throws CouldNotUpdateEntityException {
    Class<?> valType = val.getClass();
    if (fieldType.equals(valType)) {
      return val;
    }

    if (!matches(fieldType, valType)) {
      throw new CouldNotUpdateEntityException("Could not convert from class "
          + valType.getSimpleName() + " to " + fieldType.getSimpleName() + ".");
    }

    if (fieldType.equals(int.class)) {
      return (int) val;
    } else if (fieldType.equals(double.class)) {
      return (double) val;
    } else if (fieldType.equals(long.class)) {
      return (long) val;
    } else if (fieldType.equals(float.class)) {
      return (float) val;
    } else if (fieldType.equals(boolean.class)) {
      return (boolean) val;
    } else if (!fieldType.isArray() && !valType.isArray()) {
      return val;
    }

    Object output = Array.newInstance(fieldType.getComponentType(), Array.getLength(val));
    for (int i = 0; i < Array.getLength(output); i++) {
      Object elt = Array.get(val, i);
      Array.set(output, i, handleTypeConversion(fieldType.getComponentType(), elt));
    }

    return output;
  }

  protected void setSafe(Field field, Object val) throws CouldNotUpdateEntityException {
    try {
      if (field.getType().equals(val.getClass())) {
        field.set(this, val);
      } else {
        field.set(this, handleTypeConversion(field.getType(), val));
      }
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
      throw new CouldNotUpdateEntityException(
          "Could not set object " + this + " with field " + field + " to val " + val
              + ". Either the security settings prevented it or you did something silly.");
    }
  }

  private static String listToString(List<Object> list, int indents, boolean useSpaces,
      int spacesPerTab)
          throws IllegalArgumentException, IllegalAccessException, CouldNotTranformValueException {

    String indent = getIndent(indents, useSpaces, spacesPerTab);
    StringBuilder result = new StringBuilder();

    boolean shouldNest = false;

    if (!list.isEmpty()) {
      Object first = list.get(0);

      if (first instanceof JEFEntity || List.class.isAssignableFrom(first.getClass())
          || Map.class.isAssignableFrom(first.getClass()) || first.getClass().isArray()) {
        shouldNest = true;
      }

      if (shouldNest) {
        result.append("\n" + indent);
      }

      result.append(getValueFromObject(first, indents, useSpaces, spacesPerTab));

      for (int i = 1; i < list.size(); i++) {
        Object entry = list.get(i);
        if (shouldNest) {
          result.append("\n" + indent);
        } else {
          result.append(", ");
        }
        result.append(getValueFromObject(entry, indents, useSpaces, spacesPerTab));
      }

      if (shouldNest) {
        result.append("\n" + getIndent(indents - 1, useSpaces, spacesPerTab));
      }
    }
    return result.toString();
  }

  private static String arrayToString(Object arr, int indents, boolean useSpaces, int spacesPerTab)
      throws IllegalArgumentException, IllegalAccessException, CouldNotTranformValueException {
    List<String> elements = new ArrayList<>();
    boolean shouldNest = false;
    for (int i = 0; i < Array.getLength(arr); i++) {
      Object obj = Array.get(arr, i);
      if (obj.getClass().isArray()) {
        elements.add("[" + arrayToString(obj, indents + 1, useSpaces, spacesPerTab) + "]");
        shouldNest = true;
      } else {
        elements.add(getValueFromObject(obj, indents, useSpaces, spacesPerTab));
        if (obj instanceof JEFEntity || List.class.isAssignableFrom(obj.getClass())
            || Map.class.isAssignableFrom(obj.getClass())) {
          shouldNest = true;
        }
      }
    }

    StringBuilder result = new StringBuilder();
    if (!elements.isEmpty()) {
      if (shouldNest) {
        result.append("\n" + getIndent(indents, useSpaces, spacesPerTab));
      }

      result.append(elements.get(0));

      for (int i = 1; i < elements.size(); i++) {
        String element = elements.get(i);
        if (shouldNest) {
          result.append("\n");
          result.append(getIndent(indents, useSpaces, spacesPerTab));
        } else {
          result.append(", ");
        }
        result.append(element);
      }
    }

    if (shouldNest) {
      result.append("\n");
      result.append(getIndent(indents - 1, useSpaces, spacesPerTab));
    }

    return result.toString();
  }

  protected static String getIndent(int indents, boolean useSpaces, int spacesPerTab) {
    String tab = getTab(useSpaces, spacesPerTab);
    String indent = "";
    for (int i = 0; i < indents; i++) {
      indent += tab;
    }
    return indent;
  }

  protected static String getTab(boolean useSpaces, int spacesPerTab) {
    String tab;
    if (useSpaces) {
      tab = "";
      for (int i = 0; i < spacesPerTab; i++) {
        tab += " ";
      }
    } else {
      tab = "\t";
    }

    return tab;
  }

  protected static String getValueFromField(Object obj, Field field, int indents, boolean useSpaces,
      int spacesPerTab)
          throws IllegalArgumentException, IllegalAccessException, CouldNotTranformValueException {
    field.setAccessible(true);
    return getValueFromObject(field.get(obj), indents, useSpaces, spacesPerTab);
  }

  @SuppressWarnings("unchecked")
  protected static String getValueFromObject(Object obj, int indents, boolean useSpaces,
      int spacesPerTab)
          throws IllegalArgumentException, IllegalAccessException, CouldNotTranformValueException {
    String tab = getTab(useSpaces, spacesPerTab);
    String indent = getIndent(indents, useSpaces, spacesPerTab);
    String result = "";

    if (obj instanceof JEFEntityMap) {
      result += "{\n" + ((JEFEntityMap) obj).toJEFEntityFormat(indents + 1, useSpaces, spacesPerTab)
          + indent + "}";
    } else if (obj instanceof JEFEntityTuple) {
      result += ((JEFEntityTuple) obj).toJEFEntityFormat(indents + 1, useSpaces, spacesPerTab)
          + indent;

    } else if (classIsPrimitive(obj.getClass())) {
      result += obj.toString();

    } else if (obj instanceof String) {
      result += "'" + obj.toString() + "'";

    } else if (obj.getClass().isEnum()) {
      result = "$" + obj.toString();

    } else if (obj instanceof Boolean || obj.getClass().equals(boolean.class)) {
      result = "$" + obj.toString();

    } else if (obj instanceof List) {
      result = "<" + listToString((List<Object>) obj, indents + 1, useSpaces, spacesPerTab) + ">";
    } else if (obj instanceof Map) {
      result += "{\n";
      for (Entry<String, Object> entry : ((Map<String, Object>) obj).entrySet()) {

        String typeName = "";
        if (JEFEntityMap.class.isAssignableFrom(entry.getValue().getClass())) {
          typeName = " " + toTypeName(entry.getValue().getClass());
        }
        result += indent + (indents < 0 ? "" : tab) + entry.getKey() + " =" + typeName + " ";

        String map = getValueFromObject(entry.getValue(), indents + 1, useSpaces, spacesPerTab);
        // System.out.println(map);
        while (map.startsWith(tab)) {
          map = map.substring(1);
        }
        result += map + "\n";
      }
      result += indent + "}";
    } else if (BuiltInResurrector.containsTranformForObject(obj.getClass())) {
      result += BuiltInResurrector.transformObject(obj).toJEFEntityFormat(indents + 1, useSpaces,
          spacesPerTab);
    } else if (obj.getClass().isArray()) {
      result += "[" + arrayToString(obj, indents + 1, useSpaces, spacesPerTab) + "]";
    } else {
      result += obj.toString();
    }
    return result;
  }

  private static boolean classIsPrimitive(Class<?> cls) {
    return Number.class.isAssignableFrom(cls) || cls.isPrimitive();
  }

  public static final String toJEFEntityHeader(Class<?> cls) {
    String name;
    if (cls.isAnnotationPresent(JEFClass.class)) {
      name = cls.getAnnotation(JEFClass.class).name();
    } else {
      name = cls.getSimpleName();
    }

    if (cls.isEnum()) {
      return "enum : " + name + " of [" + asEnum(cls) + "]";
    } else {
      return "type : " + name + " " + asType(cls);
    }
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

  protected static String asType(Class<?> cls) {
    String result;
    if (JEFEntityMap.class.isAssignableFrom(cls)) {
      result = "{";
      boolean first = true;
      for (Field field : cls.getDeclaredFields()) {
        String typeName = "", valName = "";

        if (!(field.isAnnotationPresent(JEFField.class)
            && field.getAnnotation(JEFField.class).ignore())) {
          typeName = JEFEntity.toTypeName(field);

          if (field.isAnnotationPresent(JEFField.class)) {
            if (field.getAnnotation(JEFField.class).equals("")) {
              valName = field.getName();
            } else {
              valName = field.getAnnotation(JEFField.class).key();
            }
          } else {
            valName = field.getName();
          }

          result += (first ? "" : ", ") + typeName + " " + valName;
          first = false;
        } else {
          continue;
        }
      }
      result += "}";
    } else if (JEFEntityTuple.class.isAssignableFrom(cls)) {
      result = "(";
      Map<Integer, String> ordering = new TreeMap<Integer, String>();
      for (Field field : cls.getDeclaredFields()) {
        if (field.isAnnotationPresent(JEFTuple.class)) {
          ordering.put(field.getAnnotation(JEFTuple.class).value(), JEFEntity.toTypeName(field));
        } else {
          continue;
        }
      }
      boolean first = true;
      for (Entry<Integer, String> entry : ordering.entrySet()) {
        String typeName = entry.getValue();
        result += (first ? "" : ", ") + typeName;
        first = false;

      }
      result += ")";
    } else {
      result = "???";
    }

    return result;
  }

  protected static String toTypeName(Field field) {
    Class<?> cls = field.getType();

    if (List.class.isAssignableFrom(cls)) {
      return toTypeName(
          (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])
          + "<>";
    } else if (Map.class.isAssignableFrom(cls)) {
      return toTypeName(
          (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1])
          + "{}";
    } else {
      return toTypeName(cls);
    }
  }

  protected static String toTypeName(Class<?> cls) {
    if (cls.isAnnotationPresent(JEFClass.class)) {
      return cls.getAnnotation(JEFClass.class).name();
    } else if (cls.equals(int.class) || cls.equals(Integer.class)) {
      return "Int";
    } else if (cls.equals(double.class) || cls.equals(Double.class)) {
      return "Float";
    } else if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
      return "Bool";
    } else if (classIsPrimitive(cls) || cls.equals(String.class)) {
      String name = cls.getSimpleName();
      return name.substring(0, 1).toUpperCase() + name.substring(1);
    } else if (Map.class.isAssignableFrom(cls)) {
      if (cls.getGenericInterfaces().length == 0) {
        return "{}";
      } else {
        return toTypeName((Class<?>) ((ParameterizedType) cls.getGenericInterfaces()[0])
            .getActualTypeArguments()[1]) + "{}";
      }
    } else if (List.class.isAssignableFrom(cls)) {
      return toTypeName((Class<?>) ((ParameterizedType) cls.getGenericInterfaces()[0])
          .getActualTypeArguments()[0]) + "<>";
    } else if (cls.isArray()) {
      return toTypeName(cls.getComponentType()) + "[]";
    } else {
      return cls.getSimpleName();
    }
  }

}
