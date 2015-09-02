<%-- 
    Author     : G4T8
--%>

<%@page import="com.google.gson.JsonElement"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.google.gson.JsonArray"%>
<%@page import="com.google.gson.JsonObject"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.SortedSet"%>
<%@page import="java.util.Map"%>
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
        <title>Bootstrap</title>
    </head>
    <body class="metro">
        <header class="bg-dark" data-load="design/admin_header.jsp"></header>
        <br>

        <%
            if (session.getAttribute("admin") == null) {
                response.sendRedirect("login.jsp");
                return;
            }

        %>


        <div class="container">
            <div class="page-header">
                <h1>Welcome, Admin!</h1><br>
                <h2>Bootstrap Menu</h2>
                <br>
                <div align="center">
                    <form action="bootstrap" method="POST" enctype="multipart/form-data">
                        <div class="input-control file" align='center' style="width:600px;">
                            <input type="file" name='bootstrap-file' /><br>
                            <button class="btn-file"></button><br>
                        </div>
                        <br>
                        <div align='center'>
                            <button class="primary large" type="submit" value="bootstrap" name="action">Bootstrap</button>
                        </div>
                    </form>
                    <br><h1>OR</h1><br>

                    <form action="json/bootstrap?action=upload" method="POST" enctype="multipart/form-data">
                        <div class="input-control file" align='center' style="width:600px;">
                            <input type="file" name='bootstrap-file' /><br>
                            <button class="btn-file"></button><br>
                        </div>
                        <br>
                        <div align='center'>
                            <button class="primary large" type="submit" value="update" name="action">Update</button>
                        </div>
                    </form>
                </div>

            </div>


            <%
                if (session.getAttribute("errorMsg") != null) {
                    out.println(session.getAttribute("errorMsg"));

                }

                JsonObject bootstrapResult = (JsonObject) session.getAttribute("BootstrapResult");
                if (bootstrapResult != null) {
            %>

            <h2>Summary of Bootstrap</h2>
            <table class="striped" cellpadding="10" style="width:100%">

                <%

                    session.removeAttribute("BootstrapResult");
                    if (bootstrapResult != null) {
                        if (bootstrapResult.get("num-recorded-loaded") != null) {
                            JsonArray recordedArray = bootstrapResult.getAsJsonArray("num-recorded-loaded");
                            Iterator<JsonElement> iter = recordedArray.iterator();%>
                <thead>
                <th>
                    Description
                </th>
                <th>
                    Number of data loaded
                </th>
                </thead>
                <%while (iter.hasNext()) {
                        JsonObject jobj = (JsonObject) iter.next();%>
                <% if (jobj.has("demographics.csv")) {%>
                <tr>
                    <td>

                        Demographics 
                    </td>
                    <td>
                        <%
                            out.println(jobj.get("demographics.csv").getAsInt());
                        %>
                    </td>
                </tr>
                <%}%>
                <% if (jobj.has("location.csv")) {%>   
                <tr>
                    <td>

                        Location 
                    </td>
                    <td>
                        <%
                            out.println(jobj.get("location.csv").getAsInt());%>

                    </td>
                </tr>
                <%}%>
                <% if (jobj.has("location-lookup.csv")) {%>
                <tr>
                    <td>

                        Location-Lookup
                    </td>
                    <td>
                        <%
                            out.println(jobj.get("location-lookup.csv").getAsInt());%>

                    </td>
                </tr>
                <% }%>

                <%}
                    }
                %>
            </table>
            <br>

            <%
                if (bootstrapResult != null & bootstrapResult.get("error") != null) {
                    JsonArray errorArr = bootstrapResult.get("error").getAsJsonArray();
                    if(errorArr.size()!= 0) { 
            %>
         
                    <h2>Error Message</h2>
            <br>

            <table class="striped" cellpadding="10" style="width:100%">
                <thead>
                <th>File</th>
                <th>Line</th>
                <th>Error</th>
                </thead>
                <tbody>

                    <%

                        for (JsonElement obj : errorArr) {
                            JsonObject object = obj.getAsJsonObject();%>

                    <tr>
                        <td><%=object.get("file").getAsString()%></td>
                        <td><%=object.get("line").getAsString()%></td>
                        <td>
                            <%
                                JsonArray errorArrays = object.get("message").getAsJsonArray();
                                out.println("<table>");
                                for (JsonElement element : errorArrays) {%>
                    <tr>
                        <%=element.getAsString()%>
                    </tr>
                    <%}
                        out.println("</table>");
                    %>
                    </tr>
                    </td>
                </tbody>

                <%              }
                            }
                        }
                    }
                %>

            </table>
        </div>
        <%}%>






    </body>
</html>
