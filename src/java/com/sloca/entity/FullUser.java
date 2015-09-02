/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sloca.entity;

import java.util.ArrayList;
import java.util.HashMap;
import org.joda.time.Interval;

/**
 * FullUser entity, which represents the user
 * @author G4T8
 */
public class FullUser {
    private String macAddress;
    private String email;
    private HashMap<String,ArrayList<Interval>> locMap;
    
    /**
     * A constructor with specific macAddress, email, locationID, and interval
     * @param macAddress macAddress of the user
     * @param email email of the user
     * @param locationID locationID of the user
     * @param in interval of the user
     */
    public FullUser (String macAddress, String email, String locationID, Interval in) {
        this.macAddress = macAddress;
        this.email = email;
        ArrayList<Interval> oneList = new ArrayList<Interval>();
        oneList.add(in);
        locMap = new HashMap<String,ArrayList<Interval>>();
        locMap.put(locationID, oneList);
    }
    
    /**
     * A constructor with specific macAddress, email, locationID
     * @param macAddress macAddress of the user
     * @param email email of the user
     * @param locationID  locationID of the user
     */
    public FullUser (String macAddress, String email, String locationID) {
        this.macAddress = macAddress;
        this.email = email;
        ArrayList<Interval> oneList = new ArrayList<Interval>();
        locMap = new HashMap<String,ArrayList<Interval>>();
        locMap.put(locationID, oneList);
    }

    /**
     * Retrieves the macAddress of the user
     * @return the macAddress
     */
    public String getMacAddress() {
        return macAddress;
    }
    
    /**
     * Retrieves the email of the user
     * @return the email
     */
    public String getEmail(){
        return email;
    }

    /**
     * Retrieves the list of Location
     * @return the locMap
     */
    public HashMap<String, ArrayList<Interval>> getLocMap() {
        return locMap;
    }
    
    /**
     * Adds in the interval
     * @param locationID the locationID to add in
     * @param in the interval to be added in
     */
    public void addInterval(String locationID, Interval in) {
        ArrayList<Interval> temp = locMap.get(locationID);
        if (temp != null) {
            temp.add(in);
        } else {
            temp = new ArrayList<Interval>();
            temp.add(in);
        }
        locMap.put(locationID, temp);
    }   
}
