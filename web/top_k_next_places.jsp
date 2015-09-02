<%-- 
    Document   : top_k_next_places
    Created on : Oct 10, 2014, 10:51:59 PM
    Author     : Ryan
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="com.sloca.model.LocationLookupDAO"%>
<%@page import="java.util.SortedSet"%>
<%@page import="java.util.Map"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file='protect.jsp'%>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="protect.jsp" />
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
        <title>SLOCA - Top K Next Places</title>

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
    <body class="metro">
        <header class="bg-dark" data-load="design/user_header.jsp"></header>
        <br>
        <div class="container">
            <table>
                <tr>
                    <td style="text-align:left;vertical-align:top;">
                        <h1>Top-k Next Places</h1>
                        <br>
                        <%
                            String date = (String) request.getAttribute("date");
                            String time = (String) request.getAttribute("time");
                            String kvalue1 = (String) request.getAttribute("kvalue");
                            String semanticplace = (String)request.getAttribute("semanticplace");
                            
                            String error = (String) request.getAttribute("error");
                            Integer totalUsers = (Integer) request.getAttribute("totalUsers");
                            Integer nextPlaceUsers = (Integer) request.getAttribute("nextPlaceUsers");

                            if (date == null) {
                                date = "";
                            }
                            if (time == null) {
                                time = "";
                            }

                            if (error != null) {
                                out.println("<div class='alert alert-danger'>");
                                out.println(error);
                                out.println("</div>");
                            }
                            
                            int kvalue = 3;
                            if (kvalue1 != null) {
                                kvalue = Integer.parseInt(kvalue1);
                            }
                        %>

                        <form action="nextplace.do" method="POST">
                            <table>
                                <col width="200">
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
                                    <td><b>Semantic Place Name:</b></td>
                                    <td>

                                        <!--
                                        <div class="input-control text size4">
                                            <input type="text" name="semanticPlace">
                                        </div>-->
                                        <div class="input-control select">    
                                            <%
                                                LocationLookupDAO lookup_dao = new LocationLookupDAO();
                                                ArrayList<String> list = lookup_dao.retrieveAllSemanticPlaces();

                                                out.println("<select name='sem' class='form-control'>");
                                                for (String s : list) {
                                                    if(s.equals(semanticplace)){
                                                        out.println("<option value='" + s + "' selected>" + s + "</option>");
                                                    }
                                                    out.println("<option value='" + s + "'>" + s + "</option>");
                                                }
                                                out.println("</select>");
                                            %>
                                        </div>
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
                                <tr>
                                    <td>&nbsp</td><td>&nbsp</td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td><button class="primary large" type="submit">Go!</button></td>
                                </tr>
                            </table>
                        </form>
                    </td>
                    <td width="50px">&nbsp;</td>

                    <%
                        Object o = request.getAttribute("set");
                        if (o != null) {
                    %>
                    <td style="text-align:left;vertical-align:top;">
                        <div class="tab-control" data-role="tab-control" style="width: 500px">
                            <ul class="tabs">
                                <li class="active"><a href="#_page_1">Results</a></li>
                            </ul>

                            <div class="frames">
                                <div class="frame" id="_page_1">
                                    <%
                                            SortedSet<Map.Entry<String, Integer>> newSet = (SortedSet<Map.Entry<String, Integer>>) o;

                                            if (newSet.size() == 0) {
                                                out.println("<div class='alert alert-success'>");
                                                out.println("Search complete! No results found!");
                                                out.println("</div>");
                                            } else {
                                                out.println("<table>");
                                                out.println("<col width='150'");
                                                out.println("<tr><td><b>Total users:</b></td><td>" + totalUsers + "</td></tr>");
                                                out.println("<tr><td><b>Next-place users:</b></td><td>" + nextPlaceUsers + "</td></tr>");
                                                out.println("</table><br>");
                                                out.println("<table class='table table-striped table-bordered table-hover' border='2' style='width: 100%;' align='center'>");
                                                out.println("<tr>");
                                                out.println("<th>Rank</th><th>Place</th><th>Number of users</th><th>Percentage (%)</th>");
                                                out.println("</tr>");
                                                int counter = 1;
                                                for (Map.Entry<String, Integer> cursor : newSet) {
                                                    String place = cursor.getKey();
                                                    place = place.replaceFirst("SMUSIS", "");
                                                    out.println("<tr>");
                                                    out.println("<td align='center'>" + counter + "</td>");
                                                    out.println("<td>" + place + "</td>");
                                                    out.println("<td align='center'>" + cursor.getValue() + "</td>");
                                                    out.println("<td align='center'>" + (100 * cursor.getValue() / totalUsers) + "</td>");
                                                    out.println("</tr>");
                                                    counter++;
                                                }
                                                out.println("</table>");
                                            }
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
