<%-- 
    Author     : G4T8
--%>

<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" href="assets/css/bootstrap.css" type="text/css"/>
        <link href="assets/css/metro-bootstrap.css" rel="stylesheet">
        <link href="assets/css/metro-bootstrap-responsive.css" rel="stylesheet">
        <link href="assets/css/iconFont.css" rel="stylesheet">
        <link href="assets/css/docs.css" rel="stylesheet">
        <link href="assets/js/prettify/prettify.css" rel="stylesheet">

        <!-- Load JavaScript Libraries -->
        <script src="assets/js/jquery/jquery.min.js"></script>
        <script src="assets/js/jquery/jquery.widget.min.js"></script>
        <script src="assets/js/jquery/jquery.mousewheel.js"></script>
        <script src="assets/js/prettify/prettify.js"></script>

        <!-- Metro UI CSS JavaScript plugins -->
        <script src="assets/js/load-metro.js"></script>

        <!-- Local JavaScript -->
        <script src="assets/js/docs.js"></script>
        <script src="assets/js/github.info.js"></script>
        <title>SLOCA - Login</title>
    </head>
    <body class="metro">

        <header class="bg-dark" data-load="design/header.jsp"></header>
        <div class="container">
            <p>
            <form action ="authenticate" method="post">
                <%
                    String loginuser = (String) session.getAttribute("loginuser");
                    if (loginuser == null) {
                        loginuser = "";
                    }
                %>


                <legend>Login</legend>
                <%
                    if (session.getAttribute("error") != null) {
                        out.println("<div class='alert alert-danger'>");
                        out.println("<b>Error!</b><br>Username / password is invalid! Please try again!");
                        out.println("</div>");
                        session.removeAttribute("error");
                    }

                %>
                <label>User ID</label>
                <div class="input-control text" data-role="input-control" style="width: 500px;">
                    <input value="<%=loginuser%>" type="text" placeholder="Enter User ID" name="username" autofocus required>
                    <button class="btn-clear" tabindex="-1"></button>
                </div>
                <label>Password</label>
                <div class="input-control password" data-role="input-control" style="width: 500px">
                    <input type="password" placeholder="Enter password" name="password" required>
                    <button class="btn-reveal" tabindex="-1"></button>
                </div>
                <br><br>
                <input type="submit" value="Sign in" class="primary large">
                <p class="text-alert"></p>


            </form>

        </div>
    </body>
</html>
