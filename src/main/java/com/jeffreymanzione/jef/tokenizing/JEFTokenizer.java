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

public class JEFTokenizer implements Tokenizer {

  private boolean isVerbose;

  @Override
  public void setVerbose (boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  @Override
  public boolean isVerbose () {
    return isVerbose;
  }

  @Override
  public Queue<Token> tokenize (String string) throws TokenizeException {
    return this.tokenizeWords(JEFTokenizer.split(string));
  }

  @Override
  public Queue<Token> tokenize (InputStream stream)
      throws IOException, TokenizeException {
    return this.tokenize(JEFTokenizer.fileToString(stream));
  }

  @Override
  public Queue<Token> tokenize (File file)
      throws IOException, TokenizeException {
    return this.tokenize(new FileInputStream(file));
  }

  private Queue<Token> tokenizeWords (List<Word> words)
      throws TokenizeException {
    LinkedList<Token> tokens = isVerbose ? new LinkedList<Token>() {
      private static final long serialVersionUID = 1L;

      public Token remove () {

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
        } else if (word.getText().startsWith("-")
            || word.getText().startsWith("!") || word.getText().startsWith("#")
            || word.getText().startsWith(".")
            || Character.isDigit(word.getText().toCharArray()[0])) {
          if (word.getText().contains(".")
              || (!(word.getText().toLowerCase().startsWith("0x")
                  || word.getText().toLowerCase().startsWith("!")
                  || word.getText().toLowerCase().startsWith("#"))
                  && word.getText().toLowerCase().endsWith("f"))) {
            token = new Token(word, TokenType.FLOAT);
          } else {
            token = new Token(word, TokenType.INT);
          }
        } else if (tokens.get(tokens.size() - 1).getType() != TokenType.QUOTE
            && Character.isUpperCase(word.getText().charAt(0))) {
          token = new Token(word, TokenType.DEF);
        } else if (tokens.size() > 0
            && tokens.get(tokens.size() - 1).getType() == TokenType.DOLLAR) {
          tokens.remove(tokens.size() - 1);
          token = new Token(word, TokenType.ENUMVAL);
        } else if (tokens.size() > 0
            && tokens.get(tokens.size() - 1).getType() == TokenType.QUOTE) {
          tokens.remove(tokens.size() - 1);
          token = new Token(word, TokenType.STRING);
          if (index < words.size() + 1) {
            index++;
          } else {
            throw new TokenizeException(
                "Expected token: '. Reached end of file.");
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

  private static String fileToString (InputStream file) throws IOException {
    StringBuilder strBuilder = new StringBuilder();
    try (Scanner scan = new Scanner(file)) {
      while (scan.hasNextLine()) {
        strBuilder.append(scan.nextLine());
        strBuilder.append("\n");
      }
    }
    return strBuilder.toString();
  }

  private static List<Word> split (String line) {
    List<Word> words = new ArrayList<>();

    StringBuilder buffer = new StringBuilder();
    StringBuilder lineText = new StringBuilder();

    boolean sQuote = false;
    boolean dQuote = false;
    boolean isComment = false;

    int lineNumber = 1;
    int column = 0;

    for (char c : line.toCharArray()) {
      // TODO: Make this so that it handles different indentations
      column += (c == '\t') ? 4 : 1;

      if (!isComment) {
        if (c == '\'') {
          sQuote = !sQuote;
        }
        if (c == '\"') {
          dQuote = !dQuote;
        }
        if (splitters.contains(c + "")) {
          if (!buffer.toString().equals("")) {
            words
                .add(new Word(buffer.toString(), lineText, lineNumber, column));
          }
          if (c == '\n') {
            lineNumber++;
            column = 0;
            if (words.size() != 0
                && !preline.contains(words.get(words.size() - 1).getText())) {
              words.add(new Word(",", lineText, lineNumber, column));
            }
          } else {
            words.add(new Word(c + "", lineText, lineNumber, column));
          }
          buffer.setLength(0);

        } else if (Character.isWhitespace(c) && !sQuote && !dQuote) {
          if (buffer.toString().startsWith("/*")) {
            if (!buffer.toString().endsWith("*/")) {
              isComment = true;
            }
          } else if (!buffer.toString().equals("") && !isComment) {
            words
                .add(new Word(buffer.toString(), lineText, lineNumber, column));
          }
          buffer.setLength(0);
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

  private static List<String>    splitters = Arrays.asList("\'", "\"", "[", "]",
      "{", "}", "(", ")", "=", "<", ">", "\n", ",", "$", "?");

  private static List<String>    preline   = Arrays.asList("[", "{", "(", "<",
      ",", "=", ":");

  private static List<TokenType> closers   = Arrays.asList(TokenType.RBRCE,
      TokenType.RBRAC, TokenType.RPAREN, TokenType.GTHAN);
}
