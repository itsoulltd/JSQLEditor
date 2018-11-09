package com.it.soul.lab.sql.query.models;

public class JoinExpression implements ExpressionInterpreter {
	
	private String leftColumn;
	private String rightColumn;
	private String leftTable;
	private String rightTable;
	
	public JoinExpression(String leftColumn, String rightColumn) {
		this.leftColumn = leftColumn;
		this.rightColumn = rightColumn;
	}

	@Override
	public String interpret() {
		return (" ON " + leftTable  + "." +  leftColumn + " = " + rightTable + "." + rightColumn);
	}

	@Override
	public Expression[] resolveExpressions() {
		return null;
	}

	public void setLeftTable(String leftTable) {
		this.leftTable = leftTable;
	}

	public void setRightTable(String rightTable) {
		this.rightTable = rightTable;
	}

}
