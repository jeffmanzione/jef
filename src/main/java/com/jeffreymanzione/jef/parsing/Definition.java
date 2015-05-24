package com.jeffreymanzione.jef.parsing;

import com.jeffreymanzione.jef.parsing.value.ListValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.Pair;
import com.jeffreymanzione.jef.parsing.value.TupleValue;
import com.jeffreymanzione.jef.parsing.value.Value;
import com.jeffreymanzione.jef.parsing.value.ValueType;

public abstract class Definition {

	private String name;

	public Definition setName(String text) {
		this.name = text;

		return this;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "Definition(" + name + ")";
	}

	public static final boolean check(Definition def, Value<?> val, int line, int column)
			throws DoesNotConformToDefintionException {
		if (def instanceof EnumDefinition) {
			EnumDefinition enumDef = (EnumDefinition) def;
			if (val.getType() == ValueType.ENUM) {
				if (enumDef.contains(val.getValue().toString())) {
					// System.out.println("SUCCESS!!!");
					val.setEntityID(enumDef.getName());
					return true;
				} else {
					throw new DoesNotConformToDefintionException(line, column, "Unexpected enum value. Was '"
							+ val.getValue() + "' but expected one of the following: "
							+ enumDef.toString() + ".");
				}
			} else {
				throw new DoesNotConformToDefintionException(line, column, "Expected a STRING but was a "
						+ val.getType() + ".");
			}
		} else if (def instanceof MapDefinition) {
			MapDefinition mapDef = (MapDefinition) def;
			if (val.getType() == ValueType.MAP) {
				MapValue mapVal = (MapValue) val;
				// System.out.println("MAP DEF = " + mapDef.toString());
				// System.out.println("MAP     = " + mapVal);
				for (String key : mapDef) {
					// System.out.println(key);
					Value<?> subVal = mapVal.get(key);
					if (subVal != null) {
						Definition subDef = mapDef.get(key);

						if (subDef instanceof MapDefinition && ((MapDefinition) subDef).isRestricted()) {
							// System.out.println(((MapDefinition) subDef).isRestricted() + " "
							// + ((MapDefinition) subDef).getRestriction());
							subDef = ((MapDefinition) subDef).getRestriction();

							MapValue subMapVal = (MapValue) subVal;

							for (Pair<?> pair : subMapVal) {
								Definition.check(subDef, pair.getValue(), line, column);
							}

						} else if (subDef instanceof ListDefinition) {
							subDef = ((ListDefinition) subDef).getType();

							ListValue subListVal = (ListValue) subVal;

							for (Value<?> listVal : subListVal) {
								Definition.check(subDef, listVal, line, column);
								listVal.setEntityID(subDef.getName());
							}

						} else {
							// System.out.println(key + " " + subVal.toString() + " " + subDef);
							Definition.check(subDef, subVal, line, column);
						}

					} else {
						throw new DoesNotConformToDefintionException(line, column,
								"Unattended key in map. Expected KEY=" + key + " but it was not present.");
					}
				}
				return true;
			} else {
				throw new DoesNotConformToDefintionException(line, column, "Expected a MAP but was a " + val.getType()
						+ ".");
			}
		} else if (def instanceof TupleDefinition) {
			TupleDefinition tupleDef = (TupleDefinition) def;
			if (val.getType() == ValueType.TUPLE) {
				TupleValue tupleVal = (TupleValue) val;

				if (tupleVal.getValue().size() != tupleDef.length()) {
					throw new DoesNotConformToDefintionException(line, column,
							"Improper number of arguments, Expected " + tupleDef.toString() + " and was "
									+ tupleVal.toStringType() + ".");
				} else {
					for (int index = 0; index < tupleDef.length(); index++) {
						if (tupleDef.getTypeAt(index) != tupleVal.getValue().get(index).getType()) {
							if (tupleDef.getTypeAt(index) == ValueType.DEFINED) {
								// System.out.println("TUPLE DEF " + tupleDef.getDefinitionAt(index));
								Definition.check(tupleDef.getDefinitionAt(index), tupleVal.getValue().get(index), line,
										column);
							} else {
								throw new DoesNotConformToDefintionException(line, column,
										"Expected different tupled expression, Expected " + tupleDef.toString()
												+ " and was " + tupleVal.toStringType() + ".");
							}
						} else {

							if (tupleDef.getTypeAt(index) == ValueType.MAP) {
								// FIGURE OUT WHY WE PUT THIS HERE? IT NEVER GETS CALLED
								System.out.println("HUH1");
								// Do something

							} else if (tupleDef.getTypeAt(index) == ValueType.LIST) {
								// FIGURE OUT WHY WE PUT THIS HERE? IT NEVER GETS CALLED
								System.out.println("HUH2");
								// Do something
							}
						}
					}
				}
				return true;
			} else {
				throw new DoesNotConformToDefintionException(line, column, "Expected a TUPLE but was a "
						+ val.getType() + ".");
			}
		} else if (def instanceof IntDefinition) {
			if (val.getType() == ValueType.LONG) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException(line, column, "Expected a LONG but was a " + val.getType()
						+ ".");
			}
		} else if (def instanceof FloatDefinition) {
			if (val.getType() == ValueType.FLOAT) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException(line, column, "Expected a FLOAT but was a "
						+ val.getType() + ".");
			}
		} else if (def instanceof StringDefinition) {
			if (val.getType() == ValueType.STRING) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException(line, column, "Expected a STRING but was a "
						+ val.getType() + ".");
			}
		} else {
			throw new DoesNotConformToDefintionException(line, column, "Expected " + def + " but was " + val + ".");
		}

	}
}
