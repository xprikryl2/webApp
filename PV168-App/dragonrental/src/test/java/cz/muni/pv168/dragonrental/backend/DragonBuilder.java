/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import java.time.LocalDate;
import java.time.Month;

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
public class DragonBuilder {
    
    private Long id = null;
    private String name = "Timmy the Dragon";
    private LocalDate dateOfBirth = LocalDate.of(1350, Month.JANUARY, 1);
    private Integer maximumSpeed = 100;
    private DragonElement element = DragonElement.EARTH;
    
    public DragonBuilder() {
    }
    
    public DragonBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public DragonBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * changes dragonbuilder's dateOfBirth
     * @param date use LocalDate.of(int year, Month.MONTH_NAME, int day)
     * to create a new date
     * @return 
     */
    public DragonBuilder withDateOfBirth(LocalDate date) {
        this.dateOfBirth = date;
        return this;
    }
    
    public DragonBuilder withMaximumSpeed(Integer maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
        return this;
    }
    
    public DragonBuilder withElement(DragonElement element) {
        this.element = element;
        return this;
    }
    
    public Dragon build() {
        Dragon dragon = new Dragon();
        dragon.setId(id);
        dragon.setName(name);
        dragon.setDateOfBirth(dateOfBirth);
        dragon.setMaximumSpeed(maximumSpeed);
        dragon.setElement(element);
        return dragon;
    }
}
