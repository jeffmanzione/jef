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

	private static Queue<Token> tokenizeWords(List<String> words, boolean verbose) throws TokenizeException {
		LinkedList<Token> tokens = verbose ? new LinkedList<Token>() {
			private static final long serialVersionUID = 1L;

			public Token remove() {

				Token token = super.remove();
				System.out.println("Removing token: " + token);

				return token;
			}
		} : new LinkedList<>();

		for (int index = 0; index < words.size(); index++) {
			String word = words.get(index);
			Token token = null;

			if (TokenType.isKeyword(word)) {
				token = new Token(word, TokenType.getToken(word));
			} else {
				if (word.startsWith("'") && word.endsWith("'")) {
					token = new Token(word, TokenType.QUOTE);
				} else if (Character.isDigit(word.toCharArray()[0])) {
					if (word.contains(".")) {
						token = new Token(word, TokenType.FLOAT);
					} else {
						token = new Token(word, TokenType.LONG);
					}
				} else if (word.equals(word.toUpperCase())) {
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

	private static List<String> split(String line) {
		List<String> words = new ArrayList<>();

		String buffer = "";

		boolean sQuote = false;
		boolean dQuote = false;
		boolean isComment = false;

		for (char c : line.toCharArray()) {
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
						words.add(buffer);
						// }
					}

					if (c == '\n') {
						if (words.size() != 0 && !preline.contains(words.get(words.size() - 1))) {
							words.add(",");
						}
					} else {
						words.add(c + "");
					}
					buffer = "";
				} else if (Character.isWhitespace(c) && !sQuote && !dQuote) {
					if (buffer.equals("/*")) {
						isComment = true;
					} else if (!buffer.equals("") && !isComment) {
						words.add(buffer);
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

		if (words.get(words.size() - 1).equals(",")) {
			words.remove(words.size() - 1);
		}

		return words;
	}

	private static List<String> splitters = Arrays.asList("\'", "\"", "[", "]", "{", "}", "(", ")", "=", "<", ">",
			"\n", ",");

	private static List<String> preline = Arrays.asList("[", "{", "(", "<", ",");

	private static List<TokenType> closers = Arrays.asList(TokenType.RBRCE, TokenType.RBRAC, TokenType.RPAREN,
			TokenType.GTHAN);
}
