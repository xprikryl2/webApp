/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.pv168.webapp;

import cz.muni.pv168.dragonrental.backend.*;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ondrej Prikryl
 */
@WebServlet(PersonServlet.URL_MAPPING + "/*")
public class PersonServlet extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/people";

    private final static Logger log = LoggerFactory.getLogger(PersonServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("GET ...");
        showBooksList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //support non-ASCII characters in form
        request.setCharacterEncoding("utf-8");
        //action specified by pathInfo
        String action = request.getPathInfo();
        log.debug("POST ... {}",action);
        switch (action) {
            case "/add":
                //getting POST parameters from form
                String name = request.getParameter("name");
                String surname = request.getParameter("surname");
                String email = request.getParameter("email");
                
                //form data validity check
                if (name == null || name.length() == 0 || surname == null || surname.length() == 0 || email == null || email.length() == 0) {
                    request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty !");
                    log.debug("Form data invalid.");
                    showBooksList(request, response);
                    return;
                }
                //form data processing - storing to database
                try {
                    Person person = new Person();
                    person.setName(name);
                    person.setSurname(surname);
                    person.setEmail(email);
                    person.setId(null);
                    getPersonManager().addPerson(person);
                    //redirect-after-POST protects from multiple submission
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot add person to the DB.", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    Person search = new Person();
                    search.setId(id);
                    List <Person> people = getPersonManager().findPeople(search);
                    if(people.isEmpty()) {
                        return;
                    }
                    getPersonManager().deletePerson(search);
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot delete person from the DB.", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    String uname = request.getParameter("name");
                    String usurname = request.getParameter("surname");
                    String uemail = request.getParameter("email");
                    //form data validity check
                    if (uname == null || uname.length() == 0 || usurname == null || usurname.length() == 0 || uemail == null || uemail.length() == 0) {
                        request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty !");
                        log.debug("Form data invalid.");
                        showBooksList(request, response);
                        return;
                    }
                    Person search = new Person();
                    search.setId(id);
                    List <Person> people = getPersonManager().findPeople(search);
                    if(people.isEmpty()) {
                        return;
                    }
                    Person person = people.get(0);
                    person.setName(uname);
                    person.setSurname(usurname);
                    person.setEmail(uemail);
                    getPersonManager().updatePerson(person);
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot update person.", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            default:
                log.error("Unknown action " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    /**
     * Gets BookManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return BookManager instance
     */
    private PersonManager getPersonManager() {
        return (PersonManager) getServletContext().getAttribute("personManager");
    }

    /**
     * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
     */
    private void showBooksList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            log.debug("Showing list of all people.");
            request.setAttribute("people", getPersonManager().listAllPeople());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            log.error("Cannot list people.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}