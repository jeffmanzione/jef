package com.jeffreymanzione.jef.tokenizing;

public interface Indexable {
  public int getLineNumber();

  public int getColumnNumber();

  public String getLineText();

  public StringBuilder getLineTextBuilder();

  public String getText();

  Indexable EOF = new Indexable() {

                  @Override
                  public int getLineNumber() {
                    return -1;
                  }

                  @Override
                  public String getLineText() {
                    return "EOF";
                  }

                  @Override
                  public int getColumnNumber() {
                    return -1;
                  }

                  @Override
                  public String getText() {
                    return "EOF";
                  }

                  @Override
                  public StringBuilder getLineTextBuilder() {
                    return new StringBuilder();
                  }
                };

}
