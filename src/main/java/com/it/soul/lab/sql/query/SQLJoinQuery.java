package com.it.soul.lab.sql.query;

import java.util.ArrayList;
import java.util.List;

import com.it.soul.lab.sql.query.models.JoinExpression;
import com.it.soul.lab.sql.query.models.Operator;

public class SQLJoinQuery extends SQLQuery {
	
	private QueryType type;
	protected Integer limit = 0;
	protected Integer offset = 0;
	protected List<String> orderByList;
	protected Operator orderOpt = Operator.ASC;

	public SQLJoinQuery() {
		this(SQLQuery.QueryType.INNER_JOIN);
	}

	public SQLJoinQuery(QueryType type) {
		super();
		this.type = type;
	}
	
	@Override
	protected String queryString() throws IllegalArgumentException {
		StringBuffer buffer = new StringBuffer("SELECT ");
		//
		StringBuffer columnBuffer = new StringBuffer();
		Integer count = 0;
		for (JoinTable table : tables) {
			if (count++ != 0) {columnBuffer.append(", ");}
			columnBuffer.append(table.toColumnString().trim());
		}
		if (count > 0) {buffer.append(columnBuffer.toString());}
		//
		buffer.append(" FROM ");
		//
		Integer index = 0;
		StringBuffer joinBuffer = new StringBuffer();
		for (JoinTable table : tables) {
			if(table.joinWith() == null) {break;} //terminate the loop
			if (index++ == 0) {
				//first case
				joinBuffer.append("("+ table.getName() + " " + joinName() + " " + table.joinWith().getName() + table.getExpression().interpret()+ ")");
			}else {
				//every other case
				String lastBuffer = joinBuffer.toString();
				joinBuffer.delete(0, lastBuffer.length());
				joinBuffer.append("("+ lastBuffer + " " + joinName() + " " + table.joinWith().getName() + table.getExpression().interpret() + ")");
			}
		}
		if (index > 0) {buffer.append(joinBuffer.toString());}
		//
		appendOrderBy(buffer, orderByList, orderOpt);
		appendLimit(buffer);
		return buffer.toString();
	}
	
	public void setLimit(Integer limit, Integer offset) {
		this.limit = (limit < 0) ? 0 : limit;
		this.offset = (offset < 0) ? 0 : offset;
	}
	
	protected void appendLimit(StringBuffer pqlBuffer) {
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
	
	public void setJoins(String table, List<String> cols) {
		JoinTable jTable = new JoinTable(table);
		for (String col : cols) {
			jTable.addColumn(col);
		}
		tables.add(jTable);
		if(previousTable != null) {
			JoinExpression cExp = previousTable.getExpression(); 
			cExp.setRightTable(table);
			previousTable.join(jTable);
		}
		previousTable = jTable;
	}
	
	private List<JoinTable> tables = new ArrayList<>();
	
	public class JoinTable{
		private String name = "";
		private List<String> columns = new ArrayList<>();
		private JoinExpression expression;
		private JoinTable tail;
		public JoinTable(String name) {
			this.name = name;
		}
		public JoinTable addColumn(String column) {
			if (columns.contains(column) || column.trim().equals("")) {return this;}
			columns.add(column);
			return this;
		}
		public String toColumnString() {
			StringBuffer buffer = new StringBuffer();
			int count = 0;
			for (String col : columns) {
				if(count++ != 0){buffer.append(", ");}
				buffer.append(name+"."+col);
			}
			//If all passed parameter is empty
			if(count == 0){buffer.append(name+"."+STARIC);}
			return buffer.toString();
		}
		public void setExpression(JoinExpression expression) {
			this.expression = expression;
		}
		public JoinExpression getExpression() {
			return expression;
		}
		public String getName() {
			return name;
		}
		public void join(JoinTable tail) {
			this.tail = tail;
		}
		public JoinTable joinWith() {
			return tail;
		}
	}
	
}
