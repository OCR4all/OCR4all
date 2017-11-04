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
     * List of the regionRefIndices
     * The regionRefindices displays the reading order of the different text regions of a page
     */
    private ArrayList<RegionRefIndexed> regionRefIndices;

    /**
     * Constructor
     * @param orderedGroupID orderedGroup ID
     * @param regionRefIndices List of regionRefIndices
     */
    public ReadingOrder(String orderedGroupID, ArrayList<RegionRefIndexed> regionRefIndices) {
        this.orderedGroupID = orderedGroupID;
        this.regionRefIndices = regionRefIndices;
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

    /**
     * Gets the RegionRefIndices 
     * @return List of RegionRefindices
     */
    public ArrayList<RegionRefIndexed> getRegionRefIndices() {
        return regionRefIndices;
    }
}