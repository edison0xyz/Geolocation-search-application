/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sloca.entity;

import java.util.ArrayList;

/**
 *
 * @author G4T8
 */
/**
 * 
 * An entity that represents the User.
 */
public class User {
    private String name, password, email;
    private char gender;
    private String macAddress ; 
    
    /**
     * Creates User entity with specific macAddress, name, password, email and gender.
     * @param macAddress macAddress of the User
     * @param name name of the User
     * @param password password of the User
     * @param email email of the User
     * @param gender gender of the User 
     */
    public User(String macAddress, String name, String password, String email, char gender) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.gender = gender;
        this.macAddress = macAddress ; 
    }

    /**
     * Retrieves the name of the User 
     * @return User's name 
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the password of the User 
     * @return User's password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Retrieves the email of the User
     * @return the email of the User 
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retrieves the gender of the User
     * @return User's gender 
     */
    public char getGender() {
        return gender;
    }
    
    /**
     * Retrieves the MacAddress of the user 
     * @return MacAddress of the user
     */
    public String getMacAddress() {
        return macAddress;
    }
    
   
}

    