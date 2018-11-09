package com.it.soul.lab.sql.query.models;

public enum Operator {
	EQUAL,
	NOTEQUAL,
	GREATER_THAN,
	GREATER_THAN_OR_EQUAL,
	LESS_THAN,
	LESS_THAN_OR_EQUAL,
	IN,
	NOT_IN,
	LIKE,
	NOT_LIKE,
	ASC,
	DESC;

	public String toString(){

		String eq = "=";
		switch (this) {
		case NOTEQUAL:
			eq = "!=";
			break;
		case GREATER_THAN:
			eq = ">";
			break;
		case GREATER_THAN_OR_EQUAL:
			eq = ">=";
			break;
		case LESS_THAN:
			eq = "<";
			break;
		case LESS_THAN_OR_EQUAL:
			eq = "<=";
			break;
		case IN:
			eq = "IN";
			break;
		case NOT_IN:
			eq = "NOT IN";
			break;
		case LIKE:
			eq = "LIKE";
			break;
		case NOT_LIKE:
			eq = "NOT LIKE";
			break;
		case ASC:
			eq = "ASC";
			break;
		case DESC:
			eq = "DESC";
			break;
		default:
			break;
		}
		return eq;
	}

}
