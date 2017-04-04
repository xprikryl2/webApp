/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import cz.muni.pv168.dragonrental.common.DBUtils;
import cz.muni.pv168.dragonrental.common.IllegalEntityException;
import cz.muni.pv168.dragonrental.common.ServiceFailureException;
import cz.muni.pv168.dragonrental.common.ValidationException;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Zuzana Wolfová
 */
public class DragonManagerImplTest {

    private DragonManagerImpl dragonManager;
    private DataSource dataSource;
    private final static ZonedDateTime NOW
            = LocalDateTime.of(2017, Month.MARCH, 19, 1, 00).atZone(ZoneId.of("UTC"));

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:dragonrental-test");
        dataSource.setCreateDatabase("create");
        return dataSource;
    }

    private static Clock prepareClockMock(ZonedDateTime now) {
        return Clock.fixed(now.toInstant(), now.getZone());
    }
    
    public DragonManagerImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, DragonManager.class.getResource("createTables.sql"));
        dragonManager = new DragonManagerImpl(prepareClockMock(NOW)); //???
        dragonManager.setDataSource(dataSource);
    }
    
    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, DragonManager.class.getResource("dropTables.sql"));
    }

    //---------------------------------------------------------------------------------------------
    // Preparing test data.
    //---------------------------------------------------------------------------------------------

    private DragonBuilder sampleTimmyBuilder() {
        return new DragonBuilder().withId(null);
    }

    private DragonBuilder sampleAnyaBuilder() {
        return new DragonBuilder()
                .withId(null)
                .withDateOfBirth(LocalDate.of(1492, Month.JULY,9))
                .withName("Anya")
                .withElement(DragonElement.WATER)
                .withMaximumSpeed(200);
    }

    //---------------------------------------------------------------------------------------------
    // Tests for creating dragons.
    //---------------------------------------------------------------------------------------------

    /**
     * Test of createDragon method, of class DragonManagerImpl.
     */
    @Test
    public void testCreateDragon() {
        Dragon dragon = sampleTimmyBuilder().build(); //Timmy the Dragon without id
        dragonManager.createDragon(dragon); //puts the dragon into database

        Long dragonId = dragon.getId();
        assertThat(dragonId).isNotNull();

        assertThat(dragonManager.getDragonById(dragonId))
                .isNotNull()
                .isNotSameAs(dragon)
                .isEqualToComparingFieldByField(dragon);
    }

    @Test
    public void testCreateNullDragon() {
        expectedException.expect(IllegalArgumentException.class);
        dragonManager.createDragon(null);
    }

    @Test
    public void testCreateDragonWithID() {
        Dragon timmy = sampleTimmyBuilder().withId(42L).build();
        expectedException.expect(IllegalEntityException.class);
        dragonManager.createDragon(timmy);
    }

    @Test
    public void testCreateDragonWithNullName() {
        Dragon timmy = sampleTimmyBuilder().withName(null).build();
        expectedException.expect(ValidationException.class);
        dragonManager.createDragon(timmy);
    }

    //A dragon with empty name is ok.
    @Test
    public void testCreateDragonWithEmptyName() {
        Dragon timmy = sampleTimmyBuilder().withName("").build();
        dragonManager.createDragon(timmy);

        assertThat(dragonManager.getDragonById(timmy.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(timmy);
    }

    @Test
    public void testCreateDragonWithNullElement() {
        Dragon timmy = sampleTimmyBuilder().withElement(null).build();
        expectedException.expect(ValidationException.class);
        dragonManager.createDragon(timmy);
    }

    @Test
    public void testCreateDragonWithNegativeSpeed() {
        Dragon timmy = sampleTimmyBuilder().withMaximumSpeed(-42).build();
        expectedException.expect(ValidationException.class);
        dragonManager.createDragon(timmy);
    }

    //A dragon with 0 speed is ok (it could have been just born)
    @Test
    public void testCreateDragonWithZeroSpeed() {
        Dragon timmy = sampleTimmyBuilder().withMaximumSpeed(0).build();
        dragonManager.createDragon(timmy);

        assertThat(dragonManager.getDragonById(timmy.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(timmy);
    }

    @Test
    public void testCreateDragonWithNullDateOfBirth() {
        Dragon timmy = sampleTimmyBuilder().withDateOfBirth(null).build();
        expectedException.expect(ValidationException.class);
        dragonManager.createDragon(timmy);
    }

    @Test
    public void testCreateDragonBornAfterNow() {
        Dragon timmy = sampleTimmyBuilder().withDateOfBirth(NOW.toLocalDate().plusDays(1)).build();
        expectedException.expect(ValidationException.class);
        dragonManager.createDragon(timmy);
    }

    //A dragon born today is ok.
    @Test
    public void testCreateDragonBornToday() {
        Dragon timmy = sampleTimmyBuilder().withDateOfBirth(NOW.toLocalDate()).build();
        dragonManager.createDragon(timmy);

        assertThat(dragonManager.getDragonById(timmy.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(timmy);
    }

    //---------------------------------------------------------------------------------------------
    // Tests for removing dragons.
    //---------------------------------------------------------------------------------------------

    /**
     * Test of removeDragon method, of class DragonManagerImpl.
     */
    @Test
    public void testRemoveDragon() {

        Dragon timmy = sampleTimmyBuilder().build();
        Dragon anya = sampleAnyaBuilder().build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        assertThat(dragonManager.getDragonById(anya.getId())).isNotNull();
        assertThat(dragonManager.getDragonById(timmy.getId())).isNotNull();

        dragonManager.removeDragon(timmy); //should not change the timmy object, just remove it from the database.

        assertThat(dragonManager.getDragonById(anya.getId())).isNotNull();
        assertThat(dragonManager.getDragonById(timmy.getId())).isNull();
    }

    @Test
    public void testDeleteNullDragon() {
        expectedException.expect(IllegalArgumentException.class);
        dragonManager.removeDragon(null);
    }

    @Test
    public void testDeleteDragonWithNullId() {
        Dragon timmy = sampleTimmyBuilder().build();
        expectedException.expect(IllegalEntityException.class);
        dragonManager.removeDragon(timmy);
    }

    @Test
    public void testDeleteNonExistingDragon() {
        Dragon timmy = sampleTimmyBuilder().withId(42L).build();
        expectedException.expect(IllegalEntityException.class);
        dragonManager.removeDragon(timmy);
    }

    //---------------------------------------------------------------------------------------------
    // Tests for updating dragons.
    //---------------------------------------------------------------------------------------------

    /**
     * Test of updateDragon method, of class DragonManagerImpl.
     */
    @Test
    public void testUpdateDragon() {
        Dragon dragonToUpdate = sampleTimmyBuilder().build();
        Dragon anotherDragon = sampleAnyaBuilder().build();

        dragonManager.createDragon(dragonToUpdate);
        dragonManager.createDragon(anotherDragon);

        //update dragon
        dragonToUpdate.setName("Timothy");
        dragonManager.updateDragon(dragonToUpdate);

        //test the update
        assertThat(dragonManager.getDragonById(dragonToUpdate.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(dragonToUpdate);

        //test that the other dragon was not changed
        assertThat(dragonManager.getDragonById(anotherDragon.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(anotherDragon);
    }

    //universal test interface and method
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateDragon(Operation<Dragon> updateOperation) {
        Dragon dragonToUpdate = sampleTimmyBuilder().build();
        Dragon anotherDragon = sampleAnyaBuilder().build();

        dragonManager.createDragon(dragonToUpdate);
        dragonManager.createDragon(anotherDragon);

        //update dragon
        updateOperation.callOn(dragonToUpdate); //using the functional interface.
        dragonManager.updateDragon(dragonToUpdate);

        //test the update
        assertThat(dragonManager.getDragonById(dragonToUpdate.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(dragonToUpdate);

        //test that the other dragon was not changed
        assertThat(dragonManager.getDragonById(anotherDragon.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(anotherDragon);
    }

    private void testUpdateDragonSimple(Operation<Dragon> updateOperation) {
        Dragon timmy = sampleTimmyBuilder().build();
        dragonManager.createDragon(timmy);

        updateOperation.callOn(timmy);
        dragonManager.updateDragon(timmy);

        //if any exception is thrown, it is thrown in the line before.
        assertThat(dragonManager.getDragonById(timmy.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(timmy);
    }

    @Test
    public void testUpdateDragonElement() {
        testUpdateDragon((dragon) -> dragon.setElement(DragonElement.MAGIC));
    }

    @Test
    public void testUpdateDragonMaxSpeed() {
        testUpdateDragon((dragon) -> dragon.setMaximumSpeed(42));
    }

    @Test
    public void testUpdateDragonDateOfBirth() {
        testUpdateDragon((dragon) -> dragon.setDateOfBirth(LocalDate.of(1650, Month.APRIL, 13)));
    }

    //Test update for correct exceptions
    @Test
    public void testUpdateDragonWithNullId() {
        Dragon timmy = sampleTimmyBuilder().withId(null).build();
        expectedException.expect(IllegalEntityException.class);
        dragonManager.updateDragon(timmy);
    }

    @Test
    public void testUpdateNonExistingDragon() {
        Dragon timmy = sampleTimmyBuilder().withId(42L).build();
        expectedException.expect(IllegalEntityException.class);
        dragonManager.updateDragon(timmy);
    }

    @Test
    public void testUpdateNullDragon() {
        expectedException.expect(IllegalArgumentException.class);
        dragonManager.updateDragon(null);
    }

    @Test
    public void testUpdateDragonWithNullName() {
        assertThatThrownBy(() -> testUpdateDragonSimple((dragon) -> dragon.setName(null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testUpdateDragonWithEmptyName() {
        testUpdateDragonSimple((dragon) -> dragon.setName(""));
    }

    @Test
    public void testUpdateDragonWithNegativeMaxSpeed() {
        assertThatThrownBy(() -> testUpdateDragonSimple((dragon) -> dragon.setMaximumSpeed(-42)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testUpdateDragonWithZeroMaxSpeed() {
        testUpdateDragonSimple((dragon) -> dragon.setMaximumSpeed(0));
    }

    @Test
    public void testUpdateDragonWithNullElement() {
        assertThatThrownBy(() -> testUpdateDragonSimple((dragon) -> dragon.setElement(null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testUpdateDragonWithNullDateOfBirth() {
        assertThatThrownBy(() -> testUpdateDragonSimple((dragon) -> dragon.setDateOfBirth(null)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testUpdateDragonBornTomorrow() {
        assertThatThrownBy(() -> testUpdateDragonSimple((dragon) -> dragon.setDateOfBirth(NOW.toLocalDate().plusDays(1))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testUpdateDragonBornToday() {
        testUpdateDragonSimple((dragon) -> dragon.setDateOfBirth(NOW.toLocalDate()));
    }

    //---------------------------------------------------------------------------------------------
    // Tests for finding dragons.
    //---------------------------------------------------------------------------------------------

    /**
     * Test of listAllDragons method, of class DragonManagerImpl.
     */
    @Test
    public void testListAllDragons() {
        assertThat(dragonManager.listAllDragons().isEmpty()).isTrue();

        Dragon timmy = sampleTimmyBuilder().build();
        Dragon anya = sampleAnyaBuilder().build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        assertThat(dragonManager.listAllDragons())
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy,anya);
    }

    /**
     * Test of findDragons method, of class DragonManagerImpl.
     * Test are not complete, since there are 32 combinations of input parameters + any of the given parameters
     * in a given combination may not be found in the database. This makes some 242 different cases, which for
     * this project is not necessary to test. Therefore, only some critical parts are tested.
     */
    @Test
    public void testFindDragons() {
        Dragon timmy = sampleTimmyBuilder().build();
        Dragon anya = sampleAnyaBuilder().build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        assertThat(dragonManager.findDragons(timmy))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        assertThat(dragonManager.findDragons(anya))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(anya);
    }

    @Test
    public void testFindDragonsNullDragon() {
        assertThatThrownBy(() -> dragonManager.findDragons(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    //Finding based on one criterion
    @Test
    public void testFindDragonsById() {
        Dragon timmy = sampleTimmyBuilder().build();
        Dragon anya = sampleAnyaBuilder().build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(timmy.getId())
                .withDateOfBirth(null)
                .withName(null)
                .withMaximumSpeed(null)
                .withElement(null)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        dragonToFind.setId(anya.getId() + timmy.getId());
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();
    }

    @Test
    public void testFindDragonsByName() {
        String name = "timmy";
        Dragon timmy = sampleTimmyBuilder().withName(name).build();
        Dragon anya = sampleAnyaBuilder().build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(null)
                .withName(name)
                .withDateOfBirth(null)
                .withMaximumSpeed(null)
                .withElement(null)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        anya.setName(name);
        dragonManager.updateDragon(anya);

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy,anya);

        dragonToFind.setName("Eva");
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();
    }

    @Test
    public void testFindDragonsByElement() {
        Dragon timmy = sampleTimmyBuilder().withElement(DragonElement.MAGIC).build();
        Dragon anya = sampleAnyaBuilder().withElement(DragonElement.WATER).build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(null)
                .withName(null)
                .withDateOfBirth(null)
                .withMaximumSpeed(null)
                .withElement(DragonElement.MAGIC)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        anya.setElement(DragonElement.MAGIC);
        dragonManager.updateDragon(anya);

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy,anya);

        dragonToFind.setElement(DragonElement.EARTH);
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();
    }

    @Test
    public void testFindDragonsByMaxSpeed() {
        Dragon timmy = sampleTimmyBuilder().withMaximumSpeed(100).build();
        Dragon anya = sampleAnyaBuilder().withMaximumSpeed(50).build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(null)
                .withName(null)
                .withDateOfBirth(null)
                .withMaximumSpeed(100)
                .withElement(null)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        anya.setMaximumSpeed(100);
        dragonManager.updateDragon(anya);

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy,anya);

        dragonToFind.setMaximumSpeed(42);
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();
    }

    @Test
    public void testFindDragonsByDateOfBirth() {
        Dragon timmy = sampleTimmyBuilder().withDateOfBirth(LocalDate.of(1000,Month.JULY,1)).build();
        Dragon anya = sampleAnyaBuilder().withDateOfBirth(LocalDate.of(1500, Month.JULY,1)).build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(null)
                .withName(null)
                .withDateOfBirth(LocalDate.of(1000,Month.JULY,1))
                .withMaximumSpeed(null)
                .withElement(null)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        anya.setDateOfBirth(LocalDate.of(1000,Month.JULY,1));
        dragonManager.updateDragon(anya);

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy,anya);

        dragonToFind.setDateOfBirth(LocalDate.of(1500,Month.APRIL,1));
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();
    }

    //Finding based on two criteria
    @Test
    public void testFindDragonsByIdElement() {
        Dragon timmy = sampleTimmyBuilder().withElement(DragonElement.WATER).build();
        Dragon anya = sampleAnyaBuilder().withElement(DragonElement.WATER).build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(timmy.getId())
                .withDateOfBirth(null)
                .withName(null)
                .withMaximumSpeed(null)
                .withElement(DragonElement.WATER)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        dragonToFind.setElement(DragonElement.EARTH);
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();

        dragonToFind.setElement(DragonElement.WATER);
        dragonToFind.setId(timmy.getId() + anya.getId());
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();
    }

    //Finding based on three criteria
    @Test
    public void testFindDragonsByNameElementSpeed() {
        Dragon timmy = sampleTimmyBuilder()
                .withName("timmy")
                .withMaximumSpeed(100)
                .withElement(DragonElement.EARTH)
                .build();

        Dragon anya = sampleAnyaBuilder()
                .withName("anya")
                .withMaximumSpeed(100)
                .withElement(DragonElement.EARTH)
                .build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(null)
                .withName("timmy")
                .withDateOfBirth(null)
                .withMaximumSpeed(100)
                .withElement(DragonElement.EARTH)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        anya.setName("timmy");
        dragonManager.updateDragon(anya);

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(anya,timmy);

        dragonToFind.setElement(DragonElement.WATER);
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();
    }

    //Finding based on four criteria
    @Test
    public void testFindDragonWithNullID() {
        Dragon timmy = sampleTimmyBuilder()
                .withName("timmy")
                .withMaximumSpeed(100)
                .withElement(DragonElement.EARTH)
                .withDateOfBirth(LocalDate.of(1000,Month.APRIL,1))
                .build();

        Dragon anya = sampleAnyaBuilder()
                .withName("anya")
                .withMaximumSpeed(100)
                .withElement(DragonElement.EARTH)
                .withDateOfBirth(LocalDate.of(1000,Month.APRIL,1))
                .build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(null)
                .withName("timmy")
                .withDateOfBirth(LocalDate.of(1000,Month.APRIL,1))
                .withMaximumSpeed(100)
                .withElement(DragonElement.EARTH)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy);

        anya.setName("timmy");
        dragonManager.updateDragon(anya);

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy,anya);

        dragonToFind.setName("Eva");
        assertThat(dragonManager.findDragons(dragonToFind)).isEmpty();
    }

    //Finding based on five criteria
    @Test
    public void testFindAllDragons() {
        Dragon timmy = sampleTimmyBuilder().build();
        Dragon anya = sampleAnyaBuilder().build();

        dragonManager.createDragon(timmy);
        dragonManager.createDragon(anya);

        Dragon dragonToFind = sampleTimmyBuilder()
                .withId(null)
                .withName(null)
                .withDateOfBirth(null)
                .withMaximumSpeed(null)
                .withElement(null)
                .build();

        assertThat(dragonManager.findDragons(dragonToFind))
                .isNotEmpty()
                .usingFieldByFieldElementComparator()
                .containsOnly(timmy,anya);
    }

    /**
     * Test of getDragonById method of class DragonManagerImpl.
     */
    @Test
    public void testGetDragonById() {
        Dragon timmy = sampleTimmyBuilder().build();
        dragonManager.createDragon(timmy);

        assertThat(dragonManager.getDragonById(timmy.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(timmy);
    }

    @Test
    public void testGetDragonByNullId() {
        expectedException.expect(IllegalArgumentException.class);
        dragonManager.getDragonById(null);
    }

    @Test
    public void getDragonByInvalidId() {
        assertThat(dragonManager.listAllDragons()).isEmpty();
        assertThat(dragonManager.getDragonById(1L)).isNull();
    }

    //---------------------------------------------------------------------------------------------
    // Tests for ServiceFailureException
    //---------------------------------------------------------------------------------------------

    private void testExpectedServiceFailureException(Operation<DragonManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingSource = mock(DataSource.class);
        when(failingSource.getConnection()).thenThrow(sqlException);
        dragonManager.setDataSource(failingSource);

        assertThatThrownBy(() -> operation.callOn(dragonManager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void testCreateDragonWithSqlException() throws SQLException {
        Dragon dragon = sampleTimmyBuilder().build();
        testExpectedServiceFailureException((manager) -> manager.createDragon(dragon));
    }

    @Test
    public void testUpdateDragonWithSqlException() throws SQLException {
        Dragon dragon = sampleTimmyBuilder().build();
        dragonManager.createDragon(dragon);
        testExpectedServiceFailureException((manager) -> manager.updateDragon(dragon));
    }

    @Test
    public void testGetDragonByIdWithSqlException() throws SQLException {
        Dragon dragon = sampleTimmyBuilder().build();
        dragonManager.createDragon(dragon);
        testExpectedServiceFailureException((manager) -> manager.getDragonById(dragon.getId()));
    }

    @Test
    public void testRemoveDragonWithSqlException() throws SQLException {
        Dragon dragon = sampleTimmyBuilder().build();
        dragonManager.createDragon(dragon);
        testExpectedServiceFailureException((manager) -> manager.removeDragon(dragon));
    }

    @Test
    public void testListAllDragonsWithSqlException() throws SQLException {
        testExpectedServiceFailureException((manager) -> manager.listAllDragons());
    }

    @Test
    public void testFindDragonsWithSqlException() throws SQLException {
        Dragon dragon = sampleTimmyBuilder().build();
        testExpectedServiceFailureException((manager) -> manager.findDragons(dragon));
    }

}
