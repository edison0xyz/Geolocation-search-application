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
import com.sloca.controller.CompanionsServlet;
import com.sloca.entity.User;
import com.sloca.model.UserDAO;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 *
 * @author G4T8
 */
@WebServlet(name = "JsonTopKCompanions", urlPatterns = {"/json/top-k-companions"})
public class JsonTopKCompanions extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        Gson gson = new GsonBuilder().setPrettyPrinting().create(); //to create jsonprinter for printing json
        JsonObject finalJson = new JsonObject(); //the final jsonobject to store all json values
        JsonArray errorArray = new JsonArray();

        try {
            finalJson.addProperty("status", "error");

            String date = request.getParameter("date");
            String kvalue = request.getParameter("k");
            String mac = request.getParameter("mac-address");
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

            //validation - mac
            if (mac == null) {
                errorArray.add(new JsonPrimitive("missing mac-address"));
            } else {
                boolean check = checkValidMac(mac);
                if (!(check)) {
                    errorArray.add(new JsonPrimitive("invalid mac address"));
                }
            }

            if (errorArray.size() > 0) {
                finalJson.add("message", errorArray);
                out.println(gson.toJson(finalJson));
                return;
            }

            String time = date.substring(date.indexOf("T") + 1, date.length());
            String date_string = date.substring(0, date.indexOf("T"));

            //to check for invalid characters in mac address
            /*
             Pattern p = Pattern.compile("[^a-fA-F0-9]");
             if (p.matcher(mac).find()) {
             finalJson.addProperty("status", "error");
             finalJson.addProperty("message", "Invalid characters detected");
             out.println(gson.toJson(finalJson));
             return;
             }
             */

            //formatting dates and time variables ---- start
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
            //formatting dates and time variables ---- end


            connection = ConnectionFactory.getConnection();
            String query = "SELECT time_stamp, location_id, mac_address "
                    + "FROM location l "
                    + "WHERE mac_address = '" + mac + "' "
                    + "AND time_stamp BETWEEN '" + before + "' "
                    + "AND '" + now + "' "
                    + "ORDER BY time_stamp "
                    + ";";
            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();

            //means no companions
            if (!(rs.next())) {
                finalJson.addProperty("status", "error");
                finalJson.addProperty("message", mac + " cannot be found at specified time.");
                out.println(gson.toJson(finalJson));
                return;
            }
            rs.beforeFirst();


            //-------------------step 1 - retrieve user timestamp and sort by location
            //user location - interval
            HashMap<String, ArrayList<Interval>> userLocationInterval = new HashMap<String, ArrayList<Interval>>();

            String prevLocationid = "";
            Interval prevInterval = null;
            Duration duration = new Duration(540000); //create duration of 9 mins
            while (rs.next()) {
                String locationid = rs.getString("location_id");
                String timestamp = rs.getString("time_stamp");
                timestamp = timestamp.replace(" ", "T");
                Instant startTime = new Instant(timestamp);
                Interval interval = new Interval(startTime, duration);

                //to ensure that interval does not exceed the timeframe.
                //if ending time is after timeframe, will limit the end interval to end of timeframe
                if (interval.getEnd().isAfter(new Instant(timeNow_t))) {
                    interval = interval.withEnd(new Instant(timeNow_t));
                }

                //first case
                if (prevInterval == null) {
                    ArrayList<Interval> list = new ArrayList<Interval>();
                    list.add(interval);
                    userLocationInterval.put(locationid, list);
                } else {
                    //second case onwards
                    //if overlaps, then need to reduce the previous interval
                    if (interval.overlaps(prevInterval) || interval.abuts(prevInterval)) {
                        ArrayList<Interval> list = userLocationInterval.get(prevLocationid);
                        Interval lastInterval = list.remove(list.size() - 1);
                        lastInterval = lastInterval.withEnd(startTime);
                        list.add(lastInterval);
                        userLocationInterval.put(prevLocationid, list);

                        if (locationid.equals(prevLocationid)) {
                            DateTime endTime = interval.getEnd();
                            lastInterval = list.remove(list.size() - 1);
                            lastInterval = lastInterval.withEnd(endTime);
                            list.add(lastInterval);
                            userLocationInterval.put(locationid, list);

                        } else {
                            list = userLocationInterval.get(locationid);
                            if (list == null) {
                                list = new ArrayList<Interval>();
                            }
                            list.add(interval);
                            userLocationInterval.put(locationid, list);
                        }
                    } else { //no overlap, so just add interval to the locationid
                        ArrayList<Interval> list = userLocationInterval.get(locationid);
                        if (list == null) {
                            list = new ArrayList<Interval>();
                        }
                        list.add(interval);
                        userLocationInterval.put(locationid, list);
                    }
                }

                prevLocationid = locationid;
                prevInterval = interval;
            }
            //aboved checked and working correctly *************
            //----------- end of step 1 ----------------------------


            ArrayList<String> locationList = new ArrayList<String>(); //add the places user has went to into an arraylist
            for (String key : userLocationInterval.keySet()) {
                locationList.add(key);
            }

            String locations_query2 = "";
            for (int i = 0; i < locationList.size(); i++) {
                if (i == 0) {
                    locations_query2 += "l.location_id = '" + locationList.get(i) + "' ";
                } else {
                    locations_query2 += "OR l.location_id = '" + locationList.get(i) + "' ";
                }
            }


            //-------------------step 2 - retrieve companions timestamp, filtered out by specified timestamp and user's locationid
            String query2 = "SELECT l.time_stamp, l.location_id, l.mac_address, d.email, d.name\n"
                    + "FROM location l, demographics d\n"
                    + "WHERE l.mac_address = d.mac_address\n"
                    + "and time_stamp between '" + before + "'\n"
                    + "and '" + now + "'\n"
                    + "AND (" + locations_query2 + ")\n"
                    + "AND d.mac_address !=  '" + mac + "'\n"
                    + ";";

            pstmt = connection.prepareStatement(query2);
            rs = pstmt.executeQuery();

            if (!(rs.next())) {
                finalJson.addProperty("status", "error");
                finalJson.addProperty("message", "Emo. Specified user does not have any companions at specified time. :( "
                        + "Visit your closest peer helper now!");
                out.println(gson.toJson(finalJson));
                return;
            }
            rs.beforeFirst();

            HashMap<String, HashMap<String, ArrayList<Interval>>> possibleCompanions = new HashMap<String, HashMap<String, ArrayList<Interval>>>();

            while (rs.next()) {
                String[] arr = new String[2];
                String companionEmail = rs.getString("email");
                possibleCompanions.put(companionEmail, null);
            }
            rs.beforeFirst();

            //for each possible companions retrieve its timestamp and sort into interval
            for (Map.Entry<String, HashMap<String, ArrayList<Interval>>> e : possibleCompanions.entrySet()) {
                String companionEmail = e.getKey();
                HashMap<String, ArrayList<Interval>> companionTimestamp = e.getValue(); //null

                String companionsquery = "select l.time_stamp, d.mac_address, l.location_id, d.name \n"
                        + "from location l, demographics d\n"
                        + "where l.mac_address = d.mac_address\n"
                        + "and time_stamp between '" + before + "'\n"
                        + "and '" + now + "'\n"
                        + "and d.email = '" + companionEmail + "'\n"
                        + "order by l.time_stamp\n"
                        + ";";

                pstmt = connection.prepareStatement(companionsquery);
                rs = pstmt.executeQuery();

                prevLocationid = "";
                prevInterval = null;
                while (rs.next()) {
                    String locationid = rs.getString("location_id");
                    String timestamp = rs.getString("time_stamp");
                    timestamp = timestamp.replace(" ", "T");
                    Instant startTime = new Instant(timestamp);
                    Interval interval = new Interval(startTime, duration);

                    //to ensure that interval does not exceed the timeframe.
                    //if ending time is after timeframe, will limit the end interval to end of timeframe
                    if (interval.getEnd().isAfter(new Instant(timeNow_t))) {
                        interval = interval.withEnd(new Instant(timeNow_t));
                    }
                    //first case
                    if (prevInterval == null) {
                        ArrayList<Interval> list = new ArrayList<Interval>();
                        list.add(interval);
                        if (companionTimestamp == null) {
                            companionTimestamp = new HashMap<String, ArrayList<Interval>>();
                        }
                        companionTimestamp.put(locationid, list);
                    } else {
                        //second case onwards
                        //if overlaps, then need to reduce the previous interval
                        if (interval.overlaps(prevInterval) || interval.abuts(prevInterval)) {
                            ArrayList<Interval> list = companionTimestamp.get(prevLocationid);
                            Interval lastInterval = list.remove(list.size() - 1);
                            lastInterval = lastInterval.withEnd(startTime);
                            list.add(lastInterval);
                            companionTimestamp.put(prevLocationid, list);

                            if (locationid.equals(prevLocationid)) {
                                DateTime endTime = interval.getEnd();
                                lastInterval = list.remove(list.size() - 1);
                                lastInterval = lastInterval.withEnd(endTime);
                                list.add(lastInterval);
                                companionTimestamp.put(locationid, list);

                            } else {
                                list = companionTimestamp.get(locationid);
                                if (list == null) {
                                    list = new ArrayList<Interval>();
                                }
                                list.add(interval);
                                companionTimestamp.put(locationid, list);
                            }
                        } else { //no overlap, so just add interval to the locationid
                            ArrayList<Interval> list = companionTimestamp.get(locationid);
                            if (list == null) {
                                list = new ArrayList<Interval>();
                            }
                            list.add(interval);
                            companionTimestamp.put(locationid, list);
                        }
                    }

                    prevLocationid = locationid;
                    prevInterval = interval;
                }
                possibleCompanions.put(companionEmail, companionTimestamp);
            }

            //----------- end of step 2 ----------------------------

            //email - duration
            HashMap<String, Duration> companionCounter = new HashMap<String, Duration>();


            for (Map.Entry<String, ArrayList<Interval>> e : userLocationInterval.entrySet()) {
                String curUserLocation = e.getKey();
                ArrayList<Interval> curUserIntervals = e.getValue();
                for (Map.Entry<String, HashMap<String, ArrayList<Interval>>> f : possibleCompanions.entrySet()) {
                    String curCompanionEmail = f.getKey();
                    HashMap<String, ArrayList<Interval>> curCompanionLocationIntervals = f.getValue();
                    ArrayList<Interval> list = curCompanionLocationIntervals.get(curUserLocation);
                    if (list == null) {
                        list = new ArrayList<Interval>();
                    }
                    for (Interval l1 : list) {
                        for (Interval l2 : curUserIntervals) {
                            if (l1.overlaps(l2)) {
                                Interval overlap = l1.overlap(l2);
                                Duration currentDuration = companionCounter.get(curCompanionEmail);
                                Duration overlapDuration = overlap.toDuration();
                                if (currentDuration == null) {
                                    companionCounter.put(curCompanionEmail, overlapDuration);
                                } else {
                                    Duration newDuration = currentDuration.plus(overlapDuration);
                                    companionCounter.put(curCompanionEmail, newDuration);
                                }
                            }
                        }
                    }
                }
            }


            long[] rank = new long[companionCounter.size()];
            int count = 0;

            //ensure that rank only has 1 instance of the duration
            HashMap<Long, String> checker = new HashMap<Long, String>();
            for (Duration d : companionCounter.values()) {
                long sec = d.getStandardSeconds();
                if (checker.get(sec) == null) {
                    rank[count] = d.getStandardSeconds();
                    count++;
                    checker.put(sec, "0");
                }
            }
            Arrays.sort(rank);

            int kvalue1 = Integer.parseInt(kvalue);

            int counter = 1;
            int rankcount = 1;
            JsonArray jsonList = new JsonArray();
            for (int i = rank.length - 1; i >= 0; i--) {
                JsonObject newCompanion = new JsonObject();

                if (counter > kvalue1) {
                    break;
                }

                long a = rank[i];
                newCompanion.addProperty("rank", rankcount++);

                for (Map.Entry<String, Duration> e : companionCounter.entrySet()) {
                    if (e.getValue().getStandardSeconds() == a) {
                        String otheremail = e.getKey();
                        UserDAO userdb = new UserDAO();
                        User newuser = userdb.retrieve(otheremail);

                        newCompanion.addProperty("companion", otheremail);

                        String mac2 = newuser.getMacAddress();
                        newCompanion.addProperty("mac-address", mac2);

                        newCompanion.addProperty("time-together", e.getValue().getStandardSeconds());
                    }
                }
                counter++;
                jsonList.add(newCompanion);
            }

            finalJson.addProperty("status", "success");
            finalJson.add("results", jsonList);
            out.println(gson.toJson(finalJson));


        } catch (SQLException ex) {
            Logger.getLogger(CompanionsServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException e) {
            finalJson.addProperty("status", "error");
            finalJson.addProperty("message", "Json: Error! Please enter a valid datetime format.");
            out.println(gson.toJson(finalJson));

        } catch (Exception e) {
            finalJson.addProperty("status", "error");
            finalJson.addProperty("message", "Json: Oops! Something went wrong! Please check your input fields!");
            out.println(gson.toJson(finalJson));
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
                out.close();
            } catch (SQLException ex) {
                Logger.getLogger(CompanionsServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Checks whether the macAddress is valid macAddress or not
     * @param mac the macAddress which is be checked
     * @return true if macAddress is valid, otherwise false
     */
    public boolean checkValidMac(String mac) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = ConnectionFactory.getConnection();

            String query = "select * from location where mac_address='" + mac + "';";

            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(JsonTopKCompanions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            System.out.println("pokeeeee");
        } finally {
            try {
                rs.close();
                pstmt.close();
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(JsonTopKCompanions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;

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
