/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author G4T8
 */
/**
 * 
 *  A servlet that is responsible for processing yearGender.
 */
public class YearGenderServlet extends HttpServlet {

    String url = "jdbc:mysql://localhost:3306/sloca";
    String username = "root";
    String password = "";
    Connection conn = null;
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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {



            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);

            String first = request.getParameter("option1");
            String second = request.getParameter("option2");
            String third = request.getParameter("option3");
            String together = first + second + third;

            String tsDate = request.getParameter("date");
            String tsTime = request.getParameter("time");
            request.setAttribute("date", tsDate);
            request.setAttribute("time", tsTime);

            Timestamp ts = Timestamp.valueOf(tsDate + " " + tsTime);
            Timestamp tsBefore = getTimeBefore(ts);
            double counter;
            double percentage;

            if (together.equals("y")) {
                pstmt = conn.prepareStatement("select f.year, count(*) from (select d.email as email, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') group by f.year");

            } else if (together.equals("g")) {
                pstmt = conn.prepareStatement("select temp.gender, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') group by temp.gender order by temp.gender DESC");

            } else if (together.equals("s")) {
                pstmt = conn.prepareStatement("select s.schname, count(*) from (select d.email as email, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by s.schname");

            } else if (together.equals("yg")) {
                pstmt = conn.prepareStatement("select f.year, temp.gender, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') group by f.year, temp.gender order by f.year, temp.gender DESC");

            } else if (together.equals("ys")) {
                pstmt = conn.prepareStatement("select f.year, s.schname, count(*) from (select d.email as email, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by f.year, s.schname");

            } else if (together.equals("gy")) {
                pstmt = conn.prepareStatement("select temp.gender, f.year, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') group by temp.gender, f.year order by temp.gender DESC, f.year");

            } else if (together.equals("gs")) {
                pstmt = conn.prepareStatement("select temp.gender, s.schname, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by temp.gender, s.schname order by temp.gender DESC, s.schname");

            } else if (together.equals("sy")) {
                pstmt = conn.prepareStatement("select s.schname, f.year, count(*) from (select d.email as email, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by s.schname, f.year");

            } else if (together.equals("sg")) {
                pstmt = conn.prepareStatement("select s.schname, temp.gender, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by s.schname, temp.gender order by s.schname, temp.gender DESC");

            } else if (together.equals("ygs")) {
                pstmt = conn.prepareStatement("select f.year, temp.gender, s.schname, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '2014-02-02 10:00:00' and '2014-03-23 12:00:00') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by f.year, temp.gender, s.schname order by f.year, temp.gender DESC, s.schname");

            } else if (together.equals("ysg")) {
                pstmt = conn.prepareStatement("select f.year, s.schname, temp.gender, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by f.year, s.schname, temp.gender order by f.year, s.schname, temp.gender DESC");

            } else if (together.equals("gys")) {
                pstmt = conn.prepareStatement("select temp.gender, f.year, s.schname, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by temp.gender, f.year, s.schname order by temp.gender DESC, f.year, s.schname");

            } else if (together.equals("gsy")) {
                pstmt = conn.prepareStatement("select temp.gender, s.schname, f.year, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by temp.gender, s.schname, f.year order by temp.gender DESC, s.schname, f.year");

            } else if (together.equals("syg")) {
                pstmt = conn.prepareStatement("select s.schname, f.year, temp.gender, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by s.schname, f.year, temp.gender order by s.schname, f.year, temp.gender DESC");

            } else if (together.equals("sgy")) {
                pstmt = conn.prepareStatement("select s.schname, temp.gender, f.year, count(*) from (select d.email as email, d.gender as gender, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and time_stamp between '" + tsBefore + "' and '" + ts + "') as temp join fullyear f on temp.email like concat('%', f.year,'@%') join school s on temp.email like concat('%@', s.schname, '%') group by s.schname, temp.gender, f.year order by s.schname, temp.gender DESC, f.year");

            } else {
                request.setAttribute("error", "<b>Error!</b><br>This combination is not valid. Please try another.");
                RequestDispatcher rd = request.getRequestDispatcher("yeargender.jsp");
                rd.forward(request, response);
                return;
            }
            rs = pstmt.executeQuery();
            request.setAttribute("set", rs);
            RequestDispatcher rd = request.getRequestDispatcher("yeargender.jsp");
            rd.forward(request, response);
            /*    
             pstmt = conn.prepareStatement("select f.year, count(*) from (select d.email as email, l.time_stamp from demographics d join location l where l.mac_address = d.mac_address and `time_stamp` between '" + tsBefore + "' and '" + ts + "' as temp join fullyear f on temp.email like concat('%', f.year,'@%') group by f.year");
             rs = pstmt.executeQuery();

             counter = 0;
             while (rs.next()) {
             counter++;
             if (!userMap.containsKey(rs.getString(1))) {
             userMap.put(rs.getString(1), rs.getTimestamp(3));
             } else {
             Timestamp t = userMap.get(rs.getString(1));
             if (t.before(rs.getTimestamp(3))) {
             userMap.put(rs.getString(1), rs.getTimestamp(3));
             }
             }
             }
             percentage = userMap.size() / counter;
             out.println("<table border='1'>");
             out.println("<tr><th>Name</th><th>Email</th><th>Timestamp</th><tr>");
             for (Map.Entry<String,Timestamp> cursor: userMap.entrySet()) {
             out.println("<tr>");
             out.println("<td>");
             out.println(cursor.getKey());
             out.println("</td>");
             out.println("<td>");
             out.println(cursor.getValue());
             out.println("</td>");
             out.println("</tr>");
             }
             out.println("</table>");

             HashMap<String,String> secondMap = new HashMap<String,String>();  

             pstmt = conn.prepareStatement("select `name`, `gender` from demographics d, location l where l.mac_address = d.mac_address and `gender` = '" + selCriteria + "' and `time_stamp` between '" + tsBefore + "' and '" + ts + "'");
             rs = pstmt.executeQuery();
             counter = 0;
             while (rs.next()) {
             counter++;
             if (!secondMap.containsKey(rs.getString(1))) {
             secondMap.put(rs.getString(1), rs.getString(2));
             }
             }
             percentage = secondMap.size() / counter;
             out.println("<h3>Percentage = " + percentage + "</h3>");
             out.println("<table border='1'>");
             out.println("<tr><th>Name</th><th>Gender</th><tr>");
             for (Map.Entry<String,String> cursor: secondMap.entrySet()) {
             out.println("<tr>");
             out.println("<td>");
             out.println(cursor.getKey());
             out.println("</td>");
             out.println("<td>");
             out.println(cursor.getValue());
             out.println("</td>");
             out.println("</tr>");
             }
             out.println("</table>");
             */

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException ie) {
            request.setAttribute("error", "<b>Error!</b><br>This combination is not valid. Please try another.");
                RequestDispatcher rd = request.getRequestDispatcher("yeargender.jsp");
                rd.forward(request, response);
        } catch (Exception e) {
            //out.println(e.getMessage());
            request.setAttribute("error", "<b>Oops!</b><br> Something went wrong! Please check your input fields!");
            //rd.forward(request, response);
        } finally {
            out.close();
        }
    }

    /**
     * Retrieves timestamp which is 15 minutes before the current timestamp
     * @param timestamp the current timestamp
     * @return timestamp
     */
    public Timestamp getTimeBefore(Timestamp timestamp) {
        Long mSeconds = timestamp.getTime();
        Timestamp before = new Timestamp(mSeconds - 900000);
        return before;
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
