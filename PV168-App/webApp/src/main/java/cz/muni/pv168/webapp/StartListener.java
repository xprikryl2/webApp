/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.webapp;

import cz.muni.pv168.dragonrental.backend.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ondrej Prikryl
 */
@WebListener
public class StartListener implements ServletContextListener {

    private final static Logger log = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("WebApp initialized.");
        ServletContext servletContext = ev.getServletContext();
        DataSource dataSource = DragonRental.createMemoryDatabase();
        //DragonManagerImpl dragonManager = new DragonManagerImpl(null);
        PersonManagerImpl personManager = new PersonManagerImpl();
        //dragonManager.setDataSource(dataSource);
        personManager.setDataSource(dataSource);
        servletContext.setAttribute("personManager", personManager);
        //servletContext.setAttribute("dragonManager", dragonManager);
        log.info("Managers created and stored into servlet context.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("App quits.");
    }
}