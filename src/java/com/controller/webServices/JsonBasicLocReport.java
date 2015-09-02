package com.controller.webServices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sloca.entity.User;
import com.sloca.model.UserDAO;
import is203.JWTException;
import is203.JWTUtility;
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
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 *
 * @author G4T8
 */
@WebServlet(name = "JsonBasicLocReport", urlPatterns = {"/json/basic-loc-report"})
public class JsonBasicLocReport extends HttpServlet {

    ArrayList<String> breakdownOrder;
    int orderNo = 0;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        breakdownOrder = new ArrayList<String>();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject finalJson = new JsonObject();
        JsonArray errorArray = new JsonArray();


        Timestamp ts = new Timestamp((new java.util.Date()).getTime());
        Timestamp tsBefore = new Timestamp((new java.util.Date()).getTime());
        try {
            finalJson.addProperty("status", "error");

            String order = request.getParameter("order");
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
            
            //validation - order
            if (order == null) {
                errorArray.add(new JsonPrimitive("missing order"));
            } else {
                ArrayList<String> validvalues = new ArrayList<String>();
                validvalues.add("year");
                validvalues.add("gender");
                validvalues.add("school");
                String orderwithoutcomma = order.replaceAll(",", "");
                int num = order.length() - orderwithoutcomma.length();

                String[] filter = order.split(",");
                int count = 0;
                switch (num) {
                    case 0:
                        for (String s : validvalues) {
                            if (s.equals(order)) {
                                count++;
                                break;
                            }
                        }
                        if (count != 1) {
                            errorArray.add(new JsonPrimitive("invalid order"));
                        }
                        break;
                    case 1:
                        for (String s : validvalues) {
                            for (int i = 0; i < filter.length; i++) {
                                String s2 = filter[i];
                                if (s2.equals(s)) {
                                    count++;
                                    break;
                                }
                            }

                        }
                        if (count != 2) {
                            errorArray.add(new JsonPrimitive("invalid order"));
                        }
                        break;
                    case 2:
                        for (String s : validvalues) {
                            for (int i = 0; i < filter.length; i++) {
                                String s2 = filter[i];
                                if (s2.equals(s)) {
                                    count++;
                                    break;
                                }
                            }

                        }
                        if (count != 3) {
                            errorArray.add(new JsonPrimitive("invalid order"));
                        }
                        break;
                    default:
                        errorArray.add(new JsonPrimitive("invalid order"));
                        break;
                }

            }

            if (errorArray.size() > 0) {
                finalJson.add("message", errorArray);
                out.println(gson.toJson(finalJson));
                return;
            }
            
            String[] filter = order.split(",");
            for(String s : filter){
                breakdownOrder.add(s);
            }

            String time = date.substring(date.indexOf("T") + 1, date.length());
            date = date.substring(0, date.indexOf("T"));


            LocalTime timeNow_t = new LocalTime(time);
            LocalTime timeBefore_t = timeNow_t.minusMinutes(15);

            String timeNow_s = timeNow_t.toString("HH:mm:ss");
            String timeBefore_s = timeBefore_t.toString("HH:mm:ss");
            String timeNow = date + " " + timeNow_s;
            String timeBefore = date + " " + timeBefore_s;

            UserDAO user_dao = new UserDAO();

            // Retrieving userlist based on timestamp query 
            //ArrayList<User> userList = user_dao.retrieveAll() ; 
            ArrayList<User> userList = user_dao.retrieveUsersByTimestamp(timeNow, timeBefore);

            //Start of Query
            JsonObject reportObj = computeResults(userList);


            out.println(gson.toJson(reportObj));



        } catch (Exception e) {
            //out.println(e.getMessage());
        } finally {

            out.close();
        }

    }

    
    /**
     * Compute the given list to JsonObject
     * @param list the list to convert to JsonObject
     * @return the computed JsonObject
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
            genderObj.addProperty("count", tempList.size());

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
        
        int loopYear = 2010 ; 
        for (int i = 0 ; i < 5 ; i++)   {
            yearList.add(String.valueOf(loopYear)) ; 
            loopYear++  ; 
        }
        
//        for (User user : list) {
//            String email = user.getEmail();
//            String year = email.substring(email.indexOf("@") - 4, email.indexOf("@"));
//            if (!yearList.contains(year)) {
//                yearList.add(year);
//            }
//        }
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
            yearObj.addProperty("year", Integer.valueOf(s));
            yearObj.addProperty("count", tempList.size());

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
     * Retrieve year from email
     * @param email the email from which year is taken
     * @return year as String 
     */
    public String getYear(String email) {
        return email.substring(email.indexOf("@") - 4, email.indexOf("@"));
    }

    /**
     * Compute the given list to JsonArray
     * @param list the list to be computed
     * @param order the order to be computed in 
     * @return the computed JsonArray 
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

        schoolList.add("sis") ; 
        schoolList.add("accountancy") ; 
        schoolList.add("economics");
        schoolList.add("law") ; 
        schoolList.add("business") ;
        schoolList.add("socsc");
        
        Collections.sort(schoolList) ; 

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
            schObj.addProperty("count", counter);

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
     * Retrieve previous 15 minutes timeStamp
     * @param timestamp the current timestamp
     * @return the calculated timestamp
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
