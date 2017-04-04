<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>

    <head>
        <title>DragonRental</title>
    </head>    

    <body>
        <h1>
            Actual database data:
        </h1>
        <table border="1">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Surname</th>
                    <th>Email</th>
                </tr>
            </thead>
            <c:forEach items="${people}" var="person">
                <tr>
                    <td><c:out value="${person.name}"/></td>
                    <td><c:out value="${person.surname}"/></td>
                    <td><c:out value="${person.email}"/></td>
                </tr>
             </c:forEach>       
        </table>

        <h1>
            Database modification:
        </h1>
        <table border="1">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Surname</th>
                    <th>Email</th>
                </tr>
            </thead>
            <c:forEach items="${people}" var="person">
                <tr>
                    <form method="post" action="${pageContext.request.contextPath}/people/update?id=${person.id}"
                      style="margin-bottom: 0;">
                        <td><input type="text" name="name" value="<c:out value="${person.name}"/>"/></td>
                        <td><input type="text" name="surname" value="<c:out value="${person.surname}"/>"/></td>
                        <td><input type="text" name="email" value="<c:out value="${person.email}"/>"/></td>
                        <td><input type="submit" value="Update"></td>
                    </form>
                    <td>
                        <form method="post" action="${pageContext.request.contextPath}/people/delete?id=${person.id}"
                          style="margin-bottom: 0;"><input type="submit" value="Delete"></form>
                    </td>
                </tr>
            </c:forEach>
        </table>

        <h2>
            Add person to the DB:
        </h2>
        <c:if test="${not empty chyba}">
            <div style="border: solid 1px red; background-color: yellow; padding: 10px">
                <c:out value="${chyba}"/>
            </div>
        </c:if>
        <form action="${pageContext.request.contextPath}/people/add" method="post">
            <table>
                <tr>
                    <th>Name:</th>
                    <td><input type="text" name="name" /></td>
                </tr>
                <tr>
                    <th>Surname:</th>
                    <td><input type="text" name="surname" /></td>
                </tr>
                <tr>
                    <th>Email:</th>
                    <td><input type="text" name="email" /></td>
                </tr>
            </table>
            <input type="Submit" value="Add person" />
        </form>

    </body>
</html>