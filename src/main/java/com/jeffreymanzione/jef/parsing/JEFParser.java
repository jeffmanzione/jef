package com.jeffreymanzione.jef.parsing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
import com.jeffreymanzione.jef.parsing.value.SetValue;
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

public class JEFParser implements Parser {
  private DefinitionsContainer definitions = new DefinitionsContainer();
  private boolean              isVerbose;
  private ValidationResponse   exceptions;

  public JEFParser() {
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
  public boolean hasErrors() {
    return exceptions != null && exceptions.hasErrors();
  }

  @Override
  public ValidationResponse getExceptions() {
    return exceptions;
  }

  @Override
  public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  @Override
  public boolean isVerbose() {
    return isVerbose;
  }

  @Override
  public MapValue parse(Queue<Token> tokens) throws IndexableException {
    return parseTopLevel(tokens);
  }

  private boolean parseHeaders(Queue<Token> tokens) throws ParsingException {
    boolean isEmpty = false;
    defCheck: do {
      Token first = tokens.peek();
      switch (first.getType()) {
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
          break defCheck;
      }
    } while (!(isEmpty = tokens.isEmpty()) && tokens.peek().getType() == TokenType.COMMA
        && tokens.remove().getType() == TokenType.COMMA);

    for (Entry<String, Definition> entry : definitions.entrySet()) {
      Definition definition = entry.getValue();
      definition.validate(definitions.definitions);
    }
    return isEmpty;
  }

  private void parseHeadersType(Queue<Token> tokens) throws ParsingException {
    tokens.remove();
    Definition def = parseType(tokens);
    definitions.put(def);
    if (isVerbose) {
      System.out.println("Parsed type def: " + def.getName());
    }
  }

  private void parseHeadersEnum(Queue<Token> tokens) throws ParsingException {
    tokens.remove();
    Definition def = parseEnum(tokens);
    definitions.put(def);
    if (isVerbose) {
      System.out.println("Parsed enum def: " + def.getName());
    }
  }

  private void parseHeaderFile(Queue<Token> tokens) throws ParsingException {
    tokens.remove();
    Token filePath = tokens.remove();
    if (filePath.getType() == TokenType.STRING) {
      File file = new File(filePath.getText());
      if (file.exists()) {
        try {
          // TODO: Make tokenizer customizable.
          JEFTokenizer tokenizer = new JEFTokenizer();
          // tokenizer.setVerbose(true);
          Queue<Token> headerTokens = tokenizer.tokenize(file);
          parseHeaders(headerTokens);
        } catch (IOException | TokenizeException e) {
          throw new ParsingException(filePath, e.toString());
        }
      } else {
        throw new ParsingException(filePath, "Include path does not exist. Was: '"
            + filePath.getText() + "'.");
      }
    } else {
      throw new ParsingException(filePath, "Expected a string after keyword type : INCLUDE.");
    }
  }

  private MapValue parseTopLevel(Queue<Token> tokens) throws IndexableException {
    parseHeaders(tokens);
    return parseMap(tokens, tokens.peek());
  }

  private Value<?> parseValuesUntyped(Queue<Token> tokens) throws IndexableException {
    Token val = tokens.peek();
    switch (val.getType()) {
      case INT:
        return new IntValue(Integer.parseInt(tokens.remove().getText()), val);
      case FLOAT:
        return new FloatValue(Double.parseDouble(tokens.remove().getText()), val);
      case ENUMVAL:
        return new EnumValue(tokens.remove().getText(), val);
      case STRING:
        return new StringValue(tokens.remove().getText(), val);
      default:
        return parseStructures(tokens);
    }
  }

  private Definition parseEnum(Queue<Token> tokens) throws ParsingException {
    Token colon = tokens.remove();

    if (colon.getType() == TokenType.COLON) {
      Token defName = tokens.remove();

      if (defName.getType() == TokenType.DEF) {
        Token of = tokens.remove();
        if (of.getType() == TokenType.OF) {

          return parseEnumInner(tokens).setName(defName.getText());
        } else {
          throw new ParsingException(of, "Expected OF after keyword type : ENUM.");
        }
      } else {
        throw new ParsingException(defName, "Expected DEFINITION after keyword type and ':'.");
      }
    } else {
      throw new ParsingException(colon, "Expected ':' after keyword type.");
    }
  }

  private Definition parseEnumInner(Queue<Token> tokens) throws ParsingException {
    Token open = tokens.remove();

    Definition def;

    if (open.getType() == TokenType.LBRAC) {

      def = parseEnumSet(tokens);

      Token close = tokens.remove();
      if (close.getType() == TokenType.RBRAC) {
        return def;
      } else {
        throw new ParsingException(open, "Expected ']' at end of an enumaration.");
      }
    } else {
      throw new ParsingException(open, "Expected '[' after keyword OF.");
    }
  }

  private Definition parseEnumSet(Queue<Token> tokens) throws ParsingException {
    EnumDefinition def = new EnumDefinition();
    do {
      Token name = tokens.remove();
      if (name.getType() == TokenType.VAR) {
        def.add(name.getText());
      } else {
        throw new ParsingException(name, "Invalid within an enumaration.", TokenType.VAR);
      }
    } while (tokens.peek().getType() == TokenType.COMMA
        && tokens.remove().getType() == TokenType.COMMA);

    return def;
  }

  private Definition parseType(Queue<Token> tokens) throws ParsingException {
    Token colon = tokens.remove();

    if (colon.getType() == TokenType.COLON) {
      Token defName = tokens.remove();

      if (defName.getType() == TokenType.DEF) {
        return parseTypeInner(tokens).setName(defName.getText());
      } else {
        throw new ParsingException(defName, "Expected definition constrictions after ':'.",
            TokenType.DEF);
      }
    } else {
      throw new ParsingException(colon, "For some reason we expected a ':'.", TokenType.COLON);
    }
  }

  private Definition resolveType(Token type) {
    Definition innerDef = definitions.get(type.getText());
    if (innerDef == null) {
      // System.out.println("1Could not find " + type.getText());
      innerDef = new TempDefinition(type.getText());
    }
    return innerDef;
  }

  private Definition parseTypeInner(Queue<Token> tokens) throws ParsingException {
    Token howToProceed = tokens.peek();

    switch (howToProceed.getType()) {
      case LPAREN:
        return parseTypeTuple(tokens);
      case LBRCE:
        return parseTypeMap(tokens);
      default:
        throw new ParsingException(howToProceed, "Unexpected token. Expected '{' or '('.");
    }
  }

  private Declaration parseDeclaration(Queue<Token> tokens) throws ParsingException {
    Token type = tokens.remove();

    if (type.getType() == TokenType.DEF) {
      Token mods = tokens.peek();

      if (mods.getType() != TokenType.VAR) {
        Modification mod = getModDeclaration(tokens);
        Token name = tokens.remove();

        if (name.getType() == TokenType.VAR) {
          Definition outerDef;
          Definition innerDef = resolveType(type);

          if (mod == Modification.LIST) {
            outerDef = new ListDefinition(innerDef);
          } else /* if (mod == Modification.MAP) */{
            outerDef = new MapDefinition();
            ((MapDefinition) outerDef).setRestricted(definitions.get(type.getText()));
          }
          return new Declaration(outerDef, name.getText());
        } else {
          throw new ParsingException(name, "Unexpected token. Expected token VAR_NAME after TYPE.",
              TokenType.VAR);
        }
      } else {
        Token name = tokens.remove();

        if (name.getType() == TokenType.VAR) {
          Definition innerDef = resolveType(type);
          return new Declaration(innerDef, name.getText());
        } else {
          throw new ParsingException(name, "Unexpected token. Expected token VAR_NAME after TYPE.",
              TokenType.VAR);
        }
      }
    } else {
      throw new ParsingException(type, "Unexpected token.", TokenType.TYPE);
    }
  }

  private Modification getModDeclaration(Queue<Token> tokens) throws ParsingException {
    Token type = tokens.remove();
    Modification mod;

    if (type.getType() == TokenType.LBRCE && tokens.remove().getType() == TokenType.RBRCE) {
      mod = Modification.MAP;
    } else if (type.getType() == TokenType.LTHAN && tokens.remove().getType() == TokenType.GTHAN) {
      mod = Modification.LIST;
    } else {
      throw new ParsingException(type, "Unexpected token. Expected mod.");
    }

    return mod;
  }

  private Definition parseTypeInfo(Queue<Token> tokens) throws ParsingException {
    Token type = tokens.remove();

    if (type.getType() == TokenType.DEF) {
      return definitions.get(type.getText());
    } else {
      throw new ParsingException(type, "Unexpected token.", TokenType.TYPE);
    }
  }

  private MapDefinition parseTypeMap(Queue<Token> tokens) throws ParsingException {
    MapDefinition def = new MapDefinition();
    tokens.remove();

    do {
      Declaration dec = parseDeclaration(tokens);
      def.add(dec.getName(), dec.getDefinition());
    } while (tokens.peek().getType() == TokenType.COMMA
        && tokens.remove().getType() == TokenType.COMMA);

    Token end = tokens.remove();

    if (end.getType() == TokenType.RBRCE) {
      return def;
    } else {
      throw new ParsingException(end, "Missing '}' ending map declaration.", TokenType.RBRAC);
    }
  }

  private TupleDefinition parseTypeTuple(Queue<Token> tokens) throws ParsingException {
    TupleDefinition format = new TupleDefinition();
    tokens.remove();

    do {
      format.add(parseTypeInfo(tokens));
    } while (tokens.peek().getType() == TokenType.COMMA
        && tokens.remove().getType() == TokenType.COMMA);

    Token end = tokens.remove();

    if (end.getType() == TokenType.RPAREN) {
      return format;
    } else {
      throw new ParsingException(end, "Missing ')' ending tuple declaration.", TokenType.RPAREN);
    }
  }

  private Value<?> parseStructures(Queue<Token> tokens) throws IndexableException {
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
        val = parseSet(tokens, open);
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
        throw new ParsingException(open, "Was: " + open.getText() + ". Expected struct block type.");
    }

    try {
      Token close = tokens.remove();

      if (close.getType() != closeToken) {
        throw new ParsingException(close, "Struct closure missmatch.", closeToken);
      } else {
        return val;
      }
    } catch (NoSuchElementException e) {
      throw new ParsingException("Unexpected EOF.", closeToken);
    }
  }

  private TupleValue parseTuple(Queue<Token> tokens, Token start) throws IndexableException {
    TupleValue list = new TupleValue(start);
    do {
      list.add(parseValues(tokens));
    } while (tokens.peek().getType() == TokenType.COMMA
        && tokens.remove().getType() == TokenType.COMMA);

    return list;
  }

  private SetValue parseSet(Queue<Token> tokens, Token start) throws IndexableException {
    SetValue set = new SetValue(start);
    do {
      set.add(parseValues(tokens));
    } while (tokens.peek().getType() == TokenType.COMMA
        && tokens.remove().getType() == TokenType.COMMA);

    return set;
  }

  private ListValue parseList(Queue<Token> tokens, Token start) throws IndexableException {
    ListValue list = new ListValue(start);
    do {
      list.add(parseValues(tokens));
    } while (tokens.peek().getType() == TokenType.COMMA
        && tokens.remove().getType() == TokenType.COMMA);

    return list;

  }

  private MapValue parseMap(Queue<Token> tokens, Token start) throws IndexableException {
    MapValue map = new MapValue(start);
    do {
      map.add(parseAssignment(tokens));
    } while (!tokens.isEmpty() && tokens.peek().getType() == TokenType.COMMA
        && tokens.remove().getType() == TokenType.COMMA);

    return map;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Pair<String, ?> parseAssignment(Queue<Token> tokens) throws IndexableException {
    Token var = tokens.remove();
    Value<?> val;
    if (var.getType() == TokenType.VAR) {
      Token eq = tokens.remove();
      if (eq.getType() == TokenType.EQUALS) {
        val = parseValues(tokens);
      } else if (eq.getType() == TokenType.LTHAN) {
        Token className = tokens.remove();
        if (className.getType() == TokenType.DEF) {
          if (definitions.containsKey(className.getText())) {
            Definition def = definitions.get(className.getText());
            Token gt = tokens.remove();
            if (gt.getType() == TokenType.GTHAN) {
              Token eq1 = tokens.remove();
              if (eq1.getType() == TokenType.EQUALS) {
                val = parseValuesUntyped(tokens);
                Definition tmp;
                if (val instanceof ListValue) {
                  tmp = new ListDefinition(def);
                } else if (val instanceof MapValue) {
                  tmp = new MapDefinition();
                  MapDefinition mapDef = (MapDefinition) tmp;
                  mapDef.setRestricted(def);
                } else {
                  throw new ParsingException(eq1, "NOT IMPLEMENTED!!!");
                }
                exceptions.addResponse(Definition.check(tmp, val));
                if (tmp instanceof ListDefinition) {
                  val.setEntityID(def.getName());
                }
              } else {
                throw new ParsingException(eq1, "Expected '='.", TokenType.EQUALS);
              }
            } else {
              throw new ParsingException(gt, "Expect '>'.");
            }
          } else {
            throw new ParsingException(className, "ClassName is undefined.");
          }
        } else {
          throw new ParsingException(className, "Expect ClassName.");
        }
      } else {
        throw new ParsingException(eq, "Expected '=' or '<'.", TokenType.EQUALS);
      }
    } else {
      throw new ParsingException(var, "Where is the variable name?", TokenType.VAR);
    }
    return new Pair(var.getText(), val);

  }

  // make it so you do not need equals (anonymous)
  private Value<?> parseValues(Queue<Token> tokens) throws IndexableException {
    Value<?> val;
    Token className = tokens.peek();
    if (className.getType() == TokenType.DEF) {
      tokens.remove();
      if (definitions.containsKey(className.getText())) {
        Definition def = definitions.get(className.getText());
        val = parseValuesUntyped(tokens);
        exceptions.addResponse(Definition.check(def, val));
        val.setEntityID(def.getName());
      } else {
        throw new ParsingException(className, "ClassName is undefined. Was '" + className.getText()
            + "'.");
      }
    } else {
      val = parseValuesUntyped(tokens);
    }

    return val;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void addDefaultTransforms() throws ParsingException {
    for (Transformer<?> trans : BuiltInResurrector.getTransformers()) {
      definitions.put(trans.id, new BuiltInDefinition(trans));
    }
  }

  class DefinitionsContainer {
    Map<String, Definition> definitions = new HashMap<>();

    void put(Definition def) throws ParsingException {
      put(def.getName(), def);
    }

    void put(String key, Definition value) throws ParsingException {
      if (definitions.containsKey(key) && !definitions.get(key).equals(value)) {
        throw new ParsingException("Duplicate Definintion. Old=" + definitions.get(key) + ". New="
            + value + ".", TokenType.DEF);
      } else {
        definitions.put(key, value);
      }
    }

    void putAll(Map<String, Definition> defs) throws ParsingException {
      for (Entry<String, Definition> entry : defs.entrySet()) {
        put(entry.getKey(), entry.getValue());
      }
    }

    Set<Entry<String, Definition>> entrySet() {
      return definitions.entrySet();
    }

    Definition get(String key) {
      return definitions.get(key);
    }

    boolean containsKey(String key) {
      return definitions.containsKey(key);
    }
  }

}
