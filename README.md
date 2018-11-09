## JDBConnector


### JDBConnector has 3 ways of connecting with DataSource:
----
	
    - JDBC Connection URL
    - JDBC Connection Pool (J2EE/Servlet Container using JNDI Naming)
    - Using JPA persistence.xml


#### JDBC Connection URL

##### Creating Connections:
	
    Connection conn = new JDBConnection.Builder("jdbc:mysql://localhost:3306/testDB")
										.driver(DriverClass.MYSQL)
										.credential("root","****")
										.build();
    
##### Closing Connections :
	
    JDBConnection.close(conn);
    
    
#### JDBC Connection Pool (J2EE/Servlet Container using JNDI Naming)

##### First Define a context.xml in /src/main/resources/META-INF/context.xml or /WebContent/META-INF/context.xml
	
    <?xml version="1.0" encoding="UTF-8"?>
	<Context>
    	<Resource auth="Container" 
    		driverClassName="com.mysql.jdbc.Driver" 
    		name="jdbc/testDB" 
    		type="javax.sql.DataSource" 
    		url="jdbc:mysql://localhost:3306/testDB" 
    		username="root" password="****"/>
	</Context>
    
    
 ##### Then Using JNDI Naming
 	
    JDBConnectionPool.configure("java:comp/env/jdbc/testDB");
    Connection conn = JDBConnectionPool.connection();
    
 ##### We can have multiple resource in context.xml, in that case,
 	
    JDBConnectionPool.configure("java:comp/env/jdbc/testDB-1");
    Connection conn = JDBConnectionPool.connection(); //First one get configured as default.
    
    JDBConnectionPool.configure("java:comp/env/jdbc/testDB-2");
    Connection conn = JDBConnectionPool.connection("testDB-2");
    .
    .
    .
    
    
 
 #### Using JPA persistence.xml
 
 	
    ORMController controller = new ORMController("persistence-unit-name");
    EntityManager em = controller.getEntityManager();
    .
    .
    //We may have an entity that represent person_tbl in database
    @Entiry
    @Table(name="person_tbl")
    class Person{
    	@ID
    	private int id;
        @Column
        private String name;
        ...
    }
    
    //Now we can create a service for Person entity.
    ORMService<Person> service = new ORMService(em, Person.class);
    List<Person> all = (List<Person>) service.read();
    //Create a new Person
    Person newOne = new Person();
    newOne.setName("Jack Gyl");
    service.insert(newOne);
    //Update
    newOne.setName("Jack Gyllenhaal");
    service.update(newOne);
    //Delete
    service.delete(newOne);
    //Check exist
    service.exist(newOne.getId()); return true if exist in persistence layer.
    //Total rows
    long count = service.rowCount(); // return number of rows inserted till now.
    
    
    
### JDBConnector has QueryBuilder to create verbose sql statements.
----

#### Select Query Example:
	
    SQLSelectQuery query = new SQLQuery.Builder(QueryType.SELECT)
									.columns()
									.from("Passenger")
									.build();
	
    SQLSelectQuery query = new SQLQuery.Builder(QueryType.SELECT)
							.columns("name", "id")
							.from("Passenger")
							.build();
                            
  	SQLQuery query = new SQLQuery.Builder(QueryType.SELECT)
									.columns("name","age")
									.from("Passenger")
									.whereParams(Logic.OR, "id", "age")
									.build();
                
	Predicate predicate = new Where("id")
							.isEqualTo(229)
							.and("age")
							.isGreaterThenOrEqual(24)
							.or("name")
							.isLike("soha");
    
	SQLQuery query = new SQLQuery.Builder(QueryType.SELECT)
									.columns()
									.from("Passenger")
									.where(predicate)
									.build();
                                    
	System.out.printls(query.toString()); //Will print the SQL statement, that can be used any JDBC implementation.
    
    //Here we have a generic JDBC implementation for executing the SQL Statements.
    
    SQLExecutor exe = new SQLExecutor(conn); //Use connection creating by JDBConnection class
    ResultSet set = exe.executeSelect(query); //take a SQLSelectQuery
    Table table = exe.collection(set);
    List<Passenger> passengers = table.inflate(Passenger.class);
                                    
    
 
 #### Count & Distinct
 	
    Expression comps = new Expression("name", Operator.EQUAL);
    SQLQuery count = new SQLQuery.Builder(QueryType.COUNT)
										.columns().on("Passenger")
										.where(comps)
										.build();
                                        
	SQLQuery distinct = new SQLQuery.Builder(QueryType.DISTINCT)
									.columns().from("Passenger")
									.where(comps)
									.build();
    
  
 #### Insert, Update & Delete
 	
    //Insert
    Row nP = new Row().add("name").add("age").add("sex");
	Property[] values =  (Property[]) nP.getCloneProperties().toArray(new Property[0]);
		
	SQLInsertQuery insert = new SQLQuery.Builder(QueryType.INSERT)
									.into("Passenger")
									.values(values)
									.build();
                                    
	//Update
    ExpressionInterpreter clause = new AndExpression(new Expression("name", Operator.EQUAL)
    											, new Expression("age", Operator.GREATER_THAN));
		
	SQLUpdateQuery update = new SQLQuery.Builder(QueryType.UPDATE)
								.columns("name","age")
								.from("Passenger")
								.where(clause)
								.build();
                                
	//Delete
    SQLQuery delete = new SQLQuery.Builder(QueryType.DELETE)
										.rowsFrom("Passenger")
										.where(clause)
										.build();
    
    
#### OrderBY, GroupBy, Limit, Offset
	
    ExpressionInterpreter clause = new AndExpression(new Expression("name", Operator.EQUAL)
    											, new Expression("age", Operator.GREATER_THAN));
                    
	//ORderBy
	SQLQuery query = new SQLQuery.Builder(QueryType.SELECT)
							.columns()
							.from("Passenger")
							.where(clause)
							.orderBy("id")
							.addLimit(10, 20)
							.build();
                            
    
    //GroupBy
    String groupByColumn = ScalerType.COUNT.toAlias("age");
	SQLQuery query = new SQLQuery.Builder(QueryType.SELECT)
							.columns("name",groupByColumn)
							.from("Passenger")
							.groupBy("name")
							.having(new Expression(groupByColumn, Operator.GREATER_THAN))
							.orderBy(groupByColumn)
							.build();
    
    
#### Joins
	
    SQLJoinQuery join = new SQLQuery.Builder(QueryType.INNER_JOIN)
							.join("Customers", "CustomerName")
							.on(new JoinExpression("CustomerID", "CustomerID"))
							.join("Orders", "OrderID")
							.on(new JoinExpression("ShipperID", "ShipperID"))
							.join("Shippers", "ShipperName").build();
                            
	
    SQLJoinQuery leftJoin = new SQLQuery.Builder(QueryType.LEFT_JOIN)
							.join("Customers", "CustomerName")
							.on(new JoinExpression("CustomerID", "CustomerID"))
							.join("Orders", "OrderID")
							.orderBy("Customers.CustomerName").build();
    
 
### JDBConnector has built-in Entity class (light weight), only required a JDBC Connection.
----
	
#### Here are detailed example:


##### First assume We Have Person.java class that extends from Entity class

	
    @TableName(value = "Person", acceptAll = false)
	public class Person extends Entity {
		@Property
		@PrimaryKey(name = "uuid", autoIncrement = false)
		private String uuid;
        public String getUuid() {
            return uuid;
        }
        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
        @Property(defaultValue="Mr/Mrs")
        private String name;

        @Property(defaultValue="34", type = DataType.INT)
        private Integer age;

        @Property(defaultValue="true", type = DataType.BOOL)
        private Boolean isActive;

        @Property(defaultValue="0.00", type = DataType.DOUBLE)
        private Double salary;

        private Date dob;

        @Property(defaultValue="2010-06-21 21:01:01", type=DataType.SQLTIMESTAMP, parseFormat="yyyy-MM-dd HH:mm:ss")
        private Timestamp createDate;

        private Float height;

        @Property(defaultValue="2010-06-21" , type=DataType.SQLDATE, parseFormat="yyyy-MM-dd")
        private Date dobDate;

        @Property(defaultValue="21:01:01" , type=DataType.SQLTIMESTAMP, parseFormat="HH:mm:ss")
        private Timestamp createTime;

        public Person() {
            super();
        }
        ...... Setter & Getters ........
    }
    

##### How to work with Person.java entiry.
	
    Connection conn = new JDBConnection.Builder("jdbc:mysql://localhost:3306/testDB")
										.driver(DriverClass.MYSQL)
										.credential("root","****")
										.build();
                                        
	SQLExecutor exe = new SQLExecutor(conn);
    
    //Insert
    Person person = new Person();
	person.setUuid(UUID.randomUUID().toString());
	person.setName(getRandomName());
    Boolean isInserted = person.insert(exe);
    
    //Update
    person.setAge(getRandomAge());
	person.setIsActive(false);
	person.setSalary(200.00);
	person.setDob(new Date(Calendar.getInstance().getTimeInMillis()));
    Boolean isUpdated = person.update(exe, "age","isActive","salary","dob");
    
    //Delete
    Boolean isDeleted = person.delete(exe);
    
    //Read All
    ExpressionInterpreter clause = new Expression(new Property("name", "Jake"), Operator.EQUAL);
	List<Person> sons = Person.read(Person.class, exe, clause); //if clause is null the return all.
    
    
    
####

### Questions?
-------------
##### Send your query to us: contact@itsoulltd.com
##### http://www.itsoulltd.com/

### Author
----
#### Towhidul Islam (m.towhid.islam@gmail.com)

### License
----
#### JDBConnector is available under the MIT license.
 
 
 
 
 

