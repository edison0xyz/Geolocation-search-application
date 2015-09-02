<%-- 
    Document   : protect
    Created on : Oct 10, 2014, 10:41:11 PM
    Author     : G4T8
--%>

<%@page import="is203.JWTUtility"%>
<%@page import="com.google.gson.JsonObject"%>
<%
        // Retrieves jsonobject from authenticate
        String token = (String) session.getAttribute("token");
        String username = (String) session.getAttribute("user");
        
        if (token != null) {
            JWTUtility tokenUtility = new JWTUtility();

            if (!tokenUtility.verify(token, "ylleeg4t8").equals(username)) {
                response.sendRedirect("login.jsp");
                return;
            }
        } else {
            response.sendRedirect("login.jsp");
            return;
        }

    

%>
