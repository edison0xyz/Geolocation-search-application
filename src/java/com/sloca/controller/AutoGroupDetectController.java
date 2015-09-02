/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sloca.controller;

import com.google.gson.JsonObject;
import com.sloca.entity.FullUser;
import com.sloca.entity.Group;
import com.sloca.model.LocationDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.RequestDispatcher;

/**
 *
 * @author Ryan
 */
public class AutoGroupDetectController {
    
    public ArrayList<Group> getFullGroups(String tsBefore, String tsAfter) throws SQLException{
        
        JsonObject autoDetectObject = new JsonObject();
        ArrayList<Group> groupList = new ArrayList<Group>();
        ReportController rp = new ReportController();

        // Get full list of eligible users, with data inside.
        HashMap<String, FullUser> userMap = rp.getFullUserList(tsBefore, tsAfter);
        if (userMap == null) {
            return groupList;
        }
        System.out.println("UserList initialised." + userMap.size());
        autoDetectObject.addProperty("total-user", userMap.size());

        // Use normal for-loop to avoid unnecessary comparisons.
        // Add eligible groups to group list.
        HashMap<String, Group> groupMap = new HashMap<String, Group>();
        ArrayList<FullUser> userList = new ArrayList<FullUser>(userMap.values());
        for (int i = 0; i < userMap.size(); i++) {
            for (int j = i + 1; j < userMap.size(); j++) {
                FullUser u = userList.get(i);
                FullUser u1 = userList.get(j);
                Group g = rp.checkOverlapNew(u, u1);
                if (g != null) {
                    groupMap.put(g.getUniqueName(), g);
                }
            }
        }
        System.out.println("PairList initialised." + groupMap.size());

        groupList = rp.getLargeGroups(groupMap);

        System.out.println("GroupList initialised." + groupList.size());
        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //out.println(gson.toJson(groupList));

        return groupList;

        //HttpSession session = request.getSession();
        //session.setAttribute("results", groupList);
        //response.sendRedirect("autoGroupDetection.jsp");
    }
    
    public ArrayList<Group> getMatchingGroups(String tsBefore, String tsAfter, ArrayList<Group> groupList) throws SQLException{
        
        ArrayList<Group> fullList = new ArrayList<Group>();
        ReportController rp = new ReportController();
        
        // Get full list of eligible users, with data inside.
        HashMap<String, FullUser> userMap = rp.getFullUserList(tsBefore, tsAfter);
        for (Group g: groupList) {
            boolean isGroup = true;
            String[] nameSplit = g.getUniqueName().split("@");
            FullUser user1 = userMap.get(nameSplit[0]);
            FullUser user2 = userMap.get(nameSplit[1]);
            if (user1 == null || user2 == null) {
                continue;
            }
            Group newG = rp.checkOverlapForAll(user1, user2);
            if (newG == null) {
                //System.out.println("The group is not a group!");
                continue;
            }
            HashSet<FullUser> userSet = g.getIndivSet();
            
            for (FullUser u: userSet) {
                // Check for null here.
                FullUser u1 = userMap.get(u.getMacAddress());
                if (u1 == null) {
                    isGroup = false;
                    break;
                }
                if (!newG.getIndivSet().contains(u1)) {
                    ReportController.doGroupUserOverlap(newG, u1);
                }
            }
            
            // Group verification 
            if (isGroup) {
                fullList.add(newG);
            }
        }
        return fullList;
    }
}
