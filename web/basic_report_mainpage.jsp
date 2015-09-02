<%-- 
    Document   : basic_report_mainpage
    Created on : Sep 13, 2014, 2:57:18 PM
    Author     : G4T8
--%>
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
        <title>SLOCA - Basic Location Report Main Page</title>
    </head>
    <body class="metro">
        <header class="bg-dark" data-load="design/user_header.jsp"></header>
        <br> 
        <div class="container">

            <h1> Basic Location Reports<br></h1>
            <br><br>
            <a href="report_breakdown_page.jsp"><div class="tile double">
                <div class="tile-content image">
                    <center><img src="images/yeargender.png"></center>
                </div>
                <div class="brand bg-dark opacity">
                    <span class="text">
                        <b><u>Breakdown By Year and Gender</u></b><br>
                        This basic report shows the breakdown of students in the SIS building by their year (2010/2011/2012/2013), by their gender(male/female), and by their school.
                    </span>
                </div>
                </div></a>
            <a href="top_k_popular_places_page.jsp"><div class="tile double">
                <div class="tile-content image">
                    <center><img src="images/popularplaces.png"></center>
                </div>
                <div class="brand bg-dark opacity">
                    <span class="text">
                        <b><u>Top-k popular places</u></b><br>
                        A user can see the top-k popular places in the whole SIS building at a specified time.
                    </span>
                </div>
                </div></a>
            <a href="top_k_companions_page.jsp"><div class="tile double">
                <div class="tile-content image">
                    <center><img src="images/companion.png"></center>
                </div>
                <div class="brand bg-dark opacity">
                    <span class="text">
                        <b><u>Top-k companions</u></b><br>
                        A user can see the top-k other users who were co-located with a specified user (using MAC address) in a specified query window.
                    </span>
                </div>
                </div></a>
            <a href="top_k_next_places.jsp"><div class="tile double">
                <div class="tile-content image">
                    <center><img src="images/nextplace.png"></center>
                </div>
                <div class="brand bg-dark opacity">
                    <span class="text">
                        <b><u>Top-k next places</u></b><br>
                        A user can see the top-k popular places in the whole SIS building at a specified time.
                    </span>
                </div>
                </div></a>
        </div>
    </body>
</html>
