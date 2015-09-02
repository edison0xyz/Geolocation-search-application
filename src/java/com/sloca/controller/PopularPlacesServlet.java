/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import com.sloca.connection.ConnectionFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

/**
 *
 * @author G4T8
 */
/**
 * A servlet that is responsible for processing popularPlaces.
 *
 */
public class PopularPlacesServlet extends HttpServlet {

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

        try {
            RequestDispatcher rd = request.getRequestDispatcher("top_k_popular_places_page.jsp");

            String date = request.getParameter("date");
            String time = request.getParameter("time");
            String kvalue = request.getParameter("kvalue");
            request.setAttribute("date", date);
            request.setAttribute("time", time);
            request.setAttribute("kvalue", kvalue);

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


            connection = ConnectionFactory.getConnection();

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
                    + "order by totalCount desc"
                    + ";";
            
            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();

            int kvalue1 = Integer.parseInt(kvalue);
            int counter = 0;
            String prevNumOfPeople = "";
            ArrayList<String[]> list = new ArrayList<String[]>();
            while (rs.next()) {

                if (prevNumOfPeople.equals(rs.getString("totalCount")) == false) {
                    counter++;
                }
                if (counter > kvalue1) {
                    break;
                }

                String[] arr = new String[3];
                arr[0] = String.valueOf(counter);
                arr[1] = rs.getString("totalCount");
                String splace = rs.getString("sem2");
                splace = splace.replaceFirst("SMUSIS", "");
                arr[2] = splace;
                prevNumOfPeople = arr[1];
                list.add(arr);
            }

            request.setAttribute("results", list);
            rd.forward(request, response);

        } catch (SQLException ex) {
            Logger.getLogger(PopularPlacesServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
            request.setAttribute("error", "<b>Error!</b><br>Please enter a valid datetime format.");
            RequestDispatcher rd = request.getRequestDispatcher("top_k_popular_places_page.jsp");
            rd.forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "<b>Oops!</b><br>Something went wrong! You must be trying to be funny!");
            RequestDispatcher rd = request.getRequestDispatcher("top_k_popular_places_page.jsp");
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
