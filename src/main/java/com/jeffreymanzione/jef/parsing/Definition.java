package com.jeffreymanzione.jef.parsing;

import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.StringValue;
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
		return name;
	}

	public static final boolean check(Definition def, Value<?> val) throws DoesNotConformToDefintionException {
		if (def instanceof EnumDefinition) {
			EnumDefinition enumDef = (EnumDefinition) def;
			if (val.getType() == ValueType.STRING) {
				if (enumDef.contains(((StringValue) val).getValue())) {
					// System.out.println("SUCCESS!!!");
					return true;
				} else {
					throw new DoesNotConformToDefintionException("Unexpected enum value. Was '"
							+ ((StringValue) val).getValue() + "' but expected one of the following: "
							+ enumDef.toString() + ".");
				}
			} else {
				throw new DoesNotConformToDefintionException("Expected a STRING but was a " + val.getType() + ".");
			}
		} else if (def instanceof MapDefinition) {
			MapDefinition mapDef = (MapDefinition) def;
			if (val.getType() == ValueType.MAP) {
				MapValue mapVal = (MapValue) val;
				//System.out.println("MAP=" + mapVal);
				for (String key : mapDef) {
					Value<?> subVal = mapVal.get(key);
					if (subVal != null) {
						//System.out.println(key + " " + subVal.toString());
						Definition subDef = mapDef.get(key);
						Definition.check(subDef, subVal);
					} else {
						throw new DoesNotConformToDefintionException("Unattended key in map. Expected KEY=" + key
								+ " but it was not present.");
					}
				}
				return true;
			} else {
				throw new DoesNotConformToDefintionException("Expected a MAP but was a " + val.getType() + ".");
			}
		} else if (def instanceof TupleDefinition) {
			TupleDefinition tupleDef = (TupleDefinition) def;
			if (val.getType() == ValueType.TUPLE) {
				TupleValue tupleVal = (TupleValue) val;

				for (int index = 0; index < tupleDef.length(); index++) {
					if (tupleVal.getValue().size() <= index) {
						throw new DoesNotConformToDefintionException("Improper number of arguments, " + val.getType()
								+ ".");
					} else if (tupleDef.getTypeAt(index) != tupleVal.getValue().get(index).getType()) {
						throw new DoesNotConformToDefintionException("Expected different tupled expression, "
								+ tupleDef.toString() + " and " + tupleVal.toStringType() + ".");
					} else {
						if (tupleDef.getTypeAt(index) == ValueType.MAP) {
							// Do something
						} else if (tupleDef.getTypeAt(index) == ValueType.LIST) {
							// Do something
						}
					}
				}
				return true;
			} else {
				throw new DoesNotConformToDefintionException("Expected a TUPLE but was a " + val.getType() + ".");
			}
		} else if (def instanceof IntDefinition) {
			if (val.getType() == ValueType.LONG) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException("Expected a LONG but was a " + val.getType() + ".");
			}
		} else if (def instanceof FloatDefinition) {
			if (val.getType() == ValueType.FLOAT) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException("Expected a FLOAT but was a " + val.getType() + ".");
			}
		} else if (def instanceof StringDefinition) {
			if (val.getType() == ValueType.STRING) {
				return true;
			} else {
				throw new DoesNotConformToDefintionException("Expected a STRING but was a " + val.getType() + ".");
			}
		} else {
			throw new DoesNotConformToDefintionException("Expected a data structure type. " + def + " " + val);
		}

	}
}
