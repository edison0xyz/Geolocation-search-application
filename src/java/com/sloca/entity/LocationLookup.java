/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sloca.entity;

/**
 *
 * @author G4T8
 */

/**
 * LoactionLookup entity which consists of LookupID and name
 * 
 */
public class LocationLookup {
    
    int lookupId;
    String name;

    
    /**
     * Creates LocationLookup with specific lookupID and name
     * @param lookupId The ID of LocationLookup
     * @param name The name of LocationLookup
     */
    public LocationLookup(int lookupId, String name) {
        this.lookupId = lookupId;
        this.name = name;
    }

    /**
     * Retrieves ID of LocationLookup
     * @return ID of LocationLookup
     */
    public int getLookupId() {
        return lookupId;
    }

    /**
     * Retrieves the name of LocationLookupID
     * @return Name of LocationLookup
     */
    public String getName() {
        return name;
    }
    
    
    
}
