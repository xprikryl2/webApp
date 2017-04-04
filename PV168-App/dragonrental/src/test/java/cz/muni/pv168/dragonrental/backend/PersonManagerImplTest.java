/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import cz.muni.pv168.dragonrental.common.*;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * @author Ondrej Prikryl
 */
public class PersonManagerImplTest {

    private PersonManagerImpl manager;
    private DataSource ds;
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:personmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }
    
    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds,PersonManager.class.getResource("createTables.sql"));
        manager = new PersonManagerImpl();
        manager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds,PersonManager.class.getResource("dropTables.sql"));
    }
    
    //-------------------------------------------------------------------------

    private PersonBuilder sampleJohnnyPerson() {
        return new PersonBuilder()
                .withEmail("johnny.cash@rocks.it")
                .withName("Johnny")
                .withSurname("Cash")
                .withId(null);
    }

    private PersonBuilder sampleJoshuaPerson() {
        return new PersonBuilder()
                .withEmail("johnnys.brother@rocks.it")
                .withName("Joshua")
                .withSurname("Cash")
                .withId(null);
    }

    private PersonBuilder sampleAnotherJohnnyPerson() {
        return new PersonBuilder()
                .withEmail("johnny.smith@rocks.it")
                .withName("Johnny")
                .withSurname("Smith")
                .withId(null);
    }

    private PersonBuilder sampleSearchPerson() {
        return new PersonBuilder()
                .withEmail(null)
                .withName(null)
                .withSurname(null)
                .withId(null);
    }

    //-------------------------------------------------------------------------

    @Test
    public void testAddPersonNull() {
        assertThatThrownBy(() -> manager.addPerson(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void testAddPerson(Operation<Person> addOperation) {
        Person johnny = sampleJohnnyPerson().build();
        addOperation.callOn(johnny);

        assertThatThrownBy(() -> manager.addPerson(johnny))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testAddPersonHappyScenario() {
        Person johnny = sampleJohnnyPerson().build();
        manager.addPerson(johnny);

        Long personId = johnny.getId();
        assertThat(personId).isNotNull();

        assertThat(manager.findPeople(johnny))
                .isNotSameAs(johnny)
                .usingFieldByFieldElementComparator()
                .containsOnly(johnny);
    }

    @Test
    public void testAddPersonWithNullName() {
        testAddPerson((person) -> person.setName(null));
    }

    @Test
    public void testAddPersonWithNullSurname() {
        testAddPerson((person) -> person.setSurname(null));
    }

    @Test
    public void testAddPersonWithNullEmail() {
        testAddPerson((person) -> person.setEmail(null));
    }

    @Test
    public void testAddPersonWithEmptyName() {
        testAddPerson((person) -> person.setName(""));
    }
    @Test
    public void testAddPersonWithEmptySurname() {
        testAddPerson((person) -> person.setSurname(""));
    }

    @Test
    public void testAddPersonWithEmptyEmail() {
        testAddPerson((person) -> person.setEmail(""));
    }

    @Test
    public void testAddPersonWithExistingEmailInDB() {
        Person johnny = sampleJohnnyPerson().build();
        Person johnny2 = sampleJohnnyPerson().build();

        manager.addPerson(johnny);
        assertThatThrownBy(() -> manager.addPerson(johnny2))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testAddPersonWithAlreadySetID() {
        Person joshua = sampleJoshuaPerson().withId(1L).build();

        assertThatThrownBy(() -> manager.addPerson(joshua))
                .isInstanceOf(IllegalEntityException.class);
    }

    //-------------------------------------------------------------------------

    @Test
    public void testDeletePersonNull() {
        assertThatThrownBy(() -> manager.deletePerson(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // CANNOT HAPPEN IN OUR SYSTEM
    @Test
    public void testDeletePersonNonExtingPerson() {
        Person johnny = sampleJohnnyPerson().build();
        Person joshua = sampleJoshuaPerson().build();

        manager.addPerson(johnny);

        assertThatThrownBy(() -> manager.deletePerson(joshua))
                .isInstanceOf(IllegalEntityException.class);
    }

    @Test
    public void testDeletePersonHappyScenario() {
        Person johnny = sampleJohnnyPerson().build();

        assertThat(manager.listAllPeople()).isEmpty();
        manager.addPerson(johnny);
        assertThat(manager.listAllPeople()).containsOnly(johnny);

        manager.deletePerson(johnny);
        assertThat(manager.listAllPeople()).isEmpty();
    }

    //-------------------------------------------------------------------------

    @Test
    public void testUpdatePersonNull() {
        assertThatThrownBy(() -> manager.updatePerson(null))
                .isInstanceOf(IllegalArgumentException.class);

    }
    
    private void testUpdatePerson(Operation<Person> updateOperation) {
        Person johnny = sampleJohnnyPerson().build();
        manager.addPerson(johnny);

        updateOperation.callOn(johnny);

        assertThatThrownBy(() -> manager.updatePerson(johnny))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testUpdatePersonWithNullName() {
        testUpdatePerson((person) -> person.setName(null));
    }

    @Test
    public void testUpdatePersonWithNullSurname() {
        testUpdatePerson((person) -> person.setSurname(null));
    }

    @Test
    public void testUpdatePersonWithNullEmail() {
        testUpdatePerson((person) -> person.setEmail(null));
    }

    @Test
    public void testUpdatePersonWithEmptyName() {
        testUpdatePerson((person) -> person.setName(""));
    }

    @Test
    public void testUpdatePersonWithEmptySurname() {
        testUpdatePerson((person) -> person.setSurname(""));
    }

    @Test
    public void testUpdatePersonWithEmptyEmail() {
        testUpdatePerson((person) -> person.setEmail(""));
    }

    @Test
    public void testUpdatePersonWithAlreadyExistingEmail() {
        Person johnny = sampleJohnnyPerson().build();
        Person joshua = sampleJoshuaPerson().build();
        manager.addPerson(johnny);
        manager.addPerson(joshua);

        johnny.setEmail(joshua.getEmail());
        assertThatThrownBy(() -> manager.updatePerson(johnny))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testUpdatePersonHappyScenario() {
        Person anotherBody = sampleJoshuaPerson().build();
        Person updatedBody = sampleJohnnyPerson().build();
        manager.addPerson(anotherBody);
        manager.addPerson(updatedBody);

        updatedBody.setName("John");
        updatedBody.setSurname("Smith");
        updatedBody.setEmail("john.smith@rules.com");

        manager.updatePerson(updatedBody);

        assertThat(manager.findPeople(updatedBody))
                .usingFieldByFieldElementComparator()
                .containsOnly(updatedBody);
        assertThat(manager.findPeople(anotherBody))
                .usingFieldByFieldElementComparator()
                .containsOnly(anotherBody);
    }

    //-------------------------------------------------------------------------

    @Test
    public void testListAllPeopleHappyScenario() {
        Person johnny = sampleJohnnyPerson().build();
        Person joshua = sampleJoshuaPerson().build();

        assertThat(manager.listAllPeople()).isEmpty();
        manager.addPerson(johnny);
        manager.addPerson(joshua);

        assertThat(manager.listAllPeople())
                .usingFieldByFieldElementComparator()
                .containsOnly(johnny, joshua);
    }

    //-------------------------------------------------------------------------

    @Test
    public void testFindPeopleNull() {
        assertThatThrownBy(() -> manager.findPeople(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testFindPeopleNullAttributes() {
        Person searchPerson = sampleSearchPerson().build();
        
        assertThatThrownBy(() -> manager.findPeople(searchPerson))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void testFindPeopleEmpty(Operation<Person> updateOperation) {
        Person johnny = sampleJohnnyPerson().build();
        Person joshua = sampleJoshuaPerson().build();
        Person searchPerson = sampleSearchPerson().build();
        
        manager.addPerson(johnny);
        manager.addPerson(joshua);
        
        updateOperation.callOn(searchPerson);
        assertThat(manager.findPeople(searchPerson)).isEmpty();
    }

    @Test
    public void testFindPeopleWithNonExistingID() {
        testFindPeopleEmpty((person) -> person.setId(1456465L));
    }
    
    @Test
    public void testFindPeopleWithNonExistingName() {
        testFindPeopleEmpty((person) -> person.setName("Joseph"));
    }
    
    @Test
    public void testFindPeopleWithNonExistingSurname() {
        testFindPeopleEmpty((person) -> person.setSurname("Knight"));
    }
    
    @Test
    public void testFindPeopleWithNonExistingMail() {
        testFindPeopleEmpty((person) -> person.setEmail("nonexisting@seznam.cz"));
    }
    
    private void testFindPeopleOnlyJohnny(Operation<Person> updateOperation) {
        Person johnny = sampleJohnnyPerson().build();
        Person joshua = sampleJoshuaPerson().withSurname("Smith").build();
        Person searchPerson = sampleSearchPerson().build();
        
        manager.addPerson(johnny);
        manager.addPerson(joshua);
        
        updateOperation.callOn(searchPerson);
        assertThat(manager.findPeople(searchPerson))
                .usingFieldByFieldElementComparator()
                .containsOnly(johnny);
    }
    
    @Test
    public void testFindPeopleWitIdJohnny() {
        testFindPeopleOnlyJohnny((person) -> person.setId(1L));
    }
    
    @Test
    public void testFindPeopleWithNameJohnny() {
        testFindPeopleOnlyJohnny((person) -> person.setName("Johnny"));
    }
    
    @Test
    public void testFindPeopleWithSurnameJohnny() {
        testFindPeopleOnlyJohnny((person) -> person.setSurname("Cash"));
    }
    
    @Test
    public void testFindPeopleWithEmailJohnny() {
        testFindPeopleOnlyJohnny((person) -> person.setEmail("johnny.cash@rocks.it"));
    }

    @Test
    public void testFindPeopleMultipleByName(){
        Person johnny = sampleJohnnyPerson().build();
        Person anotherJohnny = sampleAnotherJohnnyPerson().build();
        Person joshua = sampleJoshuaPerson().build();
        Person searchPerson = sampleSearchPerson().withName("Johnny").build();

        manager.addPerson(johnny);
        manager.addPerson(anotherJohnny);
        manager.addPerson(joshua);

        assertThat(manager.findPeople(searchPerson))
                .containsOnly(johnny, anotherJohnny);
    }

    @Test
    public void testFindPeopleMultipleBySurname(){
        Person johnny = sampleJohnnyPerson().build();
        Person anotherJohnny = sampleAnotherJohnnyPerson().build();
        Person joshua = sampleJoshuaPerson().build();
        Person searchPerson = sampleSearchPerson().withSurname("Cash").build();

        manager.addPerson(johnny);
        manager.addPerson(anotherJohnny);
        manager.addPerson(joshua);

        assertThat(manager.findPeople(searchPerson))
                .containsOnly(johnny, joshua);
    }
    
    @Test
    public void testFindPeopleHappyScenario() {
        Person johnny = sampleJohnnyPerson().build();
        Person joshua = sampleJoshuaPerson().build();
        manager.addPerson(johnny);
        manager.addPerson(joshua);

        assertThat(manager.findPeople(johnny))
                .containsOnly(johnny);
        assertThat(manager.findPeople(joshua))
                .containsOnly(joshua);
    }
    
    //--------------------------------------------------------------------------
    // Tests if BodyManager methods throws ServiceFailureException in case of
    // DB operation failure
    //--------------------------------------------------------------------------

    @Test
    public void testAddPersonWithSqlExceptionThrown() throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);

        manager.setDataSource(failingDataSource);
        Person johnny = sampleJohnnyPerson().build();

        assertThatThrownBy(() -> manager.addPerson(johnny))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    private void testExpectedServiceFailureException(Operation<PersonManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void testUpdatePersonWithSqlExceptionThrown() throws SQLException {
        Person johnny = sampleJohnnyPerson().build();
        manager.addPerson(johnny);
        testExpectedServiceFailureException((personManager) -> personManager.updatePerson(johnny));
    }
    
    @Test
    public void testFindPersonWithSqlExceptionThrown() throws SQLException {
        Person johnny = sampleJohnnyPerson().build();
        manager.addPerson(johnny);
        testExpectedServiceFailureException((personManager) -> personManager.findPeople(johnny));
    }

    @Test
    public void testDeletePersonWithSqlExceptionThrown() throws SQLException {
        Person johnny = sampleJohnnyPerson().build();
        manager.addPerson(johnny);
        testExpectedServiceFailureException((personManager) -> personManager.deletePerson(johnny));
    }

    @Test
    public void testListAllPeopleWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((personManager) -> personManager.listAllPeople());
    }
}