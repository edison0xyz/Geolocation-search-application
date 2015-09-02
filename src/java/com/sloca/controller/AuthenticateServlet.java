/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import com.google.gson.JsonObject;
import com.sloca.entity.User;
import com.sloca.model.UserDAO;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author G4T8
 */
/**
 * 
 * A servlet that is responsible for processing authentication
 */
@WebServlet(name = "AuthenticateServlet", urlPatterns = {"/authenticate"})
public class AuthenticateServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        final String ADMIN_USER = "admin";
        final String ADMIN_PASSWORD = "123";


        //response.setContentType("text");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        try {

            // Retrieve username and password from the request 
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String token = JWTUtility.sign("ylleeg4t8", username);
            System.out.println(password) ; 
            System.out.println("token: " + token) ; 
                    

            session.setAttribute("loginuser", username);

            JsonObject jsonAuthenticate = new JsonObject();

            if (username.equals(ADMIN_USER)) {

                if (password.equals(ADMIN_PASSWORD)) {
                    jsonAuthenticate.addProperty("status", "success");
                    jsonAuthenticate.addProperty("token", token);
                    session.setAttribute("jsonAuthenticate", jsonAuthenticate);

                    session.setAttribute("admin", username);
                    session.removeAttribute("loginuser");
                    response.sendRedirect("admin");
                } else {
                    // Invalid User
                    jsonAuthenticate.addProperty("status", "error");
                    jsonAuthenticate.addProperty("message", "invalid username/password");

                    session.setAttribute("errorAuthenticate", jsonAuthenticate);
                    session.setAttribute("error", true);
                    response.sendRedirect("login.jsp");
                }
            } else {
                User user = null;
                UserDAO userdm = new UserDAO();
                if (userdm.authenticate(username, password)) {
                    // Handles Unlikely Exception that User might be null
                    if (userdm.retrieve(username) != null) {
                        user = userdm.retrieve(username);

                        session.setAttribute("user", username);
                        session.setAttribute("token", token);
                        session.removeAttribute("loginuser");
                        response.sendRedirect("user.jsp");
                    } else {
                        // Invalid User
                        jsonAuthenticate.addProperty("status", "error");
                        jsonAuthenticate.addProperty("message", "invalid username/password");

                        session.setAttribute("errorAuthenticate", jsonAuthenticate);
                        session.setAttribute("error", true);
                        response.sendRedirect("login.jsp");
                    }

                } else {
                    jsonAuthenticate.addProperty("status", "error");
                    jsonAuthenticate.addProperty("message", "invalid username/password");

                    session.setAttribute("errorAuthenticate", jsonAuthenticate);
                    session.setAttribute("error", true);
                    response.sendRedirect("login.jsp");
                }

            }
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            out.println(gson.toJson(jsonAuthenticate));
        } catch (Exception e) {
            out.println("Errormessage: " + e.getMessage());
        } finally {

            out.close();
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
            Logger.getLogger(AuthenticateServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(AuthenticateServlet.class.getName()).log(Level.SEVERE, null, ex);
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
