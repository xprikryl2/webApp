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

import javax.sql.ConnectionEvent;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of dragonManager.
 *
 * @author Zuzana Wolfová
 */
public class DragonManagerImpl implements DragonManager {

    private DataSource dataSource;
    private final Clock clock;

    public DragonManagerImpl(Clock clock) {
        this.clock = clock;
    }

    public void setDataSource(DataSource  dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("Data source is not set.");
        }
    }

    @Override
    public void createDragon(Dragon dragon) {

        checkDataSource();
        validate(dragon);

        if (dragon.getId() != null) {
            throw new IllegalEntityException("Dragon id is already set!");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false); //temporarily disable autocommit

            statement = connection.prepareStatement(
                    "INSERT INTO Dragon (name, element, speed, born) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, dragon.getName());
            statement.setString(2, elementToString(dragon.getElement()));
            statement.setInt(3, dragon.getMaximumSpeed());
            statement.setDate(4, localDateToSqlDate(dragon.getDateOfBirth()));

            int count = statement.executeUpdate();
            DBUtils.checkUpdatesCount(count, dragon, true);

            Long id = DBUtils.getId(statement.getGeneratedKeys());
            dragon.setId(id);

            connection.commit();
        } catch (SQLException ex) {
            String message = "Error when saving new dragon to database.";
            //logging
            throw new ServiceFailureException(message,ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection,statement);
        }
    }

    @Override
    public void removeDragon(Dragon dragon) {
        checkDataSource();

        if (dragon == null) {
            throw new IllegalArgumentException("Dragon is null.");
        }
        if (dragon.getId() == null) {
            throw new IllegalEntityException("Dragon id is null.");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            statement = connection.prepareStatement(
                    "DELETE FROM Dragon WHERE id = ?");
            statement.setLong(1,dragon.getId());

            int count = statement.executeUpdate();
            DBUtils.checkUpdatesCount(count, dragon, false);
            connection.commit();
        } catch (SQLException ex) {
            String message = "Error deleting dragon with id = " + dragon.getId() + " from database.";
            //logging
            throw new ServiceFailureException(message, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection,statement);
        }
    }

    @Override
    public void updateDragon(Dragon dragon) {
        checkDataSource();
        validate(dragon);

        if (dragon.getId() == null) {
            throw new IllegalEntityException("Dragon id is null.");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(
                    "UPDATE Dragon SET name = ?, element = ?, speed = ?, born = ? WHERE id = ?");
            statement.setString(1, dragon.getName());
            statement.setString(2, elementToString(dragon.getElement()));
            statement.setInt(3, dragon.getMaximumSpeed());
            statement.setDate(4, localDateToSqlDate(dragon.getDateOfBirth()));
            statement.setLong(5, dragon.getId());

            int count = statement.executeUpdate();
            DBUtils.checkUpdatesCount(count, dragon, false);
            connection.commit();
        } catch (SQLException ex) {
            String message = "Error updating dragon with id = " + dragon.getId() + " in database.";
            //logging
            throw new ServiceFailureException(message,ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection,statement);
        }
    }

    @Override
    public List<Dragon> listAllDragons() throws ServiceFailureException {
        checkDataSource();
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT id, name, element, speed, born FROM Dragon");
            return executeQueryForMultipleDragons(statement);
        } catch (SQLException e) {
            String message = "Error getting all dragons from database";
            //logging
            throw new ServiceFailureException(message, e);
        } finally {
            DBUtils.closeQuietly(connection, statement);
        }
    }

    @Override
    public List<Dragon> findDragons(Dragon dragon) {
        checkDataSource();

        if (dragon == null) {
            throw new IllegalArgumentException("Dragon to find is null.");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = dataSource.getConnection();
            StringBuilder builder = new StringBuilder("SELECT id, name, element, speed, born FROM Dragon");
            boolean allNull = (dragon.getId() == null && dragon.getName() == null && dragon.getElement() == null
                    && dragon.getMaximumSpeed() == null && dragon.getDateOfBirth() == null);

            if (!allNull) {
                builder.append(" WHERE ");
                if (dragon.getId() != null) {
                    builder.append("id=" + dragon.getId() + " AND ");
                }
                if (dragon.getName() != null) {
                    builder.append("name=\'" + dragon.getName() + "\' AND ");
                }
                if (dragon.getElement() != null) {
                    builder.append("element=\'" + elementToString(dragon.getElement()) + "\' AND ");
                }
                if (dragon.getMaximumSpeed() != null) {
                    builder.append("speed=" + dragon.getMaximumSpeed() + " AND ");
                }
                if (dragon.getDateOfBirth() != null) {
                    builder.append("born=\'" + localDateToSqlDate(dragon.getDateOfBirth()) + "\' AND ");
                }
                builder.delete(builder.length() - " AND ".length(),builder.length());
            }

            statement = connection.prepareStatement(builder.toString());
            return executeQueryForMultipleDragons(statement);
        } catch (SQLException ex) {
            String message = "Error finding based on criteria: id = " + dragon.getId()
                    + ", name = " + dragon.getName() + ", speed = " + dragon.getMaximumSpeed()
                    + ", born = " + dragon.getDateOfBirth() + ", element = " + dragon.getElement()
                    + " in database.";
            throw new ServiceFailureException(message,ex);
        } finally {
            DBUtils.closeQuietly(connection, statement);
        }
    }

    @Override
    public Dragon getDragonById(Long id) {
        checkDataSource();

        if (id == null) {
            throw new IllegalArgumentException("Id is null.");
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = dataSource.getConnection();

            statement = connection.prepareStatement(
                    "SELECT id, name, element, speed, born FROM Dragon WHERE id = ?");
            statement.setLong(1,id);
            return executeQueryForSingleDragon(statement);
        } catch (SQLException ex) {
            String message = "Error getting dragon with id = " + id + " from database.";
            //logging
            throw new ServiceFailureException(message, ex);
        } finally {
            DBUtils.closeQuietly(connection,statement);
        }
    }

    static Dragon executeQueryForSingleDragon(PreparedStatement statement) throws SQLException, ServiceFailureException {
        ResultSet rs = statement.executeQuery();
        Dragon result = null;

        if (rs.next()) {
            result = rowToDragon(rs);
            if (rs.next()) {
                throw new ServiceFailureException("Internal integrity error: more dragons with the same ID found!");
            }
        }

        return result;
    }

    static List<Dragon> executeQueryForMultipleDragons(PreparedStatement statement) throws SQLException {
        ResultSet rs = statement.executeQuery();
        List<Dragon> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowToDragon(rs));
        }
        return results;
    }

    private void validate(Dragon dragon) {
        if (dragon == null) {
            throw new IllegalArgumentException("Dragon is null.");
        }
        if (dragon.getName() == null) {
            throw new ValidationException("Dragon name is null.");
        }
        if (dragon.getElement() == null) {
            throw new ValidationException("Dragon element is null.");
        }
        if (dragon.getMaximumSpeed() == null) {
            throw new ValidationException("Dragon speed is null.");
        }
        if (dragon.getMaximumSpeed() != null && dragon.getMaximumSpeed() < 0) {
            throw new ValidationException("Dragon speed is negative: " + dragon.getMaximumSpeed());
        }
        if (dragon.getDateOfBirth() == null) {
            throw new ValidationException("Dragon date of birth is null.");
        }
        LocalDate today = LocalDate.now(clock);
        if (dragon.getDateOfBirth() != null && dragon.getDateOfBirth().isAfter(today)) {
            throw new ValidationException("Dragon born after today: " + today);
        }
    }

    static private Dragon rowToDragon(ResultSet rs) throws SQLException {
        Dragon result = new Dragon();
        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));
        result.setElement(stringToElement(rs.getString("element")));
        result.setDateOfBirth(sqlDateToLocalDate(rs.getDate("born")));
        result.setMaximumSpeed(rs.getInt("speed"));
        return result;
    }

    private static DragonElement stringToElement(String element) {
        return element == null ? null : DragonElement.valueOf(element);
    }

    private static String elementToString(DragonElement element) {
        return element == null ? null : element.name();
    }

    private static LocalDate sqlDateToLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static Date localDateToSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

}
