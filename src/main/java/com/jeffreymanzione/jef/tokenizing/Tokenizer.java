package com.jeffreymanzione.jef.tokenizing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Tokenizer {

	private Tokenizer() {
		throw new RuntimeException("You have no business using this constructor!");
	}

	public static Queue<Token> tokenize(InputStream stream, boolean verbose) throws IOException, TokenizeException {
		return Tokenizer.tokenizeWords(Tokenizer.split(Tokenizer.fileToString(stream)), verbose);
	}

	private static Queue<Token> tokenizeWords(List<Word> words, boolean verbose) throws TokenizeException {
		LinkedList<Token> tokens = verbose ? new LinkedList<Token>() {
			private static final long serialVersionUID = 1L;

			public Token remove() {

				Token token = super.remove();
				System.out.println("Removing token: " + token);

				return token;
			}
		} : new LinkedList<>();

		for (int index = 0; index < words.size(); index++) {
			Word word = words.get(index);
			Token token = null;

			if (TokenType.isKeyword(word.getText())) {
				token = new Token(word, TokenType.getToken(word.getText()));
			} else {
				if (word.getText().startsWith("'") && word.getText().endsWith("'")) {
					token = new Token(word, TokenType.QUOTE);
				} else if (Character.isDigit(word.getText().toCharArray()[0])) {
					if (word.getText().contains(".")) {
						token = new Token(word, TokenType.FLOAT);
					} else {
						token = new Token(word, TokenType.LONG);
					}
				} else if (word.getText().equals(word.getText().toUpperCase())) {
					token = new Token(word, TokenType.DEF);
				} else if (tokens.size() > 0 && tokens.get(tokens.size() - 1).getType() == TokenType.QUOTE) {
					tokens.remove(tokens.size() - 1);
					token = new Token(word, TokenType.STRING);
					if (index < words.size() + 1) {
						index++;
					} else {
						throw new TokenizeException("EXPECTED TOKEN '.");
					}
				} else {
					token = new Token(word, TokenType.VAR);
				}
			}

			if (token != null) {

				if (tokens.size() > 0 && closers.contains(token.getType())
						&& tokens.get(tokens.size() - 1).getType() == TokenType.COMMA) {
					tokens.remove(tokens.get(tokens.size() - 1));
				}

				tokens.add(token);
			}

		}

		return tokens;
	}

	public static String fileToString(InputStream file) throws IOException {
		String str = "";
		try (Scanner scan = new Scanner(file)) {
			while (scan.hasNextLine()) {
				str += scan.nextLine() + "\n";
			}
		}
		return str;
	}

	private static List<Word> split(String line) {
		List<Word> words = new ArrayList<>();

		String buffer = "";

		boolean sQuote = false;
		boolean dQuote = false;
		boolean isComment = false;

		int lineNumber = 1;
		int column = 0;

		for (char c : line.toCharArray()) {
			if (c == '\t') {
				column += 4;
			} else {
				column++;
			}
			
			if (!isComment) {
				if (c == '\'') {
					sQuote = !sQuote;
				}

				if (c == '\"') {
					dQuote = !dQuote;
				}

				if (splitters.contains(c + "")) {
					if (!buffer.equals("")) {
						// if (buffer.equals("*/")) {
						// isComment = false;
						// } else {
						words.add(new Word(buffer, lineNumber, column));
						// }
					}

					if (c == '\n') {
						lineNumber++;
						column = 0;
						if (words.size() != 0 && !preline.contains(words.get(words.size() - 1).getText())) {
							words.add(new Word(",", lineNumber, column));
						}
					} else {
						words.add(new Word(c + "", lineNumber, column));
					}
					buffer = "";
				} else if (Character.isWhitespace(c) && !sQuote && !dQuote) {
					if (buffer.equals("/*")) {
						isComment = true;
					} else if (!buffer.equals("") && !isComment) {
						words.add(new Word(buffer, lineNumber, column));
					}
					buffer = "";
				} else {
					buffer += c;
				}

			} else {
				buffer += c;
				if (buffer.endsWith("*/")) {
					isComment = false;
					buffer = "";
				}

			}

		}

		if (words.get(words.size() - 1).getText().equals(",")) {
			words.remove(words.size() - 1);
		}

		return words;
	}

	private static List<String> splitters = Arrays.asList("\'", "\"", "[", "]", "{", "}", "(", ")", "=", "<", ">",
			"\n", ",");

	private static List<String> preline = Arrays.asList("[", "{", "(", "<", ",", "=", ":");

	private static List<TokenType> closers = Arrays.asList(TokenType.RBRCE, TokenType.RBRAC, TokenType.RPAREN,
			TokenType.GTHAN);
}
