/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller.webServices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sloca.controller.AutoGroupDetectController;
import com.controller.webServices.JsonArraySorter;
import com.sloca.controller.ReportController;
import com.sloca.entity.FullUser;
import com.sloca.entity.Group;
import com.sloca.model.LocationDAO;
import is203.JWTException;
import is203.JWTUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 *
 * @author G4T8
 */
@WebServlet(name = "JsonAutoDetect", urlPatterns = {"/json/group_detect"})
public class JsonAutoDetect extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ClassNotFoundException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JsonObject jsonResult = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray errorArray = new JsonArray();


        String date = request.getParameter("date");
        String token = request.getParameter("token");

        //validation - date
        if (date == null) {
            errorArray.add(new JsonPrimitive("missing date"));
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
            } catch (JWTException e) {
                errorArray.add(new JsonPrimitive("invalid token"));
            }
        }
        
        if (errorArray.size() > 0) {
            jsonResult.add("message", errorArray);
            out.println(gson.toJson(jsonResult));
            return;
        }

        if (errorArray.size() == 0) {
            String date1 = date.substring(0, date.indexOf("T"));
            String time = date.substring(date.indexOf("T") + 1, date.length());

            try {

                LocalTime after = new LocalTime(Timestamp.valueOf(date1 + " " + time));
                int hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
                int minute = Integer.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":")));
                int second = Integer.parseInt(time.substring(time.lastIndexOf(":") + 1, time.length()));
                if (hour >= 24 || minute >= 60 || second >= 60) {
                    throw new Exception();
                }
                Timestamp tsAfter = Timestamp.valueOf(date1 + " " + after);
                Timestamp tsBefore = new Timestamp(tsAfter.getTime() - 900000);

                ArrayList<Group> groupList = new ArrayList<Group>();

                AutoGroupDetectController ac = new AutoGroupDetectController();
                System.out.println(tsBefore.toString() + " " + tsAfter.toString());
                groupList = ac.getFullGroups(tsBefore.toString(), tsAfter.toString());

                jsonResult.addProperty("status", "success");

                int count = 0;
                HashSet set = new HashSet() ; 
                for (Group g : groupList) {
                    set.addAll(g.getIndivSet());
                    
                }
                count=set.size();
                jsonResult.addProperty("total-groups", groupList.size());
                jsonResult.addProperty("total-users", count);


                JsonArray groupArray = new JsonArray();
                JsonArraySorter sorter = new JsonArraySorter();

                for (Group g : groupList) {
                    JsonObject groupObject = new JsonObject();
                    groupObject.addProperty("size", g.getIndivSet().size());
                    groupObject.addProperty("total-time-spent", g.getFullCount());


                    JsonArray memberArray = new JsonArray();
                    HashSet<FullUser> userSet = g.getIndivSet();
                    Iterator i = userSet.iterator();
                    while (i.hasNext()) {
                        FullUser fullUser = (FullUser) i.next();
                        JsonObject memberObj = new JsonObject();
                        memberObj.addProperty("email", fullUser.getEmail());
                        memberObj.addProperty("mac-address", fullUser.getMacAddress());
                        memberArray.add(memberObj);
                    }
                    memberArray = sorter.sortAGDMembers(memberArray);
                    groupObject.add("members", memberArray);

                    // Add location objects 

                    JsonArray locationArray = new JsonArray();
                    HashMap<String, Integer> locationMapping = g.getLocTimeMap();
                    for (String s : locationMapping.keySet()) {
                        JsonObject locationObject = new JsonObject();
                        locationObject.addProperty("location", s);
                        locationObject.addProperty("time-spent", locationMapping.get(s));
                        locationArray.add(locationObject);
                    }


                    locationArray = sorter.sortAGDlocation(locationArray);
                    groupObject.add("locations", locationArray);


                    groupArray.add(groupObject);
                }
                groupArray = sorter.sortAGDGroup(groupArray);
                jsonResult.add("groups", groupArray);



                out.println(gson.toJson(jsonResult));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }

        // =======     End of Json Codes    ======= 
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
            Logger.getLogger(JsonAutoDetect.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JsonAutoDetect.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(JsonAutoDetect.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JsonAutoDetect.class.getName()).log(Level.SEVERE, null, ex);
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
