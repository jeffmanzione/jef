package com.jeffreymanzione.jef.tokenizing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import com.jeffreymanzione.jef.parsing.Parser;

/**
 * @author Jeffrey J. Manzione
 * @date 2015/06/09
 * 
 *       Takes an input string or file in JEF and outputs annotated tokens which can be used by the {@link Parser}
 *       class.
 */
public class Tokenizer {

	private boolean isVerbose;

	/**
	 * Sets whether to tokenizer should log details while tokenizing input.
	 * 
	 * @param isVerbose
	 *            true if the tokenizer is to be 'verbose', false, otherwise.
	 */
	public void setVerbose(boolean isVerbose) {
		this.isVerbose = isVerbose;
	}

	/**
	 * Outputs whether the tokenizer logs details while tokenizing input.
	 * 
	 * @return true if the tokenizer is 'verbose', false, otherwise.
	 */
	public boolean isVerbose() {
		return isVerbose;
	}

	/**
	 * Tokenizes a string in JEF into tokens which can be used by {@link Parser#parse(Queue)}.
	 * 
	 * @param string
	 *            String to be tokenized
	 * @return A {@link Queue}<{@link Token}> created by tokenizing the input string.
	 * @throws TokenizeException
	 */
	public Queue<Token> tokenize(String string) throws TokenizeException {
		return this.tokenizeWords(Tokenizer.split(string));
	}

	/**
	 * Tokenizes a stream in JEF into tokens which can be used by {@link Parser#parse(Queue)}.
	 * 
	 * @param stream
	 *            An {@link InputStream} to be tokenized
	 * @return A {@link Queue}<{@link Token}> created by tokenizing the input string.
	 * @throws TokenizeException
	 */
	public Queue<Token> tokenize(InputStream stream) throws IOException, TokenizeException {
		return this.tokenize(Tokenizer.fileToString(stream));
	}

	/**
	 * Tokenizes the contents of a file in JEF into tokens which can be used by {@link Parser#parse(Queue)}.
	 * 
	 * @param string
	 *            A {@link File} with content to be tokenized
	 * @return A {@link Queue}<{@link Token}> created by tokenizing the input string.
	 * @throws TokenizeException
	 */
	public Queue<Token> tokenize(File file) throws IOException, TokenizeException {
		return this.tokenize(new FileInputStream(file));
	}

	private Queue<Token> tokenizeWords(List<Word> words) throws TokenizeException {
		LinkedList<Token> tokens = isVerbose ? new LinkedList<Token>() {
			private static final long serialVersionUID = 1L;

			public Token remove() {

				Token token = super.remove();
				System.out.println("Removing token: " + token);

				return token;
			}
		} : new LinkedList<Token>();

		for (int index = 0; index < words.size(); index++) {
			Word word = words.get(index);
			Token token = null;

			if (TokenType.isKeyword(word.getText())) {
				token = new Token(word, TokenType.getToken(word.getText()));
			} else {
				if (word.getText().startsWith("'") && word.getText().endsWith("'")) {
					token = new Token(word, TokenType.QUOTE);
				} else if (word.getText().startsWith("-") || word.getText().startsWith(".")
						|| Character.isDigit(word.getText().toCharArray()[0])) {
					if (word.getText().contains(".")) {
						token = new Token(word, TokenType.FLOAT);
					} else {
						token = new Token(word, TokenType.INT);
					}
				} else if (tokens.get(tokens.size() - 1).getType() != TokenType.QUOTE
						&& Character.isUpperCase(word.getText().charAt(0))) {
					token = new Token(word, TokenType.DEF);
				} else if (tokens.size() > 0 && tokens.get(tokens.size() - 1).getType() == TokenType.DOLLAR) {
					tokens.remove(tokens.size() - 1);
					token = new Token(word, TokenType.ENUMVAL);
				} else if (tokens.size() > 0 && tokens.get(tokens.size() - 1).getType() == TokenType.QUOTE) {
					tokens.remove(tokens.size() - 1);
					token = new Token(word, TokenType.STRING);
					if (index < words.size() + 1) {
						index++;
					} else {
						throw new TokenizeException("Expected token: '. Reached end of file.");
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

	private static String fileToString(InputStream file) throws IOException {
		StringBuilder strBuilder = new StringBuilder();
		try (Scanner scan = new Scanner(file)) {
			while (scan.hasNextLine()) {
				strBuilder.append(scan.nextLine());
				strBuilder.append("\n");
			}
		}
		return strBuilder.toString();
	}

	// private static List<Word> split(String text) {
	// List<Word> words = new ArrayList<>();
	//
	// StringBuilder buffer = new StringBuilder();
	//
	// boolean sQuote = false;
	// boolean dQuote = false;
	// boolean isComment = false;
	//
	// int lineNumber = 1;
	// int column = 0;
	//
	// String[] lines = text.split("\n");
	//
	// while (lineNumber <= lines.length) {
	// char c = lines[lineNumber-1].charAt(0);
	//
	// if (c == '\t') {
	// column += 4;
	// } else {
	// column++;
	// }
	//
	// if (!isComment) {
	// if (c == '\'') {
	// sQuote = !sQuote;
	// }
	//
	// if (c == '\"') {
	// dQuote = !dQuote;
	// }
	//
	// if (splitters.contains(c + "")) {
	// if (!buffer.toString().equals("")) {
	// words.add(new Word(buffer.toString(), lineText, lineNumber, column));
	// }
	//
	// if (c == '\n') {
	// lineNumber++;
	// column = 0;
	// if (words.size() != 0 && !preline.contains(words.get(words.size() - 1).getText())) {
	// words.add(new Word(",", lineText, lineNumber, column));
	// }
	// } else {
	// words.add(new Word(c + "", lineText, lineNumber, column));
	// }
	// buffer.setLength(0);
	// } else if (Character.isWhitespace(c) && !sQuote && !dQuote) {
	// if (buffer.toString().startsWith("/*")) {
	// if (!buffer.toString().endsWith("*/")) {
	// isComment = true;
	// }
	// } else if (!buffer.toString().equals("") && !isComment) {
	// words.add(new Word(buffer.toString(), lineText, lineNumber, column));
	// }
	// buffer.setLength(0);
	// } else {
	// buffer.append(c);
	// }
	//
	// } else {
	// buffer.append(c);
	// if (buffer.toString().endsWith("*/")) {
	// isComment = false;
	// buffer.setLength(0);
	// }
	//
	// }
	//
	// column++;
	// if (column == lines[lineNumber].length()) {
	// column = 0;
	// lineNumber++;
	// }
	// }
	//
	// if (words.get(words.size() - 1).getText().equals(",")) {
	// words.remove(words.size() - 1);
	// }
	//
	// return words;
	// }
	private static List<Word> split(String line) {
		List<Word> words = new ArrayList<>();

		StringBuilder buffer = new StringBuilder();
		StringBuilder lineText = new StringBuilder();

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
					if (!buffer.toString().equals("")) {
						// if (buffer.equals("*/")) {
						// isComment = false;
						// } else {
						words.add(new Word(buffer.toString(), lineText, lineNumber, column));
						// }
					}

					if (c == '\n') {
						lineNumber++;
						column = 0;
						if (words.size() != 0 && !preline.contains(words.get(words.size() - 1).getText())) {
							words.add(new Word(",", lineText, lineNumber, column));
						}
					} else {
						words.add(new Word(c + "", lineText, lineNumber, column));
					}
					buffer.setLength(0);
					;
				} else if (Character.isWhitespace(c) && !sQuote && !dQuote) {
					if (buffer.toString().startsWith("/*")) {
						if (!buffer.toString().endsWith("*/")) {
							isComment = true;
						}
					} else if (!buffer.toString().equals("") && !isComment) {
						words.add(new Word(buffer.toString(), lineText, lineNumber, column));
					}
					buffer.setLength(0);
					;
				} else {
					buffer.append(c);
				}

			} else {
				buffer.append(c);
				if (buffer.toString().endsWith("*/")) {
					isComment = false;
					buffer.setLength(0);
				}

			}

			if (c == '\n') {
				lineText = new StringBuilder();
			} else {
				lineText.append(c);
			}

		}

		if (words.get(words.size() - 1).getText().equals(",")) {
			words.remove(words.size() - 1);
		}

		return words;
	}

	private static List<String> splitters = Arrays.asList("\'", "\"", "[", "]", "{", "}", "(", ")", "=", "<", ">",
			"\n", ",", "$");

	private static List<String> preline = Arrays.asList("[", "{", "(", "<", ",", "=", ":");

	private static List<TokenType> closers = Arrays.asList(TokenType.RBRCE, TokenType.RBRAC, TokenType.RPAREN,
			TokenType.GTHAN);
}
