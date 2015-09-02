/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
/**
 *
 * @author G4T8
 */

@MultipartConfig
public class BootstrapServlet extends HttpServlet {

    Connection connection = null;
    Statement statement = null;
    ArrayList<String> errors;
    ValidationController vc = new ValidationController();
    final String truncateLocation = "TRUNCATE location";
    final String truncateDemographics = "TRUNCATE demographics";
    final String truncateLocationLookup = "TRUNCATE location_lookup";
    // Overarching JsonObject to represent output of whole json request
    JsonObject BootstrapResult = new JsonObject();
    // JsonArray of JsonObjects containing file and numOfRecords recorded 
    JsonArray jArrNumRecord;
    // ArrayList of Error Objects from demographics, location-lookup and location.csv 
    JsonArray arrErrors;
    ZipFile zipFile = null;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        errors = new ArrayList<String>();
        response.setContentType("application/json");
        PrintWriter out = null;

        try {

            out = response.getWriter();
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

            // JsonArray containing JsonObject which represents "file" and "records-loaded"
            jArrNumRecord = new JsonArray();
            // JsonArray containing JsonObject which represents the errors in the file
            arrErrors = new JsonArray();
            connection.setAutoCommit(false);
            
            FileItemIterator items = upload.getItemIterator(request);
            
            try {
//                List fileItems = upload.parseRequest((RequestContext) request);
//
//                // Process the uploaded file items
//                Iterator i = fileItems.iterator();

                // ==== STEP 1: UNZIP FILE =======
                while (items.hasNext()) {
                    FileItemStream fi =  items.next();
                    if (!fi.isFormField()) {
                        // Tracks file upload
                        // Get uploaded file parameters, i.e ZipFile for bootstrap's case
                        String fieldName = fi.getFieldName();
                        String fileName = fi.getName();
                        // Validate Zip Format                    
                        if (fileName.substring(fileName.length() - 4, fileName.length()).equals(".zip")) {
                            Unzip(fi, repository);
                        }   else    {
                            HttpSession session = request.getSession() ;                   
                            session.setAttribute("errorMsg", "Wrong file format") ; 
                        }
                    }
                }
                
                 // Set autocommit to 0 to increase speed of INSERT statements
                connection.setAutoCommit(false);

                // Set foreign key checks to false so db can truncate database
                statement.execute("SET foreign_key_checks = 0");

                // Truncate existing tables 
                statement.execute(truncateLocation);
                statement.execute(truncateDemographics);
                statement.execute(truncateLocationLookup);

                // Set foreign key checks back to on
                statement.execute("SET foreign_key_checks = 1");
                connection.setAutoCommit(true);


                // === STEP 2 : Load the methods to bootstrap =========
                
                loadDemographics(new File(repository.getAbsolutePath() + File.separator + "demographics.csv"));
                loadLocationLookup(new File(repository.getAbsolutePath() + File.separator + "location-lookup.csv"));               
                loadLocation(new File(repository.getAbsolutePath() + File.separator + "location.csv"));

                
            } catch (Exception e) {
                e.printStackTrace();
            }

            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception e) {
                System.out.println("Connection/Statement Error");
            }
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

//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        out.println(gson.toJson(BootstrapResult));

        HttpSession session = request.getSession();

        try {
            session.setAttribute("BootstrapResult", BootstrapResult);
            response.sendRedirect("admin");
        } catch (IOException io) {
            System.out.println("IOException");
        }
    }

    /**
     * Unzip the given file
     * @param item the file to be unzipped
     * @param filePath the location of file
     */
    public void Unzip(FileItemStream item, File filePath) {

        byte[] buffer = new byte[1024];

        try {
            ZipInputStream zis = new ZipInputStream(item.openStream());
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(filePath.getAbsolutePath() + File.separator + fileName);
                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders

                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();

                ze = zis.getNextEntry();
            }
        } catch (IOException ex) {
            Logger.getLogger(BootstrapServlet.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    protected void loadLocationLookup(File lookupFile) {

        InputStream zis = null;
        try {
            zis = new FileInputStream(lookupFile);
        } catch (IOException ex) {
            Logger.getLogger(BootstrapServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            
            

            LocationLookupDAO locationLookupDM = new LocationLookupDAO();
            locationLookupDM.setConnection(connection);
            System.out.println("In location lookup method");
//            statement.execute("SET foreign_key_checks = 0;");
//            statement.execute("ALTER TABLE `location_lookup` ADD COLUMN counter INT ;");
//            
            String[] nextLine;

            CSVReader reader = new CSVReader(new InputStreamReader(zis));
            int locationId;
            String semanticPlace;
            // Skip one line 
            reader.readNext();
            int counter = 0;
            int counterNew = 0;
            boolean addEntry = false;
            ArrayList<LocationLookup> lookupList = new ArrayList<LocationLookup>();
            while ((nextLine = reader.readNext()) != null) {
                try {
                    addEntry = false;
                    counter++;
                    counterNew++;
                    JsonArray listOfErrors = null;
                    ArrayList<String> incompatibilityList = vc.validateLookup(nextLine);


                    if (incompatibilityList.isEmpty()) {
                        // Entry is okay, proceed to insert 
                        addEntry = true;
                        locationId = Integer.parseInt(nextLine[0].trim());
                        semanticPlace = nextLine[1].trim();

                        try {
                            locationLookupDM.saveLookup(new LocationLookup(locationId, semanticPlace));
                        } catch (SQLException sql_exception) {
                            System.out.println("Duplicate row");
                            counterNew--;
                        }
                    } else {
                        // There is something wrong, assign to lineErrorJson
                        counterNew--;
                        listOfErrors = new JsonArray();
                        for (String s : incompatibilityList) {
                            listOfErrors.add(new JsonPrimitive(s));
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
                    ;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            System.out.println("Old lookup counter size: " + counter);
            System.out.println("Lookup counter size: " + counterNew);
            // Adding data to the numRecord
            JsonObject numRecorded = new JsonObject();
            numRecorded.addProperty("location-lookup.csv", counterNew);
            jArrNumRecord.add(numRecorded);

//            statement.execute("ALTER TABLE `location_lookup` drop counter ;");
//            statement.execute("SET foreign_key_checks = 1;");    
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void loadLocation(File locationFile) {

        InputStream zis = null;
        try {
            zis = new FileInputStream(locationFile);
        } catch (IOException ex) {
            Logger.getLogger(BootstrapServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        int counter = 0;
        int numInserted = 0 ; 
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
            
            ReversedLinesFileReader reverseReader = new ReversedLinesFileReader(locationFile) ; 
            String next = "" ; 
           // while ((nextLine = reader.readNext()) != null) {
            while((next = reverseReader.readLine())!= null) {
                
                
                nextLine = next.split(",") ; 
                if(!nextLine[0].equals("timestamp")){
                //try {
                ArrayList<String> incompatibilityList  ; 
                
                if(nextLine.length == 3 )    {
                
                incompatibilityList = vc.validateLocation(nextLine);
                }   else    {
                    incompatibilityList = new ArrayList<String>() ; 
                    incompatibilityList.add("invalid data format") ; 
                }
                       
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
                    numInserted++ ; 
                } else {

                    // Make one new JsonArray to keep track of the errors after every line 
                    listOfErrors = new JsonArray();
                    for (String s : incompatibilityList) {
                        listOfErrors.add(new JsonPrimitive(s));

                    }

                    if (!listOfErrors.isJsonNull()) {
                        JsonObject lineErrorObject = new JsonObject();
                        lineErrorObject.addProperty("file", "location.csv");
                        lineErrorObject.addProperty("line", counter);
                        lineErrorObject.add("message", listOfErrors);
                        arrErrors.add(lineErrorObject);

                    }

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
        numRecorded.addProperty("location.csv", numInserted);
        jArrNumRecord.add(numRecorded);

    }

    protected void loadDemographics(File demographics) {
        try {
            InputStream zis = null;
            try {
                zis = new FileInputStream(demographics);
            } catch (IOException ex) {
                Logger.getLogger(BootstrapServlet.class.getName()).log(Level.SEVERE, null, ex);
            }


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

            int counter = 0;
            int counterNew = 0;
            while ((nextLine = reader.readNext()) != null) {
                try {
                    counter++;
                    counterNew++;
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
                        counterNew--;
                        // Adding to a bigger container
                        arrErrors.add(lineErrorObject);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Adding data to the numRecord
            JsonObject numRecorded = new JsonObject();
            numRecorded.addProperty("demographics.csv", counterNew);
            jArrNumRecord.add(numRecorded);

        } catch (Exception e) {
            e.printStackTrace();
        }
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