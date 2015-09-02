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
        <title>SLOCA - Group Popular Places</title>

    </head>
    <body class="metro">
        <header class="bg-dark" data-load="design/user_header.jsp"></header>
        <br>
        <div class="container">
            <table>
                <tr>
                    <td style="text-align:left;vertical-align:top;">
                        <h1>Group Popular Places</h1>
                        <br>
                        <%

                            String date = (String) request.getAttribute("date");
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
                            String kvalue1 = (String) request.getAttribute("kvalue");
                            int kvalue = 3;
                            if (kvalue1 != null) {
                                kvalue = Integer.parseInt(kvalue1);
                            }
                        %>
                        <form action="group-popular-places.do" method="post">
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
                                <tr>
                                    <td><b>K-value:</b></td>
                                    <td>
                                        <div class='input-control select'>
                                            <select name="kvalue" class='form-control'>
                                                <%
                                                    for (int i = 1; i <= 10; i++) {%>
                                                <option value='<%=i%>' <%
                                                    if (i == kvalue) {
                                                        out.println("selected");
                                                    }
                                                        %>><%=i%></option>
                                                <%}
                                                %>
                                            </select>
                                        </div>
                                    </td>    
                                </tr>
                                <tr><td>&nbsp</td><td>&nbsp</td></tr>
                                <tr>
                                    <td>&nbsp</td>
                                    <td><button class="primary large" type="submit">Go!</button></td>
                                </tr>
                            </table>
                        </form>
                    </td>
                    <td width="50px">&nbsp;</td>
                    <%
                        ArrayList<String[]> results = (ArrayList<String[]>) request.getAttribute("results");
                        if (results != null) {
                    %>
                    <td>
                        <div class="tab-control" data-role="tab-control" style="width: 500px;">
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

                                    %>
                                    <table class='table table-striped table-bordered table-hover' style="width: 100%">
                                        <tr height="35">
                                            <th style="text-align: center">Ranking</th>
                                            <th style="text-align: center">Number of Groups</th>
                                            <th style="text-align: center">Number of People</th>
                                            <th>Semantic Place</th>
                                        </tr>
                                        <%
                                            for (int i = 0; i < results.size(); i++) {
                                                String[] arr = results.get(i);
                                                out.println("<tr height='35'>");
                                                out.println("<td align='center'>" + arr[0] + "</td>");
                                                out.println("<td align='center'>" + arr[1] + "</td>");
                                                out.println("<td align='center'>" + arr[3] + "</td>");
                                                out.println("<td>" + arr[2] + "</td>");
                                                out.println("</tr>");
                                            }
                                        %>

                                    </table>
                                    <% }
                                        }

                                    %>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            </table>
        </div>
    </body>
</html>
