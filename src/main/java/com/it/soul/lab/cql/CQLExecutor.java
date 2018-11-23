package com.it.soul.lab.cql;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.it.soul.lab.cql.entity.CQLEntity;
import com.it.soul.lab.cql.entity.ClusteringKey;
import com.it.soul.lab.cql.query.*;
import com.it.soul.lab.cql.query.ReplicationStrategy;
import com.it.soul.lab.sql.AbstractExecutor;
import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.QueryTransaction;
import com.it.soul.lab.sql.entity.Column;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.entity.PrimaryKey;
import com.it.soul.lab.sql.entity.TableName;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLScalerQuery;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.models.DataType;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.Table;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CQLExecutor extends AbstractExecutor implements QueryExecutor<CQLSelectQuery, CQLInsertQuery, CQLUpdateQuery, CQLDeleteQuery, SQLScalerQuery>, QueryTransaction {

    public static class Builder {

        private Cluster.Builder clBuilder;
        private String keySpace;

        public Builder() {
            clBuilder = Cluster.builder();
        }

        public <T extends QueryExecutor> T build(){
            return (T) new CQLExecutor(clBuilder.build(), keySpace);
        }

        public Builder connectTo(Integer port, String...points){
            clBuilder.addContactPoints(points)
                    .withPort(port);
            return this;
        }

        public Builder useKeyspace(String keyspace){
            this.keySpace = keyspace;
            return this;
        }

        public Builder authProvider(String username, String password){
            clBuilder.withAuthProvider(new PlainTextAuthProvider(username, password));
            return this;
        }

        public Builder authProvider(AuthProvider provider){
            clBuilder.withAuthProvider(provider);
            return this;
        }

        public Builder configureLoadBalancer(String localDataCenter){
            clBuilder.withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder().withLocalDc(localDataCenter).build());
            return this;
        }

    }

    private CQLExecutor(Cluster cluster, String keyspace) {

        Metadata metadata = cluster.getMetadata();
        System.out.printf("Connected to cluster: %s\n", metadata.getClusterName());

        for (Host host: metadata.getAllHosts()) {
            System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n", host.getDatacenter(), host.getAddress(), host.getRack());
        }
        //
        setupSession(cluster, keyspace);
    }

    private Session _session;

    protected Session getSession(){
        assert _session != null : "Invalid Cassandra Session : CQLExecutor";
        return _session;
    }

    protected void setupSession(Cluster cluster, String keyspace){
        if (keyspace == null) keyspace = "";
        //Check keyspace exist or not.
        KeyspaceMetadata keyspaceMetadata = cluster.getMetadata().getKeyspace(keyspace);
        if (keyspaceMetadata != null){
            keyspace = validateKeyspace(keyspace);
            _session = cluster.connect(keyspace);
        }else {
            _session = cluster.connect();
        }
        //TODO: WHAT IS THIS:
        //_session = cluster.newSession();
    }

    public void reconnectSession(String keyspace){
        Cluster _cluster = getSession().getCluster();
        if (_cluster.isClosed() == false){
            //FIXME: asynchronous in future.
            Session cSession = getSession();
            if (cSession.isClosed() == false){
                try {
                    //Blocking...wait until close:
                    cSession.closeAsync().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            //
            setupSession(_cluster, keyspace);
        }
    }

    public void switchKeyspace(String keyspace){
        if (getSession().isClosed() == false){
            keyspace = validateKeyspace(keyspace);
            getSession().execute("USE " + keyspace + ";");
        }else{
            reconnectSession(keyspace);
        }
    }

    @Override
    public CQLQuery.Builder createBuilder(QueryType queryType) {
        return new CQLQuery.Builder(queryType);
    }

    @Override
    public Integer executeUpdate(CQLUpdateQuery cqlUpdateQuery) throws SQLException {
        try {
            Statement statement = createUpdateStatement(cqlUpdateQuery);
            getSession().execute(statement);
            return 1;
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    protected Statement createUpdateStatement(CQLUpdateQuery cqlUpdateQuery) {
        //Order of keys in statements
        PreparedStatement smt = StatementPool.createIfNotExist(cqlUpdateQuery.toString(), getSession());
        List<Object> properties = new ArrayList<>();
        //
        Row row = cqlUpdateQuery.getRow();
        for (Property prop : row.getProperties()){
            properties.add(prop.getValue());
        }
        //
        Row whereRow = cqlUpdateQuery.getWhereProperties();
        for (Property prop : whereRow.getProperties()){
            properties.add(prop.getValue());
        }
        //
        Object[] values = properties.toArray(new Object[0]);
        return (properties.isEmpty()) ? smt.bind() : smt.bind(values);
    }

    @Override
    public Integer[] executeBatchUpdate(int i, CQLUpdateQuery cqlUpdateQuery, List<Row> list, List<Row> list1) throws SQLException, IllegalArgumentException {
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public Integer executeDelete(CQLDeleteQuery cqlDeleteQuery) throws SQLException {
        try{
            Statement statement = createSelectStatementFrom(cqlDeleteQuery);
            getSession().execute(statement);
            return 1;
        }catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public Integer executeBatchDelete(int i, CQLDeleteQuery cqlDeleteQuery, List<Row> list) throws SQLException {
        throw new SQLException("Not Implemented YET");
    }

    public Integer executeInsert(boolean autoId, String s) throws SQLException, IllegalArgumentException {
        try{
            getSession().execute(s);
            return 1;
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public Integer executeInsert(boolean autoId, CQLInsertQuery cqlInsertQuery) throws SQLException, IllegalArgumentException {
        try{
            Statement statement = createInsertStatement(cqlInsertQuery);
            getSession().execute(statement);
            return 1;
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    protected Statement createInsertStatement(CQLInsertQuery cqlInsertQuery) {
        //Order of keys in statements
        PreparedStatement smt = StatementPool.createIfNotExist(cqlInsertQuery.toString(), getSession());
        //TODO: Have to understand:
        //smt.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);
        //smt.setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL);
        //
        Row row = cqlInsertQuery.getRow();
        List<Object> properties = new ArrayList<>();
        for (Property prop : row.getProperties()){
            properties.add(prop.getValue());
        }
        Object[] values = properties.toArray(new Object[0]);

        return (properties.isEmpty()) ? smt.bind() : smt.bind(values);
    }

    public Integer getScalerValue(String s) throws SQLException {
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public Integer getScalerValue(SQLScalerQuery sqlScalerQuery) throws SQLException {
        throw new SQLException("Not Implemented YET");
    }

    public <T extends Entity> List<T> executeSelect(CQLSelectQuery cqlSelectQuery, Class<T> aClass) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        //Must pass the mapping:
        return executeSelect(cqlSelectQuery, aClass, CQLEntity.mapColumnsToProperties(aClass));
    }

    protected List<Row> createRowsFrom(ResultSet set) {
        //Java-8 >= ...
        /*return StreamSupport.stream(set.spliterator(), false)
                .map(row -> {
                    ColumnDefinitions def = row.getColumnDefinitions();
                    Row nRow = new Row();
                    //FIXME:
                    for (ColumnDefinitions.Definition definition : def.asList()){
                        nRow.add(definition.getName(), row.getObject(definition.getName()));
                    }
                    return nRow;
                }).collect(Collectors.toList());*/
        //Java-7 <= ...
        List<Row> rows = new ArrayList<>();
        Iterator<com.datastax.driver.core.Row> iterator = set.iterator();
        while (iterator.hasNext()){
            com.datastax.driver.core.Row row = iterator.next();
            ColumnDefinitions def = row.getColumnDefinitions();
            Row nRow = new Row();
            //FIXME:
            for (ColumnDefinitions.Definition definition : def.asList()){
                nRow.add(definition.getName(), row.getObject(definition.getName()));
            }
            rows.add(nRow);
        }
        return rows;
    }

    @Override
    public Object createBlob(String s) throws SQLException {
        ProtocolVersion proto = getSession().getCluster().getConfiguration().getProtocolOptions().getProtocolVersion();
        byte[] bytes = s.getBytes();
        ByteBuffer buffer = TypeCodec.blob().serialize(ByteBuffer.wrap(bytes), proto);
        return buffer;
    }

    public Object createBlobFrom(BufferedImage s) throws SQLException {
        //ProtocolVersion proto = getSession().getCluster().getConfiguration().getProtocolOptions().getProtocolVersion();
        //byte[] bytes = s.getBytes();
        //ByteBuffer buffer = TypeCodec.blob().serialize(ByteBuffer.wrap(bytes), proto);
        //return buffer;
        return null;
    }

    @Override
    public <T> List<T> executeSelect(CQLSelectQuery cqlSelectQuery, Class<T> aClass, Map<String, String> map) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        try {
            Statement statement = createSelectStatementFrom(cqlSelectQuery);
            ResultSet set = getSession().execute(statement);
            List<Row> rows = createRowsFrom(set);
            Table table = new Table();
            table.setRows(rows);
            return table.inflate(aClass, map);
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    private final ExecutorService executionPool = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public <T extends Entity> void executeSelect(CQLSelectQuery cqlSelectQuery, Class<T> aClass, Consumer<List<T>> consumer)  {
        Statement statement = null;
        try{
            statement = createSelectStatementFrom(cqlSelectQuery);
        }catch (Exception e) {
            System.out.println(e.getMessage());
            consumer.accept(null);
            return;
        }
        ResultSetFuture future = getSession().executeAsync(statement);
        Futures.addCallback(future
                , new FutureCallback<ResultSet>() {
                    @Override
                    public void onSuccess(ResultSet rows) {
                        //
                        List<Row> items = StreamSupport.stream(rows.spliterator(), false)
                                .map(row -> {
                                    ColumnDefinitions def = row.getColumnDefinitions();
                                    Row nRow = new Row();
                                    for (ColumnDefinitions.Definition definition : def.asList()){
                                        nRow.add(definition.getName(), row.getObject(definition.getName()));
                                    }
                                    return nRow;
                                }).collect(Collectors.toList());

                        //items.forEach(row -> System.out.println(row.size()));
                        Table table = new Table();
                        table.setRows(items);
                        try {
                            List<T> results = table.inflate(aClass, CQLEntity.mapColumnsToProperties(aClass));
                            consumer.accept(results);
                        } catch (InstantiationException e) {
                            System.out.println(e.getMessage());
                            consumer.accept(null);
                        } catch (IllegalAccessException e) {
                            System.out.println(e.getMessage());
                            consumer.accept(null);
                        }
                        //
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                        consumer.accept(null);
                    }
                }, executionPool);
        //
    }

    protected Statement createSelectStatementFrom(SQLSelectQuery cqlSelectQuery) {
        //Order of keys in statements
        String query = cqlSelectQuery.toString();
        PreparedStatement smt = StatementPool.createIfNotExist(query, getSession());
        Row row = cqlSelectQuery.getWhereProperties();
        List<Object> properties = new ArrayList<>();
        for (Property prop : row.getProperties()){
            properties.add(prop.getValue());
        }
        Object[] values = properties.toArray(new Object[0]);
        return (properties.isEmpty()) ? smt.bind() : smt.bind(values);
    }

    public <T extends Entity> List<T> executeSelect(String s, Class<T> aClass) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        //Must pass the mapping:
        return executeSelect(s, aClass, CQLEntity.mapColumnsToProperties(aClass));
    }

    @Override
    public <T> List<T> executeSelect(String s, Class<T> aClass, Map<String, String> map) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        try{
            ResultSet set = getSession().execute(s);
            List<Row> rows = createRowsFrom(set);
            Table table = new Table();
            table.setRows(rows);
            return table.inflate(aClass, map);
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    @Override
    public List executeCRUDQuery(String s, Class aClass) throws SQLException, IllegalAccessException, InstantiationException {
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public Integer[] executeBatchInsert(boolean b, int i, String s, List list) throws SQLException, IllegalArgumentException {
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public void begin() throws SQLException {
        //Cassandra don't support transaction
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public void end() throws SQLException {
        //Cassandra don't support transaction
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public void abort() throws SQLException {
        //Cassandra don't support transaction
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public void close() throws Exception {
        try {
            if (!getSession().getCluster().isClosed())
                getSession().getCluster().close();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public Boolean executeDDLQuery(String query) throws SQLException {
        if(query == null
                || query.length() <=0
                || !query.trim().toLowerCase().startsWith("create")
                || !query.trim().toLowerCase().startsWith("drop")
                || !query.trim().toLowerCase().startsWith("alter")){
            throw new SQLException("Bad Formated Query : " + query);
        }
        try{
            ResultSet set = getSession().execute(query);
            boolean isCreated = set.wasApplied();
            return isCreated;
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    protected String validateKeyspace(String provided){
        return provided.trim().replace(' ', '_').toLowerCase();
    }

    public Boolean createKeyspace(String keyspace, ReplicationStrategy strategy, int replicationFactor) throws SQLException {
        if (keyspace == null || keyspace.trim().isEmpty()) return false;

        keyspace = validateKeyspace(keyspace);
        if (keyspace.equalsIgnoreCase(getSession().getLoggedKeyspace())) return true;

        String query = String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class': '%s', 'replication_factor' : %d}", keyspace, strategy.name(), replicationFactor);
        try{
            ResultSet set = getSession().execute(query);
            return set.wasApplied();
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    public Boolean dropKeyspace(String keyspace) throws SQLException {
        if (keyspace == null)
            keyspace = getSession().getLoggedKeyspace();
        else
            keyspace = validateKeyspace(keyspace);

        String query = String.format("DROP KEYSPACE IF EXISTS %s;", keyspace);
        try{
            ResultSet set = getSession().execute(query);
            return set.wasApplied();
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends Entity> boolean createIndexOn(String onColumn, Class<T> tableType) throws SQLException {
        String tableNameStr = getTableName(tableType);
        if (tableNameStr == null) return false;

        String query = String.format("CREATE INDEX IF NOT EXISTS %s_idx ON %s (%s);", onColumn, tableNameStr, onColumn);
        try{
            ResultSet set = getSession().execute(query);
            return set.wasApplied();
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    public boolean dropIndex(String onColumn) throws SQLException {
        String keyspace = getSession().getLoggedKeyspace();
        String query = String.format("DROP INDEX IF EXISTS %s.%s_idx;", keyspace, onColumn);
        try{
            ResultSet set = getSession().execute(query);
            return set.wasApplied();
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends Entity> boolean dropTable(Class<T> tableType) throws SQLException {
        String tableNameStr = getTableName(tableType);
        if (tableNameStr == null) return false;

        String keyspace = getSession().getLoggedKeyspace();
        String query = String.format("DROP TABLE IF EXISTS %s.%s;", keyspace, tableNameStr);
        try{
            ResultSet set = getSession().execute(query);
            return set.wasApplied();
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    public <T extends Entity> boolean truncateTable(Class<T> tableType) throws SQLException {
        String tableNameStr = getTableName(tableType);
        if (tableNameStr == null) return false;

        String keyspace = getSession().getLoggedKeyspace();
        String query = String.format("Truncate %s.%s;", keyspace, tableNameStr);
        try{
            ResultSet set = getSession().execute(query);
            return set.wasApplied();
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    private <T extends Entity> String getTableName(Class<T> tableType) {
        if(tableType.isAnnotationPresent(TableName.class) == false) {
            return null;
        }
        TableName tableName = tableType.getAnnotation(TableName.class);
        String tableNameStr = tableName.value().trim();
        if (tableNameStr.isEmpty()) tableNameStr = tableType.getSimpleName();
        return tableNameStr;
    }

    public <T extends Entity> boolean alterTable(Class<T> tableType, AlterAction action, Property...properties) throws SQLException {

        String name = getTableName(tableType);
        if (name == null) return false;

        String keyspace = getSession().getLoggedKeyspace();

        String head = "ALTER TABLE " + keyspace + "." + name;

        if (action == AlterAction.ALTER){
            Property prop = properties[0];
            //buffer.append(prop.getKey() + " TYPE " + getCompatibleDataType(prop.getType().name()) + ";");
            String add = head + " ADD " + prop.getKey() + " " + getCompatibleDataType(prop.getType().name(), null) + ";";
            String drop = head + " DROP " + prop.getKey() + ";";
            try {
                ResultSet set = getSession().execute(drop);
                set = getSession().execute(add);
                return set.wasApplied();
            }catch (Exception e){
                throw new SQLException(e.getMessage());
            }
        }else{

            StringBuffer buffer = new StringBuffer(" " + action.name() + " ");

            for (Property prop : properties) {
                if (action == AlterAction.ADD){
                    buffer.append(prop.getKey() + " " + getCompatibleDataType(prop.getType().name(), null) + ",");
                }
                else if (action == AlterAction.RENAME){
                    //ONLY Applicable to PrimaryKey:
                    buffer.append(prop.getKey() + " TO " + prop.getValue().toString() + ";");
                    break;
                }
                else if (action == AlterAction.DROP){
                    buffer.append(prop.getKey() + ",");
                }
                else if (action == AlterAction.WITH){
                    //Not Supported.
                }
            }

            if (action == AlterAction.ADD || action == AlterAction.DROP)
                buffer.replace(buffer.length()-1, buffer.length(), ";");

            String query = head + buffer.toString();
            try {
                ResultSet set = getSession().execute(query);
                return set.wasApplied();
            }catch (Exception e){
                throw new SQLException(e.getMessage());
            }
        }
    }

    public <T extends Entity> Boolean createTable(Class<T> tableType) throws SQLException {

        String tableNameStr = getTableName(tableType);
        if (tableNameStr == null) return false;

        String keyspace = getSession().getLoggedKeyspace();
        StringBuffer headBuffer = new StringBuffer("CREATE TABLE IF NOT EXISTS " + keyspace + "." + tableNameStr);

        StringBuffer columnBuf = new StringBuffer();
        StringBuffer primaryBuf = new StringBuffer(" PRIMARY KEY (");
        StringBuffer primaryComposit = new StringBuffer("(");
        StringBuffer clusterComposit = new StringBuffer();
        StringBuffer clusterBuf = new StringBuffer("WITH CLUSTERING ORDER BY (");

        Field[] fields = tableType.getDeclaredFields();
        for (Field field : fields) {
            if(field.isAnnotationPresent(PrimaryKey.class)) {
                PrimaryKey pm = field.getAnnotation(PrimaryKey.class);
                String fieldName = pm.name().trim();
                columnBuf.append(fieldName + " " + getDataType(field) + ",");
                primaryComposit.append(fieldName + ",");
                continue;
            }
            if(field.isAnnotationPresent(ClusteringKey.class)) {
                ClusteringKey cl = field.getAnnotation(ClusteringKey.class);
                String fieldName = cl.name().trim();
                columnBuf.append(fieldName + " " + getDataType(field) + ",");
                clusterComposit.append(fieldName + ",");
                clusterBuf.append(fieldName + " " + cl.order().name() + ",");
                continue;
            }
            //
            String fieldName = field.getName();
            if (field.isAnnotationPresent(Column.class)){
                Column col = field.getAnnotation(Column.class);
                if(col.name().trim().isEmpty() == false) fieldName = col.name().trim();
            }
            columnBuf.append(fieldName + " " + getDataType(field) + ",");
        }
        //
        primaryComposit.replace(primaryComposit.length()-1, primaryComposit.length(),")");
        if (clusterComposit.toString().trim().length() > 0) {
            clusterComposit.replace(clusterComposit.length() - 1, clusterComposit.length(), ")");
            primaryBuf.append(primaryComposit.toString()).append( "," + clusterComposit.toString());
            clusterBuf.replace(clusterBuf.length()-1, clusterBuf.length(), ")");
        }else{
            primaryBuf.append(primaryComposit.toString());
            clusterBuf.replace(0, clusterBuf.length(), ")");
        }
        //With the line by line meaning.
        String query = headBuffer.append(" (") // CREATE TABLE IF NOT EXISTS (
                .append(columnBuf.toString())  // col-1 type, col-2 type, .... ,
                .append(primaryBuf.toString()) // PRIMARY KEY ((partition-1, partition-2, ..), cluster-1, cluster-2, ... )
                .append(") ")                  // )
                .append(clusterBuf.toString()) // WITH CLUSTERING ORDER BY (cluster-1 ASE, cluster-2 DESC, ... )
                .append(";").toString();
        try{
            ResultSet set = getSession().execute(query);
            return set.wasApplied();
        }catch (Exception e){
            throw new SQLException(e.getMessage());
        }
    }

    private String getDataType(Field field){
        String name = field.getType().getName();
        String compName = getCompatibleDataType(name, field);
        return compName != null ? compName : name.toLowerCase();
    }

    private String getCompatibleDataType(String name, Field field) {
        name = name.toLowerCase();
        //
        if (field == null || (field.getGenericType() instanceof ParameterizedType) == false){
            //System.out.println("Type: " + field.getType());
            return getSimpleDataType(name);
        }
        //
        ParameterizedType pType = (ParameterizedType)field.getGenericType();;
        //We only care about simple types not user defined types.
        Type[] subscripts = pType.getActualTypeArguments();
        if (name.contains("list")) {
            //return "list<text>";
            Type sub = subscripts.length > 0 ? subscripts[0] : null;
            if (sub != null){
                return "list<" + getSimpleDataType(sub.getTypeName()) + ">";
            }
        }
        else if (name.contains("map")) {
            //return "map<text,text>";
            StringBuffer buffer = new StringBuffer("map<");
            for (Type sub : subscripts) {
                buffer.append(getSimpleDataType(sub.getTypeName()) + ",");
            }
            buffer.replace(buffer.length()-1,buffer.length(),">");
            return buffer.toString();
        }
        else if (name.contains("set")) {
            //return "set<text>";
            Type sub = subscripts.length > 0 ? subscripts[0] : null;
            if (sub != null){
                return "set<" + getSimpleDataType(sub.getTypeName()) + ">";
            }
        }
        //
        return getSimpleDataType(name);
    }

    private String getSimpleDataType(String name) {
        name = name.toLowerCase();
        if (name.contains("string") || name.contains("text")) return "text";
        if (name.contains("integer")) return "int";
        if (name.contains("int")) return "int";
        if (name.contains("long")) return "bigint";
        if (name.contains("boolean")) return "boolean";
        if (name.contains("double")) return "double";
        if (name.contains("float")) return "float";
        if (name.contains("blob")) return "blob";
        if (name.contains("uuid")) return "uuid";
        if (name.contains("date") || name.contains("time") || name.contains("timestamp")) return "timestamp";
        return null;
    }

    //FIXME: com.datastax.driver.core.Cluster Re-preparing already prepared query is generally an anti-pattern and will likely affect performance. Consider preparing the statement only once.
    //TODO: Check if this query already prepared in this session:
    private static class StatementPool{

        static final Map<String, PreparedStatement> statementPool = new ConcurrentHashMap<>();

        static PreparedStatement createIfNotExist(String key, Session session){
            PreparedStatement smt = statementPool.get(key);
            if (smt == null){
                smt = session.prepare(key);
                statementPool.put(key, smt);
            }
            return smt;
        }
    }

}
