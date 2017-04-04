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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Petr Soukop
 */
public class ReservationManagerImplTest {
    
    private ReservationManagerImpl rManager;
    private DataSource ds;
    private PersonManagerImpl pManager;
    private DragonManagerImpl dManager;
    private Person bob = sampleBobPerson().build();
    private Person helmut = sampleHelmutPerson().build();
    private Dragon grigori = sampleGrigoriDragon().build();
    private Dragon icey = sampleIceyDragon().build();
        
    
    //constant for fixed current time
    //it's exactly 12:00 on the 1st of January, year 1450
    private final static LocalDateTime NOW = 
            LocalDateTime.of(1450, Month.JANUARY, 1, 12, 0, 0, 0);
    
    //-------------------------------------------------------------------------
    
    private PersonBuilder sampleBobPerson() {
        return new PersonBuilder()
                .withId(null)
                .withName("Bob")
                .withSurname("D'Builder")
                .withEmail("sleep@work.alot");
    }
    
    private PersonBuilder sampleHelmutPerson() {
        return new PersonBuilder()
                .withId(null)
                .withName("Helmut")
                .withSurname("Schwartz")
                .withEmail("schwartz.helmut@email.de");
    }
    
    private DragonBuilder sampleGrigoriDragon() {
        return new DragonBuilder()
                .withId(null)
                .withName("Grigori")
                .withDateOfBirth(LocalDate.of(1150, Month.JUNE, 12))
                .withElement(DragonElement.FIRE)
                .withMaximumSpeed(200);
    }
    
    private DragonBuilder sampleIceyDragon() {
        return new DragonBuilder()
                .withId(null)
                .withName("Icey")
                .withDateOfBirth(LocalDate.of(1175, Month.NOVEMBER, 7))
                .withElement(DragonElement.WATER)
                .withMaximumSpeed(150);
    }
    
    //a valid reservation to be created
    private ReservationBuilder sampleReservation1() {
        return new ReservationBuilder()
                .withId(null)
                .withBorrower(helmut)
                .withDragon(grigori)
                .withFrom(LocalDateTime.of(1450, Month.FEBRUARY, 1, 12, 0))
                .withTo(LocalDateTime.of(1450, Month.FEBRUARY, 16, 12, 0))
                .withMoneyPaid(new BigDecimal(0))
                .withPricePerHour(new BigDecimal(50));
    }
    
    //another valid reservation to be created
    private ReservationBuilder sampleReservation2() {
        return new ReservationBuilder()
                .withId(null)
                .withBorrower(bob)
                .withDragon(icey)
                .withFrom(LocalDateTime.of(1450, Month.MARCH, 25, 12, 0))
                .withTo(LocalDateTime.of(1450, Month.APRIL, 1, 12, 0))
                .withMoneyPaid(new BigDecimal(300))
                .withPricePerHour(new BigDecimal(60));
    }
    
    //a valid reservation with the minimum parameters
    private ReservationBuilder sampleReservation3() {
        return new ReservationBuilder()
                .withId(null)
                .withBorrower(helmut)
                .withDragon(icey)
                .withFrom(LocalDateTime.of(1450, Month.JUNE, 5, 12, 0))
                .withTo(null)
                .withMoneyPaid(new BigDecimal(0))
                .withPricePerHour(new BigDecimal(40));
    }
    
    //-------------------------------------------------------------------------
    
    //to make a Clock object with a fixed time given by NOW constant
    //Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:reservationManager-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds,ReservationManager.class.getResource("createTables.sql"));
        rManager = new ReservationManagerImpl();
        rManager.setDataSource(ds);
        pManager = new PersonManagerImpl();
        pManager.setDataSource(ds);
        dManager = new DragonManagerImpl(Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
        dManager.setDataSource(ds);
        pManager.addPerson(bob);
        pManager.addPerson(helmut);
        dManager.createDragon(grigori);
        dManager.createDragon(icey);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds,ReservationManager.class.getResource("dropTables.sql"));
    }
    
    //-------------------------------------------------------------------------
    
    @Test
    public void testGetReservation() {
        Reservation reservation = sampleReservation1().build();
        
        rManager.createReservation(reservation);
        
        assertThat( rManager.getReservation(reservation.getId()) )
                .isEqualToComparingFieldByField(reservation);
    }
    
    @Test
    public void testGetNonExistingReservation() {
        assertThat(rManager.getReservation(1234L))
                .isNull();
    }
    
    //-------------------------------------------------------------------------
    
    @Test
    public void testCreateValidReservation() {
        Reservation reservation = sampleReservation1()
                .withId(null)
                .build();
        
        rManager.createReservation(reservation);
        Long reservationId = reservation.getId();
        
        assertThat( reservationId )
                .isNotNull();
        assertThat( rManager.getReservation( reservation.getId() ) )
                .isNotSameAs(reservation)
                .isEqualToComparingFieldByField(reservation);
    }
    
    @Test
    public void testCreateValidReservationWithNullTimeTo() {
        Reservation reservation = sampleReservation1()
                .withId(null)
                .withTo(null)
                .build();
        
        rManager.createReservation(reservation);
        Long reservationId = reservation.getId();
        
        assertThat( reservationId )
                .isNotNull();
        assertThat( rManager.getReservation( reservation.getId() ) )
                .isNotSameAs(reservation)
                .isEqualToComparingFieldByField(reservation);
    }
    
    @Test
    public void testCreateNullReservation() {
        assertThatThrownBy( () -> rManager.createReservation(null) )
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testCreateReservationWithExistingId() {
        Reservation reservation1 = sampleReservation1()
                .withId(1234L)
                .build();
        
        assertThatThrownBy( () -> rManager.createReservation(reservation1) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testCreateReservationWithNoTimeFrom() {
        Reservation reservation = sampleReservation1()
                .withFrom(null)
                .build();
        
        assertThatThrownBy( () -> rManager.createReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testCreateReservationWithNoDragon() {
        Reservation reservation = sampleReservation1()
                .withDragon(null)
                .build();
        
        assertThatThrownBy( () -> rManager.createReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testCreateReservationWithNoBorrower() {
        Reservation reservation = sampleReservation1()
                .withBorrower(null)
                .build();
        
        assertThatThrownBy( () -> rManager.createReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testCreateReservationWithNoMoneyPaid() {
        Reservation reservation = sampleReservation1()
                .withMoneyPaid(null)
                .build();
        
        assertThatThrownBy( () -> rManager.createReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testCreateReservationWithNegativeMoneyPaid() {
        Reservation reservation = sampleReservation1()
                .withMoneyPaid(new BigDecimal(-100))
                .build();
        
        assertThatThrownBy( () -> rManager.createReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testCreateReservationWithNoPricePerHour() {
        Reservation reservation = sampleReservation1()
                .withPricePerHour(null)
                .build();
        
        assertThatThrownBy( () -> rManager.createReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testCreateReservationWithNegativePricePerHour() {
        Reservation reservation = sampleReservation1()
                .withPricePerHour(new BigDecimal(-50))
                .build();
        
        assertThatThrownBy( () -> rManager.createReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    //-------------------------------------------------------------------------
    
    @Test
    public void testUpdateReservation() {
        Reservation reservation = sampleReservation3()
                .withTo(null)
                .withMoneyPaid(new BigDecimal(0))
                .build();
        Reservation anotherReservation = sampleReservation1().build();
        
        rManager.createReservation(reservation);
        rManager.createReservation(anotherReservation);
        
        reservation.setTo(LocalDateTime.of(1450, Month.JUNE, 25, 12, 0));
        reservation.setMoneyPaid(new BigDecimal(200));
        
        rManager.updateReservation(reservation);
        
        assertThat( rManager.getReservation(reservation.getId()) )
                .isEqualToComparingFieldByField(reservation);
        assertThat( rManager.getReservation(anotherReservation.getId()) )
                .isEqualToComparingFieldByField(anotherReservation);
    }
    
    @Test
    public void testUpdateReservationWithNullId() {
        Reservation reservation = sampleReservation1()
                .withId(null)
                .build();
        
        assertThatThrownBy( () -> rManager.updateReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testUpdateNullReservation() {
        assertThatThrownBy( () -> rManager.updateReservation(null) )
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testUpdateReservationWithTimeFrom() {
        Reservation reservation = sampleReservation1().build();
        
        rManager.createReservation(reservation);
        
        reservation.setFrom(LocalDateTime.of(1451, Month.FEBRUARY, 10, 12, 0));
        
        assertThatThrownBy( () -> rManager.updateReservation(reservation) )
                .isInstanceOf(IllegalEntityException.class);
    }
    
    @Test
    public void testUpdateReservationWithPerson() {
        Reservation reservation = sampleReservation1()
                .withBorrower(bob)
                .build();
        
        rManager.createReservation(reservation);
        
        reservation.setBorrower(helmut);
        
        assertThatThrownBy( () -> rManager.updateReservation(reservation) )
                .isInstanceOf(IllegalEntityException.class);
    }
    
    @Test
    public void testUpdateReservationWithDragon() {
        Reservation reservation = sampleReservation1()
                .withDragon(grigori)
                .build();
        
        rManager.createReservation(reservation);
        
        reservation.setDragon(icey);
        
        assertThatThrownBy( () -> rManager.updateReservation(reservation) )
                .isInstanceOf(IllegalEntityException.class);
    }
    
    @Test
    public void testUpdateReservationWithPricePerHour() {
        Reservation reservation = sampleReservation1()
                .withPricePerHour( new BigDecimal(30) )
                .build();
        
        rManager.createReservation(reservation);
        
        reservation.setPricePerHour( new BigDecimal(40) );
        
        assertThatThrownBy( () -> rManager.updateReservation(reservation) )
                .isInstanceOf(IllegalEntityException.class);
    }
    
    @Test
    public void testUpdateReservationWithDifferentTimeTo() {
        Reservation reservation = sampleReservation1()
                .withTo( LocalDateTime.of(1450, Month.FEBRUARY, 16, 12, 0) )
                .build();
        
        rManager.createReservation(reservation);
        
        reservation.setTo( LocalDateTime.of(1450, Month.APRIL, 16, 12, 0) );
        
        assertThatThrownBy( () -> rManager.updateReservation(reservation) )
                .isInstanceOf(IllegalEntityException.class);
    }
    
    //-------------------------------------------------------------------------
    
    @Test
    public void testRemoveNullReservation() {
        assertThatThrownBy( () -> rManager.removeReservation(null) )
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testRemoveReservationWithNullId() {
        Reservation reservation = sampleReservation1()
                .withId(null)
                .build();
        
        assertThatThrownBy( () -> rManager.removeReservation(reservation) )
                .isInstanceOf(ValidationException.class);
    }
    
    @Test
    public void testRemoveReservationLast() {
        Reservation reservation = sampleReservation1().build();
        
        rManager.createReservation(reservation);
        
        assertThat( rManager.listAllReservations() )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation);
        
        rManager.removeReservation(reservation);
        
        assertThat( rManager.listAllReservations() )
                .isEmpty();
    }
    
    public void testRemoveReservationSecondLast() {
        Reservation reservation1 = sampleReservation1().build();
        Reservation reservation2 = sampleReservation2().build();
        
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        
        assertThat( rManager.listAllReservations() )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation1, reservation2);
        
        rManager.removeReservation(reservation1);
        
        assertThat( rManager.listAllReservations() )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation2);
    }
    
    @Test
    public void testRemoveNonPresentReservation() {
        Reservation reservation1 = sampleReservation1().build();
        Reservation reservation2 = sampleReservation2()
                .withId(1234L)
                .build();
        
        rManager.createReservation(reservation1);
        
        assertThat( rManager.listAllReservations() )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation1);
        
        assertThatThrownBy( () -> rManager.removeReservation(reservation2) )
                .isInstanceOf(IllegalEntityException.class);
        
        assertThat( rManager.listAllReservations() )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation1);
    }

    //-------------------------------------------------------------------------
    
    @Test
    public void testListNoReservations() {
        assertThat( rManager.listAllReservations() )
                .isEmpty();
    }
    
    @Test
    public void testListAllReservations() {
        Reservation reservation1 = sampleReservation1().build();
        Reservation reservation2 = sampleReservation2().build();
        
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        
        assertThat( rManager.listAllReservations() )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation1, reservation2);
    }
    
    //-------------------------------------------------------------------------
 
    @Test
    public void testFindNullReservation() {
        assertThatThrownBy( () -> rManager.findReservation(null) )
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testFindReservationForId() {
        Reservation reservation1 = sampleReservation1().build();
        Reservation reservation2 = sampleReservation2().build();
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        
        assertThat( rManager.findReservation( 
                new ReservationFilter().withId(reservation1.getId())
                                            ) )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation1);
    }
    
    @Test
    public void testFindReservationForDragon() {
        Reservation reservation1 = sampleReservation1()
                .withDragon(grigori)
                .build();
        Reservation reservation2 = sampleReservation2()
                .withDragon(grigori)
                .build();
        Reservation reservation3 = sampleReservation3()
                .withDragon(icey)
                .build();
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        rManager.createReservation(reservation3);
        
        assertThat( rManager.findReservation( 
                new ReservationFilter().withDragon(grigori.getId())
                                            ) )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation1, reservation2);
    }
    
    @Test
    public void testFindReservationForPerson() {
        Reservation reservation1 = sampleReservation1()
                .withBorrower(bob)
                .build();
        Reservation reservation2 = sampleReservation2()
                .withBorrower(helmut)
                .build();
        Reservation reservation3 = sampleReservation3()
                .withBorrower(helmut)
                .build();
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        rManager.createReservation(reservation3);
        
        assertThat( rManager.findReservation( 
                new ReservationFilter().withBorrower(helmut.getId())
                                            ) )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation2, reservation3);
    }
    
    
    //Can't make this work
    
    @Test
    public void testFindReservationAfterTimeFrom() {
        Reservation reservation1 = sampleReservation1()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 16, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.APRIL, 19, 12, 0) )
                .build();
        Reservation reservation2 = sampleReservation2()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 20, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.APRIL, 30, 12, 0) )
                .build();
        Reservation reservation3 = sampleReservation3()
                .withFrom( LocalDateTime.of(1450, Month.JUNE, 16, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.JULY, 6, 12, 0) )
                .build();
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        rManager.createReservation(reservation3);
        
        assertThat( rManager.findReservation(
                new ReservationFilter().withFromIsAfter(LocalDateTime.of(1450, Month.APRIL, 20, 11, 0))
                                            ) )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation2, reservation3);
    }
    
    @Test
    public void testFindReservationInATimeInterval() {
        Reservation reservation1 = sampleReservation1()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 16, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.APRIL, 19, 12, 0) )
                .build();
        Reservation reservation2 = sampleReservation2()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 20, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.APRIL, 30, 12, 0) )
                .build();
        Reservation reservation3 = sampleReservation3()
                .withFrom( LocalDateTime.of(1450, Month.JUNE, 16, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.JULY, 6, 12, 0) )
                .build();
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        rManager.createReservation(reservation3);
        
        assertThat( rManager.findReservation(
                new ReservationFilter().withFromIsAfter(LocalDateTime.of(1450, Month.APRIL, 19, 11, 0))
                .withToIsBefore(LocalDateTime.of(1450, Month.JUNE, 17, 12, 0))
                                            ) )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation2);
    }
    
    
    @Test
    public void testFindReservationWithPricePerHourLargerThan() {
        Reservation reservation1 = sampleReservation1()
                .withPricePerHour(new BigDecimal(20))
                .build();
        Reservation reservation2 = sampleReservation2()
                .withPricePerHour(new BigDecimal(30))
                .build();
        Reservation reservation3 = sampleReservation3()
                .withPricePerHour(new BigDecimal(40))
                .build();
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        rManager.createReservation(reservation3);
        
        assertThat( rManager.findReservation(
                new ReservationFilter().withPricePerHourIsMoreThan(new BigDecimal(25))
                                            ) )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation2, reservation3);
    }
    
    
    //The OnlyActive function from ReservationFilter works with current time, but
    //I dunno how to override database's CURRENT_TIMESTAMP
    //if reservation2 has timeTo, which is in future, test will pass
    
    @Test
    public void testFindActiveReservations() {
        Reservation reservation1 = sampleReservation1()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 16, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.APRIL, 19, 12, 0) )
                .build();
        Reservation reservation2 = sampleReservation2()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 20, 12, 0) )
                .withTo( LocalDateTime.of(2017, Month.APRIL, 30, 12, 0) )
                .build();
        Reservation reservation3 = sampleReservation3()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 18, 12, 0) )
                .withTo(null)
                .build();
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        rManager.createReservation(reservation3);
        
        assertThat( rManager.findReservation(
                new ReservationFilter().withOnlyActive(true)
                                            ) )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation2, reservation3);
    }
    
    
    @Test
    public void testFindUnpaidReservations() {
        Reservation reservation1 = sampleReservation1()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 16, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.APRIL, 17, 12, 0) )
                .withPricePerHour(new BigDecimal(10))
                .withMoneyPaid(new BigDecimal(250))
                .build();
        Reservation reservation2 = sampleReservation2()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 18, 12, 0) )
                .withTo( LocalDateTime.of(1450, Month.APRIL, 19, 12, 0) )
                .withPricePerHour(new BigDecimal(10))
                .withMoneyPaid(new BigDecimal(130))
                .build();
        Reservation reservation3 = sampleReservation3()
                .withFrom( LocalDateTime.of(1450, Month.APRIL, 13, 12, 01) )
                .withTo( LocalDateTime.of(1450, Month.APRIL, 14, 12, 00) )
                .withPricePerHour(new BigDecimal(10))
                .withMoneyPaid(new BigDecimal(240))
                .withPricePerHour(new BigDecimal(0))
                .build();
        rManager.createReservation(reservation1);
        rManager.createReservation(reservation2);
        rManager.createReservation(reservation3);
        
        assertThat( rManager.findReservation(
                new ReservationFilter().withOnlyUnpaid(true)
                                            ) )
                .usingFieldByFieldElementComparator()
                .containsOnly(reservation2);
    }
    
    //-------------------------------------------------------------------------
    
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }
    
    private void testExpectedServiceFailureException(Operation<ReservationManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        rManager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(rManager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }
    
    @Test
    public void testGetReservationWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException(manager -> manager.getReservation(1234L));
    }
    
    @Test
    public void testCreateReservationWithSqlExceptionThrown() throws SQLException {
        Reservation reservation = sampleReservation1().build();
        testExpectedServiceFailureException(manager -> manager.createReservation(reservation));
    }
    
    @Test
    public void testUpdateReservationWithSqlExceptionThrown() throws SQLException {
        Reservation reservation = sampleReservation1().build();
        rManager.createReservation(reservation);
        testExpectedServiceFailureException(manager -> manager.updateReservation(reservation));
    }
    
    @Test
    public void testRemoveReservationWithSqlExceptionThrown() throws SQLException {
        Reservation reservation = sampleReservation1().build();
        rManager.createReservation(reservation);
        testExpectedServiceFailureException(manager -> manager.removeReservation(reservation));
    }
    
    @Test
    public void testListAllReservationsWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException(manager -> manager.listAllReservations());
    }
    
    @Test
    public void testFindReservationWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException(manager -> manager.findReservation(new ReservationFilter()));
    }
    

}
