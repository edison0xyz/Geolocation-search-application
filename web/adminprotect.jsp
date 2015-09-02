        <%
            if (session.getAttribute("admin") == null) {
                response.sendRedirect("login.jsp");
            }
        %>