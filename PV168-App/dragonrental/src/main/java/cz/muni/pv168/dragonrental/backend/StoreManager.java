package cz.muni.pv168.dragonrental.backend;

import java.math.BigDecimal;
import java.util.List;
import cz.muni.pv168.dragonrental.common.IllegalEntityException;
import cz.muni.pv168.dragonrental.common.ServiceFailureException;
import cz.muni.pv168.dragonrental.common.ValidationException;
import java.time.LocalDateTime;

/**
 * Interface allowing manipulation with Dragons, People and Reservations
 *
 * @author Zuzana Wolfová
 */
public interface StoreManager {

    /**
     * This method allows to lend a dragon to a given person.
     *
     * @throws IllegalArgumentException if any of the arguments is null or if "to" is before "from"
     * @throws IllegalEntityException if dragon or person is not in the database
     * @throws ValidationException when dragon is currently reserved, when trying to reserve and borrow one the same day.
     * @throws ServiceFailureException when database fails
     * @param dragon to lend
     * @param person borrower
     */
    public void lendDragon(Dragon dragon, Person person, LocalDateTime from, LocalDateTime to)
            throws IllegalEntityException, ServiceFailureException, ValidationException;

    /**
     * Allows to return a dragon. The reservation does not have to be paid before the dragon is returned.
     * The end date in reservation represents the date when a dragon was returned.
     *
     * @throws IllegalArgumentException if dragon is null
     * @throws IllegalEntityException if there is no such dragon in database
     * @throws ServiceFailureException when database fails
     * @param dragon returned dragon
     */
    public void returnDragon(Reservation reservation, Dragon dragon) throws IllegalEntityException, ServiceFailureException;

    /**
     * Allows to find free dragons to a given time span. A dragon shall not be borrowed the day it is returned.
     *
     * @throws IllegalArgumentException if either of the dates is null, or "to" is before "from"
     * @throws ServiceFailureException when database fails
     * @param from from
     * @param to to
     * @return list of available dragons
     */
    public List<Dragon> findFreeDragonsInTimeSpan(LocalDateTime from, LocalDateTime to) throws  ServiceFailureException;

    /**
     * Allows a person to pay a reservation. Reservation can be paid partially and after a dragon is returned.
     *
     * @throws IllegalEntityException if either of the arguments is null or amount is <= 0.
     * @throws IllegalEntityException when there is no such reservation
     * @throws ServiceFailureException when database fails
     * @param reservation reservation
     * @param amount amount to be paid
     */
    public void payReservation(Reservation reservation, BigDecimal amount) throws IllegalEntityException, ServiceFailureException;

}
