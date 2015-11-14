package com.jeffreymanzione.jef.tokenizing;

public final class Word extends IndexedObject {
  private final String word;

  public Word (String word, StringBuilder lineText, int lineNum,
      int columnWordEnd) {
    super(word, lineText, lineNum, columnWordEnd - word.length());
    this.word = word;
  }

  public String getWord () {
    return word;
  }

}
