<%-- 
    Document   : user
    Created on : Sep 3, 2014, 2:24:52 PM
    Author     : G4T8
--%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="org.joda.time.LocalDate"%>
<%@page import="org.joda.time.LocalTime"%>
<%@page import="com.sloca.model.UserDAO"%>
<%@page import="com.sloca.entity.User"%>
<%@include file='protect.jsp'%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
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
        <title>SLOCA - Welcome</title>
    </head>
    <body class="metro">
        <%
            UserDAO userdm = new UserDAO();
            String name = userdm.retrieve(username).getName();
        %>
        <header class="bg-dark" data-load="design/user_header.jsp"></header>
        <div class="container">
            <br>

            <h1>Welcome, <%=name%></h1>
            <br><br><br><br>
            <div align='center'>
                <p><i>Sometimes our code works, we don't know why. Sometimes it works, we don't know why too. - A wise man</i></p>
            </div>

        </div>
    </body>

</html>
