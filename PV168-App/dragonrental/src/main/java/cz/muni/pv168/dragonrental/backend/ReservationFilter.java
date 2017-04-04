/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;


/**
 * SQL attacks ?
 * @author Petr Soukop
 */
public class ReservationFilter {
        private Long id = null;
        private LocalDateTime fromIsBefore = null;
        private LocalDateTime fromIsAfter = null;
        private LocalDateTime toIsBefore = null;
        private LocalDateTime toIsAfter = null;
        private Long borrower = null;
        private Long dragon = null;
        private BigDecimal moneyPaidIsLessThan = null;
        private BigDecimal moneyPaidIsMoreThan = null;
        private BigDecimal pricePerHourIsLessThan = null;
        private BigDecimal pricePerHourIsMoreThan = null;
        private boolean onlyUnpaid = false;
        private boolean onlyActive = false;
                
        public ReservationFilter withId(Long id) {
            this.id = id;
            return this;
        }
        
        public ReservationFilter withFromIsBefore(LocalDateTime time) {
            this.fromIsBefore = time;
            return this;
        }
        
        public ReservationFilter withFromIsAfter(LocalDateTime time) {
            this.fromIsAfter = time;
            return this;
        }
        
        public ReservationFilter withToIsBefore(LocalDateTime time) {
            this.toIsBefore = time;
            return this;
        }
        
        public ReservationFilter withToIsAfter(LocalDateTime time) {
            this.toIsAfter = time;
            return this;
        }
        
        public ReservationFilter withBorrower(Long borrower) {
            this.borrower = borrower;
            return this;
        }
        
        public ReservationFilter withDragon(Long dragon) {
            this.dragon = dragon;
            return this;
        }
        
        public ReservationFilter withMoneyPaidIsLessThan(BigDecimal moneyPaid) {
            this.moneyPaidIsLessThan = moneyPaid;
            return this;
        }
        
        public ReservationFilter withMoneyPaidIsMoreThan(BigDecimal moneyPaid) {
            this.moneyPaidIsMoreThan = moneyPaid;
            return this;
        }
        
        public ReservationFilter withPricePerHourIsLessThan(BigDecimal pricePerHour) {
            this.pricePerHourIsLessThan = pricePerHour;
            return this;
        }
        
        public ReservationFilter withPricePerHourIsMoreThan(BigDecimal pricePerHour) {
            this.pricePerHourIsMoreThan = pricePerHour;
            return this;
        }
        
        public ReservationFilter withOnlyUnpaid(boolean value) {
            this.onlyUnpaid = value;
            return this;
        }
        
        public ReservationFilter withOnlyActive(boolean value) {
            this.onlyActive = value;
            return this;
        }
        
        public String toSQL() {
            boolean anyNull = false;
            StringBuilder stringBuilder = new StringBuilder();
            if (id != null) {
                stringBuilder.append("id = ").append(id).append(" AND ");
                anyNull = true;
            }
            /*
            Disabled because I can't make it work. Timestamp.toString() doesn't fit
            derby's TIMESTAMP format
            Would have to use PreparedStatement
            */
            if (fromIsBefore != null) {
                stringBuilder.append("timeFrom < \'").append(Timestamp.valueOf(fromIsBefore)).append("\' AND ");
                anyNull = true;
            }
            if (fromIsAfter != null) {
                stringBuilder.append("timeFrom > \'").append(Timestamp.valueOf(fromIsAfter)).append("\' AND ");
                anyNull = true;
            }
            if (toIsBefore != null) {
                stringBuilder.append("timeTo < \'").append(Timestamp.valueOf(toIsBefore)).append("\' AND ");
                anyNull = true;
            }
            if (toIsAfter != null) {
                stringBuilder.append("timeTo > \'").append(Timestamp.valueOf(toIsAfter)).append("\' AND ");
                anyNull = true;
            }
            if (borrower != null) {
                stringBuilder.append("borrower = ").append(borrower).append(" AND ");
                anyNull = true;
            }
            if (dragon != null) {
                stringBuilder.append("dragon = ").append(dragon).append(" AND ");
                anyNull = true;
            }
            if (moneyPaidIsLessThan != null) {
                stringBuilder.append("moneyPaid < ").append(moneyPaidIsLessThan).append(" AND ");
                anyNull = true;
            }
            if (moneyPaidIsMoreThan != null) {
                stringBuilder.append("moneyPaid > ").append(moneyPaidIsMoreThan).append(" AND ");
                anyNull = true;
            }
            if (pricePerHourIsLessThan != null) {
                stringBuilder.append("pricePerHour < ").append(pricePerHourIsLessThan).append(" AND ");
                anyNull = true;
            }
            if (pricePerHourIsMoreThan != null) {
                stringBuilder.append("pricePerHour > ").append(pricePerHourIsMoreThan).append(" AND ");
                anyNull = true;
            }
            if (onlyUnpaid) {
                stringBuilder.append("{fn TIMESTAMPDIFF(SQL_TSI_HOUR, timeFrom, timeTO)} * pricePerHour > moneyPaid").append(" AND ");
                anyNull = true;
            }
            if (onlyActive) {
                stringBuilder.append("timeFrom < CURRENT_TIMESTAMP AND (timeTo IS NULL OR timeTo > CURRENT_TIMESTAMP)").append(" AND ");
                anyNull = true;
            }
            return !anyNull ? "" : stringBuilder.substring(0, stringBuilder.lastIndexOf(" AND "));
        }
    }
