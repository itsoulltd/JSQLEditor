package com.it.soul.lab.sql;

import com.it.soul.lab.sql.entity.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonMapper implements RowMapper<Person> {
    @Override
    public Person row(ResultSet rs, int rowNum, int columnCount) throws SQLException {
        Person person = new Person();
        person.setUuid_idx(rs.getString("uuid"));
        person.setAge(rs.getInt("age"));
        person.setName_test(rs.getString("name"));
        person.setSalary(rs.getDouble("salary"));
        person.setCreateDate(rs.getTimestamp("createDate"));
        person.setCreateTime(rs.getTimestamp("createTime"));
        return person;
    }
}
