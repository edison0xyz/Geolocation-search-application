/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller.webServices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sloca.entity.User;
import com.sloca.model.UserDAO;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
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
@WebServlet(name = "JsonAuthenticate", urlPatterns = {"/json/authenticate"})
public class JsonAuthenticate extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        final String ADMIN_USER = "admin";
        final String ADMIN_PASSWORD = "123";

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {

            // Retrieve username and password from the request 
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            String token = JWTUtility.sign("ylleeg4t8", username);
            System.out.println(password);
            System.out.println("token: " + token);

            JsonObject jsonAuthenticate = new JsonObject();

            if (username == null || password == null) {
                // May have to change to blank/missing 
                jsonAuthenticate.addProperty("status", "error");
                //jsonAuthenticate.addProperty("message", "invalid username/password");
                JsonArray array = new JsonArray();
                JsonObject obj = new JsonObject();
                obj.addProperty("message", "invalid username/password");
                JsonElement element = obj.get("message");
                array.add(element);
                jsonAuthenticate.add("messages", array);
            } else {
                if (username.equals(ADMIN_USER)) {

                    if (password.equals(ADMIN_PASSWORD)) {
                        jsonAuthenticate.addProperty("status", "success");
                        jsonAuthenticate.addProperty("token", token);

                    } else {
                        // Invalid User
                        jsonAuthenticate.addProperty("status", "error");
                        //jsonAuthenticate.addProperty("message", "invalid username/password");
                        JsonArray array = new JsonArray();
                JsonObject obj = new JsonObject();
                obj.addProperty("message", "invalid username/password");
                JsonElement element = obj.get("message");
                array.add(element);
                jsonAuthenticate.add("messages", array);
                    }
                } else {
                    User user = null;
                    UserDAO userdm = new UserDAO();
                    if (userdm.authenticate(username, password)) {
                        // Handles Unlikely Exception that User might be null
                        if (userdm.retrieve(username) != null) {

                            // Successful authentication of user. 
                            jsonAuthenticate.addProperty("status", "success");
                            jsonAuthenticate.addProperty("token", token);
                        } else {
                            // Invalid User
                            jsonAuthenticate.addProperty("status", "error");
                            //jsonAuthenticate.addProperty("message", "invalid username/password");
                            JsonArray array = new JsonArray();
                JsonObject obj = new JsonObject();
                obj.addProperty("message", "invalid username/password");
                JsonElement element = obj.get("message");
                array.add(element);
                jsonAuthenticate.add("messages", array);
                        }
                    } else {
                        jsonAuthenticate.addProperty("status", "error");
                        //jsonAuthenticate.addProperty("message", "invalid username/password");
                        JsonArray array = new JsonArray();
                JsonObject obj = new JsonObject();
                obj.addProperty("message", "invalid username/password");
                JsonElement element = obj.get("message");
                array.add(element);
                jsonAuthenticate.add("messages", array);

                    }

                }
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            out.println(gson.toJson(jsonAuthenticate));

        } catch (Exception e) {
            out.println("Errormessage: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
            Logger.getLogger(JsonAuthenticate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
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
            Logger.getLogger(JsonAuthenticate.class.getName()).log(Level.SEVERE, null, ex);
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
