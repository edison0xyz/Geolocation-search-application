/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller.webServices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sloca.connection.ConnectionFactory;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 *
 * @author G4T8
 */
/**
 * A servlet that is responsible for processing Heatmap.
 *
 * @author G4T8
 */
//@WebServlet(name = "JsonHeatMap", urlPatterns = {"/json/heatmap"})
public class JsonHeatMap extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject finalJson = new JsonObject();
        JsonArray errorArray = new JsonArray();

        try {
            finalJson.addProperty("status", "error");

            String floor = request.getParameter("floor");
            String date = request.getParameter("date");
            String token = request.getParameter("token");

            //validation - date
            if (date == null) {
                errorArray.add(new JsonPrimitive("missing date"));
            } else if (date.equals("")) {
                errorArray.add(new JsonPrimitive("blank date"));
            } else if (date.indexOf("T") == -1) {
                errorArray.add(new JsonPrimitive("invalid date"));
            } else {
                try {
                    DateTime ts2 = new DateTime(date);
                } catch (IllegalArgumentException e) {
                    errorArray.add(new JsonPrimitive("invalid date"));
                }
            }

            //validation - token
            if (token == null) {
                errorArray.add(new JsonPrimitive("missing token"));
            } else if (token.equals("")) {
                errorArray.add(new JsonPrimitive("blank token"));
            } else {
                try {
                    JWTUtility.verify(token, "ylleeg4t8");
                } catch (JWTException e) {
                    errorArray.add(new JsonPrimitive("invalid token"));
                }
            }

            int floor1;
            //validation - floor
            if (floor == null) {
                errorArray.add(new JsonPrimitive("missing floor"));
            } else if (floor.equals("")) {
                errorArray.add(new JsonPrimitive("blank floor"));
            } else {
                try {
                    floor1 = Integer.parseInt(floor);
                    if (floor1 < 0 || floor1 > 5) { //if value is not valid
                        throw new NumberFormatException();
                    } else {
                        switch (floor1) {
                            case 0:
                                floor = "B1";
                                break;
                            case 1:
                                floor = "L1";
                                break;
                            case 2:
                                floor = "L2";
                                break;
                            case 3:
                                floor = "L3";
                                break;
                            case 4:
                                floor = "L4";
                                break;
                            case 5:
                                floor = "L5";
                                break;
                        }
                    }
                } catch (NumberFormatException e) {
                    errorArray.add(new JsonPrimitive("invalid floor"));
                }
            }

            if (errorArray.size() > 0) {
                finalJson.add("messages", errorArray);
                out.println(gson.toJson(finalJson));
                return;
            }
            /*
             String year = date.substring(date.lastIndexOf("-") + 1);
             String month = date.substring(date.indexOf("-") + 1, date.lastIndexOf("-"));
             String day = date.substring(0, date.indexOf("-"));
             String date_string = year + "-" + month + "-" + day;
             */
            String time = date.substring(date.indexOf("T") + 1, date.length());
            date = date.substring(0, date.indexOf("T"));

            System.out.println(time);
            System.out.println(date);

            LocalDate dateNow_s = new LocalDate(date);
            LocalDate dateBefore_s = dateNow_s;
            LocalTime timeNow = new LocalTime(time);
            LocalTime timeBefore = timeNow.minusMinutes(15);
            if (timeBefore.isAfter(timeNow)) {
                dateBefore_s = dateNow_s.minusDays(1);
            }

            String timeNow_s = timeNow.toString("HH:mm:ss");
            String timeBefore_s = timeBefore.toString("HH:mm:ss");
            String now = dateNow_s + " " + timeNow_s;
            String before = dateBefore_s + " " + timeBefore_s;
            String timeNow_t = now.replace(" ", "T");
            String timeBefore_t = before.replace(" ", "T");


            connection = ConnectionFactory.getConnection();
            // need to update sql query
            // code might not cater for people who do not exist in demographics
            // get number of mac_addresses under a semanticplace

            String query = "select ll.semanticplace, count(distinct mac_address) as mac_address "
                    + "from location_lookup ll left outer join "
                    + "	(select time_stamp, mac_address, location_id, semanticplace "
                    + "	from "
                    + "		(select time_stamp, mac_address, l.location_id, semanticplace "
                    + "		from location l, location_lookup ll "
                    + "		where time_stamp between '" + before + "' "
                    + "		and '" + now + "' "
                    + "		and l.location_id = ll.location_id "
                    + "		order by time_stamp desc) as temp1 "
                    + "	group by temp1.mac_address) as temp2 "
                    + "on ll.location_id = temp2.location_id "
                    + "where ll.semanticplace like 'SMUSIS" + floor + "%' "
                    + "group by ll.semanticplace "
                    + ";";

            System.out.println(floor);
            System.out.println(now);

            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();
            System.out.println("query successful");
            ArrayList<String[]> returnList = new ArrayList<String[]>();

            while (rs.next()) {
                int count = rs.getInt("mac_address");
                int density = 0;
                if (count == 0) {
                    density = 0;
                } else if (count >= 1 && count <= 2) {
                    density = 1;
                } else if (count >= 3 && count <= 5) {
                    density = 2;
                } else if (count >= 6 && count <= 10) {
                    density = 3;
                } else if (count >= 11 && count <= 20) {
                    density = 4;
                } else if (count >= 21 && count <= 30) {
                    density = 5;
                } else if (count >= 31) {
                    density = 6;
                }

                String splace = rs.getString("semanticplace");
                String count1 = Integer.toString(count);
                String density1 = Integer.toString(density);

                String[] arr = new String[3];
                arr[0] = splace;
                arr[1] = count1;
                arr[2] = density1;
                returnList.add(arr);
            }
            System.out.println("size " + returnList.size());


            finalJson.addProperty("status", "success");
            JsonArray array = new JsonArray();

            for (String[] arr : returnList) {
                JsonObject obj = new JsonObject();
                obj.addProperty("semantic-place", arr[0]);
                obj.addProperty("num-people", Integer.valueOf(arr[1]));
                obj.addProperty("crowd-density", Integer.valueOf(arr[2]));
                array.add(obj);

            }

            finalJson.add("heatmap", array);
            out.println(gson.toJson(finalJson));


        } catch (SQLException ex) {
//            Logger.getLogger(JsonHeatMap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ie) {
//            request.setAttribute("error", "<b>Error!</b><br>Please enter a valid datetime format.");
//            rd.forward(request, response);
            out.println(ie.getMessage());
        } catch (Exception e) {
            out.println(e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (connection != null) {
                    connection.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(JsonHeatMap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        out.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
