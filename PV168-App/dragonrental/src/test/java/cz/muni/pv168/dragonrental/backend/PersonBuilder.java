/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

/**
 * A simple builder class to make dragons for tests with methods that are readable
 * 
 * was suggested in BodyManagerImplTest of project GraveManager-Backend by 
 * Mr.Adamek
 * 
 * used example at
 * https://www.javacodegeeks.com/2013/06/builder-pattern-good-for-code-great-for-tests.html
 * 
 * @author Petr Soukop
 */

public class PersonBuilder {

    private Long id = null;
    private String name = "Johnny";
    private String surname = "Cash";
    private String email = "johnny.cash@rocks.it";
    
    public PersonBuilder() {
    }
    
    public PersonBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public PersonBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public PersonBuilder withSurname(String surname) {
        this.surname = surname;
        return this;
    }
    
    public PersonBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public Person build() {
        Person person = new Person();
        person.setId(id);
        person.setName(name);
        person.setSurname(surname);
        person.setEmail(email);
        return person;
    }
}
