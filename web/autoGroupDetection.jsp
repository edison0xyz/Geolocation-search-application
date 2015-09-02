<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="com.sloca.entity.Group"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file='protect.jsp'%>

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

        <script src="assets/js/metro-calendar.js"></script>
        <script src="assets/js/metro-datepicker.js"></script>

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

        <title>SLOCA - Automatic Group Detection</title>

    </head>
    <body class="metro">
        <header class="bg-dark" data-load="design/user_header.jsp"></header>
        <br>
        <div class="container">
            <table>
                <tr>
                    <td style="text-align:left;vertical-align:top;">
                        <h1>Automatic Group Detection </h1>
                        <br>
                        <%                            String date = (String) request.getAttribute("date");
                            String time = (String) request.getAttribute("time");

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
                        <form action="autogroup.do" method="post">
                            <table>
                                <col width="100">
                                <tr>
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

                                <tr><td>&nbsp</td><td>&nbsp</td></tr>
                                <tr>
                                    <td>&nbsp</td>
                                    <td><button class="primary large" type="submit">Go!</button></td>
                                </tr>
                            </table>
                        </form>
                    </td>
                    <td width="100px">&nbsp;</td>
                    <%
                        ArrayList<Group> results = (ArrayList<Group>) request.getAttribute("results");
                        if (results != null) {
                    %>
                    <td style='width: 700px;'>
                        <!---- Start of Tab Menu Bar ----> 
                        <div class="tab-control" data-role="tab-control">
                            <ul class="tabs">
                                <li class="active"><a href="#_page_1">Results</a></li>
                            </ul>

                            <div class="frames">
                                <div class="frame" id="_page_1">
                                    <%
                                        if (results.size() == 0) {
                                            out.println("<div class='alert alert-success'>");
                                            out.println("Search complete! No results found.");
                                            out.println("</div>");
                                        } else {
                                            // == Under Development, to print output with Json object returned.
                                            //out.println("Number of people in Building: " + "<br>") ; 
                                            int counter = (Integer) request.getAttribute("counter");
                                    %>
                                    <table>
                                        <tr>
                                            <td><b>Number of Groups Detected:</b></td>
                                            <td><%=results.size()%> </td>
                                        </tr>
                                        <tr>
                                            <td><b>Number of People in SIS Building: </b></td>
                                            <td><%=counter%></td>
                                        </tr>
                                    </table>
                                    <br>
                                    <%
                                        for (int i = 0; i < results.size(); i++) {
                                            Group curGroup = results.get(i);
                                            ArrayList<String> userList = curGroup.getAllUserMac();
                                            System.out.println(userList.size());
                                            ArrayList<String[]> strArray = curGroup.getLocationTimeArrayOrdered();
                                            System.out.println(strArray.size());
                                    %>
                                    <div class="accordion" data-role="accordion">
                                        <div class="accordion-frame">
                                            <a style="background-color: #116c9a; color: white" class="heading" href="#">Group <%=(i + 1)%></a>
                                            <div class="content clearfix">
                                                <div class="accordion" data-role="accordion" data-closeany="false">
                                                    <div class="accordion-frame">
                                                        <a style="background-color: #9ecfe7" class="heading" href="#">Mac Address (Email ID)</a>
                                                        <div class="content">
                                                            <p>
                                                                <%
                                                                    out.println("<ol>");
                                                                    for (String s : userList) {
                                                                        String[] st = s.split("!!");
                                                                        out.println("<li>" + st[0] + " (" + st[1] + ")</li>");
                                                                    }
                                                                    out.println("</ol>");
                                                                %>
                                                            </p>
                                                        </div>

                                                    </div>
                                                </div>
                                                <table class="table table-striped table-bordered table-hover">
                                                    <tr>
                                                        <th style="text-align: center">Location ID Visited</th>
                                                        <th style="text-align: center">Time Spent</th>
                                                        <th style="text-align: center">Percentage(%)</th>
                                                    </tr>
                                                    <%
                                                        double perMaxCount = 0;
                                                        for (String[] s: strArray) {
                                                            Integer integer = Integer.parseInt(s[1]);
                                                            perMaxCount += integer;
                                                        }
                                                        
                                                        for (String[] s: strArray) {
                                                            String newPlace = s[0];
                                                            String newTime = s[1];
                                                            int newTimeInt = Integer.parseInt(newTime);
                                                            
                                                            BigDecimal bdTest = new BigDecimal(100 * newTimeInt / perMaxCount);
                                                            bdTest = bdTest.setScale(0, BigDecimal.ROUND_HALF_UP);
                                                            
                                                            out.println("<tr>");
                                                            //out.println("<font size='1'>");
                                                            out.println("<td align='center'>" + newPlace + "</td>");
                                                            out.println("<td align='center'>" + newTime + "</td>");
                                                            out.println("<td align='center'>" + bdTest + "</td>");
                                                            //out.println("</font>");
                                                            out.println("</tr>");
                                                        }
                                                    %>
                                                </table>
                                                </font>
                                            </div>
                                        </div>
                                    </div>
                                    <%
                                        }
                                    %>

                                    </table>
                                    <%
                                            }
                                        }
                                    %>
                                </div>
                            </div>
                        </div>
                        <!-- End of Tab Menu Bar ---> 
                    </td>
                </tr>
            </table>
        </div>
        <br><br><br>
    </body>
</html>
