package com.it.soul.lab.connect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum DriverClass {
    MYSQL,
    PostgraySQLv7,
    DB2,
    OracleOCI9i,
    SQLServer,
    H2_MEM,
    H2_FILE,
    H2_SERVER,
    H2_SERVER_TLS,
    NONE;

    public String toString(){
        String result = "";
        switch (this) {
        case MYSQL:
            result = "com.mysql.jdbc.Driver";
            break;
        case PostgraySQLv7:
            result = "org.postgresql.Driver";
            break;
        case DB2:
            result = "COM.ibm.db2.jdbc.app.DB2Driver";
            break;
        case OracleOCI9i:
            result = "oracle.jdbc.driver.OracleDriver";
            break;
        case SQLServer:
            result = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
            break;
        case H2_MEM:
        case H2_FILE:
        case H2_SERVER:
        case H2_SERVER_TLS:
            result = "org.h2.Driver";
            break;
        default:
            result = "sun.jdbc.odbc.JdbcOdbcDriver";
            break;
        }
        return result;
    }

    public String urlSchema() {
        String result = "";
        switch (this) {
        case MYSQL:
            result = "jdbc:mysql://";
            break;
        case PostgraySQLv7:
            result = "jdbc:postgresql://";
            break;
        case DB2:
            result = "jdbc:db2://";
            break;
        case OracleOCI9i:
            result = "jdbc:oracle:thin:@//";
            break;
        case SQLServer:
            result = "jdbc:sqlserver://";
            break;
        case H2_MEM:
            result = "jdbc:h2:mem:";
            break;
        case H2_FILE:
            result = "jdbc:h2:file:";
            break;
        case H2_SERVER:
            result = "jdbc:h2:tcp://";
            break;
        case H2_SERVER_TLS:
            result = "jdbc:h2:ssl://";
            break;
        default:
            result = "jdbc:odbc:";
            break;
        }
        return result;
    }

    public String defaultPort() {
        String result = "";
        switch (this) {
        case MYSQL:
            result = "3306";
            break;
        case PostgraySQLv7:
            result = "5432";
            break;
        case DB2:
            result = "446";
            break;
        case OracleOCI9i:
            result = "";
            break;
        case SQLServer:
            result = "1433";
            break;
        case H2_MEM:
        case H2_FILE:
            result = "";
            break;
        case H2_SERVER:
            result = "8084";
            break;
        case H2_SERVER_TLS:
            result = "8085";
            break;
        default:
            result = "";
            break;
        }
        return result;
    }

    public static DriverClass getMatchedDriver(String connectionURL) {
        DriverClass result = NONE;
        List<DriverClass> all = new ArrayList<>(EnumSet.allOf(DriverClass.class));
        for (DriverClass driverClass : all) {
            if (connectionURL.startsWith(driverClass.urlSchema())) {
                result = driverClass;
                break;
            }
        }
        return result;
    }

}
