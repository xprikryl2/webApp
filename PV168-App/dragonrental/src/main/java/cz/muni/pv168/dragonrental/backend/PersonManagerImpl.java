/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import cz.muni.pv168.dragonrental.common.DBUtils;
import cz.muni.pv168.dragonrental.common.IllegalEntityException;
import cz.muni.pv168.dragonrental.common.ServiceFailureException;
import cz.muni.pv168.dragonrental.common.ValidationException;
import static java.lang.Boolean.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 *
 * @author Ondrej Prikryl
 */
public class PersonManagerImpl implements PersonManager{
    
    private DataSource dataSource;
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    public PersonManagerImpl() {
    }
    
    public PersonManagerImpl(DataSource datasource) {
        this.dataSource = datasource;
    }

    @Override
    public void addPerson(Person person) throws ServiceFailureException {
        checkDataSource();
        validate(person);

        if (person.getId() != null) {
            throw new IllegalEntityException("Persons ID is already set");
        }
        if(!checkEmail(person.getEmail())) {
            throw new ValidationException("Mail is already used.");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Person (name,surname,email) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, person.getName());
            st.setString(2, person.getSurname());
            st.setString(3, person.getEmail());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, person, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            person.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting person into db";
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void deletePerson(Person person) {
        checkDataSource();
        if (person == null) {
            throw new IllegalArgumentException("Person is null");
        }        
        if (person.getId() == null) {
            throw new IllegalEntityException("Person id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Person WHERE id = ?");
            st.setLong(1, person.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, person, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting person from the db";
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void updatePerson(Person person) {
        checkDataSource();
        validate(person);
        
        if (person.getId() == null) {
            throw new IllegalEntityException("Person id is null");
        }
        
        Person search = new Person();
        search.setEmail(person.getEmail());
        List <Person> people = findPeople(search);
        if(!people.isEmpty()) {
            if(people.get(0).getId() != person.getId()) {
                throw new ValidationException("Mail is already used.");
            }
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);            
            st = conn.prepareStatement(
                    "UPDATE Person SET name = ?, surname = ?, email = ? WHERE id = ?");
            st.setString(1, person.getName());
            st.setString(2, person.getSurname());
            st.setString(3, person.getEmail());
            st.setLong(4, person.getId());
            
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, person, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating person in the db";
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Person> listAllPeople() throws ServiceFailureException {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, surname, email FROM person");
            return executeQueryForMultiplePeople(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all bodies from DB";
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }
    
    @Override
    public Person getPersonById(Long id) {
        checkDataSource();

        if (id == null) {
            throw new IllegalArgumentException("Id is null.");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = dataSource.getConnection();

            statement = connection.prepareStatement(
                    "SELECT id, name, element, speed, born FROM Person WHERE id = ?");
            statement.setLong(1,id);
            return executeQueryForSinglePerson(statement);
        } catch (SQLException ex) {
            String message = "Error getting person with id = " + id + " from database.";
            //logging
            throw new ServiceFailureException(message, ex);
        } finally {
            DBUtils.closeQuietly(connection,statement);
        }
    }

    @Override
    public List<Person> findPeople(Person person) {
        checkDataSource();
        if(person == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if(person.getId() == null && person.getName() == null && 
                person.getSurname() == null && person.getEmail() == null) {
            throw new IllegalArgumentException("All search criterias are null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            StringBuilder strBuilder = new StringBuilder("SELECT id, name, surname, email FROM person WHERE ");
            if(person.getId() != null) {
                strBuilder.append("id=");
                strBuilder.append(person.getId());
                strBuilder.append(" AND ");
            }
            if(person.getName() != null) {
                strBuilder.append("name=");
                strBuilder.append("\'");
                strBuilder.append(person.getName());
                strBuilder.append("\'");
                strBuilder.append(" AND ");
            }
            if(person.getSurname() != null) {
                strBuilder.append("surname=");
                strBuilder.append("\'");
                strBuilder.append(person.getSurname());
                strBuilder.append("\'");
                strBuilder.append(" AND ");
            } 
            if(person.getEmail() != null) {
                strBuilder.append("email=");
                strBuilder.append("\'");
                strBuilder.append(person.getEmail());
                strBuilder.append("\'");
                strBuilder.append(" AND ");
            } 
            strBuilder.delete(strBuilder.length() - " AND ".length(), strBuilder.length());
            st = conn.prepareStatement(strBuilder.toString());
            return executeQueryForMultiplePeople(st);
        } catch (SQLException ex) {
            String msg = "Error when finding people from DB";
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }
    
    private Boolean checkEmail(String adress) {
        Person searchPerson = new Person();
        searchPerson.setId(null);
        searchPerson.setName(null);
        searchPerson.setSurname(null);
        searchPerson.setEmail(adress);
        
        if(findPeople(searchPerson).isEmpty()) {
            return TRUE;
        }
        return FALSE;
    }
    
    private void validate(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person is null");
        }
        if (person.getName() == null || person.getName().isEmpty()) {
            throw new ValidationException("Name is null");
        }
        if (person.getSurname() == null || person.getSurname().isEmpty()) {
            throw new ValidationException("Surname is null");
        }
        if (person.getEmail() == null || person.getEmail().isEmpty()) {
            throw new ValidationException("Email is null");
        }
    }
    
    static private Person resToPerson(ResultSet rs) throws SQLException {
        Person result = new Person();
        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));
        result.setSurname(rs.getString("surname"));
        result.setEmail(rs.getString("email"));
        
        return result;
    }

    static List<Person> executeQueryForMultiplePeople(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Person> result = new ArrayList<>();
        while (rs.next()) {
            result.add(resToPerson(rs));
        }
        return result;
    }
    
    static Person executeQueryForSinglePerson(PreparedStatement statement) throws SQLException, ServiceFailureException {
        ResultSet rs = statement.executeQuery();
        Person result = null;

        if (rs.next()) {
            result = resToPerson(rs);
            if (rs.next()) {
                throw new ServiceFailureException("Internal integrity error: more people with the same ID found!");
            }
        }

        return result;
    }
}
