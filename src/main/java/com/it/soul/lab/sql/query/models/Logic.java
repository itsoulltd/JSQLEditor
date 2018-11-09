package com.it.soul.lab.sql.query.models;

public enum Logic {
	AND,
	OR;
	
	public String toString() {
		String val = "&&";
		switch (this) {
		case OR:
			val = "||";
			break;
		default:
			break;
		}
		return val;
	}
}
