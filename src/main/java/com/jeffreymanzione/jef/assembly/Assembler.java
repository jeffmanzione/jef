package com.jeffreymanzione.jef.assembly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Queue;

import com.jeffreymanzione.jef.parsing.Parser;
import com.jeffreymanzione.jef.parsing.exceptions.DoesNotConformToDefintionException;
import com.jeffreymanzione.jef.parsing.exceptions.ParsingException;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.resurrection.Resurrector;
import com.jeffreymanzione.jef.resurrection.exceptions.ClassFillingException;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenizeException;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;

public class Assembler {
	private Parser parser;
	private Tokenizer tokenizer;
	private Resurrector filler;
	private String stringSource;
	private InputStream streamSource;
	private File fileSource;

	public boolean setSource(String source) {
		streamSource = null;
		fileSource = null;
		stringSource = source;
		return true;
	}

	public boolean setSource(InputStream source) {
		streamSource = source;
		fileSource = null;
		stringSource = null;
		return true;
	}

	public boolean setSource(File source) {
		streamSource = null;
		fileSource = source;
		stringSource = null;
		return true;
	}

	public Parser getParser() {
		return parser;
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public Resurrector getFiller() {
		return filler;
	}

	public void setFiller(Resurrector filler) {
		this.filler = filler;
	}

	public Map<String, Object> assemble() throws TokenizeException, IOException, ParsingException,
			DoesNotConformToDefintionException, ClassFillingException {
		if (tokenizer == null) {
			throw new NullPointerException();
		} else if (parser == null) {
			throw new NullPointerException();
		} else if (filler == null) {
			throw new NullPointerException();
		} else {

			Queue<Token> tokens;
			if (stringSource != null) {
				tokens = tokenizer.tokenize(stringSource);
			} else if (streamSource != null) {
				tokens = tokenizer.tokenize(streamSource);
			} else if (fileSource != null) {
				tokens = tokenizer.tokenize(fileSource);
			} else {
				throw new NullPointerException();
			}
			
			Parser parser = new Parser();
			MapValue value = parser.parse(tokens);
			Map<String, Object> map = filler.parseToObject(value);
			return map;

		}
	}

}
