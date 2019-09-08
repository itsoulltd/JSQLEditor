package com.it.soul.lab.sql;

import org.junit.Assert;
import org.junit.Test;

import com.it.soul.lab.jpql.query.JPQLQuery;
import com.it.soul.lab.jpql.query.JPQLSelectQuery;
import com.it.soul.lab.jpql.query.JPQLUpdateQuery;
import com.it.soul.lab.sql.query.SQLJoinQuery;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLScalarQuery;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.models.AndExpression;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.JoinExpression;
import com.it.soul.lab.sql.query.models.OrExpression;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.ScalarType;
import com.it.soul.lab.sql.query.models.Property;

public class QueryBuilderTest {
	
	private static String SELECT_ALL = "SELECT * FROM Passenger";
	private static String SELECT_NAME_ID = "SELECT name, id FROM Passenger";
	private static String SELECT_WHERE_OR = "SELECT name, age FROM Passenger WHERE id = ? OR age = ?";
	private static String SELECT_WHERE = "SELECT name, age FROM Passenger WHERE ( name LIKE ? OR ( id = ? AND age >= ? ) )";
	
	private static String COUNT_VALUE = "SELECT COUNT(id) FROM Passenger WHERE name = 'sohana'";
	private static String COUNT_WHERE = "SELECT COUNT(*) FROM Passenger WHERE name = ?";
	
	private static String DISTINCT_VALUE = "SELECT DISTINCT name FROM Passenger";
	private static String DISTINCT_WHERE = "SELECT DISTINCT * FROM Passenger WHERE name = ?";
	
	private static String INSERT_INTO = "INSERT INTO Passenger ( name, age, sex) VALUES ( ?, ?, ?)";
	
	private static String UPDATE = "UPDATE Passenger SET name = ?, age = ? WHERE ( name = ? AND age > ? )";
	
	private static String DELETE = "DELETE FROM Passenger WHERE ( name = ? AND age > ? )";
	
	private static String JPQL_SELECT = "SELECT e.name, e.age, e.sex FROM Passenger e WHERE ( e.name = :name AND e.age > :age )";
	
	private static String JPQL_UPDATE = "UPDATE Passenger e SET e.name = :name, e.age = :age, e.sex = :sex WHERE ( e.name = :name AND e.age > :age )";
	
	@Test
	public void selectAllTest() {
		
		SQLQuery query = new SQLQuery.Builder(QueryType.SELECT)
									.columns()
									.from("Passenger")
									.build();
		
		Assert.assertEquals(SELECT_ALL
				, query.toString());
	}
	
	@Test
	public void selectName_IDTest(){
		SQLQuery qu2 = new SQLQuery.Builder(QueryType.SELECT)
							.columns("name", "id")
							.from("Passenger")
							.build();
		Assert.assertEquals(SELECT_NAME_ID, qu2.toString());
	}
	
	@Test
	public void select_Where_test(){
		
		SQLQuery qu5 = new SQLQuery.Builder(QueryType.SELECT)
									.columns("name","age")
									.from("Passenger")
									.whereParams(Logic.OR, "id", "age")
									.build();
		
		Assert.assertEquals(SELECT_WHERE_OR, qu5.toString());
		
	}
	
	@Test
	public void select_where_expressionTest(){
		
		ExpressionInterpreter andExp = new AndExpression(new Expression(new Property("id", "kajalrer"), Operator.EQUAL), new Expression(new Property("age", 18), Operator.GREATER_THAN_OR_EQUAL));
		ExpressionInterpreter orExp = new OrExpression(new Expression(new Property("name", "batil"), Operator.LIKE), andExp);
		
		SQLSelectQuery qu6 = new SQLQuery.Builder(QueryType.SELECT)
									.columns("name","age")
									.from("Passenger")
									.where(orExp)
									.build();
		
		Assert.assertEquals(SELECT_WHERE, qu6.toString());
	}
	
	@Test
	public void countTest(){
		Property prop = new Property("name", "sohana");
		Expression comps = new Expression(new Property("name", "batil"), Operator.EQUAL);
		
		SQLScalarQuery count = new SQLQuery.Builder(QueryType.COUNT)
										.columns("id")
										.on("Passenger")
										.scalarClause(prop, comps)
										.build();
		Assert.assertEquals(COUNT_VALUE, count.toString());
		
		SQLQuery count2 = new SQLQuery.Builder(QueryType.COUNT)
										.columns().on("Passenger")
										.where(comps)
										.build();
		Assert.assertEquals(COUNT_WHERE, count2.toString());
	}
	
	@Test 
	public void distinctTest(){
		
		Expression comps = new Expression(new Property("name", "batil"), Operator.EQUAL);
		
		SQLQuery distinct = new SQLQuery.Builder(QueryType.DISTINCT)
										.columns("name")
										.from("Passenger")
										.build();
		Assert.assertEquals(DISTINCT_VALUE, distinct.toString());
		
		SQLQuery count2 = new SQLQuery.Builder(QueryType.DISTINCT)
									.columns().from("Passenger")
									.where(comps)
									.build();
		Assert.assertEquals(DISTINCT_WHERE, count2.toString());
		
	}
	
	@Test
	public void insertTest(){
		
		Row nP = new Row().add("name").add("age").add("sex");
		Property[] values =  (Property[]) nP.getCloneProperties().toArray(new Property[0]);
		
		SQLQuery insert = new SQLQuery.Builder(QueryType.INSERT)
									.into("Passenger")
									.values(values)
									.build();
		Assert.assertEquals(INSERT_INTO, insert.toString());
		
	}
	
	@Test
	public void updateTest(){
		
		ExpressionInterpreter andExpression = new AndExpression(new Expression(new Property("name", "batil"), Operator.EQUAL), new Expression(new Property("age", 18), Operator.GREATER_THAN));
		
		SQLQuery update = new SQLQuery.Builder(QueryType.UPDATE)
								.columns("name","age")
								.from("Passenger")
								.where(andExpression)
								.build();
		
		Assert.assertEquals(UPDATE, update.toString());
		
	}
	
	@Test
	public void deleteTest(){
		
		ExpressionInterpreter andExpression = new AndExpression(new Expression(new Property("name", "batil"), Operator.EQUAL), new Expression(new Property("age", 18), Operator.GREATER_THAN));
		
		SQLQuery delete = new SQLQuery.Builder(QueryType.DELETE)
										.rowsFrom("Passenger")
										.where(andExpression)
										.build();
		
		Assert.assertEquals(DELETE, delete.toString());
		
	}
	
	@Test
	public void jpqlTest(){
		
		ExpressionInterpreter andExpression = new AndExpression(new Expression(new Property("name", "batil"), Operator.EQUAL), new Expression(new Property("age", 18), Operator.GREATER_THAN));
		
		JPQLSelectQuery jqpSel = new JPQLQuery.Builder(QueryType.SELECT)
											.columns("name","age","sex")
											.from("Passenger")
											.where(andExpression)
											.build();
		Assert.assertEquals(JPQL_SELECT, jqpSel.toString());
		
		JPQLUpdateQuery jpqlUp = new JPQLQuery.Builder(QueryType.UPDATE)
											.columns("name", "age", "sex")
											.from("Passenger")
											.where(andExpression)
											.build();
		Assert.assertEquals(JPQL_UPDATE, jpqlUp.toString());
	}
	
	@Test public void OrderByTest() {
		SQLQuery qu5 = new SQLQuery.Builder(QueryType.SELECT)
				.columns("name","age")
				.from("Passenger")
				.whereParams(Logic.OR, "id", "age")
				.orderBy("id")
				.addLimit(10, 20)
				.build();

		Assert.assertEquals("SELECT name, age FROM Passenger WHERE id = ? OR age = ? ORDER BY id ASC LIMIT 10 OFFSET 20", qu5.toString());
		
		SQLQuery qu8 = new SQLQuery.Builder(QueryType.SELECT)
				.columns("name","age")
				.from("Passenger")
				.whereParams(Logic.OR, "id", "age")
				.orderBy()
				.addLimit(-1, 0)
				.build();

		Assert.assertEquals("SELECT name, age FROM Passenger WHERE id = ? OR age = ?", qu8.toString());
		
		SQLQuery qu6 = new SQLQuery.Builder(QueryType.SELECT)
				.columns("name","age")
				.from("Passenger")
				.whereParams(Logic.OR, "id", "age")
				.orderBy("id")
				.addLimit(10, 0)
				.build();

		Assert.assertEquals("SELECT name, age FROM Passenger WHERE id = ? OR age = ? ORDER BY id ASC LIMIT 10", qu6.toString());
		
		SQLQuery qu7 = new SQLQuery.Builder(QueryType.SELECT)
				.columns("name","age")
				.from("Passenger")
				.whereParams(Logic.OR, "id", "age")
				.orderBy("id")
				.addLimit(-1, 0)
				.build();

		Assert.assertEquals("SELECT name, age FROM Passenger WHERE id = ? OR age = ? ORDER BY id ASC", qu7.toString());
		
		SQLQuery qu9 = new SQLQuery.Builder(QueryType.SELECT)
				.columns("name","age")
				.from("Passenger")
				.addLimit(10, 0)
				.build();

		Assert.assertEquals("SELECT name, age FROM Passenger  LIMIT 10", qu9.toString());
		
		SQLQuery qu10 = new SQLQuery.Builder(QueryType.SELECT)
				.columns("name","age")
				.from("Passenger")
				.orderBy("id")
				.build();

		Assert.assertEquals("SELECT name, age FROM Passenger  ORDER BY id ASC", qu10.toString());
		
	}
	
	@Test public void GroupBy() {
		
		SQLQuery qu11 = new SQLQuery.Builder(QueryType.SELECT)
				.columns("name",ScalarType.COUNT.toAlias("age"))
				.from("Passenger")
				.groupBy("name")
				.orderBy(ScalarType.COUNT.toString("age"))
				.build();

		Assert.assertEquals("SELECT name, COUNT(age) AS count_age FROM Passenger  GROUP BY name ORDER BY COUNT(age) ASC", qu11.toString());
		
		SQLQuery qu12 = new SQLQuery.Builder(QueryType.SELECT)
				.columns("name",ScalarType.COUNT.toAlias("age"))
				.from("Passenger")
				.groupBy("name")
				.having(new Expression(new Property(ScalarType.COUNT.toString("age"), ""), Operator.GREATER_THAN))
				.orderBy(ScalarType.COUNT.toString("age"))
				.build();

		Assert.assertEquals("SELECT name, COUNT(age) AS count_age FROM Passenger  GROUP BY name HAVING COUNT(age) > ? ORDER BY COUNT(age) ASC", qu12.toString());
		
	}
	
	@Test public void JoinTest() {
		SQLJoinQuery join = new SQLQuery.Builder(QueryType.INNER_JOIN)
				.join("Customers", "CustomerName")
				.on(new JoinExpression("CustomerID", "CustomerID"))
				.join("Orders", "OrderID")
				.on(new JoinExpression("ShipperID", "ShipperID"))
				.join("Shippers", "ShipperName").build();
		
		String expected = 	"SELECT Customers.CustomerName, Orders.OrderID, Shippers.ShipperName " + 
							"FROM ((Customers " + 
							"INNER JOIN Orders ON Customers.CustomerID = Orders.CustomerID) " + 
							"INNER JOIN Shippers ON Orders.ShipperID = Shippers.ShipperID)";
		
		Assert.assertEquals(expected, join.toString());
	}
	
	@Test public void LeftJoinTest() {
		SQLJoinQuery join = new SQLQuery.Builder(QueryType.LEFT_JOIN)
				.join("Customers", "CustomerName")
				.on(new JoinExpression("CustomerID", "CustomerID"))
				.join("Orders", "OrderID")
				.orderBy("Customers.CustomerName").build();
		
		String expected = 	"SELECT Customers.CustomerName, Orders.OrderID " + 
							"FROM (Customers " + 
							"LEFT JOIN Orders " + 
							"ON Customers.CustomerID = Orders.CustomerID) " + 
							"ORDER BY Customers.CustomerName ASC";
		
		Assert.assertEquals(expected, join.toString());
	}

	@Test
	public void indexQuery(){
		//CREATE INDEX index_name
		//ON table_name (column1, column2, ...);

		SQLQuery query = new SQLQuery.Builder(QueryType.CREATE)
				.index("idx_name")
				.columns("column1", "column2")
				.on("table_name")
				.build();


		//CREATE UNIQUE INDEX index_name
		//ON table_name (column1, column2, ...);

		SQLQuery unquery = new SQLQuery.Builder(QueryType.CREATE)
				.uniqueIndex("idx_name")
				.columns("column1", "column2")
				.on("table_name")
				.build();
	}
	
}
