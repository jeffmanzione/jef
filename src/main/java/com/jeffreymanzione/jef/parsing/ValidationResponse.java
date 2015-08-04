package com.jeffreymanzione.jef.parsing;

import java.util.ArrayList;
import java.util.List;

import com.jeffreymanzione.jef.parsing.exceptions.IndexableException;

public class ValidationResponse {
	private List<IndexableException> exceptions = new ArrayList<>();

	public boolean hasErrors() {
		return exceptions.size() > 0;
	}

	public List<IndexableException> getExceptions() {
		return exceptions;
	}

	void addException(IndexableException exception) {
		if (!exceptions.contains(exception)) {
			exceptions.add(exception);
		}
	}

	boolean addResponse(ValidationResponse response) {
		for (IndexableException e : response.exceptions) {
			addException(e);
		}
		return hasErrors();
	}
}