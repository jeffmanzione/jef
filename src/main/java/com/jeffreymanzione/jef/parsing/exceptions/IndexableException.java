package com.jeffreymanzione.jef.parsing.exceptions;

import com.jeffreymanzione.jef.tokenizing.Indexable;

public class IndexableException extends Exception implements Indexable {

  /**
	 * 
	 */
  private static final long serialVersionUID = -5370945001284100616L;

  private final Indexable   inner;
  private final String      message;
  private final String      fullText;

  public IndexableException(Indexable indexable, String message) {
    super("On line " + indexable.getLineNumber() + " column " + indexable.getColumnNumber() + ": "
        + message);
    this.inner = indexable;
    this.message = message;
    this.fullText = "On line " + indexable.getLineNumber() + " column "
        + indexable.getColumnNumber() + ": " + message;
  }

  @Override
  public int getLineNumber() {
    return inner.getLineNumber();
  }

  @Override
  public String getLineText() {
    return inner.getLineText();
  }

  @Override
  public int getColumnNumber() {
    return inner.getColumnNumber();
  }

  public String getParsingMessage() {
    return message;
  }

  public String getFullText() {
    return fullText;
  }

  public String getLineAnnotation() {
    StringBuilder lineAnnotation = new StringBuilder();
    for (int i = 0; i < inner.getColumnNumber() - 1; i++) {
      lineAnnotation.append(' ');
    }
    for (int i = inner.getColumnNumber() - 1; i < inner.getColumnNumber()
        + inner.getText().length() - 1; i++) {
      lineAnnotation.append('^');
    }
    return lineAnnotation.toString();
  }

  @Override
  public String getText() {
    return toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fullText == null) ? 0 : fullText.hashCode());
    result = prime * result + ((inner == null) ? 0 : inner.hashCode());
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IndexableException other = (IndexableException) obj;
    if (fullText == null) {
      if (other.fullText != null)
        return false;
    } else if (!fullText.equals(other.fullText))
      return false;
    if (inner == null) {
      if (other.inner != null)
        return false;
    } else if (!inner.equals(other.inner))
      return false;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    return true;
  }

  @Override
  public StringBuilder getLineTextBuilder() {
    throw new UnsupportedOperationException();
  }

}
