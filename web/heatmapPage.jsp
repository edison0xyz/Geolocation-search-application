<%-- 
    Document   : heatmapPage
    Created on : Sep 13, 2014, 2:29:19 PM
    Author     : G4T8
--%>

<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.util.HashMap"%>
<%@include file="protect.jsp" %>

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

        <script src="assets/js/metro-button-set.js"></script>
        <!-- Metro UI CSS JavaScript plugins -->
        <script src="assets/js/load-metro.js"></script>

        <!-- Local JavaScript -->
        <script src="assets/js/docs.js"></script>
        <script src="assets/js/github.info.js"></script>

        <!-- datepicker -->
        <link rel="stylesheet" href="//code.jquery.com/ui/1.11.1/themes/smoothness/jquery-ui.css">
        <script src="//code.jquery.com/jquery-1.10.2.js"></script>
        <script src="//code.jquery.com/ui/1.11.1/jquery-ui.js"></script> 

        <script language='JavaScript'>
            $(function() {
                $("#datepicker").datepicker({dateFormat: 'dd-mm-yy'});
            });
        </script>

        <style>
            .heatmap{
                width:800;
                height:600;
            }
        </style>


        <title>SLOCA - Heatmap</title>

    </head>
    <body class="metro">
        <header class="bg-dark" data-load="design/user_header.jsp"></header>
        <br>
        <div class="container">
            <table>
                <tr>
                    <td style="text-align:left;vertical-align:top;">
                        <h1>Heatmap</h1>
                        <br>
                        <%
                            System.out.println(token);
                            String date = (String) request.getAttribute("date");
                            String time = (String) request.getAttribute("time");
                            String floor = (String) request.getAttribute("floor");

                            if (floor == null) {
                                floor = "";
                            }
                            if (date == null) {
                                date = "";
                            }
                            if (time == null) {
                                time = "";
                            }


                            if (request.getAttribute("error") != null) {
                                out.println("<div class='alert alert-danger'>");
                                out.println(request.getAttribute("error"));
                                out.println("</div>");
                            }
                        %>

                        <!-- FORM -->
                        <form action="Heatmap.do" method="post">
                            <table>
                                <col width='100'>
                                <tr>
                                    <td><b>Date:</b></td>
                                    <td>
                                        <input value="<%=date%>" name='date' type="text" placeholder='dd-mm-yyyy' id="datepicker" size="30" class='form-control' required>
                                    </td>
                                </tr>
                                <tr><td>&nbsp</td><td>&nbsp</td></tr>
                                <tr>
                                    <td><b>Time:</b></td>
                                    <td>
                                        <input value="<%=time%>" placeholder="hh:mm:ss" type="text" name="time" class="form-control" required>
                                    </td>
                                </tr>
                                <tr><td>&nbsp</td><td>&nbsp</td></tr>
                                <tr>
                                    <td><b>Floor:</b></td>
                                    <td>
                                        <div class="button-set">
                                            <button name="floor" value="B1" <% if (floor.equals("B1")) {
                                                    out.println("class='active'");
                                                }%>>Basement</button>
                                            <button name="floor" value="L1" <% if (floor.equals("L1")) {
                                                    out.println("class='active'");
                                                }%>>1</button>
                                            <button name="floor" value="L2" <% if (floor.equals("L2")) {
                                                    out.println("class='active'");
                                                }%>>2</button>
                                            <button name="floor" value="L3" <% if (floor.equals("L3")) {
                                                    out.println("class='active'");
                                                }%>>3</button>
                                            <button name="floor" value="L4" <% if (floor.equals("L4")) {
                                                    out.println("class='active'");
                                                }%>>4</button>
                                            <button name="floor" value="L5" <% if (floor.equals("L5")) {
                                                    out.println("class='active'");
                                                }%>>5</button>
                                        </div>                
                                    </td>
                                </tr>
                                <tr><td>&nbsp</td><td>&nbsp</td></tr>
                            </table>
                        </form>
                        <!-- END OF FORM --> 
                    </td>
                    <td width="10px">&nbsp;</td>
                    <%
                        ArrayList<String[]> results = (ArrayList<String[]>) request.getAttribute("results");
                        if (results != null) {
                    %>
                    <td>
                        <br>
                        <div class="tab-control" data-role="tab-control" style="width: 625px">
                            <ul class="tabs">
                                <li class="active"><a href="#_page_1">Location Info - Map</a></li>
                                <li><a href="#_page_2">Location Info - Analysis</a></li>
                            </ul>
                            <div class="frames">
                                <div class="frame" id="_page_1">
                                    <p>
                                        <%
                                            if (results.size() == 0) {
                                                out.println("<div class='alert alert-success'>");
                                                out.println("Search complete! No results found.");
                                                out.println("</div>");
                                            }
                                        %>
                                    </p>

                                    <!-- PRINT IMAGE OF MAP -->
                                    <div class="heatmap">
                                        <% if (!floor.isEmpty()) {%>
                                        <img src="images/SIS_<%=floor%>.png"  style="height:600px">
                                        <%}%>
                                    </div>

                                    <script src="assets/js/heatmap.js"></script>
                                    <script>
            window.onload = function() {
                var heatmapContainer = document.querySelector('.heatmap');

                // minimal heatmap instance configuration
                var heatmapInstance = h337.create({
                    // only container is required, the rest will be defaults
                    container: document.querySelector('.heatmap')
                });
                // now generate some random data
                var points = [];
                var max = 6;
                var width = 800;
                var height = 600;

                var lobbyArr = [120, 75, 140, 275];
                var seminarOne = [60, 50, 100, 100];
                var seminarTwo = [60, 50, 210, 100];
                var seminarThree = [60, 50, 280, 100];
                var seminarFour = [70, 50, 390, 100];
                var classRoom = [120, 50, 245, 210];
                var studyOne = [60, 170, 70, 235];
                var studyTwo = [65, 50, 365, 210];

                var heatArea = function(arr, density) {

                    var boxWidth = arr[0];
                    var boxHeight = arr[1];
                    var xStart = arr[2];
                    var yStart = arr[3];

                    for (var i = 0; i < boxWidth; i += 30) {
                        for (var j = 0; j < boxHeight; j += 25) {
                            var point = {
                                x: xStart + i,
                                y: yStart + j,
                                value: density
                            };
                            points.push(point);
                        }
                    }
                }


                                        <% // ============= Start of JSP Code =================
                                            //==================================================

                                            if (results != null && results.size() > 0) {
                                                System.out.println("runs hereeee");
                                                for (String[] s : results) {

                                                    int densityNo = Integer.valueOf(s[2]);
                                                    if (!s[0].contains("SMUSISB1")) {

                                                        if (s[0].contains("LOBBY") && densityNo > 0) {
                                        %>  heatArea(lobbyArr, <%= densityNo%>); <%
                                            }
                                            if ((s[0].contains("SR3-1") | s[0].contains("SR2-1")) && densityNo > 0) {
                                        %>  heatArea(seminarOne, <%= densityNo%>); <%
                                            }

                                            if ((s[0].contains("SR3-2") | s[0].contains("SR2-2")) && densityNo > 0) {
                                        %>  heatArea(seminarTwo, <%= densityNo%>); <%
                                            }

                                            if ((s[0].contains("SR3-3") | s[0].contains("SR2-3")) && densityNo > 0) {
                                        %>  heatArea(seminarThree, <%= densityNo%>); <%
                                            }

                                            if ((s[0].contains("SR3-4") | s[0].contains("SR2-4")) && densityNo > 0) {
                                        %>  heatArea(seminarFour, <%= densityNo%>); <%
                                            }

                                            if (s[0].contains("CLSRM") && densityNo > 0) {
                                        %>  heatArea(classRoom, <%= densityNo%>); <%
                                            }

                                            if (s[0].contains("L3STUDYAREA1") && densityNo > 0) {
                                        %>  heatArea(studyOne, <%= densityNo%>); <%
                                            }
                                            if (s[0].contains("L2STUDYAREA1") && densityNo > 0) {
                                        %>  heatArea(studyOne, <%= densityNo%>); <%
                                            }
                                            if (s[0].contains("L3STUDYAREA2") && densityNo > 0) {
                                        %>  heatArea(studyTwo, <%= densityNo%>); <%
                                            }
                                            if (s[0].contains("L2STUDYAREA2") && densityNo > 0) {
                                        %>  heatArea(studyTwo, <%= densityNo%>); <%
                                                        }

                                                    }
                                                }
                                            }
                                            //==================================================
                                            //==================================================
%>
                // FOR TESTING 
                // heatArea(seminarOne, 3);
                var data = {
                    max: max,
                    data: points
                };

                heatmapInstance.setData(data);

            };


                                    </script>



                                </div>
                                <div class="frame" id="_page_2">
                                    <p>
                                        <%
                                            if (results.size() > 0) {
                                        %>
                                        <br><br>

                                    <table class="table-bordered table" cellpadding='10' style="width: 100%">
                                        <tr style="background-color: #efeded" height="35">
                                            <th>Semantic Place</th>
                                            <th style="text-align: center">Number of People</th>
                                            <th style="text-align: center">Crowd Density</th>
                                        </tr>
                                        <%
                                            for (String[] s : results) {
                                                if (s[2].equals("5") || s[2].equals("6")) {
                                                    out.println("<tr style='background-color: pink' height='35'>");
                                                } else if (s[2].equals("3") || s[2].equals("4")) {
                                                    out.println("<tr style='background-color: lightyellow' height='35'>");
                                                } else {
                                                    out.println("<tr style='background-color: #CCFFCC' height='35'>");
                                                }
                                                out.println("<td>" + s[0] + "</td>");
                                                out.println("<td align='center'>" + s[1] + "</td>");
                                                out.println("<td align='center'>" + s[2] + "</td>");
                                                out.println("</tr>");
                                            }
                                        %>
                                    </table>
                                    <%
                                            }
                                        }
                                    %>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            </table>
            <br>
        </div>
    </body>
</html>
