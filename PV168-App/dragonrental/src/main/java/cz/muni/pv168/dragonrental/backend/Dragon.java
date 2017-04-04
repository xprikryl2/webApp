/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import java.time.LocalDate;

/**
 *
 * @author zuz
 */
public class Dragon {

    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private Integer maximumSpeed;
    private DragonElement element;

    public Dragon() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(Integer maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public DragonElement getElement() {
        return element;
    }

    public void setElement(DragonElement element) {
        this.element = element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dragon dragon = (Dragon) o;

        return id != null ? id.equals(dragon.id) : false;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Dragon{" +
                "id=" + id +
                ", name=" + name +
                ", dateOfBirth=" + dateOfBirth +
                ", element=" + element.name() +
                ", speed=" + maximumSpeed +
                '}';
    }
}
