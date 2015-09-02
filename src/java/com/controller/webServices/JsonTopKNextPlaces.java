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
import com.sloca.controller.NextPlaceServlet;
import static com.sloca.controller.NextPlaceServlet.entriesSortedByValues;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Seconds;

/**
 *
 * @author G4T8
 */
public class JsonTopKNextPlaces extends HttpServlet {

    Connection conn;
    PreparedStatement pstmt;
    ResultSet rs;

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JsonObject finalJson = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray errorArray = new JsonArray();

        try {
            String semPlace = request.getParameter("origin");
            String date_s = request.getParameter("date");
            String time_s = null;


            //validation - date
            if (date_s == null) {
                errorArray.add(new JsonPrimitive("missing date"));
            } else {
                try {
                    DateTime ts2 = new DateTime(date_s);
                } catch (IllegalArgumentException e) {
                    errorArray.add(new JsonPrimitive("invalid date"));
                }
            }


            String kvalue = request.getParameter("k");
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



            if (errorArray.size() == 0) {

                time_s = date_s.substring(date_s.indexOf("T") + 1, date_s.length());
                System.out.println(time_s);


                String year = date_s.substring(0, date_s.indexOf("-"));
                String month = date_s.substring(date_s.indexOf("-") + 1, date_s.lastIndexOf("-"));
                String day = date_s.substring(date_s.lastIndexOf("-") + 1, date_s.indexOf("T"));
                date_s = year + "-" + month + "-" + day;
                System.out.println(date_s);

                Timestamp ts = Timestamp.valueOf(date_s + " " + time_s);
                Timestamp tsBefore = getTimeBefore(ts);
                Timestamp tsAfter = getTimeAfter(ts);

                int totalUsers = 0;
                int nextPlaceUsers = 0;

                conn = ConnectionFactory.getConnection();

                String sql = "select temp.mac as mac2 from location t inner join "
                        + "(select mac_address as mac, max(time_stamp) as time from location t "
                        + "where time_stamp between '" + tsBefore + "' and '" + ts + "' "
                        + "group by mac_address) as temp "
                        + "on t.mac_address = temp.mac and t.time_stamp = temp.time "
                        + "inner join location_lookup l on l.location_id = t.location_id where semanticplace = '" + semPlace + "'";
                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                    totalUsers++;
                }


                pstmt = conn.prepareStatement("select l2.mac_address as mac, u2.semanticplace, l2.time_stamp from location l2 "
                        + "inner join ((" + sql
                        + ") as temp2), location_lookup u2 "
                        + "where l2.mac_address = temp2.mac2 and u2.location_id = l2.location_id and l2.time_stamp between '" + ts + "' and '" + tsAfter + "' "
                        + "group by l2.mac_address, l2.time_stamp "
                        + "order by l2.mac_address, l2.time_stamp DESC");
                rs = pstmt.executeQuery();

                String mac = "";
                String semanticPlace = "";
                DateTime before = null;
                DateTime after = null;
                DateTime temp = null;
                DateTime last = new DateTime(tsAfter.getTime());
                boolean alrCount = false;
                HashMap<String, Integer> placeTime = new HashMap<String, Integer>();

                while (rs.next()) {
                    if (mac.equals("") && semanticPlace.equals("")) {
                        System.out.println("firstTime");
                        mac = rs.getString(1);
                        semanticPlace = rs.getString(2);
                        after = last;
                        temp = new DateTime(rs.getTimestamp(3).getTime());
                    } else if (!mac.equals(rs.getString(1))) {
                        System.out.println("changeName");
                        before = temp;
                        Interval in = new Interval(before, after);
                        if (Seconds.secondsIn(in).getSeconds() >= 300 && !alrCount) {
                            Integer timeCount = placeTime.get(semanticPlace);
                            if (timeCount != null) {
                                placeTime.put(semanticPlace, (timeCount + 1));
                            } else {
                                placeTime.put(semanticPlace, 1);
                            }
                            nextPlaceUsers++;
                        }
                        alrCount = false;
                        mac = rs.getString(1);
                        semanticPlace = rs.getString(2);
                        after = last;
                        before = null;
                        temp = new DateTime(rs.getTimestamp(3).getTime());
                    } else if (!semanticPlace.equals(rs.getString(2))) {
                        System.out.println("same user changePlace");
                        before = temp;
                        Interval in = new Interval(before, after);
                        if (Seconds.secondsIn(in).getSeconds() >= 300 && !alrCount) {
                            Integer timeCount = placeTime.get(semanticPlace);
                            if (timeCount != null) {
                                placeTime.put(semanticPlace, (timeCount + 1));
                            } else {
                                placeTime.put(semanticPlace, 1);
                            }
                            nextPlaceUsers++;
                            alrCount = true;
                        }
                        semanticPlace = rs.getString(2);
                        after = before;
                        before = null;
                        temp = new DateTime(rs.getTimestamp(3).getTime());
                    } else {
                        System.out.println("nothing changes!");
                        temp = new DateTime(rs.getTimestamp(3).getTime());
                        continue;
                    }
                }
                before = temp;
                Interval in = new Interval(before, after);
                if (Seconds.secondsIn(in).getSeconds() >= 300 && !alrCount) {
                    Integer timeCount = placeTime.get(semanticPlace);
                    if (timeCount != null) {
                        placeTime.put(semanticPlace, (timeCount + 1));
                    } else {
                        placeTime.put(semanticPlace, 1);
                    }
                    nextPlaceUsers++;
                }
                System.out.println(placeTime.size());

                SortedSet<Map.Entry<String, Integer>> newSet = entriesSortedByValues(placeTime);

                request.setAttribute("totalUsers", totalUsers);
                request.setAttribute("nextPlaceUsers", nextPlaceUsers);
                request.setAttribute("set", newSet);

                finalJson.addProperty("status", "success");
                finalJson.addProperty("total-users", totalUsers);
                finalJson.addProperty("total-next-place-users", nextPlaceUsers);

                
                JsonArray result = new JsonArray() ; 
                int counter = 1;
                for (Map.Entry<String, Integer> cursor : newSet) {
                    JsonObject nextPlaceObject = new JsonObject() ; 
                    nextPlaceObject.addProperty("rank", counter) ; 
                    String place = cursor.getKey();
                    nextPlaceObject.addProperty("semantic-place", place) ; 
                    nextPlaceObject.addProperty("count",cursor.getValue()) ; 
                    result.add(nextPlaceObject) ; 

                    counter++;
                }
                finalJson.add("results",result) ; 


                out.println(finalJson);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "<b>Error!</b><br>Please enter a valid datetime format.");
            RequestDispatcher rd = request.getRequestDispatcher("top_k_next_places.jsp");
            rd.forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "<b>Oops!</b><br> Something went wrong! Please check your input fields!");
            RequestDispatcher rd = request.getRequestDispatcher("top_k_next_places.jsp");
            rd.forward(request, response);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (out != null) {
                    out.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(NextPlaceServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Retrieve the previous 15 minutes timestamp
     * @param timestamp the current timestamp
     * @return calculated timestamp
     */
    public Timestamp getTimeBefore(Timestamp timestamp) {
        Long mSeconds = timestamp.getTime();
        Timestamp before = new Timestamp(mSeconds - 900000);
        return before;
    }

    /**
     * Retrieve the 15 minutes elapsed timestamp 
     * @param timestamp the current timestamp
     * @return the calculated timestamp
     */
    public Timestamp getTimeAfter(Timestamp timestamp) {
        Long mSeconds = timestamp.getTime();
        Timestamp after = new Timestamp(mSeconds + 900000);
        return after;
    }

    
    public static <String, Integer extends Comparable<? super Integer>> SortedSet<Map.Entry<String, Integer>> entriesSortedByValues(Map<String, Integer> map) {
        SortedSet<Map.Entry<String, Integer>> sortedEntries = new TreeSet<Map.Entry<String, Integer>>(
                new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                int res = e1.getValue().compareTo(e2.getValue());
                if (res < 1) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
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
