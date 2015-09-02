
<%@page import="com.sloca.entity.User"%>
<%@page import="com.sloca.model.UserDAO"%>
<%@page import="org.joda.time.Instant"%>
<%@page import="org.joda.time.Interval"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="org.joda.time.Duration"%>
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

        <title>Top-k Companions</title>

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

        <!-- datepicker -->
        <link rel="stylesheet" href="//code.jquery.com/ui/1.11.1/themes/smoothness/jquery-ui.css">
        <script src="//code.jquery.com/jquery-1.10.2.js"></script>
        <script src="//code.jquery.com/ui/1.11.1/jquery-ui.js"></script> 

        <script language='JavaScript'>
            $(function() {
                $("#datepicker").datepicker({dateFormat: 'dd-mm-yy'});
            });
        </script>

    </head>
    <body class='metro'>
        <header class="bg-dark" data-load="design/user_header.jsp"></header>
        <br>
        <div class="container">
            <table>
                <tr>
                    <td style="text-align:left;vertical-align:top;">
                        <h1>Top-k Companion</h1>
                        <br>
                        <%
                            String date = (String) request.getAttribute("date");
                            String time = (String) request.getAttribute("time");
                            String mac_input = (String) request.getAttribute("mac_input");
                            String mac_select = (String) request.getAttribute("mac_select");
                            String kvalue1 = (String) request.getAttribute("kvalue");

                            if (date == null) {
                                date = "";
                            }
                            if (time == null) {
                                time = "";
                            }
                            if (mac_input == null) {
                                mac_input = "";
                            }
                            if (mac_select == null) {
                                mac_select = "";
                            }

                            int kvalue = 3;
                            if (kvalue1 != null) {
                                kvalue = Integer.parseInt(kvalue1);
                            }
                            if (request.getAttribute("error") != null) {
                                out.println("<div class='alert alert-danger'>");
                                out.println(request.getAttribute("error"));
                                out.println("</div>");
                            }


                        %>



                        <form action="Companions.do" method="post">
                            <table>
                                <col width='100'>
                                <tr>

                                    <td><b>Mac Address:</b></td>
                                    <td>
                                        <input type="text" name="mac_input" placeholder="Enter mac-address" value="<%=mac_input%>"class="form-control">

                                        <div align="center"><b>OR</b></div>

                                        <div class='input-control select'>
                                            <select name="mac_select" class="form-control">
                                                <%
                                                    UserDAO userdao = new UserDAO();
                                                    ArrayList<User> userList = userdao.retrieveAll();
                                                    out.println("<option value=''>--- Select mac-address ---</option>");
                                                    for (User u : userList) {
                                                        out.println("<option value='" + u.getMacAddress() + "' ");
                                                        if (u.getMacAddress().equals(mac_select)) {
                                                            out.println("selected");
                                                        }
                                                        out.println(">" + u.getMacAddress() + ", " + u.getName() + "</option>");
                                                    }
                                                %>

                                            </select>
                                        </div>


                                    </td>
                                </tr>
                                <tr><td>&nbsp</td><td>&nbsp</td></tr>
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
                                    <td></td>
                                    <td>
                                        <button class="primary large" type="submit">Go!</button>
                                    </td>
                                </tr>
                            </table>
                        </form>

                    </td>
                    <td width="50px">&nbsp;</td>
                    <%
                        ArrayList<String[]> results = (ArrayList<String[]>) request.getAttribute("results");
                        String message = (String) request.getAttribute("message");

                        if (results != null || message != null) {
                    %>
                    <td style="text-align:left;vertical-align:top;">
                        <div class="tab-control" data-role="tab-control" style="width: 500px;">
                            <ul class="tabs">
                                <li class="active"><a href="#_page_1">Results</a></li>
                            </ul>

                            <div class="frames">
                                <div class="frame" id="_page_1">
                                    <%
                                        if (message != null) {
                                            out.println("<div class='alert alert-success'>");
                                            out.println("<b>Search complete! No results found!</b><br><br>");
                                            out.println(message);
                                            out.println("</div>");
                                        }

                                        if (results != null) {
                                    %>
                                    <table class='table-bordered table-striped' style="width: 100%">

                                        <tr height='35'>
                                            <th style="text-align: center">Ranking</th>
                                            <th>Name</th>
                                            <th style="text-align: center">Duration (seconds)</th>
                                        </tr>

                                        <%
                                            int counter = 1;
                                            String prevDuration = null;
                                            for (String[] s : results) {
                                                out.println("<tr height='35'>");
                                                if (s[1].equals(prevDuration)) {
                                                    counter--;
                                                }
                                                out.println("<td align='center'>" + (counter++) + "</td>");
                                                out.println("<td>" + s[0] + "</td>");
                                                out.println("<td align='center'>" + s[1] + "</td>");
                                                out.println("</tr>");

                                                prevDuration = s[1];
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
