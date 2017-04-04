-- schema-javadb.sql
-- DDL commands for JavaDB/Derby
CREATE TABLE person (
  id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  name  VARCHAR(255),
  surname VARCHAR(255),
  email VARCHAR(255)
);

CREATE TABLE dragon (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255),
    element VARCHAR(42),
    speed INT,
    born DATE
);

