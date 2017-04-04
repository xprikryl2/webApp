  -- schema-psql.sql
  -- DDL commands for PostgreSQL
  DROP TABLE IF EXISTS person;
  DROP TABLE IF EXISTS dragon;
  DROP TABLE IF EXISTS reservation;

 CREATE TABLE person (
   id     SERIAL PRIMARY KEY,
   name   VARCHAR,
   surname VARCHAR,
   email VARCHAR);

 CREATE TABLE dragon (
   id       SERIAL PRIMARY KEY,
   name VARCHAR,
   element VARCHAR,
   speed INT,
   born DATE);

 CREATE TABLE reservation (
   id          SERIAL PRIMARY KEY);