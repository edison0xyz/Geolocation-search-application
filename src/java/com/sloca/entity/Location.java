
package com.sloca.entity;

import java.sql.Date;
import java.sql.Timestamp;

/**
 *
 * @author G4T8
 */
/**
 * 
 * Location entity.It represents the location that is used in the application.
 */
public class Location {
    
    Timestamp timestamp;
    String macAddress;
    int locationId;
    
    /**
     * Creates a location object with timestamp, macAddress, and locationID
     * @param timestamp Timestamp of the location
     * @param macAddress MacAddress of the location
     * @param locationId LocationID of the location
     */
    public Location(Timestamp timestamp, String macAddress, int locationId){
        this.timestamp = timestamp;
        this.macAddress = macAddress;
        this.locationId = locationId;
    }
    
    /**
     * Gets the timestamp of the location
     * @return timestamp
     */
    public Timestamp getTimeStamp(){
        return timestamp;
    }
    
    /**
     * Gets MacAddress that location has
     * 
     * @return MacAddress
     */
    public String getMacAddress(){
        return macAddress;
    }
    
    /**
     * Gets the locationID of the location
     * @return LocationID of the location
     */
    public int getLocationId(){
        return locationId;
    }
    
}
