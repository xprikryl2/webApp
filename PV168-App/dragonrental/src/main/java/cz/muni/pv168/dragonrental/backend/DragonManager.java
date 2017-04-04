/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import cz.muni.pv168.dragonrental.common.IllegalEntityException;
import cz.muni.pv168.dragonrental.common.ServiceFailureException;
import cz.muni.pv168.dragonrental.common.ValidationException;

import java.util.List;

/**
 * Service which allows dragon manipulation in connection with database.
 * @author Zuzana Wolfová
 */
public interface DragonManager {
    
    /**
     * This method adds new dragon to the database, generates id and assigns it to the given object.
     *
     * @throws IllegalArgumentException when dragon is null
     * @throws IllegalEntityException when dragon already has an ID
     * @throws ValidationException when dragon has null name, null date of birth, date of birth after today,
     *                             null element or negative maximum speed (can be 0 though)
     * @throws ServiceFailureException when database fails
     * @param dragon to be created
     */
    public void createDragon(Dragon dragon) throws IllegalEntityException, ValidationException, ServiceFailureException;

    /**
     * This method removes dragon from the database based on all it's ID.
     *
     * @throws IllegalArgumentException when dragon is null
     * @throws IllegalEntityException when dragon has a null id or is not in the database
     * @throws ServiceFailureException when database fails
     * @param dragon to be removed
     */
    public void removeDragon(Dragon dragon) throws IllegalEntityException, ServiceFailureException;

    /**
     * This method updates dragon characteristics like speed, element, name, etc. based on ID.
     *
     * @throws IllegalArgumentException when dragon is null
     * @throws IllegalEntityException when dragon has null id or does not exist in the database
     * @throws ValidationException when criteria breaks the contract for dragons as described
     *                             in {@link #createDragon(Dragon)}
     * @throws ServiceFailureException when database fails
     * @param dragon to be updated
     */
    public void updateDragon(Dragon dragon) throws IllegalEntityException, ValidationException, ServiceFailureException;
    
    /**
     * This method lists all dragon stored in the database.
     * @throws ServiceFailureException when database fails
     * @return list of all dragons in the database
     */
    public List<Dragon> listAllDragons() throws ServiceFailureException;
    
    /**
     * This method searches for dragon in database using various criteria.
     * When a criterion is null, it is not taken into consideration.
     * When all criteria are null, this method behaves like {@link #listAllDragons()}.
     *
     * @throws IllegalArgumentException when dragon is null
     * @throws ServiceFailureException when database fails
     * @param dragon searching criteria
     * @return list of all dragons matching searching criteria
     */
    public List<Dragon> findDragons(Dragon dragon) throws ServiceFailureException;

    /**
     * Searches for a dragon with given id.
     *
     * @throws IllegalArgumentException when id is null
     * @throws ServiceFailureException when database fails
     * @param id id of the dragon
     * @return a dragon with given id, null if none was found
     */
    public Dragon getDragonById(Long id) throws IllegalEntityException, ServiceFailureException;
}
