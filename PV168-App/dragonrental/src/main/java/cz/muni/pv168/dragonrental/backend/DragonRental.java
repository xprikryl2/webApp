/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.dragonrental.backend;

import java.util.List;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 *
 * @author zuz
 */
public class DragonRental {

    final static Logger log = LoggerFactory.getLogger(DragonRental.class);
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        log.info("Starting:");

        DataSource dataSource = createMemoryDatabase();
        PersonManagerImpl personManager = new PersonManagerImpl(dataSource);

        List<Person> listofPeople = personManager.listAllPeople();
        System.out.println("People = " + listofPeople);
    }
    
    public static DataSource createMemoryDatabase() {
        BasicDataSource bds = new BasicDataSource();
        //set JDBC driver and URL
        bds.setDriverClassName(EmbeddedDriver.class.getName());
        bds.setUrl("jdbc:derby:memory:peopleDB;create=true");
        //populate db with tables and data
        new ResourceDatabasePopulator(
                new ClassPathResource("schema-javadb.sql"),
                new ClassPathResource("test-data.sql"))
                .execute(bds);
        return bds;
    }
    
}
