package com.jeffreymanzione.jef.resurrection;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jeffreymanzione.jef.resurrection.annotations.JEFField;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotTranformValueException;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotUpdateEntityException;

public class JEFEntityMap extends JEFEntity<String> {

  private Map<String, Object>   mappings;
  private Map<String, Class<?>> classes;

  {
    mappings = new HashMap<>();
    classes = new HashMap<>();
  }

  @Override
  public boolean set(String fieldName, Object val) throws CouldNotUpdateEntityException {
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
          throw new CouldNotUpdateEntityException(
              "Could not set field's value in map as it was already set to a different type "
                  + " nor in the map: fieldName='" + fieldName + "' and val=" + val + " expected="
                  + mappings.get(fieldName) + ".");
        }
      } else {
        System.err
            .println("Warning: Tried to add (key="
                + fieldName
                + ",val="
                + val
                + ") to a "
                + this.getClass().getName()
                + ", but it does not have a field that matches it. Going to add it to the auxilliary map, "
                + "but check to see if this is a mistake.");

        classes.put(fieldName, val.getClass());
        mappings.put(fieldName, val);
      }
    } catch (SecurityException e) {
      throw new CouldNotUpdateEntityException("Unexpected security exception: fieldName='"
          + fieldName + "' and val=" + val + ".\n See Exception in the stack trace.");
    }
    return false;
  }

  @Override
  public Object get(String fieldName) throws CouldNotUpdateEntityException {
    for (Field field : this.getClass().getFields()) {
      if (field.isAnnotationPresent(JEFField.class)) {
        JEFField annot = field.getAnnotation(JEFField.class);
        if (annot.key().equals(fieldName)) {
          field.setAccessible(true);
          try {
            return field.get(this);
          } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CouldNotUpdateEntityException("Field " + field + " in " + this
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
            throw new CouldNotUpdateEntityException("Field " + field + " in " + this
                + " could not be read. Check the security settings.");
          }
        }
      }
    } catch (NoSuchFieldException e) {
      if (mappings.containsKey(fieldName)) {
        return mappings.get(fieldName);
      } else {
        throw new CouldNotUpdateEntityException(
            "Could not set field's value in map as it was not a field"
                + " nor in the map: fieldName='" + fieldName + ".");
      }
    } catch (SecurityException e) {
      throw new CouldNotUpdateEntityException("Unexpected security exception: fieldName='"
          + fieldName + ".");
    }
    return false;
  }

  @Override
  public Class<?> getType(String fieldName) throws CouldNotUpdateEntityException {
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
        throw new CouldNotUpdateEntityException(
            "Could not set field's value in map as it was not a field"
                + " nor in the map: fieldName='" + fieldName + ".");
      }
    } catch (SecurityException e) {
      throw new CouldNotUpdateEntityException("Unexpected security exception: fieldName='"
          + fieldName + ".");
      // e.printStackTrace();
    }
    return null;
  }

  @Override
  public String toJEFEntityFormat(int indents, boolean useSpaces, int spacesPerTab)
      throws IllegalArgumentException, IllegalAccessException, CouldNotTranformValueException {
    String result = "";
    // String tab = getTab(useSpaces, spacesPerTab);
    String indent = getIndent(indents, useSpaces, spacesPerTab);

    for (Field field : this.getClass().getDeclaredFields()) {
      JEFField jf = field.getAnnotation(JEFField.class);
      String name;
      if (jf != null && !jf.ignore()) {
        if (jf.key().equals("")) {
          name = field.getName();
        } else {
          name = jf.key();
        }
      } else if (jf == null) {
        name = field.getName();
      } else {
        continue;
      }

      String typeName = "", preamble = "";
      if (JEFEntityMap.class.isAssignableFrom(field.getType())) {
        typeName = toTypeName(field);
        preamble = " = " + typeName + " ";
      } else if (List.class.isAssignableFrom(field.getType())) {
        String typeNameSimple = toTypeName(field);
        typeName = "<" + typeNameSimple.substring(0, typeNameSimple.length() - 2) + ">";
        preamble = typeName + " = ";
      } else {
        preamble = " = ";
      }
      result += indent + name + preamble + writeBody(this, field, indents, useSpaces, spacesPerTab)
          + "\n";
    }
    for (Entry<String, Object> entry : this.mappings.entrySet()) {
      String name = entry.getKey();

      String typeName = "";
      if (JEFEntityMap.class.isAssignableFrom(classes.get(entry.getKey()))) {
        typeName = " " + toTypeName(classes.get(entry.getKey()));
      }
      result += indent + name + " =" + typeName + " "
          + getValueFromObject(entry.getValue(), indents, useSpaces, spacesPerTab) + "\n";
    }
    // result += "\n";

    return result;
  }

}
