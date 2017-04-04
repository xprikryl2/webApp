/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

/**
 * A simple builder class to make reservations
 * for tests with methods that are readable
 * 
 * was suggested in BodyManagerImplTest of project GraveManager-Backend by 
 * Mr.Adamek
 * 
 * used example at
 * https://www.javacodegeeks.com/2013/06/builder-pattern-good-for-code-great-for-tests.html
 * 
 * @author Petr Soukop
 */
public class ReservationBuilder {
    
    private Long id = null;
    private LocalDateTime from = LocalDateTime.of(1450, Month.JANUARY, 1, 12, 0, 0, 0);
    private LocalDateTime to = LocalDateTime.of(1450, Month.JANUARY, 11, 12, 0, 0, 0);
    private Person borrower = new Person();
    private Dragon dragon = new Dragon();
    private BigDecimal moneyPaid = new BigDecimal(0);
    private BigDecimal pricePerHour = new BigDecimal(1);
    
    public ReservationBuilder(){
    }

    public ReservationBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    /**
     * changes reservationbuilder's time from
     * @param from use LocalDateTime.of(int year, Month.MONTH_NAME, int day,
     * int hour, int minute, int second, int millisecond) to make a new time,
     * seconds and milliseconds may be omitted
     * @return 
     */
    public ReservationBuilder withFrom(LocalDateTime from) {
        this.from = from;
        return this;
    }
    
    /**
     * changes reservationbuilder's time to
     * @param to use LocalDateTime.of(int year, Month.MONTH_NAME, int day,
     * int hour, int minute, int second, int millisecond) to make a new time,
     * seconds and milliseconds may be omitted
     * @return 
     */
    public ReservationBuilder withTo(LocalDateTime to) {
        this.to = to;
        return this;
    }
    
    public ReservationBuilder withBorrower(Person borrower) {
        this.borrower = borrower;
        return this;
    }
    
    public ReservationBuilder withDragon(Dragon dragon) {
        this.dragon = dragon;
        return this;
    }
    
    /**
     * changes reservationbuilder's moneyPaid
     * @param moneyPaid use constructor new BigDecimal(int value) to make a
     * BigDecimal value
     * @return 
     */
    public ReservationBuilder withMoneyPaid(BigDecimal moneyPaid) {
        this.moneyPaid = moneyPaid;
        return this;
    }
    
    /**
     * changes reservationbuilder's pricePerHour
     * @param pricePerHour use constructor new BigDecimal(int value) to make a
     * BigDecimal value
     * @return 
     */
    public ReservationBuilder withPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
        return this;
    }
    
    public Reservation build() {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setFrom(from);
        reservation.setTo(to);
        reservation.setBorrower(borrower);
        reservation.setDragon(dragon);
        reservation.setMoneyPaid(moneyPaid);
        reservation.setPricePerHour(pricePerHour);
        return reservation;
    }
}
