package de.uniwue.feature.pageXML;

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
        setIndex(index);
        setRegionRef(regionRef);
    }

    /**
     * Gets the index of the object
     * @return String representation of the index
     */
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Gets the regionref of the object
     * @return
     */
    public String getRegionRef() {
        return regionRef;
    }

    public void setRegionRef(String regionRef) {
        this.regionRef = regionRef;
    }
}