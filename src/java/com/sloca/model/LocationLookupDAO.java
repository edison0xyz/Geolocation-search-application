
package com.sloca.model;

import com.sloca.connection.ConnectionFactory;
import com.sloca.entity.LocationLookup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G4T8
 */
/**
 * 
 * Provides access to the  database for LocationLookup entities.
 */
public class LocationLookupDAO {
    static Connection connection = null;
    static HashMap<String, String> lookupList;
    static PreparedStatement pstmt = null;
    static ResultSet rs = null;

    /**
     * Creates LocationLookupDAO
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    /**
     * Retrieves list of lookup 
     * @return the HashMap of the Lookup
     */
    
    public void setConnection(Connection connection) { 
        this.connection = connection ; 
    }
    
    public HashMap<String, String> retrieveAll() {
        lookupList = new HashMap<String, String>();
        try {
            connection = ConnectionFactory.getConnection() ; 
            String query = "SELECT location_id, semanticplace from location_lookup;";
            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                lookupList.put(rs.getString(1), rs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(LocationLookupDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lookupList;
    }

    /**
     * Checks the validity of LocationID
     * @param i the sequence of the Location in the HashMap
     * @return semanticPlace of the Location
     */
    public String checkValidLocationId(String i) {
        
        HashMap<String, String> lookupList = retrieveAll();
        return lookupList.get(i);
    }
    
    /**
     * Retrieve a list of SemanticPlaces
     * 
     * @return the list of SemanticPlaces
     */
    public static ArrayList<String> retrieveAllSemanticPlaces() {
        
        ArrayList<String> semanticPlacesList = new ArrayList<String>() ;  
        try {
            connection = ConnectionFactory.getConnection() ; 
            String query = "SELECT distinct `semanticplace` FROM location_lookup";
            
            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                semanticPlacesList.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                pstmt.close();
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(LocationLookupDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return semanticPlacesList;
        
    }

    /**
     * Save a new LocationLookup
     * @param locationLookup the locationLookup to save
     * @throws SQLException 
     */
    public void saveLookup(LocationLookup locationLookup) throws SQLException {
        
        if (connection ==  null )  {     
            connection = ConnectionFactory.getConnection() ; 
        }
        
        try {
            String query = "INSERT INTO location_lookup (location_id, semanticplace) VALUES (" + locationLookup.getLookupId() + " , '" + locationLookup.getName() + "')";
            pstmt = connection.prepareStatement(query);
            pstmt.execute();
        } finally {
            pstmt.close();
        }
    }        
    
   
}
