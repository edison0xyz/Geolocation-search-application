/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import java.util.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author G4T8
 */
/**
 * 
 * A servlet that is responsible for processing validating controller.
 */
public class ValidationController {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    /**
     * Validates the Lookup
     * @param input the file to process
     * @return ArrayList of errors
     */
    public ArrayList<String> validateLookup(String[] input) {
        ArrayList<String> errors = new ArrayList<String>();
        
        //locationId
        String locationIdString = input[0].trim();
        if (locationIdString.isEmpty()) {
            errors.add("invalid location id");
            
        }
        
        
        try {
            Integer.parseInt(locationIdString);
        } catch (NumberFormatException e) {
            errors.add("invalid location id");
        }
        
        //semanticPlace
        String semanticPlace = input[1].trim();
        if (semanticPlace.isEmpty()) {
            errors.add("invalid semantic place");
            return errors;
        } else if (!semanticPlace.contains("SMUSISL") && !semanticPlace.contains("SMUSISB")) {
            errors.add("Invalid semantic place.");
        }
      
        return errors; 
    }
    
    /**
     * Validate the Location.csv 
     * @param input location input 
     * @return a list of errors
     */
    public ArrayList<String> validateLocation(String[] input) {
        ArrayList<String> errors = new ArrayList<String>();
        //timestamp
        String stringTimestamp = input[0].trim();
        if (stringTimestamp.isEmpty()) {
            errors.add("invalid timestamp");
            return errors;
        }

        // Valid Timestamp 
        try {
            Date parsedTimeStamp = dateFormat.parse(stringTimestamp);
            Timestamp timestamp = new Timestamp(parsedTimeStamp.getTime());
        } catch (ParseException e) {
            errors.add("invalid timestamp");
        }
        //macaddress
        String macAddress = input[1].trim();
        //check not empty
        if (macAddress.equals("")) {
            errors.add("invalid mac address");
            return errors;
        }
        //check make sure it fits the length
        if (macAddress.length() != 40) {
            errors.add("invalid mac address");

        }
        //check make sure only contain allowed characters
        Pattern p = Pattern.compile("[^a-f0-9]");
        if (p.matcher(macAddress).find()) {
            errors.add("invalid mac address");
        }


        //locationid
        String locationId1 = input[2].trim();
        if (locationId1.equals("")) {
            errors.add("invalid location id");
            return errors;
        }
        int locationId;
        try {
            locationId = Integer.parseInt(locationId1);
        } catch (NumberFormatException e) {
            errors.add("invalid location id");
            return errors;
        }        
        /*
        String lookup = LocationLookupDAO.checkValidLocationId(locationId);
        if (lookup == null) {
            errors.add("Location-ID is not in the list.");
            
        }
        */
        return errors;
    }

    /**
     * Validate the demographics.csv 
     * @param input demographics file which is to be validated
     * @return  list of errors
     */
    public ArrayList<String> validateDemographics(String[] input) {
        ArrayList<String> errors = new ArrayList<String>();

        //macaddress
        String macAddress = input[0].trim();
        //check not empty
        if (macAddress.equals("")) {
            errors.add("invalid mac address");
            return errors;
        }
        //check make sure it fits the length
        if (macAddress.length() != 40) {
            errors.add("invalid mac address");

        }

        //check make sure only contain allowed characters
        Pattern p = Pattern.compile("[^a-f0-9]");
        if (p.matcher(macAddress).find()) {
            errors.add("invalid mac address");
        }

        //name
        String name = input[1].trim();
        //check not empty
        if (name.equals("")) {
            errors.add("invalid name");
            return errors;
        }

        //passsword
        String password = input[2].trim();

        //check minimum length and has no blank spaces
        if (password.length() < 8 || password.contains(" ")) {
            errors.add("invalid password");
        }

        //email
        String email = input[3].trim();
        //check not empty
        if (email.equals("")) {
            errors.add("invalid email");
            return errors;
        }

        // Check Validity 
        try {

            p = Pattern.compile("^[A-Za-z0-9.]+\\.+[2]{1}+[0]{1}+[1]{1}+[0-4]{1}+@(?:{1}|business|accountancy|sis|economics|law|socsc)+\\.+smu\\.+edu\\.+sg+$");
            if (!p.matcher(email).find()) {
                errors.add("invalid email");     
            }
        } catch (Exception e) {
            errors.add("invalid email");         
        }

        //gender
        String genCheck = input[4].trim();
        //check if empty
        if (genCheck.equals("")) {
            errors.add("invalid gender");
            return errors;
        }
        //check only 1 character
        if (genCheck.length() != 1) {
            errors.add("invalid gender");
        }
        //check correct character entered
        if (!(genCheck.equalsIgnoreCase("m") || genCheck.equalsIgnoreCase("f"))) {
            errors.add("invalid gender");
        }
        return errors ; 
    }
}