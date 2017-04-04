package cz.muni.pv168.dragonrental.backend;

import cz.muni.pv168.dragonrental.common.DBUtils;
import cz.muni.pv168.dragonrental.common.IllegalEntityException;
import cz.muni.pv168.dragonrental.common.ServiceFailureException;
import cz.muni.pv168.dragonrental.common.ValidationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
/**
 * @author Petr Soukop
 */
public class ReservationManagerImpl implements ReservationManager {
    
    private DataSource dataSource;
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    @Override
    public Reservation getReservation(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID == null");
        }
        List<Reservation> reservations = findReservation(new ReservationFilter().withId(id));
        if (reservations.isEmpty()) {
            return null;
        }
        if (reservations.size() > 1) {
            throw new IllegalEntityException("Found more than 1 reservations with null" + id);
        }
        return reservations.get(0);
    }
        
    @Override
    public void createReservation(Reservation reservation) {
        checkDataSource();
        validateForCreating(reservation);
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(
                    "INSERT INTO Reservation "
                  + "(timeFrom, timeTo,borrower,dragon,moneyPaid,pricePerHour)"
                  + " VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS
                                                    );
            statement.setTimestamp(1, Timestamp.valueOf(reservation.getFrom()));
            if (reservation.getTo() != null) {
                statement.setTimestamp(2, Timestamp.valueOf(reservation.getTo()));
            } else {
            statement.setTimestamp(2, null);
            }
            statement.setLong(3, reservation.getBorrower().getId());
            statement.setLong(4, reservation.getDragon().getId());
            statement.setBigDecimal(5, reservation.getMoneyPaid());
            statement.setBigDecimal(6, reservation.getPricePerHour());
            int updateCount = statement.executeUpdate();
            DBUtils.checkUpdatesCount(updateCount, reservation, true);
            Long id = DBUtils.getId(statement.getGeneratedKeys());
            reservation.setId(id);
            connection.commit();
        } catch(SQLException ex) {
            String msg = "Error when trying to create reservation" + reservation;
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, statement);
        }
    }
    @Override
    public void removeReservation(Reservation reservation) {
        checkDataSource();
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation == null");
        }
        if (reservation.getId() == null) {
            throw new ValidationException("Reservation has null id");
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(
                    "DELETE FROM Reservation WHERE id = ?");
            statement.setLong(1, reservation.getId());
            
            int updateCount = statement.executeUpdate();
            DBUtils.checkUpdatesCount(updateCount, reservation, false);
            connection.commit();
        } catch(SQLException ex) {
            String msg = "Error when trying to remove reservation" + reservation;
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, statement);
        }
    }
    @Override
    public void updateReservation(Reservation reservation) {
        checkDataSource();
        if (reservation == null) {
            throw new IllegalArgumentException("Trying to update with null");
        }
        if (reservation.getId() == null) {
            throw new ValidationException("Reservation has no ID");
        }
        Connection connection = null;
        PreparedStatement statement= null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            validateForUpdating(getReservation(reservation.getId()), reservation);
            statement = connection.prepareStatement(
                    "UPDATE Reservation SET timeTo = ?, moneyPaid = ? WHERE id = ?");
            statement.setTimestamp(1, Timestamp.valueOf(reservation.getTo()));
            statement.setBigDecimal(2, reservation.getMoneyPaid());
            statement.setLong(3, reservation.getId());
            int updateCount = statement.executeUpdate();
            DBUtils.checkUpdatesCount(updateCount, reservation, false);
            connection.commit();
        } catch(SQLException ex) {
            String msg = "Error when trying to update reservation" + reservation;
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, statement);
        }
    }
    @Override
    public List<Reservation> listAllReservations() {
        return findReservation(new ReservationFilter());
    }
    
    
    @Override
    public List<Reservation> findReservation(ReservationFilter filter) {
        checkDataSource();
        if (filter == null) {
            throw new IllegalArgumentException("Filter is null");
        }
        try(Connection connection = dataSource.getConnection()) {
            try(
                    PreparedStatement findIdSt = connection.prepareStatement(
                    "SELECT * FROM Reservation" +
                    (filter.toSQL().equals("") ? "" : " WHERE ") + filter.toSQL())
                ) {
                try(ResultSet resultSet = findIdSt.executeQuery()) {
                    List<Reservation> reservations = parsedResultSet(resultSet);
                    return reservations;
                }
            }
        } catch(SQLException ex) {
            String msg = "Error when trying to find reservations";
            System.out.println(msg);
            throw new ServiceFailureException(msg, ex);
        }
    }
    
    //-------------------------------------------------------------------------
    
    private List<Reservation> parsedResultSet(ResultSet resultSet) throws SQLException {
        PersonManagerImpl personManager = new PersonManagerImpl();
        personManager.setDataSource(dataSource);
                                                //this null here is a bit dodgey
                         //but it suffices the contract and the clock isn't used
        DragonManagerImpl dragonManager = new DragonManagerImpl(null);
        dragonManager.setDataSource(dataSource);
        
        List<Reservation> reservationList = new ArrayList<>();
        while (resultSet.next()) {
            Reservation res = new Reservation();
            res.setId(resultSet.getLong("id"));
            res.setFrom(resultSet.getTimestamp("timeFrom").toLocalDateTime());
                Timestamp timeTo = resultSet.getTimestamp("timeTo");
            if (timeTo == null) {
                res.setTo(null);
            } else {
                res.setTo(timeTo.toLocalDateTime());
            }
                Person person = new Person();
                person.setId(resultSet.getLong("borrower"));
            if (personManager.findPeople(person).size() != 1) {
                throw new ValidationException("Error when fetching person for reservation " + res.getId());
            }
            res.setBorrower(personManager.findPeople(person).get(0));
                Dragon dragon = new Dragon();
                dragon.setId(resultSet.getLong("dragon"));
            if (dragonManager.findDragons(dragon).size() != 1) {
                throw new ValidationException("Error when fetching dragon for reservation " + res.getId());
            }
            res.setDragon(dragonManager.findDragons(dragon).get(0));
            res.setMoneyPaid(resultSet.getBigDecimal("moneyPaid"));
            res.setPricePerHour(resultSet.getBigDecimal("pricePerHour"));
            reservationList.add(res);
        }
        return reservationList;
    }
    
    private void validateForCreating(Reservation reservation) throws ValidationException {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation == null");
        }
        if (reservation.getId() != null) {
            throw new ValidationException("Reservation has ID already set");
        }
        if (reservation.getBorrower() == null) {
            throw new ValidationException("Reservation has no borrower set");
        }
        if (reservation.getDragon() == null) {
            throw new ValidationException("Reservation has no dragon set");
        }
        if (reservation.getFrom() == null) {
            throw new ValidationException("Reservation has no time from set");
        }
        if (reservation.getMoneyPaid() == null) {
            throw new ValidationException("Reservation has moneyPaid == null");
        }
        if (reservation.getPricePerHour() == null) {
            throw new ValidationException("Reservation has pricePerHour == null");
        }
        if (reservation.getPricePerHour().signum() == -1) {
            throw new ValidationException("Reservation has negative price per hour");
        }
        if (reservation.getMoneyPaid().signum() == -1) {
            throw new ValidationException("Reservation has negative money paid");
        }
    }
    
    private void validateForUpdating2(Reservation reservation) throws ValidationException, IllegalEntityException {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation == null");
        }
        if (reservation.getId() == null) {
            throw new ValidationException("Reservation has no ID");
        }
        if (reservation.getBorrower() != null) {
            throw new IllegalEntityException("Borrower cannot be changed");
        }
        if (reservation.getDragon() != null) {
            throw new IllegalEntityException("Dragon cannot be changed");
        }
        if (reservation.getFrom() != null) {
            throw new IllegalEntityException("Time from cannot be changed");
        }
        if (reservation.getPricePerHour() != null) {
            throw new IllegalEntityException("Price per hour cannot be changed");
        }
    }
    
    private void validateForUpdating(Reservation existingRes, Reservation newRes) {
        if (!existingRes.getFrom().equals(newRes.getFrom())) {
            throw new IllegalEntityException("Time from cannot be changed");
        }
        if (existingRes.getTo() != null &&
                 newRes.getTo() != null && 
                 !existingRes.getTo().equals(newRes.getTo())) {
            throw new IllegalEntityException("Time to is already set");
        }
        if (!existingRes.getBorrower().equals(newRes.getBorrower())) {
            throw new IllegalEntityException("Borrwer cannot be changed");
        }
        if (!existingRes.getDragon().equals(newRes.getDragon())) {
            throw new IllegalEntityException("Dragon cannot be changed");
        }
        if (!existingRes.getPricePerHour().equals(newRes.getPricePerHour())) {
            throw new IllegalEntityException("Price per hour cannot be changed");
        }
    }
}