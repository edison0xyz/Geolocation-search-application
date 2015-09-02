/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import com.sloca.entity.Group;
import com.sloca.model.LocationLookupDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;

/**
 * A servlet that is responsible for processing groupNextPlaces
 * It contains connection, preparedStatement and ResultSet
 * @author G4T8
 */
public class GroupNextPlacesServlet extends HttpServlet {

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
    public class IntervalEndComparator implements Comparator<Interval> {

        ArrayList<Interval> base;

        public IntervalEndComparator(ArrayList<Interval> base) {
            this.base = base;
        }

        public int compare(Interval x, Interval y) {
            if (x.isAfter(y)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public class ValueComparator implements Comparator<String[]> {

        ArrayList<String[]> base;

        public ValueComparator(ArrayList<String[]> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String[] a, String[] b) {
            if (Integer.parseInt(a[2]) >= Integer.parseInt(b[2])) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        Timestamp ts = null;
        Timestamp tsBefore = null;
        Timestamp tsAfter = null;
        HashMap<String, Integer> finalMap = null;
        ArrayList<String[]> personCount = null;
        AutoGroupDetectController agd = null;

        try {
            String date = request.getParameter("date");
            String time = request.getParameter("time");
            String k = request.getParameter("kvalue");
            String place = request.getParameter("sem");

            request.setAttribute("date", date);
            request.setAttribute("time", time);
            request.setAttribute("kvalue", k);
            request.setAttribute("semanticplace", place);

            String year = date.substring(date.lastIndexOf("-") + 1);
            String month = date.substring(date.indexOf("-") + 1, date.lastIndexOf("-"));
            String day = date.substring(0, date.indexOf("-"));
            String date1 = year + "-" + month + "-" + day;


            LocationLookupDAO llDAO = new LocationLookupDAO();
            HashMap<String, String> referMap = llDAO.retrieveAll();


            int hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
            int minute = Integer.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":")));
            int second = Integer.parseInt(time.substring(time.lastIndexOf(":") + 1, time.length()));
            if (hour >= 24 || minute >= 60 || second >= 60) {
                throw new Exception();
            }

            ts = Timestamp.valueOf(date1 + " " + time);
            tsBefore = new Timestamp(ts.getTime() - 900000);
            tsAfter = new Timestamp(ts.getTime() + 900000);

            finalMap = new HashMap<String, Integer>();
            personCount = new ArrayList<String[]>();
            agd = new AutoGroupDetectController();

            ArrayList<Group> firstGroup = agd.getFullGroups(tsBefore.toString(), ts.toString());
            if (firstGroup.size() == 0) {
                request.setAttribute("results", personCount);
                RequestDispatcher rd = request.getRequestDispatcher("group_next.jsp");
                rd.forward(request, response);
                return;
            }

            System.out.println("List size for group next places: " + firstGroup.size());
            ArrayList<Group> midGroup = new ArrayList<Group>();
            for (Group g : firstGroup) {
                String s = referMap.get(g.getLastPlace());
                System.out.println(s);
                if (s.equals(place)) {
                    midGroup.add(g);
                }
            }
            System.out.println("Mid list size for group next places: " + midGroup.size());
            ArrayList<Group> finalGroup = agd.getMatchingGroups(ts.toString(), tsAfter.toString(), midGroup);
            System.out.println("Final list size for group next places: " + finalGroup.size());

            LocationLookupDAO lookupDAO = new LocationLookupDAO();
            HashMap<String, String> lookupMap = lookupDAO.retrieveAll();
            for (Group g : finalGroup) {
                ArrayList<Interval> groupInterList = new ArrayList<Interval>();
                HashMap<Interval, Integer> tempMap = new HashMap<Interval, Integer>();
                HashMap<String, ArrayList<Interval>> overlapMap = g.getLocOverlapMap();
                for (String s : overlapMap.keySet()) {
                    ArrayList<Interval> interList = overlapMap.get(s);
                    groupInterList.addAll(interList);
                    for (Interval in : interList) {
                        Integer i = Integer.parseInt(s);
                        tempMap.put(in, i);
                    }
                }
                Collections.sort(groupInterList, new IntervalEndComparator(groupInterList));
                for (Interval in : groupInterList) {
                    if (Seconds.secondsIn(in).getSeconds() >= 300) {
                        Integer i = tempMap.get(in);
                        String semPlace = lookupMap.get(String.valueOf(i));
                        Integer groupCount = finalMap.get(semPlace);
                        if (groupCount != null) {
                            finalMap.put(semPlace, (groupCount + 1));
                        } else {
                            finalMap.put(semPlace, 1);
                        }
                        break;
                    }
                }
            }

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "<b>Error!</b><br>Please enter a valid datetime format.");
            RequestDispatcher rd = request.getRequestDispatcher("group_next.jsp");
            rd.forward(request, response);
            return;
        } catch (Exception e) {
            request.setAttribute("error", "<b>Oops!</b><br>Something went wrong!");
            RequestDispatcher rd = request.getRequestDispatcher("group_next.jsp");
            rd.forward(request, response);
            return;
        }


        for (Entry<String, Integer> entry : finalMap.entrySet()) {
            String[] countArray = new String[]{entry.getKey(), entry.getValue().toString()};
            personCount.add(countArray);
        }

        Collections.sort(personCount, new ValueComparator(personCount));
        request.setAttribute("results", personCount);
        RequestDispatcher rd = request.getRequestDispatcher("group_next.jsp");
        rd.forward(request, response);
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
