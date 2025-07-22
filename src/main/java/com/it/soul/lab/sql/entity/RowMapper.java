package com.it.soul.lab.sql.entity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author towhid
 * @since 19-Aug-19
 */
public interface RowMapper<R extends Entity> {
    default List<R> extract(ResultSet rs) throws SQLException {
        int index = 0;
        List<R> collection = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();
        while (rs.next()){
            try {
                R entity = row(rs, index++, numCol);
                collection.add(entity);
            } catch (SQLException e) {}
        }
        return collection;
    }
    R row(ResultSet rs, int rowNum, int columnCount) throws SQLException;
}
