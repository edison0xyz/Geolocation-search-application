package com.sloca.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sloca.entity.User;
import com.sloca.model.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.joda.time.LocalTime;

/**
 * A servlet that is responsible for processing Breakdown function
 * @author: G4T8
 */
@WebServlet(name = "BreakdownServlet", urlPatterns = {"/basic-loc-report"})
public class BreakdownServlet extends HttpServlet {

    ArrayList<String> breakdownOrder;
    int orderNo = 0;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        breakdownOrder = new ArrayList<String>();
        HttpSession session = request.getSession();

        String firstFilter = request.getParameter("firstFilter");
        String secondFilter = request.getParameter("secondFilter");
        String thirdFilter = request.getParameter("thirdFilter");

        String order = "";
        
        if (firstFilter.equals("")) {
            // User did not choose anything
            // ==== SETS ERROR MSG  ===
            //error is never reached
            session.setAttribute("error", "<b>Error!</b><br>Please select first filter!");
            response.sendRedirect("report_breakdown_page.jsp");
            return ; 
        }
        order += firstFilter;

        if (!secondFilter.equals("")) {
            order = order + "," + secondFilter;
        }

        if (!thirdFilter.equals("")) {
            if(secondFilter.equals("")) {
                session.setAttribute("error", "<b>Error!</b><br>Please select second filter before selecting the third!");
                response.sendRedirect("report_breakdown_page.jsp");
                return ; 
            }
            order = order + "," + thirdFilter;
        }

        session.setAttribute("order", order);
        String[] bdOrder = order.split(",");
        for (String s : bdOrder) {

            if (!s.equals("gender") && !s.equals("school") && !s.equals("year")) {
                session.setAttribute("error", "<b>Error!</b><br>Our sorting function is still under development. Please follow the guidelines! (eg. year,gender,school ) in small case. Thanks! :)");
                response.sendRedirect("report_breakdown_page.jsp");

                return;
            }
            System.out.println(s);
            breakdownOrder.add(s);

        }

        Timestamp ts = new Timestamp((new java.util.Date()).getTime());
        Timestamp tsBefore = new Timestamp((new java.util.Date()).getTime());
        try {
            String date = (String) request.getParameter("date");
            String time = (String) request.getParameter("time");
            session.setAttribute("date", date);
            session.setAttribute("time", time);

            String year = date.substring(date.lastIndexOf("-") + 1);
            String month = date.substring(date.indexOf("-") + 1, date.lastIndexOf("-"));
            String day = date.substring(0, date.indexOf("-"));
            String date_s = year + "-" + month + "-" + day;


            LocalTime timeNow_t = new LocalTime(time);
            LocalTime timeBefore_t = timeNow_t.minusMinutes(15);

            String timeNow_s = timeNow_t.toString("HH:mm:ss");
            String timeBefore_s = timeBefore_t.toString("HH:mm:ss");
            String timeNow = date_s + " " + timeNow_s;
            String timeBefore = date_s + " " + timeBefore_s;

            UserDAO user_dao = new UserDAO();

            // Retrieving userlist based on timestamp query 
            //ArrayList<User> userList = user_dao.retrieveAll() ; 
            ArrayList<User> userList = user_dao.retrieveUsersByTimestamp(timeNow, timeBefore);
            session = request.getSession();
            

            //Start of Query
            JsonObject reportObj = computeResults(userList);
            session.setAttribute("numofpeople", userList.size());
            session.setAttribute("data_results", reportObj);
            response.sendRedirect("report_breakdown_page.jsp");
//            Gson gson = new GsonBuilder().setPrettyPrinting().create() ;   
//            out.println(gson.toJson(reportObj)); 
//            
        } catch (IllegalArgumentException ie) {
            session = request.getSession();
            session.removeAttribute("data_results");
            session.setAttribute("error", "<b>Error!</b><br>Please enter a valid datetime format.");
            response.sendRedirect("report_breakdown_page.jsp");

        } catch (Exception e) {
            //out.println(e.getMessage());
            session.setAttribute("error", "<b>Oops!</b><br> Something went wrong! Please check your input fields!");
            response.sendRedirect("report_breakdown_page.jsp");
        } finally {

            out.close();
        }

    }

    /**
     * Compute and convert to JsonObject
     * @param list the list to be processed
     * @return the JsonObject
     */
    public JsonObject computeResults(ArrayList<User> list) {
        JsonObject jobj = new JsonObject();
        jobj.addProperty("status", "success");
        JsonArray arr = methodRedirect(list, 0);
        jobj.add("breakdown", arr);
        return jobj;

    }

    
    private JsonArray computeGender(ArrayList<User> list) {

        ArrayList<Character> charList = new ArrayList<Character>();
        
        charList.add('M');
        charList.add('F');
        

        JsonArray arr = new JsonArray();
        for (char c : charList) {
            ArrayList<User> tempList = new ArrayList<User>();
            for (User user : list) {
                if (user.getGender() == c) {
                    tempList.add(user);
                }
            }
            JsonObject genderObj = new JsonObject();
            genderObj.addProperty("gender", String.valueOf(c));
            genderObj.addProperty("count", String.valueOf(tempList.size()));

            // Check if it is the last method in the list
            if (orderNo < breakdownOrder.size() - 1) {
                JsonArray recurse = methodRedirect(tempList, ++orderNo);
                genderObj.add("breakdown", recurse);
                orderNo--;
            }
            arr.add(genderObj);

        }
        return arr;
    }

    private JsonArray computeYear(ArrayList<User> list) {
        JsonArray arr = new JsonArray();
        ArrayList<String> yearList = new ArrayList<String>();
        System.out.println("in year");
        for (User user : list) {
            String email = user.getEmail();
            String year = email.substring(email.indexOf("@") - 4, email.indexOf("@"));
            if (!yearList.contains(year)) {
                yearList.add(year);
            }
        }
        Collections.sort(yearList) ; 

        for (String s : yearList) {
            ArrayList<User> tempList = new ArrayList<User>();

            for (User user : list) {
                String year = getYear(user.getEmail());
                if (s.equals(year)) {
                    tempList.add(user);
                }
            }

            JsonObject yearObj = new JsonObject();
            yearObj.addProperty("year", s);
            yearObj.addProperty("count", String.valueOf(tempList.size()));

            if (orderNo < breakdownOrder.size() - 1) {
                JsonArray breakdownArray = methodRedirect(tempList, ++orderNo);
                yearObj.add("breakdown", breakdownArray);
                orderNo--;
            }

            arr.add(yearObj);
        }
        return arr;
    }

    /**
     * Retrieve year from the email
     * @param email the email from which the year is retrieved
     * @return year in String format
     */
    public String getYear(String email) {
        return email.substring(email.indexOf("@") - 4, email.indexOf("@"));
    }

    /**
     * Convert to JsonArray according to order
     * @param list the list to be processed
     * @param order the order to be used
     * @return JsonArray
     */
    public JsonArray methodRedirect(ArrayList<User> list, int order) {
        String variable = breakdownOrder.get(order);

        if (variable.equals("gender")) {
            return computeGender(list);
        } else if (variable.equals("year")) {
            return computeYear(list);
        } else {

            return computeSchool(list);
        }

    }

    private JsonArray computeSchool(ArrayList<User> list) {
        JsonArray arr = new JsonArray();

        ArrayList<String> schoolList = new ArrayList<String>();

        for (User user : list) {
            String school = getSchool(user.getEmail());
            if (!schoolList.contains(school)) {
                schoolList.add(school);
            }
        }

        for (String s : schoolList) {
            int counter = 0;
            JsonObject schObj = new JsonObject();
            ArrayList<User> tempList = new ArrayList<User>();

            for (User user : list) {
                String school = getSchool(user.getEmail());
                if (school.equals(s)) {
                    tempList.add(user);
                    counter++;
                }
            }
            schObj.addProperty("school", s);
            schObj.addProperty("count", String.valueOf(counter));

            if (orderNo < breakdownOrder.size() - 1) {
                JsonArray tempArray = methodRedirect(tempList, ++orderNo);
                schObj.add("breakdown", tempArray);
                orderNo--;
            }
            arr.add(schObj);
        }

        return arr;
    }

    
    private String getSchool(String email) {

        String[] temp = email.split("@");
        return temp[1].substring(0, temp[1].indexOf("."));
    }

    /**
     * Retrieve 15 minutes timestamp prior to the current timestamp
     * @param timestamp the current timestamp
     * @return computed Timestamp 
     */
    public Timestamp getTimeBefore(Timestamp timestamp) {
        Long mSeconds = timestamp.getTime();
        Timestamp before = new Timestamp(mSeconds - 900000);
        return before;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    
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
