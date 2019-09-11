package com.it.soul.lab.connect;

import java.sql.Connection;
import java.sql.SQLException;

public interface JDBConnectionBuilder {
    JDBConnectionBuilder database(String name);
    JDBConnectionBuilder credential(String username, String password);
    Connection build() throws SQLException;
}
