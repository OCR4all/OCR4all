package de.uniwue.model;

/**
 * Class to represent the readingOrder of the text regions
 */
public class RegionRefIndexed {

    /**
     * Index of the regionRef
     */
    private String index;

    /**
     * Reading Order of the region
     */
    private String regionRef;

    /** 
     * Represents the content of an image region in a page
     */
    public RegionRefIndexed(String index, String regionRef) {
        this.index = index;
        this.regionRef = regionRef;
    }

    /**
     * Gets the index of the object
     * @return String representation of the index
     */
    public String getIndex() {
        return index;
    }

    /**
     * Gets the regionref of the object
     * @return
     */
    public String getRegionRef() {
        return regionRef;
    }
}