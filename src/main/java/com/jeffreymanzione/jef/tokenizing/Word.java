package com.jeffreymanzione.jef.tokenizing;

public final class Word {
	private final String word;
	private final int line;
	
	public Word(String word, int line, int columnWordEnd) {
		super();
		this.word = word;
		this.line = line;
		this.column = columnWordEnd - word.length();
	}
	
	private final int column;
	public String getText() {
		return word;
	}
	public int getLine() {
		return line;
	}
	public int getColumn() {
		return column;
	}
	
	
}
