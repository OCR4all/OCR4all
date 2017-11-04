package de.uniwue.feature.pageXML;

import java.util.ArrayList;

/**
 * Class to handle the readingOrder
 */
public class ReadingOrder {

    /**
     * Id of the ordered Group
     */
    private String orderedGroupID;

    /**
     * Arraylist of the regionRefindices
     * The regionRefindices displays the reading order of the different textregions of a page
     */
    private ArrayList<RegionRefIndexed> regionRefIndices;

    /**
     * Constructor
     * @param orderedGroupID orderedGroup ID
     * @param regionRefIndices List of regionRefIndices
     */
    public ReadingOrder(String orderedGroupID, ArrayList<RegionRefIndexed> regionRefIndices) {
        setOrderedGroupID(orderedGroupID);
        setRegionRefIndices(regionRefIndices);
    }

    /**
     * Returns the matching index of the regionref string 
     * @param regionRef regionref order
     * @return matching index
     */
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

    /**
     * Gets the OrderedGroupID
     * @return String representation of the GroupID
     */
    public String getOrderedGroupID() {
        return orderedGroupID;
    }

    public void setOrderedGroupID(String orderedGroupID) {
        this.orderedGroupID = orderedGroupID;
    }

    /**
     * Gets the RegionRefIndices 
     * @return List of RegionRefindices
     */
    public ArrayList<RegionRefIndexed> getRegionRefIndices() {
        return regionRefIndices;
    }

    public void setRegionRefIndices(ArrayList<RegionRefIndexed> regionRefIndices) {
        this.regionRefIndices = regionRefIndices;
    }
}