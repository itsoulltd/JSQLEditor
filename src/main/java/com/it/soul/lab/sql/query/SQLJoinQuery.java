package com.it.soul.lab.sql.query;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.JoinExpression;
import com.it.soul.lab.sql.query.models.Operator;

import java.util.ArrayList;
import java.util.List;

public class SQLJoinQuery extends SQLSelectQuery {
	
	private QueryType type;
	protected Integer limit = 0;
	protected Integer offset = 0;
	protected Operator orderOpt = Operator.ASC;

	public SQLJoinQuery() {
		this(QueryType.INNER_JOIN);
	}

	public SQLJoinQuery(QueryType type) {
		super();
		this.type = type;
	}
	
	@Override
	protected String queryString(DriverClass dialect) throws IllegalArgumentException {
		StringBuffer buffer = new StringBuffer("SELECT ");
		//
		StringBuffer columnBuffer = new StringBuffer();
		Integer count = 0;
		for (JoinTable table : tables) {
			String cols = table.toColumnString().trim();
			if (!cols.isEmpty()){
				if (count++ != 0) {columnBuffer.append(", ");}
				columnBuffer.append(cols);
			}
		}
		if (count > 0) {buffer.append(columnBuffer.toString());}
		//
		buffer.append(" FROM ");
		//
		Integer index = 0;
		StringBuffer joinBuffer = new StringBuffer();
		for (JoinTable table : tables) {
			if(table.joinWith() == null) {continue;} //terminate the loop
			if (index++ == 0) {
				//first case
				//joinBuffer.append("("+ table.getName() + " " + joinName() + " " + table.joinWith().getName() + table.getExpression().interpret() + ")");
				joinBuffer.append( table.getAsAlice() + " " + joinName() + " " + table.joinWith().getAsAlice() + table.getExpression().interpret() );
			}else {
				//every other case
				String lastBuffer = joinBuffer.toString();
				joinBuffer.delete(0, lastBuffer.length());
				//joinBuffer.append("("+ lastBuffer + " " + joinName() + " " + table.joinWith().getName() + table.getExpression().interpret() + ")");
				joinBuffer.append( lastBuffer + " " + joinName() + " " + table.joinWith().getAsAlice() + table.getExpression().interpret() );
			}
		}
		if (index > 0) {buffer.append(joinBuffer.toString());}
		//
		appendWhere(buffer);
		appendGroupBy(buffer, groupByList);
		appendHaving(buffer);
		appendOrderBy(buffer, orderByList, orderOpt);
		appendLimit(buffer, dialect);
		return buffer.toString();
	}

	private void appendHaving(StringBuffer buffer) {
		if(havingInterpreter != null)
			buffer.append(" HAVING " + havingInterpreter.interpret());
	}

	@SuppressWarnings("Duplicates")
	private void appendGroupBy(StringBuffer buffer, List<String> columns) {
		if (columns != null && columns.size() > 0) {
			StringBuffer groupBuffer = new StringBuffer(" GROUP BY ");
			int count = 0;
			for(String col : columns) {
				if(col.trim().equals("")){continue;}
				if(count++ != 0){groupBuffer.append(", ");}
				if(quantifierEnabled()){groupBuffer.append(getQuantifier() + "." + col);}
				else {groupBuffer.append(col);}
			}
			if (count > 0) {
				buffer.append(groupBuffer.toString());
			}
		}
	}

	private void appendWhere(StringBuffer buffer) {
		ExpressionInterpreter interpreter = super.getWhereExpression();
		if(interpreter != null)
			buffer.append(" WHERE " + interpreter.interpret());
	}

	public void setLimit(Integer limit, Integer offset) {
		this.limit = (limit < 0) ? 0 : limit;
		this.offset = (offset < 0) ? 0 : offset;
	}
	
	protected void appendLimit(StringBuffer pqlBuffer, DriverClass dialect) {
		//orc.query.limit.format=OFFSET %s ROWS FETCH NEXT %s ROWS ONLY
		//orc.query.limit.format=LIMIT %s OFFSET %s
		if (limit > 0) { 
			pqlBuffer.append(" LIMIT " + limit) ;
			if (offset > 0) { pqlBuffer.append(" OFFSET " + offset) ;}
		}
	}
	
	public void setOrderBy(List<String> columns, Operator opt) {
		orderByList = columns;
		orderOpt = opt;
	}
	
	protected void appendOrderBy(StringBuffer pqlBuffer, List<String> columns, Operator opt) {
		if (columns != null && columns.size() > 0) {
			StringBuffer orderBuffer = new StringBuffer(" ORDER BY ");
			int count = 0;
			for(String col : columns) {
				if(col.trim().equals("")){continue;}
				if(count++ != 0){pqlBuffer.append(", ");}
				orderBuffer.append(col);
			}
			if (count > 0) {
				if (opt != null) {orderBuffer.append(" " + opt.toString());}
				pqlBuffer.append(orderBuffer.toString());
			}
		}
	}

	public void setGroupBy(List<String> columns) {
		this.groupByList = columns;
	}

	public void setHavingExpression(ExpressionInterpreter interpreter) {
		this.havingInterpreter = interpreter;
	}

	@Override
	public void setWhereExpression(ExpressionInterpreter whereExpression) {
		super.setWhereExpression(whereExpression);
	}
	
	private String joinName() {
		String name;
		switch (type) {
		case FULL_JOIN:
			name = "FULL OUTER JOIN";
			break;
		case LEFT_JOIN:
			name = "LEFT JOIN";
			break;
		case RIGHT_JOIN:
			name = "RIGHT JOIN";
			break;
		default:
			name = "INNER JOIN";
			break;
		}
		return name;
	}
	
	private JoinTable previousTable;
	
	public void setJoinExpression(JoinExpression exp) {
		if(previousTable != null) {
			previousTable.setExpression(exp);
			exp.setLeftTable(previousTable.getName());
		}
	}
	
	public void setJoins(String table, String alice, List<String> cols) {
		JoinTable jTable = new JoinTable(table, alice);
		for (String col : cols) {
			jTable.addColumn(col);
		}
		tables.add(jTable);
		if(previousTable != null) {
			JoinExpression cExp = previousTable.getExpression(); 
			cExp.setRightTable(jTable.getName());
			previousTable.join(jTable);
		}
		previousTable = jTable;
	}

	public void setReJoins(String reJoins) {
		if (reJoins == null || reJoins.isEmpty()) return;
		if (previousTable != null && previousTable.matchName(reJoins))
			return;
		for (JoinTable sTable : tables) {
			if (sTable.matchName(reJoins)){
				JoinTable jTable = new JoinTable(reJoins, sTable.getAlice());
				jTable.skipColumns();
				tables.add(jTable);
				previousTable = jTable;
				break;
			}
		}
	}
	
	private List<JoinTable> tables = new ArrayList<>();
	
	public class JoinTable{
		private String name = "";
		private String alice = "";
		private List<String> columns = new ArrayList<>();
		private JoinExpression expression;
		private JoinTable tail;

		public JoinTable(String name, String alice) {
			this.name = name;
			if(alice != null) this.alice = alice.trim();
		}
		public JoinTable addColumn(String column) {
			if (columns == null) columns = new ArrayList<>();
			if (columns.contains(column) || column.trim().equals("")) {return this;}
			columns.add(column);
			return this;
		}
		public String toColumnString() {
			if (columns == null) return "";
			StringBuffer buffer = new StringBuffer();
			int count = 0;
			for (String col : columns) {
				if(count++ != 0){buffer.append(", ");}
				buffer.append(getName()+"."+col);
			}
			//If all passed parameter is empty
			if(count == 0){buffer.append(getName()+"."+STARIC);}
			return buffer.toString();
		}
		public void setExpression(JoinExpression expression) {
			this.expression = expression;
		}
		public JoinExpression getExpression() {
			return expression;
		}
		public String getName() {
			return (alice != null && !alice.isEmpty()) ? alice : name;
		}
		public String getAsAlice() {
			return (alice != null && !alice.isEmpty()) ? (name + " AS " + alice) : name;
		}
		public void join(JoinTable tail) {
			this.tail = tail;
		}
		public JoinTable joinWith() {
			return tail;
		}
		public void skipColumns() {
			columns = null;
		}
		public boolean matchName(String reJoins) {
			if (reJoins == null) return false;
			return name.trim().equalsIgnoreCase(reJoins.trim());
		}
		public String getAlice() {
			return alice;
		}
	}
	
}
