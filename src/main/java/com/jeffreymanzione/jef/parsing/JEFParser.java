package com.jeffreymanzione.jef.parsing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import com.jeffreymanzione.jef.parsing.exceptions.IndexableException;
import com.jeffreymanzione.jef.parsing.exceptions.ParsingException;
import com.jeffreymanzione.jef.parsing.value.EnumValue;
import com.jeffreymanzione.jef.parsing.value.ListValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.Pair;
import com.jeffreymanzione.jef.parsing.value.ArrayValue;
import com.jeffreymanzione.jef.parsing.value.TupleValue;
import com.jeffreymanzione.jef.parsing.value.Value;
import com.jeffreymanzione.jef.parsing.value.primitive.FloatValue;
import com.jeffreymanzione.jef.parsing.value.primitive.IntValue;
import com.jeffreymanzione.jef.parsing.value.primitive.StringValue;
import com.jeffreymanzione.jef.resurrection.BuiltInResurrector;
import com.jeffreymanzione.jef.resurrection.BuiltInResurrector.Transformer;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenType;
import com.jeffreymanzione.jef.tokenizing.TokenizeException;
import com.jeffreymanzione.jef.tokenizing.JEFTokenizer;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;

public class JEFParser implements Parser {
  private DefinitionsContainer definitions = new DefinitionsContainer();
  private boolean              isVerbose;
  private ValidationResponse   exceptions;

  public JEFParser () {
    try {
      exceptions = new ValidationResponse();
      definitions.put("Int", IntDefinition.instance());
      definitions.put("Float", FloatDefinition.instance());
      definitions.put("String", StringDefinition.instance());
      definitions.put("Integer", IntDefinition.instance());
      definitions.put("Floating", FloatDefinition.instance());
      definitions.put("Str", StringDefinition.instance());
      definitions.put("Bool", BooleanDefinition.instance());
      definitions.put("Boolean", BooleanDefinition.instance());

      addDefaultTransforms();
    } catch (ParsingException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean hasErrors () {
    return exceptions != null && exceptions.hasErrors();
  }

  @Override
  public ValidationResponse getExceptions () {
    return exceptions;
  }

  @Override
  public void setVerbose (boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  @Override
  public boolean isVerbose () {
    return isVerbose;
  }

  @Override
  public MapValue parse (Queue<Token> tokens) throws IndexableException {
    return parseTopLevel(tokens);
  }

  private static Token assertNextToken (Queue<Token> tokens, TokenType expected,
      String errMessage) throws ParsingException {
    if (tokens.isEmpty()) {
      throw new ParsingException("Strange.", expected);
    }
    Token token = tokens.remove();
    if (token.getType() != expected) {
      throw new ParsingException(token, errMessage, expected);
    }
    return token;
  }

  private boolean nextTokenIsAndRemove (Queue<Token> tokens, TokenType type) {
    if (!tokens.isEmpty() && tokens.peek().getType() == type) {
      tokens.remove();
      return true;
    }
    return false;
  }

  private static boolean nextTwoAreAndRemove (Queue<Token> tokens,
      TokenType first, TokenType second) {
    if (tokens.peek().getType() == first) {
      Token tmp = tokens.remove();
      if (tokens.peek().getType() == second) {
        tokens.remove();
        return true;
      } else {
        // TODO: This is ugly, consider doing this a different way.
        ((LinkedList<Token>) tokens).set(0, tmp);
      }
    }
    return false;
  }

  private void parseHeaders (Queue<Token> tokens) throws ParsingException {
    Loop: while (!tokens.isEmpty()) {
      switch (tokens.peek().getType()) {
        case TYPE:
          parseHeadersType(tokens);
          break;
        case ENUM:
          parseHeadersEnum(tokens);
          break;
        case INCLUDE:
          parseHeaderFile(tokens);
          break;
        default:
          break Loop;
      }
      nextTokenIsAndRemove(tokens, TokenType.COMMA);
    }

    for (Entry<String, Definition> entry : definitions.entrySet()) {
      Definition definition = entry.getValue();
      definition.validate(definitions.definitions);
    }
  }

  private void parseHeadersType (Queue<Token> tokens) throws ParsingException {
    tokens.remove();
    Definition def = parseType(tokens);
    definitions.put(def);
    if (isVerbose) {
      System.out.println("Parsed type def: " + def.getName());
    }
  }

  private void parseHeadersEnum (Queue<Token> tokens) throws ParsingException {
    tokens.remove();
    Definition def = parseEnum(tokens);
    definitions.put(def);
    if (isVerbose) {
      System.out.println("Parsed enum def: " + def.getName());
    }
  }

  private void parseHeaderFile (Queue<Token> tokens) throws ParsingException {
    tokens.remove();
    Token filePath = assertNextToken(tokens, TokenType.STRING,
        "Expected a string after keyword type : INCLUDE.");

    File file = new File(filePath.getText());
    if (!file.exists()) {
      throw new ParsingException(filePath,
          "Include path does not exist. Was: '" + filePath.getText() + "'.");
    }

    try {
      // TODO: Make tokenizer customizable.
      Tokenizer tokenizer = new JEFTokenizer();
      // tokenizer.setVerbose(true);
      Queue<Token> headerTokens = tokenizer.tokenize(file);
      parseHeaders(headerTokens);
    } catch (IOException | TokenizeException e) {
      throw new ParsingException(filePath, e.toString());
    }
  }

  private MapValue parseTopLevel (Queue<Token> tokens)
      throws IndexableException {
    parseHeaders(tokens);
    return parseMap(tokens, tokens.peek());
  }

  private Value<?> parseValuesUntyped (Queue<Token> tokens)
      throws IndexableException {
    Token val = tokens.peek();
    switch (val.getType()) {
      case INT:
        try {
          String num = tokens.remove().getText();
          if (num.toLowerCase().startsWith("0x")) {
            return new IntValue(Integer.valueOf(num.substring(2), 16), val);
          } else if (num.startsWith("0")) {
            return new IntValue(Integer.valueOf(num, 8), val);
          } else if (num.startsWith("#")) {
            return new IntValue(Integer.valueOf(num.substring(1), 36), val);
          } else if (num.startsWith("!")) {
            return new IntValue(Integer.valueOf(num.substring(1), 2), val);
          } else {
            return new IntValue(Integer.valueOf(num), val);
          }
        } catch (NumberFormatException e) {
          throw new ParsingException(val, "Value was " + val.toString()
              + " and was not in the expecte format.");
        }
      case FLOAT:
        return new FloatValue(Double.valueOf(tokens.remove().getText()), val);
      case ENUMVAL:
        return new EnumValue(tokens.remove().getText(), val);
      case STRING:
        return new StringValue(tokens.remove().getText(), val);
      default:
        return parseStructures(tokens);
    }
  }

  private Definition parseEnum (Queue<Token> tokens) throws ParsingException {

    assertNextToken(tokens, TokenType.COLON,
        "Expected ':' after keyword type.");

    Token defName = assertNextToken(tokens, TokenType.DEF,
        "Expected DEFINITION after keyword type and ':'.");
    // assertNextToken(tokens, TokenType.OF,
    // "Expected OF after keyword type : ENUM.");
    // OF is now optional for enum defs. =)
    nextTokenIsAndRemove(tokens, TokenType.OF);

    return parseEnumInner(tokens).setName(defName.getText());
  }

  private Definition parseEnumInner (Queue<Token> tokens)
      throws ParsingException {

    assertNextToken(tokens, TokenType.LBRAC, "Expected '[' after keyword OF.");

    Definition def = parseEnumSet(tokens);

    assertNextToken(tokens, TokenType.RBRAC,
        "Expected ']' at end of an enumaration.");

    return def;
  }

  private Definition parseEnumSet (Queue<Token> tokens)
      throws ParsingException {
    EnumDefinition def = new EnumDefinition();
    do {
      Token name = assertNextToken(tokens, TokenType.VAR,
          "Invalid within an enumaration.");
      def.add(name.getText());

    } while (nextTokenIsAndRemove(tokens, TokenType.COMMA));

    return def;
  }

  private Definition parseType (Queue<Token> tokens) throws ParsingException {

    assertNextToken(tokens, TokenType.COLON,
        "For some reason we expected a ':'.");

    Token defName = assertNextToken(tokens, TokenType.DEF,
        "Expected definition constrictions after ':'.");

    return parseToAnonymousType(tokens).setName(defName.getText());
  }

  private Definition resolveType (Token type) {
    Definition innerDef = definitions.get(type.getText());
    if (innerDef == null) {
      innerDef = new TempDefinition(type.getText());
    }
    return innerDef;
  }

  // Chose to do this recursively because the code is smaller this way.
  private Definition parseModificationsOnDefinition (Queue<Token> tokens,
      Definition innerType) throws ParsingException {
    Definition def;
    if (nextTwoAreAndRemove(tokens, TokenType.LBRCE, TokenType.RBRCE)) {
      def = new MapDefinition();
      ((MapDefinition) def).setRestricted(innerType);
      def = parseModificationsOnDefinition(tokens, def);
    } else if (nextTwoAreAndRemove(tokens, TokenType.LTHAN, TokenType.GTHAN)) {
      def = parseModificationsOnDefinition(tokens,
          new ListDefinition(innerType));
    } else if (nextTwoAreAndRemove(tokens, TokenType.LBRAC, TokenType.RBRAC)) {
      def = parseModificationsOnDefinition(tokens,
          new ArrayDefinition(innerType));
    } else {
      return innerType;
    }
    return def;
  }

  private Definition parseToAnonymousType (Queue<Token> tokens)
      throws ParsingException {
    Definition def;
    switch (tokens.peek().getType()) {
      case DEF:
        def = resolveType(tokens.remove());
        break;
      case LBRCE:
        def = parseTypeMap(tokens);
        break;
      case LPAREN:
        def = parseTypeTuple(tokens);
        break;
      default:
        throw new ParsingException(tokens.peek(),
            "Unexpected token. Definition is malformed.");
    }

    if (!tokens.isEmpty() && (tokens.peek().getType() == TokenType.LBRCE
        || tokens.peek().getType() == TokenType.LTHAN
        || tokens.peek().getType() == TokenType.LBRAC)) {
      return parseModificationsOnDefinition(tokens, def);
    } else {
      return def;
    }
  }

  private Declaration parseDeclaration (Queue<Token> tokens)
      throws ParsingException {
    Definition def = parseToAnonymousType(tokens);

    Token name = assertNextToken(tokens, TokenType.VAR,
        "Unexpected token. Expected token VAR_NAME after TYPE.");

    return new Declaration(def, name.getText());
  }

  private Definition parseTypeInfo (Queue<Token> tokens)
      throws ParsingException {
    Token type = assertNextToken(tokens, TokenType.DEF, "Unexpected token.");
    return definitions.get(type.getText());
  }

  private MapDefinition parseTypeMap (Queue<Token> tokens)
      throws ParsingException {
    MapDefinition def = new MapDefinition();
    tokens.remove();

    // Just a map with no requirements
    if (tokens.peek().getType() == TokenType.RBRCE) {
      tokens.remove();
      return def;
    }

    do {
      Declaration dec = parseDeclaration(tokens);
      def.add(dec.getName(), dec.getDefinition());
    } while (nextTokenIsAndRemove(tokens, TokenType.COMMA));

    assertNextToken(tokens, TokenType.RBRCE,
        "Missing '}' ending map declaration.");

    return def;
  }

  private TupleDefinition parseTypeTuple (Queue<Token> tokens)
      throws ParsingException {
    TupleDefinition format = new TupleDefinition();
    tokens.remove();
    do {
      format.add(parseTypeInfo(tokens));
    } while (nextTokenIsAndRemove(tokens, TokenType.COMMA));

    assertNextToken(tokens, TokenType.RPAREN,
        "Missing ')' ending tuple declaration.");

    return format;
  }

  private Value<?> parseStructures (Queue<Token> tokens)
      throws IndexableException {
    Token open = tokens.remove();

    TokenType closeToken;

    Value<?> val;

    switch (open.getType()) {
      case LTHAN:
        closeToken = TokenType.GTHAN;
        val = parseList(tokens, open);
        break;
      case LBRAC:
        closeToken = TokenType.RBRAC;
        val = parseArray(tokens, open);
        break;
      case LBRCE:
        closeToken = TokenType.RBRCE;
        val = parseMap(tokens, open);
        break;
      case LPAREN:
        closeToken = TokenType.RPAREN;
        val = parseTuple(tokens, open);
        break;
      default:
        throw new ParsingException(open,
            "Was: " + open.getText() + ". Expected struct block type.");
    }

    try {
      assertNextToken(tokens, closeToken, "Struct closure missmatch.");
      return val;

    } catch (NoSuchElementException e) {
      throw new ParsingException("Unexpected EOF.", closeToken);
    }
  }

  private TupleValue parseTuple (Queue<Token> tokens, Token start)
      throws IndexableException {
    TupleValue list = new TupleValue(start);
    do {
      list.add(parseValues(tokens));
    } while (nextTokenIsAndRemove(tokens, TokenType.COMMA));

    return list;
  }

  private ArrayValue parseArray (Queue<Token> tokens, Token start)
      throws IndexableException {
    ArrayValue arr = new ArrayValue(start);
    do {
      arr.add(parseValues(tokens));
    } while (nextTokenIsAndRemove(tokens, TokenType.COMMA));

    return arr;
  }

  private ListValue parseList (Queue<Token> tokens, Token start)
      throws IndexableException {
    ListValue list = new ListValue(start);
    do {
      list.add(parseValues(tokens));
    } while (nextTokenIsAndRemove(tokens, TokenType.COMMA));

    return list;
  }

  private MapValue parseMap (Queue<Token> tokens, Token start)
      throws IndexableException {
    MapValue map = new MapValue(start);
    if (tokens.peek().getType() == TokenType.RBRCE) {
      return map;
    }
    do {
      map.add(parseAssignment(tokens));
    } while (!tokens.isEmpty()
        && nextTokenIsAndRemove(tokens, TokenType.COMMA));

    return map;
  }

  private Value<?> parseTypedValues (Queue<Token> tokens)
      throws IndexableException {
    Value<?> val;
    Definition def = parseToAnonymousType(tokens);

    assertNextToken(tokens, TokenType.GTHAN, "Expect '>'.");
    assertNextToken(tokens, TokenType.EQUALS, "Expected '='.");

    val = parseValuesUntyped(tokens);
    Definition tmp;
    if (val instanceof ListValue) {
      tmp = new ListDefinition(def);
    } else if (val instanceof ArrayValue) {
      tmp = new ArrayDefinition(def);
    } else if (val instanceof MapValue) {
      tmp = new MapDefinition();
      MapDefinition mapDef = (MapDefinition) tmp;
      mapDef.setRestricted(def);
    } else {
      throw new ParsingException(val.getToken(), "NOT IMPLEMENTED!!!");
    }

    exceptions.addResponse(Definition.check(tmp, val));
    if (tmp instanceof ListDefinition) {
      val.setEntityID(def.getName());
    }
    return val;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Pair<String, ?> parseAssignment (Queue<Token> tokens)
      throws IndexableException {
    Value<?> val;
    Token var = assertNextToken(tokens, TokenType.VAR,
        "Where is the variable name?");

    Token eq = tokens.remove();
    switch (eq.getType()) {
      case EQUALS:
        val = parseValues(tokens);
        break;
      case LTHAN:
        val = parseTypedValues(tokens);
        break;
      default:
        throw new ParsingException(eq, "Expected '=' or '<'.",
            TokenType.EQUALS);
    }

    return new Pair(var.getText(), val);
  }

  // Makes it so you do not need equals (anonymous)
  private Value<?> parseValues (Queue<Token> tokens) throws IndexableException {
    Value<?> val;
    Token className = tokens.peek();
    // TODO: Decide if we should change this so it uses parseToAnon and accepts anon defined types
    // in addition to definitions.
    if (className.getType() == TokenType.DEF) {
      tokens.remove();
      if (!definitions.containsKey(className.getText())) {
        throw new ParsingException(className,
            "ClassName is undefined. Was '" + className.getText() + "'.");
      }

      Definition def = definitions.get(className.getText());
      val = parseValuesUntyped(tokens);
      exceptions.addResponse(Definition.check(def, val));
      val.setEntityID(def.getName());
    } else {
      val = parseValuesUntyped(tokens);
    }

    return val;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void addDefaultTransforms () throws ParsingException {
    for (Transformer<?> trans : BuiltInResurrector.getTransformers()) {
      definitions.put(trans.id, new BuiltInDefinition(trans));
    }
  }

  private class DefinitionsContainer {
    Map<String, Definition> definitions = new HashMap<>();

    void put (Definition def) throws ParsingException {
      put(def.getName(), def);
    }

    void put (String key, Definition value) throws ParsingException {

      if (definitions.containsKey(key) && !definitions.get(key).equals(value)) {
        throw new ParsingException("Duplicate Definintion. Old="
            + definitions.get(key) + ". New=" + value + ".", TokenType.DEF);
      }

      definitions.put(key, value);
    }

    Set<Entry<String, Definition>> entrySet () {
      return definitions.entrySet();
    }

    Definition get (String key) {
      return definitions.get(key);
    }

    boolean containsKey (String key) {
      return definitions.containsKey(key);
    }
  }
}