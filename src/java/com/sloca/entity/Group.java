/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.joda.time.Interval;
import org.joda.time.Seconds;


/**
 * Group entity, which represents a list of Users
 * @author G4T8
 */
public class Group {

    private String uniqueName;
    private HashSet<FullUser> indivSet;
    private int fullCount;
    private HashMap<String, Integer> locTimeMap;
    private HashMap<String, ArrayList<Interval>> locOverlapMap;
    private String lastPlace;

    /**
     * A constructor with specific two FullUser, timeSpent, locTimemap, locOverLapMap and lastPlace
     * @param first the first FullUser
     * @param second the second FullUser
     * @param timeSpent the total time that two users spent together
     * @param locTimeMap the locTimeMap of two users
     * @param locOverlapMap the locOverlapMap of two users
     * @param lastPlace the place that the users had been
     */
    public Group(FullUser first, FullUser second, int timeSpent, HashMap<String, Integer> locTimeMap, HashMap<String, ArrayList<Interval>> locOverlapMap, String lastPlace) {
        uniqueName = first.getMacAddress() + "@" + second.getMacAddress();
        indivSet = new HashSet<FullUser>();
        indivSet.add(first);
        indivSet.add(second);
        fullCount = timeSpent;
        this.locTimeMap = locTimeMap;
        this.locOverlapMap = locOverlapMap;
        this.lastPlace = lastPlace;
    }
    
    /**
     * A constructor for a Group
     * @param g group
     */
    public Group(Group g) {
        uniqueName = g.uniqueName;
        indivSet = g.indivSet;
        fullCount = g.fullCount;
        locTimeMap = g.locTimeMap;
        locOverlapMap = g.locOverlapMap;
    }

    
    /**
     * Retrieve all macAddress and email of the users
     * @return the list of MacAddresses and Email in the group
     */
    public ArrayList<String> getAllUserMac() {
        ArrayList<String> answer = null;
        HashSet<FullUser> userList = getIndivSet();
        if (userList.size() > 0) {
            answer = new ArrayList<String>();
            for (FullUser f : userList) {
                String mac = f.getMacAddress();
                String email = f.getEmail();
                if (email == null) {
                    email = "No email recorded";
                }
                String combi = mac + "!!" + email; 
                answer.add(combi);
            }
        }
        Collections.sort(answer);
        return answer;
    }
    
    /**
     * Retrieve common times of users in descending order
     * @return the list of common times in descending order
     */
    public ArrayList<String[]> getLocationTimeArrayOrdered() {
        ArrayList<String[]> answer = new ArrayList<String[]>();
        for (Entry<String, Integer> entry: this.locTimeMap.entrySet()) {
            String[] strArray = new String[] {entry.getKey(), entry.getValue().toString()};
            answer.add(strArray);
        }
        Collections.sort(answer, new LocationTimeMapComparator(answer));
        return answer;
    }
    
    /**
     * Retrieve a list of location IDs of all the places that the group visited together
     * @return a list of Location ids 
     */
    public ArrayList<String> getLocations() {
        HashMap<String, Integer> map = getLocTimeMap();
        ArrayList<String> answer = null;
        if (map.size() > 0) {
            answer = new ArrayList<String>();
            for (String s : map.keySet()) {
                answer.add(s);
            }
        }
        return answer;
    }

    /**
     * Retrieve IndiindivSet
     * @return HashSet
     */
    public HashSet<FullUser> getIndivSet() {
        return indivSet;
    }

    /**
     * Add in individuals
     * @param first the FullUser to be added to the list
     */
    public void addIndividuals(FullUser first) {
        indivSet.add(first);
    }

    /**
     * Set fullCount
     * @param fullCount the fullCount to set
     */
    public void setFullCount(int fullCount) {
        this.fullCount = fullCount;
    }

    /**
     * Set LocTimeMap
     * @param locTimeMap the locTimeMap to set
     */
    public void setLocTimeMap(HashMap<String, Integer> locTimeMap) {
        this.locTimeMap = locTimeMap;
    }

    /**
     * Set LocOverlapMap
     * @param locOverlapMap the locOverlapMap to set
     */
    public void setLocOverlapMap(HashMap<String, ArrayList<Interval>> locOverlapMap) {
        this.locOverlapMap = locOverlapMap;
    }

    /**
     * Retrieve uniqueName
     * @return the uniqueName
     */
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     * Add indivSet
     * @param other Group to be added 
     */
    public void addIndivSet(Group other) {
        indivSet.addAll(other.getIndivSet());
    }

    /**
     * Compare the overlap time
     * @param other the group to compare
     * @return -1 if not overlap, otherwise return fullCount
     */
    public int compareOverlap(Group other) {
        int fullCount = 0;
        ArrayList<Interval> intervalList = new ArrayList<Interval>();
        for (String s : getLocOverlapMap().keySet()) {
            ArrayList<Interval> firstList = getLocOverlapMap().get(s);
            ArrayList<Interval> secondList = other.getLocOverlapMap().get(s);
            for (Interval first : firstList) {
                for (Interval second : secondList) {
                    Interval overlap = first.overlap(second);
                    if (overlap != null) {
                        Seconds seconds = Seconds.secondsIn(overlap);
                        int secs = seconds.getSeconds();
                        fullCount = fullCount + secs;
                        intervalList.add(overlap);
                    }
                }
            }
        }
        if (fullCount >= 720) {
            return fullCount;
        } else {
            return -1;
        }
    }

    /**
     * Retrieve locOverlapMap
     * @return the locOverlapMap
     */
    public HashMap<String, ArrayList<Interval>> getLocOverlapMap() {
        return locOverlapMap;
    }

    /**
     * Retrieve fullCount 
     * @return the fullCount
     */
    public int getFullCount() {
        return fullCount;
    }

    /**
     * Retrieve LocTimeMap
     * @return the locTimeMap
     */
    public HashMap<String, Integer> getLocTimeMap() {
        return locTimeMap;
    }

    /**
     * Retrieve LastPlace
     * @return the lastPlace
     */
    public String getLastPlace() {
        return lastPlace;
    }

    /**
     * Set lastPlace
     * @param lastPlace the lastPlace to set
     */
    public void setLastPlace(String lastPlace) {
        this.lastPlace = lastPlace;
    }
    
    public class LocationTimeMapComparator implements Comparator<String[]> {

        ArrayList<String[]> base;

        public LocationTimeMapComparator(ArrayList<String[]> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String[] a, String[] b) {
            if (Integer.parseInt(a[1]) >= Integer.parseInt(b[1])) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
