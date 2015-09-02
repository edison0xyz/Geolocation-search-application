
package com.controller.webServices;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sloca.connection.ConnectionFactory;
import com.sloca.controller.ValidationController;
import com.sloca.entity.Location;
import com.sloca.entity.LocationLookup;
import com.sloca.entity.User;
import com.sloca.model.LocationDAO;
import com.sloca.model.LocationLookupDAO;
import com.sloca.model.UserDAO;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author G4T8
 */
@WebServlet(name = "JsonUpdate", urlPatterns = {"/json/update"})
@MultipartConfig
public class JsonUpdate extends HttpServlet {

    Connection connection = null;
    Statement statement = null;
    ArrayList<String> errors;
    ValidationController vc = new ValidationController();

    // Overarching JsonObject to represent output of whole json request
    JsonObject BootstrapResult = new JsonObject();
    // JsonArray of JsonObjects containing file and numOfRecords recorded 
    JsonArray jArrNumRecord;
    // ArrayList of Error Objects from demographics, location-lookup and location.csv 
    JsonArray arrErrors;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        errors = new ArrayList<String>();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {

            connection = ConnectionFactory.getConnection();
            statement = connection.createStatement();

            System.out.println("Connection Established.");

            // Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Configure a repository (to ensure a secure temp location is used)
            ServletContext servletContext = this.getServletConfig().getServletContext();
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            factory.setRepository(repository);

            // Get File From Server 
            ServletFileUpload upload = new ServletFileUpload(factory);
            FileItemIterator iter = upload.getItemIterator(request);

            System.out.println("bootstrapping in process...");

            // JsonArray containing JsonObject which represents "file" and "records-loaded"
            jArrNumRecord = new JsonArray();
            // JsonArray containing JsonObject which represents the errors in the file
            arrErrors = new JsonArray();
            connection.setAutoCommit(false);
            // Get the files from zip
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String fileName = item.getName();
                if (fileName != null) {
                    String extension = fileName.substring(fileName.length() - 4);
                    if (!extension.equals(".zip")) {
                        //  Thrown when file is not a zip file
                        response.sendRedirect("..admin.jsp?errorMsg=Wrong File Type");

                    }
                    InputStream in = item.openStream();
                    // Check if any file is uploaded
                    if (in.available() == 0) {
                        response.sendRedirect("..admin.jsp?errorMsg=No file uploaded.");
                    }

                    BufferedInputStream bis = new BufferedInputStream(in);
                    ZipInputStream zis = new ZipInputStream(bis);
                    ZipEntry ze; //Components of a Zip Content

                    String[] nextLine;

                    while ((ze = zis.getNextEntry()) != null) {
                        System.out.println("Reading contents of Zip folder...");

                        CSVReader csvreader = new CSVReader(new InputStreamReader(zis));

                        if (ze.toString().equalsIgnoreCase("location-lookup.csv")) {
                            System.out.println("Found location-lookup.csv");
                            loadLocationLookup(zis);
                        } else if (ze.toString().equalsIgnoreCase("demographics.csv")) {
                            System.out.println("Found demographics.csv");
                            loadDemographics(zis);
                        } else if (ze.toString().equalsIgnoreCase("location.csv")) {
                            System.out.println("Found location.csv");
                            loadLocation(zis);
                        }
                    }
                }
            }
            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String bootstrap_status = "";
        if (arrErrors != null && arrErrors.size() == 0) {
            bootstrap_status = "success";
        } else {
            bootstrap_status = "error";
        }

        BootstrapResult.addProperty("status", bootstrap_status);
        BootstrapResult.add("num-recorded-loaded", jArrNumRecord);
        BootstrapResult.add("error", arrErrors);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        out.println(gson.toJson(BootstrapResult));

        HttpSession session = request.getSession();
        session.setAttribute("BootstrapResult", BootstrapResult);
        response.sendRedirect("../admin.jsp");
    }

    protected void loadLocationLookup(ZipInputStream zis) {
        try {

            LocationLookupDAO locationLookupDM = new LocationLookupDAO();
            locationLookupDM.setConnection(connection);
            
            
            String[] nextLine;
            CSVReader reader = new CSVReader(new InputStreamReader(zis));
            int locationId;
            String semanticPlace;
            // Skip one line 
            reader.readNext();
            int counter = 0;
            boolean addEntry = false;
            ArrayList<LocationLookup> lookupList = new ArrayList<LocationLookup>() ; 
            while ((nextLine = reader.readNext()) != null) {
                try {
                    addEntry = false;

                    JsonArray listOfErrors = null;
                    ArrayList<String> incompatibilityList = vc.validateLookup(nextLine);
                    

                    if (incompatibilityList.isEmpty()) {
                        // Entry is okay, proceed to insert 
                        addEntry = true;
                        locationId = Integer.parseInt(nextLine[0].trim());
                        semanticPlace = nextLine[1].trim();
                        lookupList.add(new LocationLookup(locationId, semanticPlace));
                        
                        //locationLookupDM.saveLookup(new LocationLookup(locationId, semanticPlace), connection);
                    } else {
                        // There is something wrong, assign to lineErrorJson 

                        listOfErrors = new JsonArray();
                        for (String s : incompatibilityList) {
                            listOfErrors.add(new JsonPrimitive(s));
//                            errors.add("Location Lookup CSV Row " + counter + ": " + s);

                        }
                    }

                    if (!addEntry && listOfErrors != null) {

                        // ======    Setting Properties in the File    ===========
                        // Object to contain properties of files and JsonArray of errors (listOfErrors)
                        JsonObject lineErrorObject = new JsonObject();
                        lineErrorObject.addProperty("file", "location-lookup.csv");
                        lineErrorObject.addProperty("line", counter);
                        lineErrorObject.add("message", listOfErrors);
                        arrErrors.add(lineErrorObject);
                    }
                    counter++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Adding data to the numRecord
            JsonObject numRecorded = new JsonObject();
            numRecorded.addProperty("location-lookup.csv", counter);
            jArrNumRecord.add(numRecorded);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadLocation(ZipInputStream zis) {
        int counter = 1;
        try {

            LocationDAO locationDM = new LocationDAO();
            locationDM.setConnection(connection);
            String[] nextLine;

            System.out.println("In loadLocation()");

            CSVReader reader = new CSVReader(new InputStreamReader(zis));
            Timestamp timeStamp = null;
            String macAddress;
            int locationId;
            // Skip one line 
            reader.readNext();
            boolean addEntry = false;
            ArrayList<Location> locationList = new ArrayList<Location>();


            while ((nextLine = reader.readNext()) != null) {
                //try {
                ArrayList<String> incompatibilityList = vc.validateLocation(nextLine);
                //JsonArray to store all the errors that one line of code has 
                JsonArray listOfErrors = null;

                addEntry = false;

                if (incompatibilityList.isEmpty()) {
                    String timeString = nextLine[0];
                    timeStamp = java.sql.Timestamp.valueOf(timeString);

                    macAddress = nextLine[1].trim();
                    String locationId1 = nextLine[2].trim();
                    locationId = Integer.parseInt(locationId1);


                    // Adding new location into an ArrayList<Location> to be inserted when counter hits 10,000.
                    locationList.add(new Location(timeStamp, macAddress, locationId));
                    
                    // Passing arraylist to LocationDAO to be saved into database 
                    if (locationList.size() == 10000) {
                        locationDM.saveLocations(locationList);
                        
                        //Clear list after insert
                        locationList.clear();
                        System.out.println("Location List Reset to Zero. ");
                    }

                    //locationDM.saveLocation(new Location(timeStamp, macAddress, locationId));
                } else {

                    // Make one new JsonArray to keep track of the errors after every line 
                    listOfErrors = new JsonArray();
                    for (String s : incompatibilityList) {
                        listOfErrors.add(new JsonPrimitive(s));
//                            errors.add("Location CSV Row " + counter + ": " + s);

                    }

                    if (!listOfErrors.isJsonNull()) {
                        JsonObject lineErrorObject = new JsonObject();
                        lineErrorObject.addProperty("file", "location.csv");
                        lineErrorObject.addProperty("line", counter);
                        lineErrorObject.add("message", listOfErrors);
                        arrErrors.add(lineErrorObject);

                    }

                }
                counter++;
            }
            locationDM.saveLocations(locationList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Adding data to the numRecord
        JsonObject numRecorded = new JsonObject();
        numRecorded.addProperty("location.csv", counter);
        jArrNumRecord.add(numRecorded);

    }

    protected void loadDemographics(ZipInputStream zis) {
        try {

            UserDAO userDM = new UserDAO();
            
            String[] nextLine;
            CSVReader reader = new CSVReader(new InputStreamReader(zis));
            String macAddress;
            String name;
            String password;
            String email;
            String gender;

            // Skip one line 
            reader.readNext();

            int counter = 1;
            while ((nextLine = reader.readNext()) != null) {
                try {
                    ArrayList<String> incompatibilityList = vc.validateDemographics(nextLine);

                    if (incompatibilityList.isEmpty()) {
                        macAddress = nextLine[0].trim();
                        name = nextLine[1].trim();
                        password = nextLine[2].trim();
                        email = nextLine[3].trim();
                        gender = nextLine[4].trim();
                        userDM.saveUser(new User(macAddress, name, password, email, gender.charAt(0)));
                    } else {
                        JsonArray listOfErrors = new JsonArray();
                        for (String s : incompatibilityList) {
                            listOfErrors.add(new JsonPrimitive(s));
                        }

                        JsonObject lineErrorObject = new JsonObject();
                        lineErrorObject.addProperty("file", "demographics.csv");
                        lineErrorObject.addProperty("line", counter);
                        lineErrorObject.add("message", listOfErrors);
                        // Adding to a bigger container
                        arrErrors.add(lineErrorObject);
                    }
                    counter++;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Adding data to the numRecord
            JsonObject numRecorded = new JsonObject();
            numRecorded.addProperty("demographics.csv", counter);
            jArrNumRecord.add(numRecorded);

        } catch (Exception e) {
            e.printStackTrace();
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
