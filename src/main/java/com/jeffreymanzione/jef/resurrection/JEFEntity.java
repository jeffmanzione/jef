package com.jeffreymanzione.jef.resurrection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.jeffreymanzione.jef.resurrection.annotations.JEFClass;
import com.jeffreymanzione.jef.resurrection.annotations.JEFField;
import com.jeffreymanzione.jef.resurrection.annotations.JEFTuple;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotTranformValueException;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotUpdateEntityException;

public abstract class JEFEntity<KEY> {

  public abstract boolean set(KEY key, Object val) throws CouldNotUpdateEntityException;

  public abstract Object get(KEY key) throws CouldNotUpdateEntityException;

  public abstract Class<?> getType(KEY key) throws CouldNotUpdateEntityException;

  public abstract String toJEFEntityFormat(int indents, boolean useSpaces, int spacesPerTab)
      throws IllegalArgumentException, IllegalAccessException, CouldNotTranformValueException;

  protected boolean setFieldAnnot(Field field, KEY key, Object val)
      throws CouldNotUpdateEntityException {
    if (field.isAnnotationPresent(JEFField.class)) {
      JEFField annot = field.getAnnotation(JEFField.class);
      if (annot.key().equals(key.toString())) {
        field.setAccessible(true);

        if (field.getType().isInstance(val)) {
          try {
            field.set(this, val);
            return true;
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CouldNotUpdateEntityException("Could not set object " + this + " with field "
                + field + " to val " + val
                + ". Either the security settings prevented it or you did something silly.");
          }
        } else {
          throw new CouldNotUpdateEntityException("Could not set object " + this + " with field "
              + field + " to val " + val + ". Value was not instanceof field.");
        }
      }
    }
    return false;
  }

  protected boolean matches(Field field, Object val) {
    // System.out.println(field.getType().getSimpleName() + " " + val.getClass().getSimpleName());
    if (field.getType().isInstance(val)) {
      return true;
    } else {
      if ((field.getType().equals(int.class) || field.getType().equals(long.class))
          && val instanceof Integer) {
        return true;
      } else if ((field.getType().equals(float.class) || field.getType().equals(double.class))
          && val instanceof Double) {
        return true;
      } else if ((field.getType().equals(boolean.class) || field.getType().equals(Boolean.class))
          && val instanceof Boolean) {
        return true;
      }
    }
    return false;
  }

  protected boolean setField(Field field, KEY key, Object val) throws CouldNotUpdateEntityException {
    if (field.getName().equals(key.toString())) {
      field.setAccessible(true);

      // System.out.println(field.getType() + " " + val.getClass().getName());
      if (matches(field, val)) {
        // System.out.println(field.getName() + " " + fieldName);

        setSafe(field, val);
        return true;

      } else {
        throw new CouldNotUpdateEntityException("Could not match types between field '"
            + field.getName() + "' and val " + val + ".");
      }
    }
    return false;
  }

  protected void setSafe(Field field, Object val) throws CouldNotUpdateEntityException {
    try {
      if (field.getType().equals(int.class)) {
        field.set(this, val);
      } else if (field.getType().equals(float.class)) {
        field.set(this, ((Double) val).floatValue());
      } else {
        field.set(this, val);
      }
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new CouldNotUpdateEntityException("Could not set object " + this + " with field "
          + field + " to val " + val
          + ". Either the security settings prevented it or you did something silly.");
    }
  }

  @SuppressWarnings("unchecked")
  protected static String writeBody(Object obj, Field field, int indents, boolean useSpaces,
      int spacesPerTab) throws IllegalArgumentException, IllegalAccessException,
      CouldNotTranformValueException {
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
    if (JEFEntityMap.class.isAssignableFrom(field.getType())) {
      result += "{\n"
          + ((JEFEntityMap) field.get(obj)).toJEFEntityFormat(indents + 1, useSpaces, spacesPerTab)
          + indent + "}";
    } else if (JEFEntityTuple.class.isAssignableFrom(field.getType())) {
      result += ((JEFEntityTuple) field.get(obj)).toJEFEntityFormat(indents + 1, useSpaces,
          spacesPerTab) + indent;
    } else if (fieldIsPrimitive(field)) {
      if (field.getType().equals(boolean.class)) {
        result += "$" + field.get(obj).toString();
      } else {
        result += field.get(obj).toString();
      }

    } else if (field.getType().equals(String.class)) {
      result += "'" + field.get(obj).toString() + "'";

    } else if (field.getType().isEnum()) {
      result += "$" + field.get(obj).toString();

    } else if (List.class.isAssignableFrom(field.getType())) {
      result += "<\n";
      for (Object entry : ((List<Object>) field.get(obj))) {
        result += indent + tab + getValueFromObject(entry, indents + 1, useSpaces, spacesPerTab)
            + "\n";
      }
      result += indent + ">";
    } else if (Map.class.isAssignableFrom(field.getType())) {
      result += indent + "{\n"
          + getValueFromObject(field.get(obj), indents + 1, useSpaces, spacesPerTab) + indent
          + "}\n";
    } else if (BuiltInResurrector.containsTranformForObject(field.get(obj).getClass())) {
      result += BuiltInResurrector.transformObject(field.get(obj)).toJEFEntityFormat(indents + 1,
          useSpaces, spacesPerTab);
    } else {
      result += field.get(obj).toString();
    }
    // result += "\n";
    return result;
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

  @SuppressWarnings("unchecked")
  protected static String getValueFromObject(Object obj, int indents, boolean useSpaces,
      int spacesPerTab) throws IllegalArgumentException, IllegalAccessException,
      CouldNotTranformValueException {
    String tab = getTab(useSpaces, spacesPerTab);
    String indent = getIndent(indents, useSpaces, spacesPerTab);
    String result = "";

    if (obj instanceof JEFEntityMap) {
      result += "{\n"
          + ((JEFEntityMap) obj).toJEFEntityFormat(indents + 1, useSpaces, spacesPerTab) + indent
          + "}";
    } else if (obj instanceof JEFEntityTuple) {
      result += ((JEFEntityTuple) obj).toJEFEntityFormat(indents + 1, useSpaces, spacesPerTab)
          + indent;

    } else if (classIsPrimitive(obj.getClass())) {
      result += obj.toString();

    } else if (obj instanceof String) {
      result = "'" + obj.toString() + "'";

    } else if (obj.getClass().isEnum()) {
      result = "$" + obj.toString();

    } else if (obj instanceof Boolean || obj.getClass().equals(boolean.class)) {
      result += "$" + obj.toString();

    } else if (obj instanceof List) {
      result = "<\n";
      for (Object entry : ((List<Object>) obj)) {
        result += indent + getValueFromObject(entry, indents + 1, useSpaces, spacesPerTab) + "\n";
      }
      result += indent + ">";
    } else if (obj instanceof Map) {
      result += /* (indents < 0 ? "" : tab) + */"{\n";
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
    } else {
      result += obj.toString();
    }
    // result += "\n";

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
      name = cls.getSimpleName();
    }

    if (cls.isEnum()) {
      return "enum : " + name + " of [" + asEnum(cls) + "]";
    } else {

      return "type : " + name + " " + toHeader(cls);
    }
  }

  protected static String toHeader(Class<?> cls) {
    String result;
    if (JEFEntityMap.class.isAssignableFrom(cls)) {
      result = "{";
      boolean first = true;
      for (Field field : cls.getDeclaredFields()) {
        String typeName = "", valName = "";

        if (!(field.isAnnotationPresent(JEFField.class) && field.getAnnotation(JEFField.class)
            .ignore())) {
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
      // System.out.println(field);
      // System.out.println(((ParameterizedType)
      // field.getGenericType()).getActualTypeArguments()[0]);
      return toTypeName((Class<?>) ((ParameterizedType) field.getGenericType())
          .getActualTypeArguments()[0]) + "<>";
    } else if (Map.class.isAssignableFrom(cls)) {
      // System.out.println(field);
      // System.out.println(((ParameterizedType)
      // field.getGenericType()).getActualTypeArguments()[1]);
      return toTypeName((Class<?>) ((ParameterizedType) field.getGenericType())
          .getActualTypeArguments()[1]) + "{}";
    } else {
      return toTypeName(cls);
    }
  }

  protected static String toTypeName(Class<?> cls) {
    if (cls.isAnnotationPresent(JEFClass.class)) {
      return cls.getAnnotation(JEFClass.class).name();
    } else {
      if (cls.equals(double.class) || cls.equals(Double.class)) {
        return "Float";
      } else if (cls.equals(boolean.class) || cls.equals(Boolean.class)) {
        return "Bool";
      } else if (classIsPrimitive(cls) || cls.equals(String.class)) {
        String name = cls.getSimpleName();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
      } else {
        return cls.getSimpleName();
      }
    }
  }

  @Deprecated
  public static String toJEFEnumHeader(Class<?> cls) {
    String name;
    if (cls.isAnnotationPresent(JEFClass.class)) {
      name = cls.getAnnotation(JEFClass.class).name();
    } else {
      name = cls.getSimpleName();
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
