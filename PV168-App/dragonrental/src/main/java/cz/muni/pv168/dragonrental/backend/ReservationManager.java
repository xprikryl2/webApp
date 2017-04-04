package cz.muni.pv168.dragonrental.backend;

import java.util.List;

/**
 * This class allows reservation manipulation in connection with database.
 *
 * @author Zuzana Wolfová
 */
public interface ReservationManager {

    /**
     * finds a reservation with given ID
     * @param id
     * @return Reservation object, or null if no reservation was found
     */    
    public Reservation getReservation(Long id);
    
    /**
     * This method adds new reservation to the database and generates id.
     * @param reservation to be created
     */
    public void createReservation(Reservation reservation);

    /**
     * This method removes reservation from the database.
     * @param reservation to be removed
     */
    public void removeReservation(Reservation reservation);

    /**
     * This method updates the reservation.
     * @param reservation to be updated
     */
    public void updateReservation(Reservation reservation);

    /**
     * This method lists all reservations stored in the database.
     * @return list of all reservations in the database
     */
    public List<Reservation> listAllReservations();

    /**
     * This method searches for a reservation in database using various criteria.
     * @param filter
     * @return list of all reservations matching searching criteria
     */
    public List<Reservation> findReservation(ReservationFilter filter);
}
