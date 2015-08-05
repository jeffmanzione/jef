package com.jeffreymanzione.jef.resurrection;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.jeffreymanzione.jef.parsing.ColorDefinition;
import com.jeffreymanzione.jef.parsing.Definition;
import com.jeffreymanzione.jef.parsing.value.EnumValue;
import com.jeffreymanzione.jef.parsing.value.ListValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.TupleValue;
import com.jeffreymanzione.jef.parsing.value.Value;
import com.jeffreymanzione.jef.parsing.value.ValueType;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotTranformValueException;
import com.jeffreymanzione.jef.resurrection.exceptions.CouldNotUpdateEntityException;

public final class BuiltInResurrector {

  private static Map<String, Transformer<?>>   transformers;
  private static Map<Class<?>, Transformer<?>> classesToTransformers;

  static {
    classesToTransformers = new HashMap<Class<?>, BuiltInResurrector.Transformer<?>>();
    transformers = new HashMap<String, BuiltInResurrector.Transformer<?>>();
    addTransformer(new Transformer<Color>(ValueType.TUPLE, ColorDefinition.instance(), Color.class) {
      @Override
      public Color protectedTransformResurrection(Value<?> input) {
        TupleValue tupleValue = (TupleValue) input;
        Color color = new Color((Integer) tupleValue.get(1).getValue(), (Integer) tupleValue.get(2)
            .getValue(), (Integer) tupleValue.get(3).getValue(), (Integer) tupleValue.get(0)
            .getValue());
        return color;
      }

      @Override
      protected JEFEntityTuple protectedTransformToValue(Color input) {
        JEFEntityTuple entity = new JEFEntityTuple() {
          @Override
          public int size() {
            return 4;
          }
        };

        try {
          entity.set(0, input.getAlpha());
          entity.set(1, input.getRed());
          entity.set(2, input.getGreen());
          entity.set(3, input.getBlue());
        } catch (CouldNotUpdateEntityException e) {
          e.printStackTrace();
        }

        return entity;
      }
    });
  }

  private BuiltInResurrector() {
    throw new RuntimeException();
  }

  private static void putClassToTransformer(Transformer<?> transformer) {
    classesToTransformers.put(transformer.externalClass, transformer);
  }

  public static void addTransformer(BuiltInResurrector.Transformer<?> transformer) {
    transformers.put(transformer.id, transformer);
    putClassToTransformer(transformer);
  }

  public static Object transformValue(Value<?> value) throws CouldNotTranformValueException {
    if (value.hasEntityID()) {
      if (transformers.containsKey(value.getEntityID())) {
        return transformers.get(value.getEntityID()).transformToClass(value);
      } else {
        throw new CouldNotTranformValueException("There was no transformer with this entity ID.");
      }
    } else {
      throw new CouldNotTranformValueException("Value does not have this entity ID.");
    }
  }

  @SuppressWarnings("unchecked")
  public static JEFEntity<?> transformObject(Object obj) throws CouldNotTranformValueException {
    if (classesToTransformers.containsKey(obj.getClass())) {
      return ((Transformer<Object>) classesToTransformers.get(obj.getClass()))
          .transformToValue(obj);
    } else {
      throw new CouldNotTranformValueException(
          "No transformer could be found for specified object=" + obj + " of type="
              + obj.getClass().getSimpleName());
    }
  }

  public static abstract class Transformer<T> {
    public final String     id;
    final ValueType         type;
    public final Definition internalDef;
    public final Class<T>   externalClass;

    Transformer(ValueType type, Definition internalDef, Class<T> cls) {
      this.id = cls.getSimpleName();
      this.type = type;
      this.internalDef = internalDef;
      this.externalClass = cls;
    }

    protected abstract T protectedTransformResurrection(Value<?> input)
        throws CouldNotTranformValueException;

    public final T transformToClass(Value<?> input) throws CouldNotTranformValueException {
      if (correctType(input, type) && input.hasEntityID() && input.getEntityID().equals(id)) {
        return protectedTransformResurrection(input);
      } else {
        throw new CouldNotTranformValueException(
            "Input did not have the proper type during transformation of value to object. Value ="
                + input + " of type=" + input.getType() + " but expected type=" + type);
      }
    }

    protected abstract JEFEntity<?> protectedTransformToValue(T input);

    public final JEFEntity<?> transformToValue(T input) throws CouldNotTranformValueException {
      if (input.getClass().equals(externalClass)) {
        return protectedTransformToValue(input);
      } else {
        throw new CouldNotTranformValueException(
            "No way to transform this class was specified. Type="
                + input.getClass().getSimpleName()
                + ". Did you add a transformer for it using BuiltInResurrector.addTransformer()?");
      }
    }
  }

  private static boolean correctType(Value<?> val, ValueType type) {
    if (val instanceof TupleValue && type == ValueType.TUPLE || val instanceof MapValue
        && type == ValueType.MAP || val instanceof ListValue && type == ValueType.LIST
        || val instanceof EnumValue && type == ValueType.ENUM) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean containsTrasformer(Value<?> value) {
    if (value.hasEntityID()) {
      if (transformers.containsKey(value.getEntityID())) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public static Iterable<Transformer<?>> getTransformers() {
    return transformers.values();
  }

  public static boolean containsTranformForObject(Class<?> cls) {
    return classesToTransformers.containsKey(cls);
  }
}
