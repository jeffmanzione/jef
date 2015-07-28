package com.jeffreymanzione.jef.parsing.value;

import com.jeffreymanzione.jef.tokenizing.Token;

public abstract class Value<T> {

	private ValueType type;
	private T value;
	private Token token;

	protected Value(ValueType type, Token token) {
		this.type = type;
		this.token = token;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public int getLine() {
		return token.getLine();
	}
	
	public int getColumn() {
		return token.getColumn();
	}
	
	protected void set(T value) {
		this.value = value;
	}

	public ValueType getType() {
		return type;
	}

	public T getValue() {
		return value;
	}

	private String entityID;

	public String getEntityID() {
		return entityID;
	}

	public void setEntityID(String entityID) {
		this.entityID = entityID;
	}

	public boolean hasEntityID() {
		return entityID != null;
	}

	public String toStringType() {
		return type.toString();
	}

	public String toString() {
		return value.toString();
	}
}
