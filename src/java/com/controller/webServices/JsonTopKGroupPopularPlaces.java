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
import com.sloca.controller.AutoGroupDetectController;
import com.sloca.entity.Group;
import com.sloca.model.LocationLookupDAO;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

/**
 *
 * @author G4T8
 */
@WebServlet(name = "JsonTopKGroupPopularPlaces", urlPatterns = {"/json/top-k-group-popular-places"})
public class JsonTopKGroupPopularPlaces extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject finalJson = new JsonObject();
        JsonArray errorArray = new JsonArray();

        try {
            String date = request.getParameter("date");
            String kvalue = request.getParameter("k");
            String token = request.getParameter("token");
            String time = "";

            // Validation for kvalue 
            if (kvalue == null) {
                kvalue = "3";
            } else if (kvalue.equals("")) {
                errorArray.add(new JsonPrimitive("blank k"));
            } else {
                try {
                    int k = Integer.valueOf(kvalue);
                    if (k < 1 || k > 10) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    errorArray.add(new JsonPrimitive("invalid k"));
                }
            }


            //validation - date
            if (date == null) {
                errorArray.add(new JsonPrimitive("missing date"));
            } else if (date.equals("")) {
                errorArray.add(new JsonPrimitive("blank date"));
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
                } catch (JWTException je) {
                    errorArray.add(new JsonPrimitive("invalid token"));
                }
            }

            if (errorArray.size() > 0) {
                finalJson.add("message", errorArray);
                out.println(gson.toJson(finalJson));
                return;
            }

            if (date != null && date.contains("T")) {
                time = date.substring(date.indexOf("T") + 1, date.length());
                date = date.substring(0, date.indexOf("T"));
            }

            String day = date.substring(date.lastIndexOf("-") + 1);
            String month = date.substring(date.indexOf("-") + 1, date.lastIndexOf("-"));
            String year = date.substring(0, date.indexOf("-"));
            String date1 = year + "-" + month + "-" + day;
            System.out.println(date1);
            System.out.println(time);
            Timestamp ts = null;
            try {
                ts = Timestamp.valueOf(date1 + " " + time);
            } catch (Exception e) {
                errorArray.add(new JsonPrimitive("invalid date"));
            }
            Timestamp tsBefore = null;
            Timestamp tsAfter = null;
            if (ts != null) {
                tsBefore = new Timestamp(ts.getTime() - 900000);
                tsAfter = new Timestamp(ts.getTime() + 900000);
            }
            if (tsBefore != null && tsAfter != null) {
                AutoGroupDetectController ctrl = new AutoGroupDetectController();
                ArrayList<Group> groupList = ctrl.getFullGroups(tsBefore.toString(), tsAfter.toString()); //retrieving all groups within the specific timeframe


                HashMap<String, Integer> answerList = new HashMap<String, Integer>(); //hashmap to store number of groups per semantic place (location - number of groups)
                LocationLookupDAO lookupDAO = new LocationLookupDAO();
                HashMap<String, String> lookupList = lookupDAO.retrieveAll(); //hashmap to retrieve the semanticname based on locationid
                //for every group, find their last location then insert into answerlist

                for (Group g : groupList) {
                    String gLastLocation = groupLastLocation(g);
                    String semantic = lookupList.get(gLastLocation);

                    if (answerList.get(semantic) == null) {
                        answerList.put(semantic, 1);
                    } else {
                        int num = answerList.get(semantic);
                        answerList.put(semantic, ++num);
                    }
                }

                //sorting is required because need to display them in order on jsp - based on places with most groups first
                //start of sorting *************************************************
                HashMap<Integer, Integer> numOfGroupsPerLocation = new HashMap<Integer, Integer>(); //hashmap to ensure only 1 instance of same number of groups so that they can be given the same rank later
                for (Integer i : answerList.values()) {
                    numOfGroupsPerLocation.put(i, i);
                }
                //insert the single instances to an array to use sorting algorithm to sort them in order
                int[] arr = new int[numOfGroupsPerLocation.size()];
                int counter = 0;
                for (Integer i : numOfGroupsPerLocation.keySet()) {
                    arr[counter++] = i;
                }
                Arrays.sort(arr); //sort the number of groups in ascending order


                JsonArray array = new JsonArray();
                int rank = 1;
                int kvalue1 = Integer.valueOf(kvalue);
                //for every number of groups, search the places with that number of groups and add them into an array
                //each array represents a line in the jsp table
                //following which the array is added into an arraylist to be displayed at jsp
                for (int i = arr.length - 1; i >= 0; i--) {
                    if (rank > kvalue1) { //to cater for kvalue, if rank exceeds kvalue, stop adding places into returnlist 
                        break;
                    }
                    int currentNum = arr[i];
                    ArrayList<String> sortingLocation = new ArrayList<String>();

                    for (Map.Entry<String, Integer> e : answerList.entrySet()) {
                        int num = e.getValue();

                        if (currentNum == num) {
                            sortingLocation.add(e.getKey());
                            /*
                             JsonObject current = new JsonObject();
                             String key = e.getKey();
                             // key = key.replaceFirst("SMUSIS", "");

                             current.addProperty("rank", rank);
                             current.addProperty("semantic-place", key);
                             current.addProperty("count", currentNum);
                             array.add(current);
                             */
                        }
                    }
                    String[] temp = new String[sortingLocation.size()];
                    temp = sortingLocation.toArray(temp);
                    Arrays.sort(temp);

                    for (int q = 0; q < temp.length; q++) {
                        String key = temp[q];
                        JsonObject current = new JsonObject();
                        // key = key.replaceFirst("SMUSIS", "");

                        current.addProperty("rank", rank);
                        current.addProperty("semantic-place", key);
                        current.addProperty("count", currentNum);
                        array.add(current);
                    }


                    rank++;
                }

                // Successfully queried from the database, prints Json Output
                finalJson.addProperty("status", "success");
                finalJson.add("results", array);
                out.println(gson.toJson(finalJson));
            } else {
                // error detected, throw exception for Json to be printed. 
                throw new Exception();
            }
        } catch (Exception e) {
            finalJson.addProperty("status", "error");
            finalJson.add("message", errorArray);
            out.println(gson.toJson(finalJson));

        } finally {
            out.close();
        }
    }

    /**
     * Retrieve the group's last visited location
     *
     * @param g the group
     * @return location
     */
    public String groupLastLocation(Group g) {
        //retrieves all intervals of the group
        HashMap<String, ArrayList<Interval>> map = g.getLocOverlapMap();

        //for every location the group visited, check through all the intervals to find the latest interval
        //and extract the timestamp. then save the last location and interval for comparison purposes with other locations
        String lastLocation = null;
        LocalTime latestStart = null;
        for (Map.Entry<String, ArrayList<Interval>> e : map.entrySet()) {
            String currentLocation = e.getKey();
            ArrayList<Interval> locationIntervals = e.getValue();

            //for every interval checked, if the starttime is after the latest recorded starttime
            //replace the recorded starttime with the current one
            for (Interval i : locationIntervals) {
                LocalTime start = i.getStart().toLocalTime();
                if (latestStart == null || start.isAfter(latestStart)) {
                    latestStart = start;
                    lastLocation = currentLocation;
                }
            }
        }
        return lastLocation;
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
