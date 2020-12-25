package com.it.soul.lab.sql;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.entity.PrimaryKey;
import com.it.soul.lab.sql.entity.TableName;
import com.it.soul.lab.sql.query.models.Property;

import javax.persistence.Column;
import java.util.Date;

@TableName(value = "Passenger", acceptAll = false)
public class Passenger extends Entity {
	
	@PrimaryKey(name="ID", auto =true)
	private Integer id;
	@Column(name = "AGE")
	private Integer age;
	@Column(name = "NAME")
	private String name;
	@Column(name = "SEX")
	private String sex;
	@Column(name = "DOB")
	private Date dob;
	public Passenger() {}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public Date getDob() {
		return dob;
	}
	public void setDob(Date dob) {
		this.dob = dob;
	}

	public Property getPropertyTest(String key, SQLExecutor exe, boolean skipPrimary) {
		return getProperty(key, exe, skipPrimary);
	}

}
