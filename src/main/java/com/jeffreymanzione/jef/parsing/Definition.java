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

	private static final ValidationResponse checkMap(MapDefinition mapDef, Value<?> val) {
		ValidationResponse response = new ValidationResponse();
		// System.out.println(">>> " + mapDef.getRestriction());
		if (val.getType() == ValueType.MAP) {
			MapValue mapVal = (MapValue) val;

			if (mapDef.isRestricted()) {
				for (Pair<String, ?> p : mapVal) {
					String key = p.getKey();
					Value<?> subVal = p.getValue();

					if (mapDef.hasKey(key)) {
						if (mapDef.getRestriction().equals(mapDef.get(key))) {
							response.addResponse(Definition.check(mapDef.get(key), subVal));
						} else {
							response.addException(new DoesNotConformToDefintionException(subVal,
									"The explicit restriction on the map conflicts with the explicit definition "
											+ "prescribed to this key/value pair in the map. value was " + val
											+ ". The map restriction was " + mapDef.getRestriction()
											+ ". The explicit definition for the value was " + mapDef.get(key) + "."));
						}
					} else {
						response.addResponse(Definition.check(mapDef.getRestriction(), subVal));

					}
				}

				for (String key : mapDef) {
					if (!mapVal.hasKey(key)) {
						response.addException(new DoesNotConformToDefintionException(mapVal,
								"Unattended key in map. Expected KEY=" + key + " but it was not present."));

					}
				}
			} else {
				for (String key : mapDef) {
					Definition subDef = mapDef.get(key);
					if (!mapVal.hasKey(key)) {
						response.addException(new DoesNotConformToDefintionException(mapVal,
								"Unattended key in map. Expected KEY=" + key + " but it was not present."));

					} else {
						Value<?> subVal = mapVal.get(key);
						response.addResponse(Definition.check(subDef, subVal));
					}
				}

				for (String key : mapDef) {
					if (!mapVal.hasKey(key)) {
						response.addException(new DoesNotConformToDefintionException(mapVal,
								"Unattended key in map. Expected KEY=" + key + " but it was not present."));

					}
				}
			}
		} else {
			response.addException(new DoesNotConformToDefintionException(val, "Expected a MAP but was a "
					+ val.getType() + ". Value was " + val));
		}
		return response;
	}

	public static final ValidationResponse check(Definition def, Value<?> val) {
		ValidationResponse response = new ValidationResponse();
		if (def instanceof EnumDefinition) {
			EnumDefinition enumDef = (EnumDefinition) def;
			if (val.getType() == ValueType.ENUM) {
				if (enumDef.contains(val.getValue().toString())) {
					// System.out.println("SUCCESS!!!");
					val.setEntityID(enumDef.getName());
				} else {
					response.addException(new DoesNotConformToDefintionException(val, "Unexpected enum value. Was '"
							+ val.getValue() + "' but expected one of the following: " + enumDef.toString() + "."));
				}
			} else {
				response.addException(new DoesNotConformToDefintionException(val, "Expected a STRING but was a "
						+ val.getType() + "."));
			}
		} else if (def instanceof MapDefinition) {
			MapDefinition mapDef = (MapDefinition) def;
			response.addResponse(checkMap(mapDef, val));
		} else if (def instanceof TupleDefinition) {
			TupleDefinition tupleDef = (TupleDefinition) def;
			if (val.getType() == ValueType.TUPLE) {
				TupleValue tupleVal = (TupleValue) val;

				if (tupleVal.getValue().size() != tupleDef.length()) {
					response.addException(new DoesNotConformToDefintionException(tupleVal,
							"Improper number of arguments, Expected " + tupleDef.toString() + " and was "
									+ tupleVal.toStringType() + "."));
				} else {
					for (int index = 0; index < tupleDef.length(); index++) {
						if (tupleDef.getTypeAt(index) != tupleVal.getValue().get(index).getType()) {
							if (tupleDef.getTypeAt(index) == ValueType.DEFINED) {
								// System.out.println("TUPLE DEF " + tupleDef.getDefinitionAt(index));
								response.addResponse(Definition.check(tupleDef.getDefinitionAt(index), tupleVal
										.getValue().get(index)));
							} else {
								response.addException(new DoesNotConformToDefintionException(tupleVal.getValue().get(
										index), "Expected different tupled expression, Expected " + tupleDef.toString()
										+ " and was " + tupleVal.toStringType() + "."));
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
			} else {
				response.addException(new DoesNotConformToDefintionException(val, "Expected a TUPLE but was a "
						+ val.getType() + "."));
			}
		} else if (def instanceof SingletonDefintion) {
			response.addResponse(checkSingleton(def, val));
		} else if (def instanceof ListDefinition) {
			if (val.getType() == ValueType.LIST) {
				ListValue listVal = (ListValue) val;
				for (Value<?> value : listVal) {
					ValidationResponse innerResponse = check(((ListDefinition) def).getType(), value);
					if (innerResponse.hasErrors()) {
						response.addException(new DoesNotConformToDefintionException(value, "Expected "
								+ ((ListDefinition) def).getType() + " but was " + value + "."));
						response.addResponse(innerResponse);
					}
				}
			} else {
				response.addException(new DoesNotConformToDefintionException(val, "Expected a LIST but was a "
						+ val.getType() + "."));
			}
		} else if (def instanceof BuiltInDefinition) {
			val.setEntityID(def.getName());
			((BuiltInDefinition<?>) def).getInnerDefintion().setName(def.getName());
			response.addResponse(check(((BuiltInDefinition<?>) def).getInnerDefintion(), val));
		} else {
			response.addException(new DoesNotConformToDefintionException(val, "Expected " + def + " but was " + val
					+ "."));
		}
		return response;
	}

	private static ValidationResponse checkSingleton(Definition def, Value<?> val) {
		ValidationResponse response = new ValidationResponse();
		if (def instanceof FloatDefinition) {
			if (val.getType() != ValueType.FLOAT) {
				response.addException(new DoesNotConformToDefintionException(val, "Expected a FLOAT but was a "
						+ val.getType() + "."));
			}
		} else if (def instanceof StringDefinition) {
			if (val.getType() != ValueType.STRING) {
				response.addException(new DoesNotConformToDefintionException(val, "Expected a STRING but was a "
						+ val.getType() + "."));
			}
		} else if (def instanceof IntDefinition) {
			if (val.getType() != ValueType.INT) {
				response.addException(new DoesNotConformToDefintionException(val, "Expected a INT but was a "
						+ val.getType() + "."));
			}
		} else {
			response.addException(new DoesNotConformToDefintionException(val, "Expected a Primitive but was def="
					+ def.getClass().getSimpleName() + " and va=" + val.getType() + "."));
		}
		return response;
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
