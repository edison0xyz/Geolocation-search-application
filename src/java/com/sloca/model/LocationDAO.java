package com.sloca.model;

import com.sloca.connection.ConnectionFactory;
import com.sloca.entity.Location;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to the database for Location entities.
 *
 */
public class LocationDAO {

    Connection connection = null;
    PreparedStatement pstmt = null;
    int counter = 0;

    /**
     * Creates LocationDAO entity.
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public LocationDAO() throws SQLException, ClassNotFoundException {

        connection = null ; 
    }

    /**
     * Sets the Connection in LocationDAO file 
     *@param connection connection to be updated
     * 
     * 
     */
    
    public void setConnection(Connection connection)    {  
        this.connection = connection ;         
    }
    
    
    /**
     * Save the new/modified location
     *
     * @param l new/modified location
     * @throws SQLIntegrityConstraintViolationException
     */
    public void saveLocations(ArrayList<Location> locationList) {

        for (Location l : locationList) {
            String query = "INSERT INTO location (`time_stamp`, `mac_address`, `location_id`) VALUES ('" + l.getTimeStamp() + "', '" + l.getMacAddress() + "', '" + l.getLocationId() + "')";
            try {
                pstmt = connection.prepareStatement(query);
                pstmt.execute() ; 
            } catch (SQLException ex) {
                Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
            }   finally {
                try {
                    pstmt.close() ;
                } catch (SQLException ex) {
                    Logger.getLogger(LocationDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        System.out.println("Inserted " + locationList.size() + " locations with pooled method.");


    }

    /**
     * Retrieves Location entity from the database
     *
     */
    public void retrieveLocation() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }
        Connection connection = null;
        try {
            connection = ConnectionFactory.getConnection();

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }

    }
}
