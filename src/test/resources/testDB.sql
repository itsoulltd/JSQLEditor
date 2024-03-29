
DROP table IF EXISTS Passenger;
DROP table IF EXISTS Person;

CREATE TABLE IF NOT EXISTS Passenger
 (
 id int auto_increment primary key,
 name varchar(1024) null,
 age  int default '18' null,
 sex varchar(12) null,
 dob datetime null,
 createdate timestamp null,
 constraint Passenger_id_uindex unique (id)
 );

CREATE TABLE IF NOT EXISTS Person
(
  uuid       varchar(512) not null primary key,
  name       varchar(512) null,
  age        int          null,
  active     tinyint(1)   null,
  salary     double       null,
  dob        datetime     null,
  height     float        null,
  createDate timestamp    null,
  dobDate    datetime     null,
  createTime timestamp    null
);