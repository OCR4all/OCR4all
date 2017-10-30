package de.uniwue.feature.pageXML;

import java.util.ArrayList;

public class ReadingOrder {

    private String orderedGroupID;
    private ArrayList<RegionRefIndexed> regionRefIndices;
    
    public ReadingOrder(String orderedGroupID, ArrayList<RegionRefIndexed> regionRefIndices) {
        setOrderedGroupID(orderedGroupID);
        setRegionRefIndices(regionRefIndices);
    }

    public String findIndex(String regionRef) {
        String index = "";
        
        for(RegionRefIndexed regRef : regionRefIndices) {
            if(regRef.getRegionRef().toString().equals(regionRef)) {
                index = regRef.getIndex();
                break;
            }
        }
        
        if(index.length() > 0) {
            while(index.length() < 3) {
                index = "0" + index;
            }
            
            return index;
        }
        
        return null;
    }
    
    public String getOrderedGroupID() {
        return orderedGroupID;
    }

    public void setOrderedGroupID(String orderedGroupID) {
        this.orderedGroupID = orderedGroupID;
    }

    public ArrayList<RegionRefIndexed> getRegionRefIndices() {
        return regionRefIndices;
    }

    public void setRegionRefIndices(ArrayList<RegionRefIndexed> regionRefIndices) {
        this.regionRefIndices = regionRefIndices;
    }
}