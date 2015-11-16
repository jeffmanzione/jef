package com.jeffreymanzione.jef.resurrection.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JEFField {
  String key() default "";

  Base base() default Base.NOT_AN_INTEGER;

  boolean ignore() default false;

  public static enum Base {
    NOT_AN_INTEGER, BIN, BINARY, BASE2, OCT, OCTAL, BASE8, DEC, DECIMAL, BASE10, HEX, HEXIDECIMAL, BASE16, HEXATRIGESIMAL, ALPHANUMERIC, BASE36;

    public String convertToString (Number num) {
      switch (this) {
        case BIN:
        case BINARY:
        case BASE2:
          return "!" + Long.toBinaryString(num.longValue());
        case OCT:
        case OCTAL:
        case BASE8:
          return "0" + Long.toOctalString(num.longValue());
        case DEC:
        case DECIMAL:
        case BASE10:
          return Long.toString(num.longValue());
        case HEX:
        case HEXIDECIMAL:
        case BASE16:
          return "0x" + Long.toHexString(num.longValue());
        case HEXATRIGESIMAL:
        case ALPHANUMERIC:
        case BASE36:
          return "#" + Long.toString(num.longValue(), 36);
        default:
          return "NOT A NUMBER!";
      }
    }
  }
}
