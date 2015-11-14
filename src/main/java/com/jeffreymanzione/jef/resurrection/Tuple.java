package com.jeffreymanzione.jef.resurrection;

public interface Tuple {

  public int size ();

  public boolean set (Integer index, Object val)
      throws IndexOutOfBoundsException;

  public Object get (Integer index) throws IndexOutOfBoundsException;

  public Class<?> getType (Integer index) throws IndexOutOfBoundsException;

  public String toJEFEntityFormat (int indents, boolean useSpaces,
      int spacesPerTab);

}
