package com.jeffreymanzione.jef.tokenizing;

public abstract class IndexedObject implements Indexable {

	private final String text;
	private final StringBuilder lineText;
	private final int lineNumber;
	private final int columnNumber;

	protected IndexedObject(Indexable indexable) {
		this.text = indexable.getText();
		this.lineText = indexable.getLineTextBuilder();
		this.lineNumber = indexable.getLineNumber();
		this.columnNumber = indexable.getColumnNumber();
	}

	protected IndexedObject(String text, StringBuilder lineText, int lineNumber, int columnNumber) {
		super();
		this.text = text;
		this.lineText = lineText;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String getLineText() {
		return lineText.toString();
	}

	@Override
	public int getColumnNumber() {
		return columnNumber;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public StringBuilder getLineTextBuilder() {
		return lineText;
	}
}
