package cz.muni.pv168.dragonrental.backend;

import cz.muni.pv168.dragonrental.common.IllegalEntityException;
import cz.muni.pv168.dragonrental.common.ServiceFailureException;
import cz.muni.pv168.dragonrental.common.ValidationException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementation class of Store Manager.
 *
 * @author Zuzana Wolfová
 */
public class StoreManagerImpl implements StoreManager {

    private ReservationManagerImpl reservationManager;
    
    private void init() {
        reservationManager = new ReservationManagerImpl();
    }
    /*
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("Data source is not set.");
        }
    }

    public StoreManagerImpl(Clock clock) {
        this.clock = clock;
    }
    */
    @Override
    public void lendDragon(Dragon dragon, Person person, LocalDateTime from, LocalDateTime to) throws IllegalEntityException, ServiceFailureException, ValidationException {
        init();
        Reservation reservation = new Reservation();
        reservation.setBorrower(person);
        reservation.setDragon(dragon);
        reservation.setFrom(from);
        reservation.setTo(to);
        reservation.setMoneyPaid(new BigDecimal(0));
        reservation.setPricePerHour(new BigDecimal(400));
        
        reservationManager.createReservation(reservation);
    }

    @Override
    public void returnDragon(Reservation reservation, Dragon dragon) throws IllegalEntityException, ServiceFailureException {
        init();
        if(reservation == null) {
            throw new IllegalEntityException("Reservation is null.");
        }
        if(dragon == null) {
            throw new IllegalEntityException("Dragon is null.");
        }
        //if(findReservation(reservation).isEmpty()) {
        //    throw new ValidationException("Reservation does not exist within our db.");
        //}
        if(!reservation.getDragon().equals(dragon)){
            throw new IllegalEntityException("Returning dragon does not match his reservation.");
        }
        
        reservation.setTo(LocalDateTime.now());
        reservationManager.updateReservation(reservation);
    }

    @Override
    public List<Dragon> findFreeDragonsInTimeSpan(LocalDateTime from, LocalDateTime to) throws ServiceFailureException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void payReservation(Reservation reservation, BigDecimal amount) throws IllegalEntityException, ServiceFailureException {
        init();
        if(reservation == null) {
            throw new IllegalEntityException("Reservation is null.");
        }
        if(amount == null) {
            throw new IllegalEntityException("Amount is null.");
        }
        //if(findReservation(reservation).isEmpty) {
        //    throw new ValidationException("Reservation does not exist within our db.");
        //}
        
        BigDecimal paid = reservation.getMoneyPaid();
        BigDecimal diffInHours = new BigDecimal(ChronoUnit.HOURS.between(reservation.getFrom(), reservation.getTo()));
        BigDecimal wanted = reservation.getPricePerHour().multiply(diffInHours);
        wanted = wanted.subtract(paid);
        
        if(amount.compareTo(wanted) > 0) {
            throw new ValidationException("Amount is higher than money to paid.");
        }
        reservation.setMoneyPaid(paid.add(amount));
        reservationManager.updateReservation(reservation);
    }

    
}
