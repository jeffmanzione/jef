package com.jeffreymanzione.jef.parsing;

import java.util.Map;

import com.jeffreymanzione.jef.parsing.exceptions.DoesNotConformToDefintionException;
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

	private boolean validated = false;

	public boolean wasValidated() {
		return validated;
	}

	public boolean setValidated(boolean wasValidated) {
		if (validated != wasValidated) {
			validated = wasValidated;
			return true;
		} else {
			return false;
		}
	}

	private static final boolean checkMap(MapDefinition mapDef, Value<?> val) throws DoesNotConformToDefintionException {
		// System.out.println(">>> " + mapDef.getRestriction());
		if (val.getType() == ValueType.MAP) {
			MapValue mapVal = (MapValue) val;

			if (mapDef.isRestricted()) {
				for (Pair<String, ?> p : mapVal) {
					String key = p.getKey();
					Value<?> subVal = p.getValue();

					if (mapDef.hasKey(key)) {
						if (mapDef.getRestriction().equals(mapDef.get(key))) {
							Definition.check(mapDef.get(key), subVal);
						} else {
							throw new DoesNotConformToDefintionException(subVal.getLine(), subVal.getColumn(),
									"The explicit restriction on the map conflicts with the explicit definition "
											+ "prescribed to this key/value pair in the map. value was " + val
											+ ". The map restriction was " + mapDef.getRestriction()
											+ ". The explicit definition for the value was " + mapDef.get(key) + ".");
						}
					} else {
						Definition.check(mapDef.getRestriction(), subVal);

					}
				}

				for (String key : mapDef) {
					if (!mapVal.hasKey(key)) {
						throw new DoesNotConformToDefintionException(mapVal.getLine(), mapVal.getColumn(),
								"Unattended key in map. Expected KEY=" + key + " but it was not present.");

					}
				}
			} else {
				for (String key : mapDef) {
					Definition subDef = mapDef.get(key);
					if (!mapVal.hasKey(key)) {
						throw new DoesNotConformToDefintionException(mapVal.getLine(), mapVal.getColumn(),
								"Unattended key in map. Expected KEY=" + key + " but it was not present.");

					} else {
						Value<?> subVal = mapVal.get(key);
						Definition.check(subDef, subVal);
					}
				}

				for (String key : mapDef) {
					if (!mapVal.hasKey(key)) {
						throw new DoesNotConformToDefintionException(mapVal.getLine(), mapVal.getColumn(),
								"Unattended key in map. Expected KEY=" + key + " but it was not present.");

					}
				}
			}

			return true;
		} else {
			throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(), "Expected a MAP but was a "
					+ val.getType() + ". Value was " + val);
		}
	}

	public static final boolean check(Definition def, Value<?> val) throws DoesNotConformToDefintionException {
		if (def instanceof EnumDefinition) {
			EnumDefinition enumDef = (EnumDefinition) def;
			if (val.getType() == ValueType.ENUM) {
				if (enumDef.contains(val.getValue().toString())) {
					// System.out.println("SUCCESS!!!");
					val.setEntityID(enumDef.getName());
					return true;
				} else {
					throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(),
							"Unexpected enum value. Was '" + val.getValue() + "' but expected one of the following: "
									+ enumDef.toString() + ".");
				}
			} else {
				throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(),
						"Expected a STRING but was a " + val.getType() + ".");
			}
		} else if (def instanceof MapDefinition) {
			MapDefinition mapDef = (MapDefinition) def;
			return checkMap(mapDef, val);
		} else if (def instanceof TupleDefinition) {
			TupleDefinition tupleDef = (TupleDefinition) def;
			if (val.getType() == ValueType.TUPLE) {
				TupleValue tupleVal = (TupleValue) val;

				if (tupleVal.getValue().size() != tupleDef.length()) {
					throw new DoesNotConformToDefintionException(tupleVal.getLine(), tupleVal.getColumn(),
							"Improper number of arguments, Expected " + tupleDef.toString() + " and was "
									+ tupleVal.toStringType() + ".");
				} else {
					for (int index = 0; index < tupleDef.length(); index++) {
						if (tupleDef.getTypeAt(index) != tupleVal.getValue().get(index).getType()) {
							if (tupleDef.getTypeAt(index) == ValueType.DEFINED) {
								// System.out.println("TUPLE DEF " + tupleDef.getDefinitionAt(index));
								Definition.check(tupleDef.getDefinitionAt(index), tupleVal.getValue().get(index));
							} else {
								throw new DoesNotConformToDefintionException(tupleVal.getValue().get(index).getLine(),
										tupleVal.getValue().get(index).getColumn(),
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
					val.setEntityID(def.getName());
				}
				return true;
			} else {
				throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(),
						"Expected a TUPLE but was a " + val.getType() + ".");
			}
		} else if (def instanceof SingletonDefintion) {
			return checkSingleton(def, val);
		} else if (def instanceof ListDefinition) {
			if (val.getType() == ValueType.LIST) {
				ListValue listVal = (ListValue) val;
				for (Value<?> value : listVal) {
					if (!check(((ListDefinition) def).getType(), value)) {
						throw new DoesNotConformToDefintionException(value.getLine(), value.getColumn(), "Expected "
								+ ((ListDefinition) def).getType() + " but was " + value + ".");
					}
				}
				return true;
			} else {
				throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(),
						"Expected a LIST but was a " + val.getType() + ".");
			}
		} else if (def instanceof BuiltInDefinition) {
			val.setEntityID(def.getName());
			((BuiltInDefinition<?>) def).getInnerDefintion().setName(def.getName());
			return check(((BuiltInDefinition<?>) def).getInnerDefintion(), val);
		} else {
			throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(), "Expected " + def
					+ " but was " + val + ".");
		}

	}

	private static boolean checkSingleton(Definition def, Value<?> val) throws DoesNotConformToDefintionException {
		if (def instanceof FloatDefinition) {
			if (val.getType() == ValueType.FLOAT) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(),
						"Expected a FLOAT but was a " + val.getType() + ".");
			}
		} else if (def instanceof StringDefinition) {
			if (val.getType() == ValueType.STRING) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(),
						"Expected a STRING but was a " + val.getType() + ".");
			}
		} else if (def instanceof IntDefinition) {
			if (val.getType() == ValueType.INT) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(),
						"Expected a INT but was a " + val.getType() + ".");
			}
		} else {
			throw new DoesNotConformToDefintionException(val.getLine(), val.getColumn(),
					"Expected a Primitive but was def=" + def.getClass().getSimpleName() + " and va=" + val.getType()
							+ ".");
		}

	}

	public void validate(Map<String, Definition> definitions) {
		if (!wasValidated()) {
			this.validateInnerTypes(definitions);
			this.setValidated(true);
		}
	}

	protected abstract void validateInnerTypes(Map<String, Definition> definitions);

	@Override
	public boolean equals(Object obj) {
		Definition other;
		if (obj instanceof Definition) {
			other = (Definition) obj;
		} else {
			return false;
		}
		return other.getName().equals(this.getName());

	}
}
