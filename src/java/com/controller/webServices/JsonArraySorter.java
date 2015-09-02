/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.controller.webServices;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author G4T8
 */
public class JsonArraySorter {

    public JsonArray sort(JsonArray array) {
        // Sorts a JsonArray in Alphabetical Order and return the sorted JsonArray 

        ArrayList<String> jsonResult = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            jsonResult.add(array.get(i).getAsString());
        }
        Collections.sort(jsonResult);
        array = new JsonArray();
        for (int i = 0; i < jsonResult.size(); i++) {
            array.add(new JsonPrimitive(jsonResult.get(i)));
        }
        return array ; 

    }
    
    public JsonArray sortAGDlocation(JsonArray locationArray) { 
        
        JsonArray returnArr = new JsonArray() ; 
        while(locationArray.size() > 0 )    {
            
            int minCount = Integer.MAX_VALUE ; 
            JsonObject obj = locationArray.get(0).getAsJsonObject() ; 
            String locationID = obj.get("location").getAsString() ; 
            
            
            
            int index = 0 ; 
            for(int i = 0 ; i < locationArray.size();i++) {
                JsonObject locationObject = (JsonObject)locationArray.get(i) ; 
                String temp_location = locationObject.get("location").getAsString() ; 
//                int temp_count = locationObject.get("time-spent").getAsInt() ;
                if(locationID.compareTo(temp_location) > 0)   {
                    locationID = temp_location ; 
                    index = i ; 
                }
            }
            
            returnArr.add(locationArray.get(index)) ; 
            locationArray.remove(index) ; 
        }
        
        
        return returnArr ; 
    }
    
    public JsonArray sortAGDGroup(JsonArray groupArray) {
        JsonArray returnArr = new JsonArray() ; 
        
        // sort by group size first
        
        while(groupArray.size()  >  0 ) {
            
            int max = 0 ; 
            int index = -1 ; 
            int maxTimeSpent = 0 ;
           
            
            for(int i = 0 ; i < groupArray.size() ; i++)    {
                
                JsonObject groupObject = groupArray.get(i).getAsJsonObject() ; 
                int temp_size = groupObject.get("size").getAsInt() ; 
                int temp_timeSpent = groupObject.get("total-time-spent").getAsInt() ; 
                
                if(temp_size > max) {
                    max = temp_size ; 
                    index = i ; 
                    maxTimeSpent = temp_timeSpent ; 
                }  
                
                if(temp_size == max)   {
                    // same size, perform one more level of check
                    if(temp_timeSpent > maxTimeSpent)   {
                        index = i ; 
                        maxTimeSpent = temp_timeSpent ; 
                    }   
                    if(temp_timeSpent == maxTimeSpent)  {
                        
                        // Compare the lexicology of the location ids 
                        JsonObject maxObj = groupArray.get(index).getAsJsonObject() ; 
                        JsonArray max_member_array  = maxObj.get("members").getAsJsonArray()  ;
                        JsonArray compare_member_array = groupObject.get("members").getAsJsonArray() ; 
                        inner:
                        
                        for(int count = 0 ; count < max_member_array.size() ; count++ ) {
                            //prevent index out of bounds
                            if(count < compare_member_array.size()) {
                                JsonObject memberFromMax = max_member_array.get(count).getAsJsonObject() ; 
                                JsonObject memberFromCompare = compare_member_array.get(count).getAsJsonObject() ; 
                                
                                String maxMac = memberFromMax.get("mac-address").getAsString() ;
                                String compareMac = memberFromCompare.get("mac-address").getAsString() ; 
                                
                                // Gettin
                                if(maxMac.compareTo(compareMac) > 0) {
                                    index = i ; 
                                    break inner; 
                                }
                                
                            }
                            
                        }
                    }
                }
            }
            
            JsonObject addingGroup = groupArray.get(index).getAsJsonObject() ; 
            
            returnArr.add(addingGroup);
            groupArray.remove(index) ; 
        }
        
        return returnArr ; 
    }
    
    public JsonArray sortAGDMembers(JsonArray memberArray)  {
        JsonArray returnArr = new JsonArray() ; 
        while(memberArray.size()>0) {
            
            int index = 0 ; 
            
            // initialise object
            JsonObject jobj = memberArray.get(0).getAsJsonObject() ; 
            String email = jobj.get("email").getAsString() ; 
            String mac = jobj.get("mac-address").getAsString() ;  
            
            
            for(int i = 1 ; i < memberArray.size() ; i++)   {
                JsonObject memberObject = memberArray.get(i).getAsJsonObject() ; 
                String temp_email = memberObject.get("email").getAsString() ; 
                String temp_mac = memberObject.get("mac-address").getAsString() ; 
                if(email.compareTo(temp_email) > 0) {
                    email=temp_email ; 
                    mac= temp_mac ; 
                    index = i ; 
                }
                
            }
            JsonObject member = memberArray.get(index).getAsJsonObject() ; 
            returnArr.add(member);
            memberArray.remove(index);
        }
        
        return returnArr  ;
    }
}
