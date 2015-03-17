package com.jeffreymanzione.jef.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import com.jeffreymanzione.jef.parsing.value.FloatValue;
import com.jeffreymanzione.jef.parsing.value.ListValue;
import com.jeffreymanzione.jef.parsing.value.LongValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.Pair;
import com.jeffreymanzione.jef.parsing.value.SetValue;
import com.jeffreymanzione.jef.parsing.value.StringValue;
import com.jeffreymanzione.jef.parsing.value.TupleValue;
import com.jeffreymanzione.jef.parsing.value.Value;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenType;

public class Parser {

	Map<String, Definition> definitions = new HashMap<>();

	public Parser() {
		definitions.put("INT", IntDefinition.instance());
		definitions.put("FLOAT", FloatDefinition.instance());
		definitions.put("STRING", StringDefinition.instance());

	}

	public MapValue parseFile(Queue<Token> tokens, boolean verbose) throws ParsingException,
			DoesNotConformToDefintionException {
		return this.parseTopLevel(tokens, verbose);
	}

	private MapValue parseTopLevel(Queue<Token> tokens, boolean verbose) throws ParsingException,
			DoesNotConformToDefintionException {

		defCheck: do {
			Definition def = null;

			Token first = tokens.peek();
			switch (first.getType()) {
				case TYPE:
					tokens.remove();
					def = this.parseType(tokens);
					definitions.put(def.getName(), def);
					if (verbose) {
						System.out.println("Parsed def: " + def.getName());
					}
					break;
				case ENUM:
					tokens.remove();
					def = this.parseEnum(tokens);
					definitions.put(def.getName(), def);
					break;
				default:
					break defCheck;
			}
		} while (tokens.peek().getType() == TokenType.COMMA && tokens.remove().getType() == TokenType.COMMA);

		return this.parseMap(tokens);

	}

	private Value<?> parseValues(Queue<Token> tokens) throws ParsingException, DoesNotConformToDefintionException {
		Token val = tokens.peek();
		switch (val.getType()) {
			case FLOAT:
				return new FloatValue(Double.parseDouble(tokens.remove().getText()));
			case LONG:
				return new LongValue(Long.parseLong(tokens.remove().getText()));
			case STRING:
				return new StringValue(tokens.remove().getText());
			default:
				return this.parseStructures(tokens);
		}
	}

	private Definition parseEnum(Queue<Token> tokens) throws ParsingException {
		Token colon = tokens.remove();

		if (colon.getType() == TokenType.COLON) {
			Token defName = tokens.remove();

			if (defName.getType() == TokenType.DEF) {
				Token of = tokens.remove();
				if (of.getType() == TokenType.OF) {

					return this.parseEnumInner(tokens).setName(defName.getText());
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

			def = this.parseEnumSet(tokens);

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
		} while (tokens.peek().getType() == TokenType.COMMA && tokens.remove().getType() == TokenType.COMMA);

		return def;
	}

	private Definition parseType(Queue<Token> tokens) throws ParsingException {
		Token colon = tokens.remove();

		if (colon.getType() == TokenType.COLON) {
			Token defName = tokens.remove();

			if (defName.getType() == TokenType.DEF) {
				return this.parseTypeInner(tokens).setName(defName.getText());
			} else {
				throw new ParsingException(defName, "Expected definition constrictions after ':'.", TokenType.DEF);
			}
		} else {
			throw new ParsingException(colon, "For some reason we expected a ':'.", TokenType.COLON);
		}
	}

	private Definition parseTypeInner(Queue<Token> tokens) throws ParsingException {
		Token howToProceed = tokens.peek();

		switch (howToProceed.getType()) {
			case LPAREN:
				return this.parseTypeTuple(tokens);
			case LBRCE:
				return this.parseTypeMap(tokens);
			default:
				throw new ParsingException(howToProceed, "Unexpected token. Expected '{' or '('.");
		}
	}

	private Declaration parseDeclaration(Queue<Token> tokens) throws ParsingException {
		Token type = tokens.remove();

		if (type.getType() == TokenType.DEF) {

			Token mods = tokens.peek();

			if (mods.getType() != TokenType.VAR) {

				Modification mod = this.getModDeclaration(tokens);

				Token name = tokens.remove();

				if (name.getType() == TokenType.VAR) {
					Definition outerDef;
					if (mod == Modification.LIST) {
						outerDef = new ListDefinition(definitions.get(type.getText()));
					} else /* if (mod == Modification.MAP) */{
						// NEED TO MAKE THIS STRICT
						outerDef = new MapDefinition();
					}

					return new Declaration(outerDef, name.getText());
				} else {
					throw new ParsingException(name, "Unexpected token. Expected token VAR_NAME after TYPE.",
							TokenType.VAR);
				}

			} else {

				Token name = tokens.remove();
				if (name.getType() == TokenType.VAR) {
					return new Declaration(definitions.get(type.getText()), name.getText());
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
		} while (tokens.peek().getType() == TokenType.COMMA && tokens.remove().getType() == TokenType.COMMA);

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
		} while (tokens.peek().getType() == TokenType.COMMA && tokens.remove().getType() == TokenType.COMMA);

		Token end = tokens.remove();

		if (end.getType() == TokenType.RPAREN) {
			return format;
		} else {
			throw new ParsingException(end, "Missing ')' ending tuple declaration.", TokenType.RPAREN);
		}
	}

	private Value<?> parseStructures(Queue<Token> tokens) throws ParsingException, DoesNotConformToDefintionException {
		Token open = tokens.remove();

		TokenType closeToken;

		Value<?> val;

		switch (open.getType()) {
			case LTHAN:
				closeToken = TokenType.GTHAN;
				val = parseList(tokens);
				break;
			case LBRAC:
				closeToken = TokenType.RBRAC;
				val = parseSet(tokens);
				break;
			case LBRCE:
				closeToken = TokenType.RBRCE;
				val = parseMap(tokens);
				break;
			case LPAREN:
				closeToken = TokenType.RPAREN;
				val = parseTuple(tokens);
				break;
			default:
				throw new ParsingException(open, "Expected struct block type.");
		}

		try {
			Token close = tokens.remove();

			if (close.getType() != closeToken) {
				throw new ParsingException(close, "Struct closure missmatch.", closeToken);
			}

			return val;
		} catch (NoSuchElementException e) {
			throw new ParsingException("Unexpected EOF.", closeToken);
		}
	}

	private TupleValue parseTuple(Queue<Token> tokens) throws ParsingException, DoesNotConformToDefintionException {
		TupleValue list = new TupleValue();
		do {
			list.add(this.parseValues(tokens));
		} while (tokens.peek().getType() == TokenType.COMMA && tokens.remove().getType() == TokenType.COMMA);

		return list;
	}

	private SetValue parseSet(Queue<Token> tokens) throws ParsingException, DoesNotConformToDefintionException {
		SetValue set = new SetValue();
		do {
			set.add(this.parseValues(tokens));
		} while (tokens.peek().getType() == TokenType.COMMA && tokens.remove().getType() == TokenType.COMMA);

		return set;
	}

	private ListValue parseList(Queue<Token> tokens) throws ParsingException, DoesNotConformToDefintionException {
		ListValue list = new ListValue();
		do {
			list.add(this.parseValues(tokens));
		} while (tokens.peek().getType() == TokenType.COMMA && tokens.remove().getType() == TokenType.COMMA);

		return list;

	}

	private MapValue parseMap(Queue<Token> tokens) throws ParsingException, DoesNotConformToDefintionException {
		MapValue map = new MapValue();
		do {
			map.add(this.parseAssignment(tokens));
		} while (!tokens.isEmpty() && tokens.peek().getType() == TokenType.COMMA
				&& tokens.remove().getType() == TokenType.COMMA);

		return map;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Pair<?> parseAssignment(Queue<Token> tokens) throws ParsingException, DoesNotConformToDefintionException {
		Token var = tokens.remove();
		Value<?> val;
		if (var.getType() == TokenType.VAR) {
			Token eq = tokens.remove();
			if (eq.getType() == TokenType.EQUALS) {
				val = parseValues(tokens);
			} else if (eq.getType() == TokenType.COLON) {
				Token className = tokens.remove();
				if (className.getType() == TokenType.DEF) {
					if (definitions.containsKey(className.getText())) {
						Definition def = definitions.get(className.getText());
						// System.out.println(def);
						Token eq1 = tokens.remove();
						if (eq1.getType() == TokenType.EQUALS) {
							// // Do something to check that the format matches the definition.

							val = parseValues(tokens);
							Definition.check(def, val);
						} else {
							throw new ParsingException(eq1, "How can something be assigned without '='?",
									TokenType.EQUALS);
						}

					} else {
						throw new ParsingException(className, "ClassName is undefined.");
					}
				} else {
					throw new ParsingException(className, "Expect ClassName.");
				}
			} else {
				throw new ParsingException(eq, "How can something be assigned without '='?", TokenType.EQUALS);
			}
		} else {
			throw new ParsingException(var, "Where is the variable name?", TokenType.VAR);
		}
		return new Pair(var.getText(), val);

	}
}
