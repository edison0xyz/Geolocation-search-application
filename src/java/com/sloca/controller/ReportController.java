/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.controller;

import com.sloca.connection.ConnectionFactory;
import com.sloca.entity.FullUser;
import com.sloca.entity.Group;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.Seconds;

/**
 *
 * @author G4T8
 */
public class ReportController {

    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private ArrayList<Group> fillList = new ArrayList<Group>();
	
    public HashMap<String, FullUser> getFullUserList(String tsBefore, String tsAfter) throws SQLException {
        
        HashMap<String, FullUser> userMap = new HashMap<String, FullUser>();

        try {
            connection = ConnectionFactory.getConnection();

            // Grab field data from database, including email if it exists.
            pstmt = connection.prepareStatement("select time_stamp, l.mac_address, email, location_id "
            + "from location l left outer join demographics d "
            + "on l.mac_address = d.mac_address "
            + "where time_stamp between '" + tsBefore + "' "
            + "and '" + tsAfter + "' "
            + "group by mac_address, time_stamp "
            + "order by mac_address, time_stamp "
            + ";");
            rs = pstmt.executeQuery();

            String macAddress = "";
            String locationID = "";
            DateTime before = null;
            DateTime after = null;
            // "last" is meant to check for intervals greater than 9 minutes. Compare with first DateTime to do so.
            DateTime last = new DateTime(Timestamp.valueOf(tsAfter));
            FullUser user = null;

            if (!rs.isBeforeFirst() ) {    
                return null; 
            } 
            // Logic for creating intervals and creating user data.
            while (rs.next()) {
                
                // First time, when user is still null value. As you can tell, it'll only run once.
                if (before == null && after == null) {
                    
                    macAddress = rs.getString("mac_address");
                    locationID = rs.getString("location_id");
                    user = new FullUser(macAddress, rs.getString("email"), locationID);
                    after = new DateTime(rs.getTimestamp("time_stamp").getTime());
                    
                // Occurs when the macAddress changes i.e. when the user is no longer the same.
                } else if (!macAddress.equals(rs.getString("mac_address"))) {
                    
                    // Save last interval in user data before user changes.
                    Interval lastEntry = new Interval(after, last);
                    if (Seconds.secondsIn(lastEntry).getSeconds() > 540) {
                        lastEntry = new Interval(after, after.plusMinutes(9));
                    }
                    user.addInterval(locationID, lastEntry);
                    
                    if (user.getMacAddress().equals("a444444444444444444444444444444444444444")) {
                        for (Entry<String, ArrayList<Interval>> entry: user.getLocMap().entrySet()) {
                            System.out.println(entry.getKey());
                            ArrayList<Interval> tempList = entry.getValue();
                            for (Interval in: tempList) {
                                System.out.println(in);
                            }
                        }
                    }
                    
                    macAddress = rs.getString("mac_address");
                    locationID = rs.getString("location_id");
                    before = null;
                    after = new DateTime(rs.getTimestamp("time_stamp").getTime());
                    userMap.put(user.getMacAddress(), user);
                    user = new FullUser(macAddress, rs.getString("email"), locationID);
                    
                // Anytime else (even if the locationID changes and macAddress stays the same).    
                } else {
                    
                    // Code to look for intervals longer than 9 minutes. Actually occurs in the previous "else if" case too.
                    before = after;
                    after = new DateTime(rs.getTimestamp("time_stamp").getTime());
                    Interval in = new Interval(before, after);
                    if (Seconds.secondsIn(in).getSeconds() > 540) {
                        in = new Interval(before, before.plusMinutes(9));
                    }
                    user.addInterval(locationID, in);
                    locationID = rs.getString("location_id");
                    
                }
            }
            // Naturally, there'll still be one more interval after there are no more results.
            Interval lastEntry = new Interval(after, last);
            if (Seconds.secondsIn(lastEntry).getSeconds() > 540) {
                lastEntry = new Interval(after, after.plusMinutes(9));
            }
            user.addInterval(locationID, lastEntry);
            userMap.put(user.getMacAddress(), user);
            // System.out.println(userList.get(0).getLocMap().size());
            // System.out.println(userList.get(1).getLocMap().size());
            /*
            for (FullUser u: userList) {
                System.out.println(u.getMacAddress());
                for (Entry<String, ArrayList<Interval>> entry : u.getLocMap().entrySet()) {
                    System.out.println(entry.getKey());
                    ArrayList<Interval> tempList = entry.getValue();
                    for (Interval inter: tempList) {
                        System.out.println(inter);
                    }
                }
            }
            */
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return userMap;
    }
        
        // As the name suggests, this checks for interval overlap between two users.
        public Group checkOverlapNew(FullUser user1, FullUser user2) { 

            int fullCount = 0;
            //System.out.println("User Comparison:");
            //System.out.println(user1.getMacAddress());
            //System.out.println(user2.getMacAddress());
            HashMap<String, ArrayList<Interval>> firstMap = user1.getLocMap();
            HashMap<String, ArrayList<Interval>> secondMap = user2.getLocMap();
            HashMap<String, Integer> locTimeMap = new HashMap<String, Integer>();
            Interval latest = null;
            String place = null;
            HashMap<String, ArrayList<Interval>> locOverlapMap = new HashMap<String, ArrayList<Interval>>();
            // Check through every key in the HashMap (locationID), compare common intervals in each location.
            for (Entry<String, ArrayList<Interval>> entry : firstMap.entrySet()) {
                // semiCount counts the total number of seconds two people share IN A CERTAIN LOCATION.
                int semiCount = 0;
                String key = entry.getKey();
                ArrayList<Interval> inter1 = firstMap.get(key);
                ArrayList<Interval> inter2 = secondMap.get(key);
                if (inter1.size() == 0 || inter2 == null) {
                    continue;
                }
                if (latest == null && place == null) {
                    latest = inter1.get(0);
                    place = key;
                }
                ArrayList<Interval> intervalList = new ArrayList<Interval>();
                // A check against EVERY SINGLE INTERVAL to confirm overlapping of time.
                for (Interval in1: inter1) {
                    //System.out.println(in1);
                    for (Interval in2: inter2) {
                        //System.out.println("Hey!");
                        //System.out.println(in2);
                        //System.out.println("Hello.");
                        Interval overlap = in1.overlap(in2);
                        if (overlap != null) {
                            //System.out.println(overlap);
                            if (overlap.isAfter(latest)) {
                                latest = overlap;
                                place = key;
                            }
                            Seconds seconds = Seconds.secondsIn(overlap);
                            int secs = seconds.getSeconds();
                            semiCount = semiCount + secs;
                            intervalList.add(overlap);
                        } else {
                            //System.out.println("No matching overlap");
                        }
                    }
                }
                // Keys are only added to the two new HashMaps if there is at least one overlap in timing.
                if (semiCount != 0) {
                    locTimeMap.put(key, semiCount);
                    locOverlapMap.put(key, intervalList);
                }
                
                fullCount = fullCount + semiCount;
            }
            
            // Check for 720 seconds.
            if (fullCount >= 720) {
                Group g = new Group(user1, user2, fullCount, locTimeMap, locOverlapMap, place);
                System.out.println("Group: ");
                System.out.println(user1.getMacAddress());
                System.out.println(user2.getMacAddress());
                return g;
            }
            
            // returns null if pair is not found 
            return null;
        }
        
        public Group checkOverlapForAll(FullUser user1, FullUser user2) { 

            int fullCount = 0;
            //System.out.println("User Comparison:");
            //System.out.println(user1.getMacAddress());
            //System.out.println(user2.getMacAddress());
            HashMap<String, ArrayList<Interval>> firstMap = user1.getLocMap();
            HashMap<String, ArrayList<Interval>> secondMap = user2.getLocMap();
            HashMap<String, Integer> locTimeMap = new HashMap<String, Integer>();
            Interval latest = null;
            String place = null;
            HashMap<String, ArrayList<Interval>> locOverlapMap = new HashMap<String, ArrayList<Interval>>();
            // Check through every key in the HashMap (locationID), compare common intervals in each location.
            for (Entry<String, ArrayList<Interval>> entry : firstMap.entrySet()) {
                // semiCount counts the total number of seconds two people share IN A CERTAIN LOCATION.
                int semiCount = 0;
                String key = entry.getKey();
                ArrayList<Interval> inter1 = firstMap.get(key);
                ArrayList<Interval> inter2 = secondMap.get(key);
                if (inter1.size() == 0 || inter2 == null) {
                    continue;
                }
                if (latest == null && place == null) {
                    latest = inter1.get(0);
                    place = key;
                }
                ArrayList<Interval> intervalList = new ArrayList<Interval>();
                // A check against EVERY SINGLE INTERVAL to confirm overlapping of time.
                for (Interval in1: inter1) {
                    //System.out.println(in1);
                    for (Interval in2: inter2) {
                        //System.out.println("Hey!");
                        //System.out.println(in2);
                        //System.out.println("Hello.");
                        Interval overlap = in1.overlap(in2);
                        if (overlap != null) {
                            //System.out.println(overlap);
                            if (overlap.isAfter(latest)) {
                                latest = overlap;
                                place = key;
                            }
                            Seconds seconds = Seconds.secondsIn(overlap);
                            int secs = seconds.getSeconds();
                            semiCount = semiCount + secs;
                            intervalList.add(overlap);
                        } else {
                            //System.out.println("No matching overlap");
                        }
                    }
                }
                // Keys are only added to the two new HashMaps if there is at least one overlap in timing.
                if (semiCount != 0) {
                    locTimeMap.put(key, semiCount);
                    locOverlapMap.put(key, intervalList);
                }
                
                fullCount = fullCount + semiCount;
            }

            Group g = new Group(user1, user2, fullCount, locTimeMap, locOverlapMap, place);
            //System.out.println(p.getFirst().getMacAddress());
            //System.out.println(p.getSecond().getMacAddress());
            return g;
        }
		
	// The method which tests for overlap
        public static boolean doOverlap(Group here, Group other) {
            int fullCount = 0;
            HashMap<String, Integer> timeMap = new HashMap<String, Integer>();
            HashMap<String, ArrayList<Interval>> intervalMap = new HashMap<String, ArrayList<Interval>>();
            Interval latest = null;
            String place = null;
            //System.out.println("Group Comparison:");
            // More or less the same overlap method as before, with a few changes.
            for (String s: here.getLocOverlapMap().keySet()) {
                ArrayList<Interval> firstList = here.getLocOverlapMap().get(s);
                ArrayList<Interval> secondList = other.getLocOverlapMap().get(s);
                ArrayList<Interval> intervalList = new ArrayList<Interval>();
                int semiCount = 0;
                if (secondList != null) {
                    if (latest == null && place == null) {
                        latest = firstList.get(0);
                        place = s;
                    }
                    for (Interval first: firstList) {
                        //System.out.println(first);
                        for (Interval second: secondList) {
                            //System.out.println("Hey!");
                            //System.out.println(second);
                            //System.out.println("Hello.");
                            Interval overlap = first.overlap(second);
                            if (overlap != null) {
                                //System.out.println(overlap);
                                if (overlap.isAfter(latest)) {
                                    latest = overlap;
                                    place = s;
                                }
                                Seconds seconds = Seconds.secondsIn(overlap);
                                int secs = seconds.getSeconds();
                                semiCount = semiCount + secs;
                                intervalList.add(overlap);
                            } else {
                                //System.out.println("No matching interval");
                            }
                        }
                    }
                }
                // Don't add to HashMaps if there is no overlap / no common timing in a certain location. 
                if (semiCount != 0) {
                    intervalMap.put(s, intervalList);
                    timeMap.put(s, semiCount);
                    fullCount = fullCount + semiCount;
                }
            }
            
            /*
            System.out.println("Group1 Users:");
            for (FullUser u: here.getIndivSet()){
                System.out.println(u.getMacAddress());
            }
            System.out.println("Group2 Users:");
            for (FullUser u1: other.getIndivSet()){
                System.out.println(u1.getMacAddress());
            }
            System.out.println(fullCount);
            */
            
            // Reflect new location / interval HashMap, timing HashMap, full count of seconds and unique list of people if more than 12 minutes. 
            if (fullCount >= 720) {
                here.setFullCount(fullCount);
                here.setLocTimeMap(timeMap);
                here.setLocOverlapMap(intervalMap);
                here.addIndivSet(other);
                here.setLastPlace(place);
                // Show the 
                return false;
            } else {
                return true;
            }
            
        }
        
        public static void doGroupUserOverlap(Group here, FullUser other) {
            int fullCount = 0;
            HashMap<String, Integer> timeMap = here.getLocTimeMap();
            HashMap<String, ArrayList<Interval>> intervalMap = here.getLocOverlapMap();
            Interval latest = null;
            String place = null;
            // More or less the same overlap method as before, with a few changes.
            for (String s: here.getLocOverlapMap().keySet()) {
                ArrayList<Interval> firstList = here.getLocOverlapMap().get(s);
                ArrayList<Interval> secondList = other.getLocMap().get(s);
                ArrayList<Interval> intervalList = new ArrayList<Interval>();
                int semiCount = 0;
                if (secondList != null) {
                    if (latest == null && place == null) {
                        latest = firstList.get(0);
                        place = s;
                    }
                    for (Interval first: firstList) {
                        for (Interval second: secondList) {
                            Interval overlap = first.overlap(second);
                            if (overlap != null) {
                                if (overlap.isAfter(latest)) {
                                    latest = overlap;
                                    place = s;
                                }
                                Seconds seconds = Seconds.secondsIn(overlap);
                                int secs = seconds.getSeconds();
                                semiCount = semiCount + secs;
                                intervalList.add(overlap);
                            }
                        }
                    }
                }
                // Don't add to HashMaps if there is no overlap / no common timing in a certain location. 
                if (semiCount != 0) {
                    intervalMap.put(s, intervalList);
                    timeMap.put(s, semiCount);
                    fullCount = fullCount + semiCount;
                }
            }
            // Reflect new location / interval HashMap, timing HashMap, full count of seconds and unique list of people if more than 12 minutes. 
            //if (fullCount >= 720) {
                here.setFullCount(fullCount);
                here.setLocTimeMap(timeMap);
                here.setLocOverlapMap(intervalMap);
                here.addIndividuals(other);
                here.setLastPlace(place);
            //}
        }
        
	// Meant to sort the original master pair list into bigger groups of 3 and more.
	// Requires the the Group and the FullUser class.
	// Group holds an HashSet of FullUsers, and FullUser has a name / macAddress (string), email (string) and a Hashmap of locationID (key) and ArrayLists of timestamps / intervals (value).  
	public ArrayList<Group> getLargeGroups(HashMap<String, Group> masterMap) {
            
            ArrayList<Group> completeList = new ArrayList<Group>();
            // usedSet stores pairs that have been used to create other groups.
            // // HashSet<Group> usedSet = new HashSet<Group>();
            // groupList stores the original list of pairs in ArrayList form. Will undergo changes to become a list of groups 
            // which may have more than 2 members.
            ArrayList<Group> groupList = new ArrayList<Group>(masterMap.values());
            
            for (int k = 0; k < 1; k++) {
                for (Group g: groupList) {
                    compareGroups(g);
                }
            }
            
            completeList.addAll(groupList);
            Collections.sort(groupList, new GroupSizeComparator(groupList));
            Collections.sort(completeList, new GroupSizeComparator(completeList));
            
            for (int i = 0; i < groupList.size(); i++) {
                Group g = groupList.get(i);
                HashSet<FullUser> gUsers = g.getIndivSet();
                for (int j = i; j < groupList.size(); j++) {
                    Group g2 = groupList.get(j);
                    HashSet<FullUser> g2Users = g2.getIndivSet();
                    if (!g.equals(g2) && gUsers.containsAll(g2Users)) {
                        completeList.remove(g2);
                    }
                }
            }
            for (Group g: completeList) {
                System.out.println("Full Group: ");

                for (Entry<String, ArrayList<Interval>> entry: g.getLocOverlapMap().entrySet()) {
                    System.out.println(entry.getKey() + ": ");
                    for (Interval in: entry.getValue()) {
                        System.out.println(in);
                    }
                }
            }
            return completeList;
        }
        
        public void compareGroups(Group g) {
            boolean same = true;
            for (Group g2: fillList) {
                if (g2.getIndivSet().containsAll(g.getIndivSet())) {
                    same = false;
                } else {
                    same = doOverlap(g2, g);
                }
            }
            if (same) {
                Group gNew = new Group(g);
                fillList.add(gNew);
            }
        }
            /*
            for (int k = 0; k < groupList.size(); k++) {
                Group g = groupList.get(k);
                ArrayList<Pair> pairList = g.getPairList();
                for (Pair p: pairList) {
                    System.out.print(k + ". " + p.getFirst().getMacAddress() + " " + p.getSecond().getMacAddress());
                    System.out.println();
                }
            }
            */
    public class GroupSizeComparator implements Comparator<Group> {
        ArrayList<Group> base;
        public GroupSizeComparator(ArrayList<Group> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(Group a, Group b) {
            int i = Integer.valueOf(b.getIndivSet().size()).compareTo(Integer.valueOf(a.getIndivSet().size()));
            if (i != 0) return i;
            
            ArrayList<String> strAList = a.getAllUserMac();
            ArrayList<String> strBList = b.getAllUserMac();
            
            for (int j = 0; j < strAList.size(); j++) {
                String first = strAList.get(j);
                String second = strBList.get(j);
                i = first.compareTo(second);
                if (i != 0) return i;
            }
            return i;
        }
    }
}
