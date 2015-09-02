/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import com.sloca.entity.FullUser;
import com.sloca.entity.Group;
import com.sloca.model.LocationLookupDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

/**
 * A servlet that is responsible for processing GroupPopularPlaces
 *
 * @author G4T8
 */
public class GroupPopularPlacesServlet extends HttpServlet {

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
        try {
            String date = request.getParameter("date");
            String time = request.getParameter("time");
            String kvalue = request.getParameter("kvalue");
            request.setAttribute("date", date);
            request.setAttribute("time", time);
            request.setAttribute("kvalue", kvalue);

            String year = date.substring(date.lastIndexOf("-") + 1);
            String month = date.substring(date.indexOf("-") + 1, date.lastIndexOf("-"));
            String day = date.substring(0, date.indexOf("-"));
            String date1 = year + "-" + month + "-" + day;

            LocalTime after = new LocalTime(Timestamp.valueOf(date1 + " " + time));
            int hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
            int minute = Integer.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":")));
            int second = Integer.parseInt(time.substring(time.lastIndexOf(":") + 1, time.length()));
            if (hour >= 24 || minute >= 60 || second >= 60) {
                throw new Exception();
            }
            LocalTime before = after.minusMinutes(15);
            String tsBefore = date1 + " " + before;
            String tsAfter = date1 + " " + after;

            AutoGroupDetectController ctrl = new AutoGroupDetectController();
            ArrayList<Group> groupList = ctrl.getFullGroups(tsBefore.toString(), tsAfter.toString()); //retrieving all groups within the specific timeframe
            HashMap<String, Integer> answerList = new HashMap<String, Integer>(); //hashmap to store number of groups per semantic place (location - number of groups)
            LocationLookupDAO lookupDAO = new LocationLookupDAO();
            HashMap<String, String> lookupList = lookupDAO.retrieveAll(); //hashmap to retrieve the semanticname based on locationid
            HashMap<String, Integer> peopleNum = new HashMap<String, Integer>(); //key-place, value-num


            //for every group, find their last location then insert into answerlist
            for (Group g : groupList) {
                String gLastLocation = groupLastLocation(g);
                String semantic = lookupList.get(gLastLocation);
                int numOfMembers = g.getIndivSet().size();


                if (answerList.get(semantic) == null) {
                    answerList.put(semantic, 1);
                    peopleNum.put(semantic, numOfMembers);
                } else {
                    int num = answerList.get(semantic);
                    answerList.put(semantic, ++num);
                    peopleNum.put(semantic, peopleNum.get(semantic) + numOfMembers);
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


            ArrayList<String[]> returnList = new ArrayList<String[]>(); //find returnList to be sent back to jsp for printing
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
                for (Entry<String, Integer> e : answerList.entrySet()) {
                    int num = e.getValue();

                    if (currentNum == num) {
                        sortingLocation.add(e.getKey());
                    }
                }
                String[] temp = new String[sortingLocation.size()];
                temp = sortingLocation.toArray(temp);
                Arrays.sort(temp);

                for (int q = 0; q < temp.length; q++) {
                    String[] arr2 = new String[4];
                    arr2[0] = Integer.toString(rank); //ranking
                    arr2[1] = Integer.toString(currentNum); //number of groups
                    String key = temp[q];
                    int peopleCount = peopleNum.get(key);
                    key = key.replaceFirst("SMUSIS", "");
                    arr2[2] = key; //semantic place
                    arr2[3] = Integer.toString(peopleCount);
                    returnList.add(arr2);
                }

                rank++;
            }

            request.setAttribute("results", returnList);
            RequestDispatcher rd = request.getRequestDispatcher("group_popular.jsp");
            rd.forward(request, response);

        } catch (Exception e) {
            out.println(e.getMessage());
            request.setAttribute("error", "<b>Oops!</b><br> Something went wrong! Please check your input fields!");
            RequestDispatcher rd = request.getRequestDispatcher("group_popular.jsp");
            rd.forward(request, response);
        } finally {
            out.close();
        }
    }

    /*
     * 
     */
    /**
     * Retrieves the group's last visited location
     *
     * @param g group to be processed
     * @return the last location
     */
    public String groupLastLocation(Group g) {
        //retrieves all intervals of the group
        HashMap<String, ArrayList<Interval>> map = g.getLocOverlapMap();

        //for every location the group visited, check through all the intervals to find the latest interval
        //and extract the timestamp. then save the last location and interval for comparison purposes with other locations
        String lastLocation = null;
        LocalTime latestStart = null;
        for (Entry<String, ArrayList<Interval>> e : map.entrySet()) {
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
