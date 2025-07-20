## JSQLEditor
[![](https://jitpack.io/v/itsoulltd/JSQLEditor.svg)](https://jitpack.io/#itsoulltd/JSQLEditor)
### SetUp
       
       Step 1. Add the JitPack repository to your build file
        <repositories>
            <repository>
                <id>jitpack.io</id>
                <url>https://jitpack.io</url>
            </repository>
        </repositories>
        
        Step 2. Add the dependency
        <dependency>
            <groupId>com.github.itsoulltd</groupId>
            <artifactId>JSQLEditor</artifactId>
            <version>v1.1.3-RELEASE</version>
        </dependency>


### JSQLEditor has 3 ways of connecting with DataSource:
----
	
    - JDBC Connection URL
    - JDBC Connection Pool (J2EE/Servlet Container using JNDI Naming)
    - Using JPA persistence.xml


#### JDBC Connection URL

##### Creating Connections:
	
    Connection conn = new JDBConnection.Builder(DriverClass.MYSQL)
                      		.host("localhost", "3306")
                      		.database("testDB")
                      		.credential("root","towhid")
                      		.build();
    
##### Closing Connections :
	
    JDBConnection.close(conn);
    
    
#### JDBC Connection Pool (J2EE/Servlet Container using JNDI Naming)

##### First Define a context.xml in /src/main/webapp/META-INF/context.xml (:Maven-Project Structure) OR /WebContent/META-INF/context.xml (:J2EE Webapp Structure in Eclipse)
	
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
 
 ##### First Define a persistence.xml in /src/main/resources/META-INF/persistence.xml OR /src/META-INF/persistence.xml
    
    <?xml version="1.0" encoding="UTF-8"?>
    <persistence version="2.1" xmlns="http://xmlns.jcp.org/xml/ns/persistence"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence
            http://xmlns.jcp.org/xml/ns/persistence/persistence_2_1.xsd">
        <persistence-unit name="testDB">
            <!-- <provider>org.hibernate.ejb.HibernatePersistence</provider> -->
            <properties>
                 <property name="hibernate.connection.driver_class" value="com.mysql.jdbc.Driver"/>
                 <property name="hibernate.connection.username" value="root"/>
                 <property name="hibernate.connection.password" value="****"/>
                 <property name="hibernate.connection.url" value="jdbc:mysql://localhost:3306/testDB?useUnicode=true&amp;useJDBCCompliantTimezoneShift=true&amp;useLegacyDatetimeCode=false&amp;serverTimezone=UTC"/>
                 <property name="hibernate.dialect" value="org.hibernate.dialect.MySQL5Dialect"/>
                 <!--<property name="hibernate.archive.autodetection" value="class"/>-->
                 <property name="hibernate.show_sql" value="true"/>
                 <property name="hibernate.format_sql" value="true"/>
                 <property name="hibernate.hbm2ddl.auto" value="create"/>
            </properties>
       </persistence-unit>
    </persistence>
 
 ##### Sample Code:
 
 	
    ORMController controller = new ORMController("persistence-unit-name");
    EntityManager em = controller.getEntityManager();
    .
    .
    //We may have an entity that represent person_tbl in database
    @Entity
    @Table(name="person_tbl")
    public class Person{
    
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
    
    
    
### JSQLEditor has QueryBuilder to create verbose sql statements.
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
    Row nP = new Row()
    	.add("name","Peter Thiel")
    	.add("age", 51);
    	
    Property[] values =  nP.getCloneProperties().toArray(new Property[0]);
		
	SQLInsertQuery insert = new SQLQuery.Builder(QueryType.INSERT)
					.into("Passenger")
					.values(values)
					.build();
                                    
	//Update
	
    Predicate compareWith = new Where("id").isEqualTo(autoId);
		
	SQLUpdateQuery update = new SQLQuery.Builder(QueryType.UPDATE)
                            	.set(values)
                            	.from("Passenger")
                            	.where(compareWith)
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
    
 
### JSQLEditor has built-in Entity class (light weight), only required a JDBC Connection.
----
	
#### Here are detailed example:


##### First assume We Have Person.java class that extends from Entity class

	
    @TableName(value = "Person", acceptAll = false)
	public class Person extends Entity {
		
	    //Must have to be same as declaired property and as name in @PrimaryKey
	    @PrimaryKey(name = "uuid", autoIncrement = false)
	    private String uuid;
        
        @Column(defaultValue="Mr/Mrs")
        private String name;

        @Column(defaultValue="34", type = DataType.INT)
        private Integer age;

        @Column(defaultValue="true", type = DataType.BOOL)
        private Boolean active;

        @Column(defaultValue="0.00", type = DataType.DOUBLE)
        private Double salary;

        private Date dob;

        @Column(defaultValue="2010-06-21 21:01:01", type=DataType.SQLTIMESTAMP, parseFormat="yyyy-MM-dd HH:mm:ss")
        private Timestamp createDate;

        private Float height;

        @Column(defaultValue="2010-06-21" , type=DataType.SQLDATE, parseFormat="yyyy-MM-dd")
        private Date dobDate;

        @Column(defaultValue="21:01:01" , type=DataType.SQLTIMESTAMP, parseFormat="HH:mm:ss")
        private Timestamp createTime;

        public Person() {
            super();
        }
        
        /////////// Setter & Getters //////////
        
        public String getUuid() {
            return uuid;
        }
        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
        .....
        
    }
    

##### How to work with Person.java entity.
	
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
    Boolean isUpdated = person.update(exe, "age","active","salary","dob");
    
    //Delete
    Boolean isDeleted = person.delete(exe);
    
    //Read All
    //ExpressionInterpreter clause = new Expression(new Property("name", "Jake"), Operator.EQUAL);
    Predicate clause = new Where("name").isEqualTo("Jake");
	List<Person> sons = Person.read(Person.class, exe, clause); //if clause is null then return all.
    
    
    
####
##### JSQLEditor has support for Cassandra CQL!
    
    //First declare a entity call with annotation and base class 
    //CQLEntity.java
    
    @TableName(value = "order_event")
    public class OrderEvent extends CQLEntity {
    
        @PrimaryKey(name = "track_id")
        private String trackID; //Partitioning ID
        @PrimaryKey(name = "user_id")
        private String userID; //Partitioning ID
    
        @ClusteringKey(name = "uuid", type = DataType.UUID)
        private UUID uuid; //Clustering ID
    
        private Date timestamp = new Date();
        private Map<String, String> kvm;
        private Map<String, Integer> kvm2;
    
        public OrderEvent() {}
        
        ......Getter-Setter......
    }
    
    //Now How to deal with CQL!
    
    CQLExecutor cqlExecutor = new CQLExecutor.Builder()
                                  .connectTo(9042, "127.0.0.1")
                                  .build();
                                  
    //cqlExecutor.close(); //Close after use
    
    //Create Keyspace if needed.
    Boolean newKeyspace = cqlExecutor.createKeyspace("OrderLogs"
                                                     , ReplicationStrategy.SimpleStrategy
                                                     , 1);
    //Switch to created Keyspace
    if (newKeyspace){
          cqlExecutor.switchKeyspace("OrderLogs");
    }
    
    //
    //CRUD actions:
    try {
        //Creating a Table from CQLEntity @TableName description.
        boolean created = cqlExecutor.createTable(OrderEvent.class);
        Assert.assertTrue("Successfully Created", created);
        
        OrderEvent event = new OrderEvent();
        
        event.setTrackID(UUID.randomUUID().toString());
        event.setUserID(UUID.randomUUID().toString());
        event.setUuid(UUID.randomUUID());
        
        Map<String, String> names = new HashMap<>();
        names.put("name-1", "James");
        names.put("name-2", "Julian");
        event.setKvm(names);
        
        Map<String, Integer> collections = new HashMap<>();
        collections.put("hello-1", 1);
        collections.put("hello-2", 24);
        event.setKvm2(collections);

        //Insert
        boolean inserted = event.insert(cqlExecutor);
        Assert.assertTrue("Successfully Inserted", inserted);

        //Select From Cassandra:
        CQLSelectQuery query = new CQLQuery.Builder(QueryType.SELECT)
                .columns()
                .from(OrderEvent.class)
                .build();
        List<OrderEvent> items = cqlExecutor.executeSelect(query, OrderEvent.class);
        Assert.assertTrue("Successfully Fetched:", items.isEmpty() == false);

        //Update
        if (items.size() > 0){
            OrderEvent event1 = items.get(0);
            event1.getKvm().put("name-3", "sumaiya");
            boolean updated = event1.update(cqlExecutor);
            Assert.assertTrue("Successfully Updated", updated);
        }
        
        //Delete
        if (items.size() > 1){
            OrderEvent event1 = items.get(1);
            boolean delete = event1.delete(cqlExecutor);
            Assert.assertTrue("Successfully Deleted", delete);
        }

    } catch (SQLException e) {
        System.out.println(e.getMessage());
    } catch (IllegalAccessException e) {
        System.out.println(e.getMessage());
    } catch (InstantiationException e) {
        System.out.println(e.getMessage());
    }
    
    //CRUD action ends:
    
    //Read from Cassandra
    //Cassandra force to have all PartitionKey in where clause and they must have to be in sequence as they appear in table schema.
    //ClusteringKey's are optional they may or may not in clause.
    Predicate predicate = new Where("track_id")
                                    .isEqualTo("3ab863f1...89f")
                                    .and("user_id")
                                    .isEqualTo("776aa40b...be0");

    List<OrderEvent> otherItems = OrderEvent.read(OrderEvent.class, cqlExecutor, predicate);
    otherItems.stream().forEach(event -> System.out.println("track_id "+event.getTrackID()));
    
####
##### DataSource & DataStorage Api
    
    //Create an instance of DataSource. e.g. from a concreate implementation SimpleDataSource.java
    SimpleDataSource<String, Object> dataSource = new SimpleDataSource<>();
    
    //API: Create and Insert:
    Person person = new Person().setName("John")
                        .setEmail("john@gmail.com").setAge(36)
                        .setGender("male");
    dataSource.put("id-001", person);
    
    person = new Person().setName("Adam")
                         .setEmail("adam@gmail.com").setAge(31)
                         .setGender("male");
    dataSource.put("id-002", person);
    //
    
    //API: Read by Key
    Person found = dataSource.read("id-001");
    
    //API: Paginated read-sync and Convert:
    int maxItem = dataSource.size();
    Object[] items = dataSource.readSync(0, maxItem);
    List<Person> converted = Stream.of(items).map(itm -> (Person) itm).collect(Collectors.toList());
    converted.forEach(person -> System.out.println(person.toString()));
    
    //API: Remove
    Person removed = dataSource.remove("id-002");
    
    //API: Replace
    Person replaced = dataSource.replace("id-002", new Person()...);
    
####
##### Page Vs Offset:
    
    //Page Vs Offset: When limit/size is given
    public int getOffset(int page, int limit) {
         if (limit <= 0) limit = 10;
         if (page <= 0) page = 1;
         int offset = (page - 1) * limit;
         return offset;
    }
    
    //E.g. Usually in rest-api get-method, page variable being passed with starting value from 1;
    //Where as in database sql-context, we write select query with offset with startting value from 0;
    //So, usually we have to translate page into offset or vice-versa.
    int page = 2;
    int limit = 10;
    int offset = getOffset(page, limit);
    
    //Test Results:
    When (limit:10 & page:2)   Offset: 10
    When (limit:10 & page:-1)  Offset: 0
    When (limit:10 & page:7)   Offset: 60
    When (limit:10 & page:101) Offset: 1000
    When (limit:15 & page:2)   Offset: 15
    When (limit:15 & page:-1)  Offset: 0
    When (limit:20 & page:7)   Offset: 120
    When (limit:-1 & page:-1)  Offset: 0
    

### Questions?
-------------
##### Send your query to us: m.towhid.islam@gmail.com
##### http://www.deliman.com.bd/

### Author
----
#### Towhidul Islam (m.towhid.islam@gmail.com)
 
 
 
 
 

