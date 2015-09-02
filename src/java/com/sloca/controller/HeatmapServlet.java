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
 * A servlet that is responsible for processing Heatmap.
 *
 */
public class HeatmapServlet extends HttpServlet {

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
        RequestDispatcher rd = request.getRequestDispatcher("heatmapPage.jsp");
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            //time attributes can be better utilized
            String floor = request.getParameter("floor");
            String date = request.getParameter("date");
            String time = request.getParameter("time");
            System.out.println(floor);
            System.out.println(date);
            System.out.println(time);
            request.setAttribute("date", date);
            request.setAttribute("time", time);
            request.setAttribute("floor", floor);


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

            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();

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
                splace = splace.replaceFirst("SMUSIS", "");
                String count1 = Integer.toString(count);
                String density1 = Integer.toString(density);

                String[] arr = new String[3];
                arr[0] = splace;
                arr[1] = count1;
                arr[2] = density1;
                returnList.add(arr);
            }

            request.setAttribute("results", returnList);
            rd.forward(request, response);

        } catch (SQLException ex) {
            Logger.getLogger(HeatmapServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ie) {
            request.setAttribute("error", "<b>Error!</b><br>Please enter a valid datetime format.");
            rd.forward(request, response);
        } catch (Exception e) {
            //out.println(e.getMessage());
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
                Logger.getLogger(HeatmapServlet.class.getName()).log(Level.SEVERE, null, ex);
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
