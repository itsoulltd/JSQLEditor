package com.it.soul.lab.sql;

import java.sql.SQLException;

public interface QueryTransaction extends AutoCloseable{
    void begin() throws SQLException;
    void end() throws SQLException;
    void abort() throws SQLException;
}
