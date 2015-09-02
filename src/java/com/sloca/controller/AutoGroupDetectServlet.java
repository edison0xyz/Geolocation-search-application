/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import com.sloca.connection.ConnectionFactory;
import com.sloca.entity.Group;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.LocalTime;


//@WebServlet(name = "JsonAutomaticDetection", urlPatterns = {"/json/group_detect"})
public class AutoGroupDetectServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ClassNotFoundException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String date = request.getParameter("date");
            String time = request.getParameter("time");

            String year = date.substring(date.lastIndexOf("-") + 1);
            String month = date.substring(date.indexOf("-") + 1, date.lastIndexOf("-"));
            String day = date.substring(0, date.indexOf("-"));
            String date1 = year + "-" + month + "-" + day;

            request.setAttribute("date", date);
            request.setAttribute("time", time);



            LocalTime after = new LocalTime(Timestamp.valueOf(date1 + " " + time));
            int hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
            int minute = Integer.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":")));
            int second = Integer.parseInt(time.substring(time.lastIndexOf(":") + 1, time.length()));
            if (hour >= 24 || minute >= 60 || second >= 60) {
                throw new Exception();
            }

            Timestamp tsAfter = Timestamp.valueOf(date1 + " " + after);
            Timestamp tsBefore = new Timestamp(tsAfter.getTime() - 900000);


            System.out.println(tsAfter);
            System.out.println(tsBefore);


            Connection connection = ConnectionFactory.getConnection();
            String query = "select time_stamp, mac_address, location_id "
                    + "from location "
                    + "where time_stamp between '" + tsBefore + "' "
                    + "and '" + tsAfter + "' "
                    + "group by mac_address "
                    + ";";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            int counter = 0;
            while(rs.next()){
                counter++;
            }
            
            rs.close();
            pstmt.close();
            connection.close();
            
            ArrayList<Group> groupList = new ArrayList<Group>();

            AutoGroupDetectController ac = new AutoGroupDetectController();
            groupList = ac.getFullGroups(tsBefore.toString(), tsAfter.toString());

            request.setAttribute("counter", counter);
            request.setAttribute("results", groupList);
            RequestDispatcher rd = request.getRequestDispatcher("autoGroupDetection.jsp");
            rd.forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error", "Oops! Something went wrong!<br>Please check your inputs again!");
            RequestDispatcher rd = request.getRequestDispatcher("autoGroupDetection.jsp");
            rd.forward(request, response);
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
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(AutoGroupDetectServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AutoGroupDetectServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(AutoGroupDetectServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AutoGroupDetectServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
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
