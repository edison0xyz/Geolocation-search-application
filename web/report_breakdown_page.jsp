<%@page import="java.text.DecimalFormat"%>
<%@include file='protect.jsp'%>
<%@page import="com.google.gson.JsonElement"%>
<%@page import="java.io.IOException"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.google.gson.JsonArray"%>
<%@page import="com.google.gson.JsonObject"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.util.HashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
        <script type="text/javascript">
            function cleanOption2(ddl) {
                var val = ddl.options[ddl.selectedIndex].value;

                //Clear all items
                $("#second > option").remove();


                //Add all options from dropdown 1
                $("#" + ddl.id + "> option").each(function() {
                    var opt = document.createElement("option");
                    opt.text = this.text;
                    opt.value = this.value;
                    document.getElementById("second").options.add(opt);
                });

                //Remove selected
                $("#second option[value='" + val + "']").remove();

                //Clear all items
                $("#third > option").remove();


                //Add all options from dropdown 1
                $("#" + ddl.id + "> option").each(function() {
                    var opt = document.createElement("option");
                    opt.text = this.text;
                    opt.value = this.value;
                    document.getElementById("third").options.add(opt);
                });

                //Remove selected
                $("#third option[value='" + val + "']").remove();

            }
            function cleanOption3(ddl) {
                var val = ddl.options[ddl.selectedIndex].value;

                //Clear all items
                $("#third > option").remove();


                //Add all options from dropdown 1
                $("#" + ddl.id + "> option").each(function() {
                    var opt = document.createElement("option");
                    opt.text = this.text;
                    opt.value = this.value;
                    document.getElementById("third").options.add(opt);
                });

                //Remove selected
                $("#third option[value='" + val + "']").remove();

            }
        </script>
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

        <title>SLOCA - Breakdown Location Report </title>

    </head>
    <body class="metro">
        <header class="bg-dark" data-load="design/user_header.jsp"></header>
        <br>
        <div class="container">
            <h1>Location Breakdown</h1>
            <i>This report will breakdown demographics of users in the building by year, gender and/or school</i>
            <br><br>
            <%
                String date = (String) session.getAttribute("date");
                String time = (String) session.getAttribute("time");

                if (date == null) {
                    date = "";
                }
                if (time == null) {
                    time = "";
                }

                if (session.getAttribute("error") != null) {
                    out.println("<div class='alert alert-danger'>");
                    out.println(session.getAttribute("error"));
                    out.println("</div>");
                    session.removeAttribute("error");
                }
            %>
            <br>
            <form action="basic-loc-report" method="post">


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
                        <td><b>Select Order: </b></td>
                        <td>
                            <select class="form-control" id="first" onchange="cleanOption2(this)" name="firstFilter" style="width:300px">
                                <option value="">--Select the first filter--</option>
                                <option value="school">School</option>
                                <option value="year">Year</option>
                                <option value="gender">Gender</option>
                            </select>
                    </tr>
                    <tr><td></td><td>
                            <select class="form-control" id="second" onchange="cleanOption3(this)" name="secondFilter" style="width: 300px">
                                <option value="">--Select the second filter--</option>
                                <option value="school">School</option>
                                <option value="year">Year</option>
                                <option value="gender">Gender</option>
                            </select>
                        </td>
                    </tr>
                    <tr><td></td><td>
                            <select id="third" class="form-control" name="thirdFilter" style="width: 300px">
                                <option value="">--Select the third filter--</option>
                                <option value="school">School</option>
                                <option value="year">Year</option>
                                <option value="gender">Gender</option>
                            </select>
                        </td>
                    </tr>
                    <tr><td>&nbsp;</td></tr>
                    <tr>
                        <td></td><td><input type="submit" value="Go!" class='primary large'></td>
                    </tr>

                </table>

            </form>
            <br>
            <%
                JsonObject jsonResult = (JsonObject) session.getAttribute("data_results");

                if (jsonResult != null) {
                    out.println("<h2>Results:</h2><br>");

                    if (session.getAttribute("order") != null && session.getAttribute("qtime") != null) {
                        out.println("<b>Report (sorted by): </b>" + session.getAttribute("order") + "<br>");
                        out.println("<b>Time Queried: </b>" + session.getAttribute("qtime") + "<br>");
                        session.removeAttribute("order");
                        session.removeAttribute("qtime");
                    }
                    if (jsonResult.get("breakdown") == null) {
                        out.println("Sorry, there are no results. Please try again.");
                    } else {
                        
                        int count = 0 ; 
                        if(jsonResult.get("count") != null) {
                            count = Integer.parseInt(jsonResult.get("count").getAsString()) ;    
                        }
                        int numofpeople = 0;
                        if(session.getAttribute("numofpeople") != null){
                            numofpeople = (Integer)session.getAttribute("numofpeople");
                            session.removeAttribute("numofpeople");
                        }
                        System.out.println("numofpeople: " + numofpeople);
                        JsonArray breakdown1 = jsonResult.getAsJsonArray("breakdown");
                        recursivePrint(breakdown1, out, numofpeople);
                    }
                }
            %>

            
            <%!
                public void recursivePrint(JsonArray array, JspWriter out, int numofpeople) throws IOException {
                    
                    
                    
                    Iterator<JsonElement> countIter = array.iterator();
                    int max = 0 ; 
                    while(countIter.hasNext())  {
                        JsonObject countObject = (JsonObject)countIter.next() ; 
                        if(countObject.get("count") != null )   {
                            int temp = countObject.get("count").getAsInt() ; 
                            max += temp ; 
                            
                        }
                    }
                    
                    // Retrieving first breakdown results
                    Iterator<JsonElement> iter2 = array.iterator();
                    out.println("<table class='table' border='1' width='400' cellspacing='8' align='center'>");
                    
                    while (iter2.hasNext()) {
                        JsonElement element = iter2.next();
                        if (element.isJsonObject()) {

                            out.println("<tr>");
                            JsonObject object1 = (JsonObject) element;
                            int count = Integer.parseInt(object1.get("count").getAsString()) ;
                            System.out.println(count) ; 

                            out.println("<td>");
                            if (object1.get("gender") != null) { 
                                out.println(object1.getAsJsonPrimitive("gender").getAsString());
                            } else if (object1.get("year") != null) {
                                out.println(object1.getAsJsonPrimitive("year").getAsString());
                            } else {
                                out.println(object1.getAsJsonPrimitive("school").getAsString());
                            }
                            out.println("</td>");

                            out.println("<td>");
                            out.println(object1.getAsJsonPrimitive("count").getAsString());
                            out.println("</td>");
                            
                            if(max != 0 )  {
                                out.println("<td>");
                                DecimalFormat df = new DecimalFormat("###"); 
                                out.println(df.format(((double)object1.getAsJsonPrimitive("count").getAsInt()/(double)numofpeople ) * 100) + "%");
                                out.println("</td>");
                            }
                            
                            

                            if (object1.get("breakdown") != null) {
                                // Other objects present. Recurse
                                out.println("<td>");
                                JsonArray breakdownArr = object1.getAsJsonArray("breakdown");
                                recursivePrint(breakdownArr, out, numofpeople);
                                out.println("</td>");
                            }
                            out.println("</tr>");
                        }
                    }
                    out.println("</table>");
                }
                
            %>
        </div>
        <br><br>
    </body>
</html>
