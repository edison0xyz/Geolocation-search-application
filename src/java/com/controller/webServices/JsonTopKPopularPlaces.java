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
import com.sloca.controller.PopularPlacesServlet;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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
@WebServlet(name = "JsonTopKPopularPlaces", urlPatterns = {"/json/top-k-popular-places"})
public class JsonTopKPopularPlaces extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        JsonObject finalJson = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray errorArray = new JsonArray();

        try {
            finalJson.addProperty("status", "error");
            
            String date = request.getParameter("date");
            String kvalue = request.getParameter("k");
            String token = request.getParameter("token");
            
            //validation - date
            if (date == null) {
                errorArray.add(new JsonPrimitive("missing date"));
            } else {
                try {
                    DateTime ts2 = new DateTime(date);
                } catch (IllegalArgumentException e) {
                    errorArray.add(new JsonPrimitive("invalid date"));
                }
            }
            
            //validation - k
            if (kvalue == null) {
                kvalue = "3";
            } else {
                try {
                    int kvalue1 = Integer.parseInt(kvalue);
                    if (kvalue1 < 1 || kvalue1 > 10) { //if value is not valid
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    errorArray.add(new JsonPrimitive("invalid k"));
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
            
            if(errorArray.size() > 0){
                finalJson.add("message", errorArray);
                out.println(gson.toJson(finalJson));
                return;
            }
            
            
            
            String time = date.substring(date.indexOf("T") + 1, date.length());
            String date_string = date.substring(0, date.indexOf("T"));

            LocalDate dateNow_s = new LocalDate(date_string);
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
            /*
            String query = "select temp.sem as sem2, count(temp.mac) as totalCount from (select mac_address as mac, max(time_stamp), semanticplace as sem from location t " +
                           "inner join location_lookup l " +
                           "on t.location_id = l.location_id where time_stamp between '" + timeBefore_t + "' and '" + timeNow_t + "' " +
                           "group by mac) as temp " +
                           "group by sem2 " +
                           "order by totalCount DESC";
            */
            String query = "select temp2.sem as sem2, count(distinct mac) as totalCount "
                    + "from "
                    + "	(select temp1.time_stamp, temp1.mac, temp1.location_id, ll.semanticplace as sem "
                    + "	from location_lookup ll, "
                    + "		(select time_stamp, mac_address as mac, location_id "
                    + "		from location "
                    + "		where time_stamp between '" + before + "' "
                    + "		and '" + now + "' "
                    + "		order by time_stamp desc) as temp1 "
                    + "	where temp1.location_id = ll.location_id "
                    + "	group by temp1.mac) as temp2 "
                    + "group by temp2.sem "
                    + "order by totalCount desc "
                    + ";";
            
            
            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();

            int kvalue1 = Integer.parseInt(kvalue);
            int counter = 0;
            String prevNumOfPeople = "";
            JsonArray jsonList = new JsonArray();
            while (rs.next()) {

                if (prevNumOfPeople.equals(rs.getString("totalCount")) == false) {
                    counter++;
                }
                if (counter > kvalue1) {
                    break;
                }

                JsonObject place = new JsonObject();
                place.addProperty("rank", counter);
                place.addProperty("semantic-place", rs.getString("sem2"));
                place.addProperty("count", rs.getInt("totalCount"));

                prevNumOfPeople = rs.getString("totalCount");
                jsonList.add(place);
            }

            finalJson.addProperty("status", "success");
            finalJson.add("results", jsonList);
            out.println(gson.toJson(finalJson));

        } catch (SQLException ex) {
            Logger.getLogger(PopularPlacesServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException e) {
            finalJson.addProperty("status", "error");
            finalJson.addProperty("message", "Invalid datetime format");
            out.println(gson.toJson(finalJson));
        } catch (Exception e) {
            finalJson.addProperty("status", "error");
            finalJson.addProperty("message", "Invalid fields");
            out.println(gson.toJson(finalJson));
            out.println(e.getMessage());
        } finally {
            try {
                out.close();
                rs.close();
                pstmt.close();
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(PopularPlacesServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
