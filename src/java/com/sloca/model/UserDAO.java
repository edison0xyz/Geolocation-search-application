package com.sloca.model;

import com.sloca.connection.ConnectionFactory;
import com.sloca.entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 * Provides access to the  database for User entities.
 */
public class UserDAO {

    Connection connection = null;
    PreparedStatement pstmt = null;

    public void setConnection(Connection connection) { 
        this.connection = connection ; 
        
    }

    /**
     * Save a new user
     * @param u a new user
     */
    public void saveUser(User u) {
        if(connection == null ) { 
            try { 
                connection = ConnectionFactory.getConnection() ;
            } catch (SQLException ex) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
        
        try {
            String query = "INSERT INTO demographics (mac_address, name, password, email, gender) VALUES ('" + u.getMacAddress() + "', '" + u.getName() + "', '" + u.getPassword() + "', '" + u.getEmail() + "', '" + u.getGender() + "')";
            pstmt = connection.prepareStatement(query);
            pstmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Authenticate username and password
     * @param username username of the user
     * @param password password of the user
     * @return true if the user is a authenticated person
     */
    public boolean authenticate(String username, String password) {
        
        if(connection == null ) { 
            try { 
                connection = ConnectionFactory.getConnection() ;
            } catch (SQLException ex) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
        
        String correctPassword;
        String email;
        System.out.println("in authenticate");
        try {
            String authenticate = "SELECT email, password FROM demographics WHERE email like '" + username + "%' ";
            pstmt = connection.prepareStatement(authenticate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                correctPassword = rs.getString("password");
                email = rs.getString("email");
                String userid = email.substring(0, email.indexOf("@"));

                if (password.equals(correctPassword) && userid.equals(username)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Wrong username/password 
        return false;
    }

    /**
     * Retrieves User by username
     * @param username the username of the desired user
     * @return 
     */
    public User retrieve(String username) {
        
        if(connection == null ) { 
            try { 
                connection = ConnectionFactory.getConnection() ;
            } catch (SQLException ex) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
        
        User user = null;
        System.out.println("In UserDAO: retrieve");
        try {
            String authenticate = "SELECT `mac_address`, `name`, `password`, `email`, `gender` FROM demographics WHERE email like '" + username + "%' ";
            pstmt = connection.prepareStatement(authenticate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String macAddress = rs.getString("mac_address");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String gender = rs.getString("gender");

                System.out.print(name);
                user = new User(macAddress, name, password, email, gender.charAt(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("second" + user.getName());
        return user;
    }

    /**
     * Retrieves the list of Users
     * @return the list of User
     */
    public ArrayList<User> retrieveAll() {
        ArrayList<User> list = new ArrayList<User>();
        
        if(connection == null ) { 
            try { 
                connection = ConnectionFactory.getConnection() ;
            } catch (SQLException ex) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
        

        try {
            String authenticate = "SELECT `mac_address`, `name`, `password`, `email`, `gender` FROM demographics";
            pstmt = connection.prepareStatement(authenticate);
            ResultSet rs = pstmt.executeQuery();

            User user;
            while (rs.next()) {
                String macAddress = rs.getString("mac_address");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String gender = rs.getString("gender");
                user = new User(macAddress, name, password, email, gender.charAt(0));
                list.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    /**
     * Retrieves a list of Users who are within the specific timeStamp
     * @param timeNow the start of timestamp
     * @param timeBefore the end of timestamp
     * @return a list of users
     */
    public ArrayList<User> retrieveUsersByTimestamp(String timeNow, String timeBefore) {
        ArrayList<User> list = new ArrayList<User>();
        
        if(connection == null ) { 
            try { 
                connection = ConnectionFactory.getConnection() ;
            } catch (SQLException ex) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
           
        }
        

        try {
            String authenticate = "SELECT d.`mac_address`, `name`, `password`, `email`, `gender` from demographics d inner join location l where l.mac_address = d.mac_address and time_stamp between '" + timeBefore + "'and '" + timeNow + "'group by mac_address";
            pstmt = connection.prepareStatement(authenticate);
            ResultSet rs = pstmt.executeQuery();

            User user;
            while (rs.next()) {
                String macAddress = rs.getString("mac_address");
                String name = rs.getString("name");
                String password = rs.getString("password");
                String email = rs.getString("email");
                String gender = rs.getString("gender");
                user = new User(macAddress, name, password, email, gender.charAt(0));
                list.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
