package com.it.soul.lab.test;

import org.junit.Assert;
import org.junit.Test;

import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.models.AndExpression;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.NotExpression;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.models.OrExpression;
import com.it.soul.lab.sql.query.models.Predicate;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Where;

public class PredicateTest {
	
	private static String SELECT_TARGET = "SELECT * FROM Passenger WHERE  NOT ( ( name = ? AND salary > ? ) OR age = ? )";

	@Test
	public void test() {
		
		Predicate pred = new Where("name")
				.isEqualTo("sohana")
				.and("salary")
				.isGreaterThen(2000.0)
				.or(new Expression("age", Operator.EQUAL))
				.not();
		//System.out.println(pred.interpret());
		
		SQLSelectQuery query = new SQLQuery.Builder(QueryType.SELECT).columns()
									.from("Passenger")
									.where(pred).build();
		System.out.println(query.toString());
		
		Assert.assertEquals(PredicateTest.SELECT_TARGET, query.toString());
	}
	
	@Test
	public void testOnly() {
		
		Predicate pred = new Where("name").isEqualTo("sohana");
		
		SQLSelectQuery query = new SQLQuery.Builder(QueryType.SELECT).columns()
									.from("Passenger")
									.where(pred).build();
		
		System.out.println(query.toString());
		
		ExpressionInterpreter exp = new Expression(new Property("name", "sohana"), Operator.EQUAL);
		
		SQLSelectQuery query2 = new SQLQuery.Builder(QueryType.SELECT).columns()
				.from("Passenger")
				.where(exp).build();
		
		Assert.assertEquals(query.toString(), query2.toString());
	}
	
	@Test
	public void testOld() {
		
		ExpressionInterpreter pred = new AndExpression(new Expression(new Property("name", "sohana"), Operator.EQUAL)
				, new Expression(new Property("salary", "20000.00"), Operator.GREATER_THAN));
		pred = new OrExpression(pred, new Expression("age", Operator.EQUAL));
		pred = new NotExpression(pred);
		//System.out.println(pred.interpret());
		
		SQLSelectQuery query = new SQLQuery.Builder(QueryType.SELECT).columns()
									.from("Passenger")
									.where(pred).build();
		System.out.println(query.toString());
		
		Assert.assertEquals(PredicateTest.SELECT_TARGET, query.toString());
	}

}
