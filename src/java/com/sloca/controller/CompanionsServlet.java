/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import com.sloca.connection.ConnectionFactory;
import com.sloca.entity.User;
import com.sloca.model.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
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
/**
 *
 * A servlet that is responsible for processing companions.
 */
public class CompanionsServlet extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        RequestDispatcher rd = request.getRequestDispatcher("top_k_companions_page.jsp");

        try {
            //Getting parameters and saving them as attributes

            String date = request.getParameter("date");
            String time = request.getParameter("time");
            String kvalue = request.getParameter("kvalue");
            String mac_input = request.getParameter("mac_input");
            String mac_select = request.getParameter("mac_select");
            request.setAttribute("date", date);
            request.setAttribute("time", time);
            request.setAttribute("kvalue", kvalue);
            request.setAttribute("mac_input", mac_input);
            request.setAttribute("mac_select", mac_select);

            System.out.println("macinput: " + mac_input);
            System.out.println("macselect: " + mac_select);

            String mac = "";
            if (mac_input != "") {
                mac = mac_input;
            } else {
                mac = mac_select;
            }
            if (mac.equals("")) {
                request.setAttribute("error", "<b>Error!</b><br>Please either select or input a mac-address!");
                rd.forward(request, response);
            }
            System.out.println("mac: " + mac);
            //to check for invalid characters in mac address
            Pattern p = Pattern.compile("[^a-fA-F0-9]");
            if (p.matcher(mac).find()) {
                request.setAttribute("error", "<b>Error!</b><br>Mac-address contains invalid characters!");
                rd.forward(request, response);
            }


            //formatting dates and time variables ---- start
            String year = date.substring(date.lastIndexOf("-") + 1);
            String month = date.substring(date.indexOf("-") + 1, date.lastIndexOf("-"));
            String day = date.substring(0, date.indexOf("-"));
            String date_string = year + "-" + month + "-" + day;

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
            
            //to retrieve the timestamp and places the given mac address has been to
            connection = ConnectionFactory.getConnection();
            String query = "SELECT time_stamp, location_id, mac_address "
                    + "FROM location l "
                    + "WHERE mac_address = '" + mac + "' "
                    + "AND time_stamp BETWEEN '" + before + "' "
                    + "AND '" + now + "' "
                    + "ORDER BY time_stamp "
                    + "; ";
            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (!(rs.next())) {
                request.setAttribute("message", mac + " cannot be found at specified time.");
                rd.forward(request, response);
            }
            rs.beforeFirst();

            //-------------------step 1 - retrieve user timestamp and sort by location
            //to store given mac's interval. key - locationid, value - arraylist of intervals at that particular locationid
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
            for (Entry<String, ArrayList<Interval>> e : userLocationInterval.entrySet()) {
                String key = e.getKey();
                ArrayList<Interval> value = e.getValue();
            }
            //aboved checked and working correctly *************
            //----------- end of step 1 ----------------------------

            ArrayList<String> locationList = new ArrayList<String>(); //add the places user has went to into an arraylist
            for (String key : userLocationInterval.keySet()) {
                locationList.add(key);
            }

            //saving the locationids as string to query purpose
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
                request.setAttribute("message", "Emo. Specified user does not have any companions at specified time. :( \n "
                        + "Visit your closest peer helper now!");
                rd.forward(request, response);
            }
            rs.beforeFirst();

            //to store companions locationid and interval
            //1st level: key - companionemail, value - list of locationid
            //2nd level: key - locationid, value - list of intervals at locationid where companions has been to
            HashMap<String, HashMap<String, ArrayList<Interval>>> possibleCompanions = new HashMap<String, HashMap<String, ArrayList<Interval>>>();
            while (rs.next()) {
                String[] arr = new String[2];
                String companionEmail = rs.getString("email");
                possibleCompanions.put(companionEmail, null);

            }
            rs.beforeFirst();
            //for each possible companions retrieve its timestamp and sort into interval
            for (Entry<String, HashMap<String, ArrayList<Interval>>> e : possibleCompanions.entrySet()) {
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


            //masterlist of companion to log the duration each companion spends with the given mac user
            //key - email, value - duration
            HashMap<String, Duration> companionCounter = new HashMap<String, Duration>();


            for (Entry<String, ArrayList<Interval>> e : userLocationInterval.entrySet()) {
                String curUserLocation = e.getKey();
                ArrayList<Interval> curUserIntervals = e.getValue();
                for (Entry<String, HashMap<String, ArrayList<Interval>>> f : possibleCompanions.entrySet()) {
                    String curCompanionEmail = f.getKey();
                    HashMap<String, ArrayList<Interval>> curCompanionLocationIntervals = f.getValue();
                    ArrayList<Interval> list = curCompanionLocationIntervals.get(curUserLocation);
                    if (list == null) {
                        list = new ArrayList<Interval>();
                    }

                    //taking both given mac user's intervals and companion's intervals
                    //compare the intervals of the same locationid and see if they overlap
                    //if they overlap means they spent time together and add this duration to the masterlist above
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
            int j = 1;
            ArrayList<String[]> returnList = new ArrayList<String[]>();
            //adjusting the companion's details and storing them in an array to be saved in an arraylist
            //arraylist will be returned to jsp for printing
            for (int i = rank.length - 1; i >= 0; i--) {
                //to impose limit of kvalue. if reach the desired kvalue, stop adding and break loop
                if (counter > kvalue1) {
                    break;
                }

                long a = rank[i];

                for (Entry<String, Duration> e : companionCounter.entrySet()) {
                    if (e.getValue().getStandardSeconds() == a) {
                        String[] arr = new String[2];
                        String otheremail = e.getKey();
                        UserDAO userdb = new UserDAO();
                        User newuser = userdb.retrieve(otheremail);
                        String name = newuser.getName();
                        arr[0] = name;

                        String curTime = Long.toString(e.getValue().getStandardSeconds());
                        arr[1] = curTime;
                        returnList.add(arr);
                    }
                }
                counter++;
            }

            //in the event whereby no intervals overlap 
            if (returnList.isEmpty()) {
                request.setAttribute("message", "Emo. Specified user does not have any companions at specified time. :( \n "
                        + "Visit your closest peer helper now!");
                rd.forward(request, response);
            }

            //return companions and print to page
            request.setAttribute("results", returnList);
            rd.forward(request, response);


        } catch (SQLException ex) {
            Logger.getLogger(CompanionsServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "<b>Error!</b><br>Please enter a valid datetime format.");
            rd.forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "<b>Oops!</b><br> Something went wrong! Please check your input fields!");
            rd.forward(request, response);
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
                if (out != null) {
                    out.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(CompanionsServlet.class.getName()).log(Level.SEVERE, null, ex);
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
